package com.pkmngen.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Other islands. The game already generates a new island map when you surf or fly past the
 * edge of the current one (maps are saved as "map x,y"). This adds:
 *
 * - A sea gate: an island's "ring" is |x| + |y| from home. Travelling to a farther ring needs a
 *   vessel in the bag: RAFT (ring 1), BOAT (ring 2), SAILBOAT (any). Heading home is free.
 * - Island specials: every island away from home rolls 3 special species (rarer the farther
 *   out) that replace 15% of its wild encounters.
 * - Far islands (ring 3+) have a chance of a legendary boss roaming the island.
 */
class Islands {
   static final int SPECIAL_CHANCE = 15; // percent of wild encounters on an island
   static final int LEGENDARY_CHANCE = 35; // percent per new ring-3+ island

   static final List<String> RING1 = Arrays.asList(
      "eevee", "togepi", "skarmory", "heracross", "pinsir", "scyther", "tauros", "kangaskhan", "miltank",
      "chansey", "smeargle", "misdreavus", "houndour", "teddiursa", "slakoth", "ralts", "feebas", "porygon", "ditto", "riolu"
   );
   static final List<String> RING2 = Arrays.asList(
      "dratini", "lapras", "larvitar", "beldum", "gible", "absol", "snorlax", "deino", "goomy", "rotom", "spiritomb",
      "sneasel", "riolu", "eevee", "skarmory", "porygon"
   );
   static final List<String> RING3 = Arrays.asList(
      "dragonair", "beldum", "gible", "deino", "larvitar", "lapras", "snorlax", "goomy", "spiritomb", "rotom", "absol"
   );
   static final List<String> LEGENDARIES = Arrays.asList("articuno", "zapdos", "moltres", "raikou", "entei", "suicune");

   // ------------------------------------------------------------------ sea gate

   static int ring(String mapId) {
      if (mapId == null) {
         return 0;
      }
      String[] xy = mapId.split(",");
      return Math.abs(Integer.valueOf(xy[0])) + Math.abs(Integer.valueOf(xy[1]));
   }

   static String targetMapId(Game game, String dir) {
      String[] xy = game.map.currMapId.split(",");
      int x = Integer.valueOf(xy[0]);
      int y = Integer.valueOf(xy[1]);
      if (dir.equals("left")) {
         x--;
      } else if (dir.equals("right")) {
         x++;
      } else if (dir.equals("up")) {
         y++;
      } else if (dir.equals("down")) {
         y--;
      }
      return x + "," + y;
   }

   static int seaTier(Player player) {
      if (player.hasItem("sailboat")) {
         return 99;
      } else if (player.hasItem("boat")) {
         return 2;
      } else if (player.hasItem("raft")) {
         return 1;
      }
      return 0;
   }

   static boolean canTravel(Game game, String dir) {
      int target = ring(targetMapId(game, dir));
      return target <= ring(game.map.currMapId) || target <= seaTier(game.player);
   }

   static String blockedText(Game game, String dir) {
      int target = ring(targetMapId(game, dir));
      String vessel = target <= 1 ? "a RAFT" : target == 2 ? "a BOAT" : "a SAILBOAT";
      return "The open sea is too rough to cross. You'll need " + vessel + " to go farther out.";
   }

   // ------------------------------------------------------------------ specials

   /** Called when a map loads. Rolls this island's specials the first time it's visited. */
   static void init(Game game) {
      int r = ring(game.map.currMapId);
      if (r == 0 || game.map.islandSpecials != null) {
         return;
      }
      Random rng = new Random(game.map.currMapId.hashCode() * 7919L + game.map.rand.nextInt());
      List<String> pool = new ArrayList<>(r == 1 ? RING1 : r == 2 ? RING2 : RING3);
      Collections.shuffle(pool, rng);
      game.map.islandSpecials = new ArrayList<>(pool.subList(0, 3));
      System.out.println("Island " + game.map.currMapId + " specials: " + game.map.islandSpecials);

      if (r >= 3 && rng.nextInt(100) < LEGENDARY_CHANCE) {
         String legend = LEGENDARIES.get(rng.nextInt(LEGENDARIES.size()));
         ArrayList<Tile> spots = new ArrayList<>();
         for (Tile tile : game.map.overworldTiles.values()) {
            if (tile.routeBelongsTo != null && !tile.isSolid && !tile.isWater && !tile.isLedge && tile.nameUpper.equals("")
               && !game.map.pokemon.containsKey(tile.position) && tile.position.dst2(game.player.position) > 320 * 320) {
               spots.add(tile);
            }
         }
         if (!spots.isEmpty()) {
            Tile spot = spots.get(rng.nextInt(spots.size()));
            Boss.spawnSpecies(game, spot, "legendary", legend, Boss.bossLevel(game.player), true);
            game.map.islandSpecials.add("!" + legend);
         }
      }
   }

   /** Maybe swap a wild encounter for one of this island's specials. */
   static Pokemon maybeSpecial(Game game, Pokemon wild) {
      ArrayList<String> specials = game.map.islandSpecials;
      if (wild == null || specials == null || wild.isBoss || Game.rand.nextInt(100) >= SPECIAL_CHANCE) {
         return wild;
      }
      ArrayList<String> choices = new ArrayList<>();
      for (String s : specials) {
         if (!s.startsWith("!")) {
            choices.add(s);
         }
      }
      if (choices.isEmpty()) {
         return wild;
      }
      String species = choices.get(Game.rand.nextInt(choices.size())).toLowerCase(Locale.ROOT);
      try {
         return new Pokemon(species, wild.level, Pokemon.Generation.CRYSTAL);
      } catch (Exception e) {
         return wild;
      }
   }
}
