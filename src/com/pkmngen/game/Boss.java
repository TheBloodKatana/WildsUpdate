package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Boss Pokemon.
 *
 * - Fixed bosses: one per biome, at a spot chosen once per world. After it's beaten it
 *   comes back RESPAWN_DAYS later.
 * - Catch level cap: wild Pokemon above it can't be caught. Starts at BASE_CAP, +CAP_PER_BOSS
 *   per boss beaten. Bosses sit BOSS_ABOVE_CAP levels above it and are exempt from the cap;
 *   beating any boss raises every other living boss to the new level.
 * - Roaming bosses: a rare chance each morning to appear somewhere on the island, plus a
 *   tiny chance that a tall-grass encounter is a boss.
 * - In battle: double HP, rages at half HP (+1 Atk/SpAtk/Speed), can't be fled from, and can
 *   only be caught below 1/4 HP.
 * - Beating (or catching) a boss unlocks the next locked recipe, preferring the one tied to
 *   that boss's biome.
 */
class Boss {
   static final int RESPAWN_DAYS = 5;
   static final int GRASS_BOSS_CHANCE = 256;   // 1 in N tall-grass encounters
   static final int ROAMING_CHANCE = 4;        // 1 in N mornings
   static final int MAX_ROAMING = 2;
   static final int BASE_CAP = 20;             // starting catch level cap
   static final int CAP_PER_BOSS = 10;         // cap raise per boss beaten
   static final int BOSS_ABOVE_CAP = 5;        // bosses are this many levels above the cap

   static int bossLevel(Player player) {
      return Math.min(100, player.catchLevelCap() + BOSS_ABOVE_CAP);
   }

   /** Bosses are exempt (they use the 1/4 HP rule); everything else must be at or under the cap. */
   static boolean underCatchCap(Pokemon pokemon, Player player) {
      return pokemon.isBoss || pokemon.level <= player.catchLevelCap();
   }

   /** Set every living boss to the current boss level (cap + 5), keeping its HP ratio. */
   static void levelUpBosses(Game game) {
      int level = bossLevel(game.player);
      for (Pokemon p : game.map.pokemon.values()) {
         if (p.isBoss && p.level != level && p != game.battle.oppPokemon) {
            float ratio = p.currentStats.get("hp") / (float)Math.max(1, p.maxStats.get("hp"));
            p.level = level;
            p.exp = p.calcExpForLevel(level);
            p.calcMaxStats();
            p.currentStats.put("hp", Math.max(1, Math.round(p.maxStats.get("hp") * ratio)));
         }
      }
   }

   /** Unlock id -> display name. Order = order unlocks are handed out. */
   static final LinkedHashMap<String, String> UNLOCKS = new LinkedHashMap<>();
   /** Unlock id -> recipes / build tiles it gates. */
   static final HashMap<String, List<String>> UNLOCK_RECIPES = new HashMap<>();
   /** Recipe / build tile -> unlock id needed. */
   static final HashMap<String, String> RECIPE_LOCK = new HashMap<>();
   /** Biome (route name) -> the unlock its fixed boss prefers to give. */
   static final LinkedHashMap<String, String> BIOME_UNLOCK = new LinkedHashMap<>();
   /** Biomes that get a fixed boss. */
   static final List<String> BOSS_BIOMES = Arrays.asList(
      "forest1", "mountain1", "volcano1", "beach1", "deep_forest", "snow1",
      "desert1", "savanna1", "graveyard1", "wooded_lake1", "oasis1"
   );

   static {
      unlock("iron tools", "IRON TOOLS", "iron axe", "iron pick", "iron shovel");
      unlock("ultra ball", "ULTRA BALLS", "ultra ball");
      unlock("healer", "the HEALING MACHINE", "heal_machine1");
      unlock("repeat ball", "REPEAT BALLS", "repeat ball");
      unlock("hunter ball", "HUNTER BALLS", "hunter ball");
      unlock("luxury ball", "LUXURY BALLS", "luxury ball");
      BIOME_UNLOCK.put("forest1", "iron tools");
      BIOME_UNLOCK.put("mountain1", "ultra ball");
      BIOME_UNLOCK.put("volcano1", "healer");
      BIOME_UNLOCK.put("beach1", "repeat ball");
      BIOME_UNLOCK.put("deep_forest", "hunter ball");
      BIOME_UNLOCK.put("snow1", "luxury ball");
   }

   static void unlock(String id, String display, String... recipes) {
      UNLOCKS.put(id, display);
      UNLOCK_RECIPES.put(id, Arrays.asList(recipes));
      for (String r : recipes) {
         RECIPE_LOCK.put(r, id);
      }
   }

   static boolean isLocked(String recipeOrTile, Player player) {
      String id = RECIPE_LOCK.get(recipeOrTile.toLowerCase(Locale.ROOT));
      return id != null && !player.unlocks.contains(id);
   }

