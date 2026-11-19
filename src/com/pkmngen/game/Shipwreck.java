package com.pkmngen.game;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Shipwreck loot: wooden crates wash up on beach tiles next to the sea each morning.
 * Press A on one to open it. Crates are ordinary map objects ("shipwreck_crate1"), so they
 * are saved with the map.
 */
class Shipwreck {
   static final int MAX_CRATES = 6;

   /** {item, min, max, weight} */
   static final Object[][] LOOT = {
      {"poké ball", 2, 4, 10},
      {"great ball", 1, 2, 6},
      {"ultra ball", 1, 1, 2},
      {"berry juice", 1, 2, 8},
      {"rare candy", 1, 1, 3},
      {"silky thread", 1, 3, 6},
      {"soft bedding", 1, 1, 4},
      {"clear glass", 1, 2, 5},
      {"metal coat", 1, 2, 5},
      {"magnet", 1, 2, 5},
      {"copper ore", 2, 4, 6},
      {"iron ore", 1, 3, 6},
      {"gold ore", 1, 1, 2},
      {"nugget", 1, 1, 2},
      {"thin paper", 1, 3, 5},
      {"repel", 1, 2, 5},
      {"old rod", 1, 1, 1},
   };

   static void update(Game game, boolean newDay) {
      if (game.map == null || game.map.overworldTiles == null) {
         return;
      }
      int existing = 0;
      ArrayList<Tile> spots = new ArrayList<>();
      for (Tile tile : game.map.overworldTiles.values()) {
         if (tile.nameUpper.equals("shipwreck_crate1")) {
            existing++;
            continue;
         }
         if (tile.routeBelongsTo == null || !tile.routeBelongsTo.name.startsWith("beach")) {
            continue;
         }
         if (tile.isSolid || tile.isWater || tile.isLedge || !tile.nameUpper.equals("") || !tile.name.startsWith("sand")) {
            continue;
         }
         if (game.map.pokemon.containsKey(tile.position) || tile.position.dst2(game.player.position) < 64 * 64) {
            continue;
         }
         if (nextToWater(game, tile)) {
            spots.add(tile);
         }
      }
      int toSpawn = newDay ? 1 + Game.rand.nextInt(2) : (existing == 0 ? 1 : 0);
      for (int i = 0; i < toSpawn && existing < MAX_CRATES && !spots.isEmpty(); i++) {
         Tile tile = spots.remove(Game.rand.nextInt(spots.size()));
         tile.nameUpper = "shipwreck_crate1";
         tile.init();
         existing++;
         System.out.println("Crate washed up at " + tile.position);
      }
   }

   static boolean nextToWater(Game game, Tile tile) {
      float[][] d = {{16, 0}, {-16, 0}, {0, 16}, {0, -16}};
      for (float[] o : d) {
         Tile n = game.map.overworldTiles.get(tile.position.cpy().add(o[0], o[1]));
         if (n != null && n.isWater) {
            return true;
         }
      }
      return false;
   }

   /** Opens a crate: gives logs plus 1-3 random finds and returns the message to show. */
   static String open(Game game, Tile tile) {
      Player player = game.player;
      StringBuilder found = new StringBuilder();
      int logs = 2 + Game.rand.nextInt(3);
      give(player, "log", logs);
      found.append(logs).append(" LOG");
      int finds = 1 + Game.rand.nextInt(3);
      int total = 0;
      for (Object[] l : LOOT) {
         total += (Integer)l[3];
      }
      for (int f = 0; f < finds; f++) {
         int r = Game.rand.nextInt(total);
         for (Object[] l : LOOT) {
            r -= (Integer)l[3];
            if (r < 0) {
               int min = (Integer)l[1];
               int max = (Integer)l[2];
               int amount = min + Game.rand.nextInt(max - min + 1);
               give(player, (String)l[0], amount);
               found.append(", ").append(amount).append(" ").append(((String)l[0]).toUpperCase(Locale.ROOT));
               break;
            }
         }
      }
      tile.nameUpper = "";
      tile.init();
      return "The crate held " + found + "!";
   }

   static void give(Player player, String item, int amount) {
      player.addItem(item, amount);
   }
}