   static String biomeDisplay(String biome) {
      String b = biome.replaceAll("[0-9]", "").replace("_", " ").trim().toUpperCase(Locale.ROOT);
      if (b.equals("DEEP FOREST")) {
         return "DEEP FOREST";
      }
      return b;
   }

   // ------------------------------------------------------------------ battle

   static String appearText(Game game) {
      Pokemon opp = game.battle.oppPokemon;
      if (opp.isBoss) {
         return "The mighty " + opp.nickname.toUpperCase(Locale.ROOT) + " appeared! Weaken it before throwing a BALL!";
      }
      return "Wild " + opp.nickname.toUpperCase(Locale.ROOT) + " appeared!";
   }

   static boolean canCatch(Pokemon pokemon) {
      return !pokemon.isBoss || pokemon.currentStats.get("hp") * 4 <= pokemon.maxStats.get("hp");
   }

   /** Called after the boss takes damage and survives. Returns an action to run first, or null. */
   static Action checkRage(Game game, Action nextAction) {
      Pokemon opp = game.battle.oppPokemon;
      if (!opp.isBoss || opp.bossRaged || opp.currentStats.get("hp") * 2 > opp.maxStats.get("hp")) {
         return null;
      }
      opp.bossRaged = true;
      for (String stat : new String[]{"attack", "specialAtk", "speed"}) {
         opp.statStages.put(stat, Math.min(6, opp.statStages.get(stat) + 1));
      }
      return new DisplayText(
         game, opp.nickname.toUpperCase(Locale.ROOT) + " is raging! Its ATTACK, SP. ATK and SPEED rose!", null, null, nextAction
      );
   }

   /** Rewards for beating or catching a boss. Returns the text to show. */
   static String onDefeated(Game game, Pokemon boss) {
      Player player = game.player;
      String biome = boss.bossBiome == null ? "" : boss.bossBiome;
      boolean fixed = BOSS_BIOMES.contains(biome) && !boss.bossRoaming;
      if (fixed) {
         int defeats = player.bossDefeats.getOrDefault(biome, 0) + 1;
         player.bossDefeats.put(biome, defeats);
         player.bossRespawnDay.put(biome, player.daysPassed + RESPAWN_DAYS);
      }
      boss.isBoss = false;
      player.bossesBeaten++;
      levelUpBosses(game);
      String capText = " Catch limit is now LV" + player.catchLevelCap() + "!";

      String unlock = BIOME_UNLOCK.get(biome);
      if (unlock == null || player.unlocks.contains(unlock)) {
         unlock = null;
         for (String id : UNLOCKS.keySet()) {
            if (!player.unlocks.contains(id)) {
               unlock = id;
               break;
            }
         }
      }
      if (unlock != null) {
         player.unlocks.add(unlock);
         return "The BOSS was defeated! You can now craft " + UNLOCKS.get(unlock) + "!" + capText;
      }
      // Everything unlocked already: give materials instead.
      player.addItem("rare candy", 2);
      player.addItem("life force", 2);
      return "The BOSS was defeated! It left behind 2 RARE CANDY and 2 LIFE FORCE!" + capText;
   }

   // ------------------------------------------------------------------ world

   static Action auraAction = null;

   /** Called when a world loads and every morning. Keeps fixed bosses up and maybe adds a roamer. */
   static void update(Game game, boolean newDay) {
      if (game.map == null || game.player == null || game.map.overworldTiles == null) {
         return;
      }
      Player player = game.player;
      levelUpBosses(game);
      HashMap<String, Integer> alive = new HashMap<>();
      int roaming = 0;
      for (Pokemon p : game.map.pokemon.values()) {
         if (p.isBoss && p.bossBiome != null) {
            if (p.bossRoaming) {
               roaming++;
            } else {
               alive.put(p.bossBiome, 1);
            }
         }
      }

      // Group walkable overworld tiles by biome once.
      HashMap<String, ArrayList<Tile>> tilesByBiome = new HashMap<>();
      for (Tile tile : game.map.overworldTiles.values()) {
         if (tile.routeBelongsTo == null || tile.isSolid || tile.isWater || tile.isLedge || !tile.nameUpper.equals("")) {
            continue;
         }
         if (game.map.pokemon.containsKey(tile.position)) {
            continue;
         }
         tilesByBiome.computeIfAbsent(tile.routeBelongsTo.name, k -> new ArrayList<>()).add(tile);
      }

      for (String biome : BOSS_BIOMES) {
         ArrayList<Tile> tiles = tilesByBiome.get(biome);
         if (tiles == null || tiles.isEmpty() || alive.containsKey(biome)) {
            continue;
         }
         if (player.daysPassed < player.bossRespawnDay.getOrDefault(biome, 0)) {
            continue;
         }
         Tile spot = null;
         String saved = player.bossSpots.get(biome);
         if (saved != null) {
            String[] xy = saved.split(",");
            Tile t = game.map.overworldTiles.get(new Vector2(Float.valueOf(xy[0]), Float.valueOf(xy[1])));
            if (t != null && !t.isSolid && !game.map.pokemon.containsKey(t.position)) {
               spot = t;
            }
         }
         if (spot == null) {
            spot = farthestFrom(tiles, player.spawnLoc != null ? player.spawnLoc : player.position, game);
            player.bossSpots.put(biome, (int)spot.position.x + "," + (int)spot.position.y);
         }
         spawn(game, spot, biome, bossLevel(player), false);
      }

      if (newDay && roaming < MAX_ROAMING && Game.rand.nextInt(ROAMING_CHANCE) == 0) {
         ArrayList<String> biomes = new ArrayList<>(tilesByBiome.keySet());
         biomes.removeIf(b -> b.contains("ocean") || b.contains("regi") || b.contains("mansion") || b.contains("lab") || b.contains("ruins"));
         if (!biomes.isEmpty()) {
            String biome = biomes.get(Game.rand.nextInt(biomes.size()));
            ArrayList<Tile> tiles = tilesByBiome.get(biome);
            Tile spot = tiles.get(Game.rand.nextInt(tiles.size()));
            if (spot.position.dst2(player.position) > 160 * 160) {
               spawn(game, spot, biome, bossLevel(player), true);
            }
         }
      }
   }

   static Tile farthestFrom(ArrayList<Tile> tiles, Vector2 from, Game game) {
      // Random sample, keep the one farthest from spawn so bosses feel like a destination.
      Tile best = null;
      float bestDst = -1;
      for (int i = 0; i < 40; i++) {
         Tile t = tiles.get(game.map.rand.nextInt(tiles.size()));
         float d = t.position.dst2(from);
         if (d > bestDst) {
            bestDst = d;
            best = t;
         }
      }
      return best;
   }

   static String strongestSpecies(Route route) {
      String best = null;
      int bestTotal = -1;
      for (String name : route.allowedPokemon()) {
         name = name.toLowerCase(Locale.ROOT);
         try {
            if (!Specie.species.containsKey(name)) {
               Specie.species.put(name, new Specie(name));
            }
            Map<String, Integer> s = Specie.species.get(name).baseStats;
            int total = s.get("hp") + s.get("attack") + s.get("defense") + s.get("specialAtk") + s.get("specialDef") + s.get("speed");
            if (total > bestTotal) {
               bestTotal = total;
               best = name;
            }
         } catch (Exception e) {
            // skip species that fail to load
         }
      }
      return best;
   }

   static void spawn(Game game, Tile tile, String biome, int level, boolean roaming) {
      String species = strongestSpecies(tile.routeBelongsTo);
      if (species == null) {
         return;
      }
      if (roaming) {
         ArrayList<String> options = new ArrayList<>(tile.routeBelongsTo.allowedPokemon());
         species = options.get(Game.rand.nextInt(options.size())).toLowerCase(Locale.ROOT);
      }
      Pokemon boss = new Pokemon(species, level, Pokemon.Generation.CRYSTAL);
      boss.makeBoss(biome, roaming);
      boss.position = tile.position.cpy();
      boss.mapTiles = game.map.overworldTiles;
      boss.aggroPlayer = true;
      boss.standingAction = boss.new Standing();
      game.insertAction(boss.standingAction);
      game.map.pokemon.put(boss.position.cpy(), boss);
      System.out.println("Spawned boss " + species + " Lv" + level + " in " + biome + " at " + tile.position);
   }

   // ------------------------------------------------------------------ aura

   /** Draws a pulsing aura under every boss on the current map layer. */
   static class DrawAuras extends Action {
      public Action.Layer layer = Action.Layer.map_131;
      Sprite aura;
      int timer = 0;
      Color saved = new Color();

      DrawAuras() {
         Texture tex = TextureCache.get(Gdx.files.internal("boss_aura1.png"));
         this.aura = new Sprite(tex, 0, 0, 32, 32);
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         if (game.map == null || game.map.pokemon == null) {
            return;
         }
         this.timer++;
         int frame = (this.timer / 12) % 2;
         this.aura.setRegion(frame * 32, 0, 32, 32);
         float alpha = 0.55F + 0.35F * (float)Math.sin(this.timer / 10.0);
         this.saved.set(game.mapBatch.getColor());
         game.mapBatch.setColor(1.0F, 1.0F, 1.0F, alpha);
         for (Pokemon p : game.map.pokemon.values()) {
            if (p.isBoss && p.mapTiles == game.map.tiles) {
               game.mapBatch.draw(this.aura, p.position.x - 8.0F, p.position.y - 8.0F);
            }
         }
         game.mapBatch.setColor(this.saved);
      }
   }
}
