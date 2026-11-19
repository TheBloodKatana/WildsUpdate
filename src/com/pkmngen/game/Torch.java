package com.pkmngen.game;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Passing the torch (docs/PROGRESSION.md section 9).
 *
 * Your character retires as a core character (DAD) who keeps the exact team that won, the
 * CHAMPION title, and your house. You continue as the kid: same house and world, no Pokemon,
 * a fresh bag, no badges. Professor WILLOW (or Dad, if Willow isn't around) offers three
 * starters: one Fire, one Water, one Grass from the starter pool. Every legendary encounter
 * resets. From here on the kid's catch cap comes from badges.
 */
class Torch {
   static void pass(Game game) {
      Player p = game.player;
      // 1. Dad: a core character with your winning team.
      // the previous parent becomes the grandparent
      for (Npcs.Npc n : game.map.npcs) {
         if (n.data.type.equals("dad")) {
            n.data.type = "grandpa";
            n.data.role = null;
         } else if (n.data.type.equals("grandpa")) {
            n.data.type = "t_gentleman";   // great-grandparent: retires into the background
            n.data.sprite = null;
            n.data.role = null;
         }
      }
      Npcs.NpcData d = new Npcs.NpcData();
      d.type = "dad";
      // your name and look from character creation
      d.name = p.name == null || p.name.trim().isEmpty() ? "DAD" : p.name.toUpperCase(Locale.ROOT);
      d.sprite = p.character;
      d.spriteColor = com.badlogic.gdx.graphics.Color.rgba8888(p.color);
      d.spriteSkin = com.badlogic.gdx.graphics.Color.rgba8888(p.skinColor);
      // Dad lives at home: next to your bed if it's outdoors, else where you retired.
      com.badlogic.gdx.math.Vector2 dadHome = p.spawnIndex == -1 ? openSpotNear(game, p.spawnLoc) : null;
      if (dadHome == null) {
         dadHome = p.position.cpy();
      }
      d.x = d.homeX = dadHome.x;
      d.y = d.homeY = dadHome.y;
      d.bedLayer = -1;
      d.friendship = 8;
      d.questStage = 2;   // no quest
      d.team = new ArrayList<>();
      for (Pokemon pk : p.pokemon) {
         if (!pk.isEgg) {
            d.team.add(pk.specie.name.toLowerCase(Locale.ROOT) + ":" + pk.level);
         }
      }
      d.role = p.championYear >= 0 ? "champion" : null;
      d.wins = 0;
      Npcs.Npc dad = new Npcs.Npc(d);
      game.map.npcs.add(dad);
      applyNames(game);

      // 2. You become the kid.
      p.generation++;
      p.championYear = -1;
      p.badges.clear();
      p.pokemon.clear();
      p.currPokemon = null;
      p.getItemsDict().clear();
      p.addItem("poké ball", 5);
      p.addItem("berry juice", 3);
      p.addItem("pokédex", 1);
      p.unlocks.remove("starter:" + p.generation);
      // 3. Legends come back.
      p.legendsDone.clear();
      p.legendEvents.clear();

      game.playerCanMove = false;
      String prof = willow(game) != null ? "PROFESSOR WILLOW" : d.name;
      game.insertAction(new DisplayText(game, Characters.T("Years later..."), null, null,
         new RunCode(() -> goHome(game), new DisplayText(game, Characters.T(d.name + " - Morning, kiddo. Today's the day. Go see " + (prof.equals(d.name) ? "me" : prof)
            + " about your first POKéMON!"), "fanfare1.ogg", null,
            new SetField(game, "playerCanMove", true, null)))));
      Alerts.push(game, "Generation " + p.generation + " begins! Get your first POKéMON from " + prof + ".");
      System.out.println("[torch] generation " + p.generation + ", dad team " + d.team);
   }

   /** A free, visible tile next to home: not your tile, not another villager's, not tall grass. */
   static com.badlogic.gdx.math.Vector2 openSpotNear(Game game, com.badlogic.gdx.math.Vector2 c) {
      for (int r = 1; r <= 4; r++) {
         for (int[] dd : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}}) {
            com.badlogic.gdx.math.Vector2 q = c.cpy().add(dd[0] * 16 * r, dd[1] * 16 * r);
            Tile t = game.map.overworldTiles.get(q);
            if (t != null && Routines.standable(game, q) && Npcs.at(game, q) == null && !q.epsilonEquals(c, 1)
               && !t.nameUpper.contains("grass")) {
               return q;
            }
         }
      }
      return null;
   }

   /** The kid wakes up at home (same move the game makes when you black out). */
   static void goHome(Game game) {
      Player p = game.player;
      if (p.hmPokemon != null) {
         p.hmPokemon.removeDrawActions(game);
         game.actionStack.remove(p.hmPokemon.standingAction);
         p.hmPokemon = null;
         p.currFieldMove = "";
      }
      java.util.Map<com.badlogic.gdx.math.Vector2, Tile> tiles = game.map.overworldTiles;
      if (p.spawnIndex != -1 && p.spawnIndex < game.map.interiorTiles.size() && game.map.interiorTiles.get(p.spawnIndex) != null) {
         tiles = game.map.interiorTiles.get(p.spawnIndex);
         game.map.interiorTilesIndex = p.spawnIndex;
      }
      game.map.tiles = tiles;
      p.position = p.spawnLoc.cpy();
      game.cam.position.set(p.spawnLoc.x + 16.0F, p.spawnLoc.y, 0.0F);
      p.dirFacing = "down";
      p.currSprite = p.standingSprites.get("down");
      Tile t = tiles.get(p.spawnLoc);
      if (t != null && t.routeBelongsTo != null) {
         game.map.currRoute = t.routeBelongsTo;
      }
      game.map.refreshCache = true;
   }

   /** Parents show their own name (from character creation) everywhere a core character's name appears. */
   static void applyNames(Game game) {
      if (game.map == null || game.map.npcs == null) {
         return;
      }
      for (Npcs.Npc n : game.map.npcs) {
         if ((n.data.type.equals("dad") || n.data.type.equals("grandpa")) && n.data.name != null) {
            Characters.DEFS.get(n.data.type).name = n.data.name;
            n.type().title = n.data.name;
         }
      }
   }

   static java.util.HashMap<String, com.badlogic.gdx.graphics.Texture> parentTextures = new java.util.HashMap<>();

   /** Builds a 10-frame NPC sheet from the parent's character-creation sprite, with their colors. */
   static com.badlogic.gdx.graphics.Texture parentTexture(Npcs.Npc n) {
      String key = n.data.sprite + ":" + n.data.spriteColor + ":" + n.data.spriteSkin;
      com.badlogic.gdx.graphics.Texture cached = parentTextures.get(key);
      if (cached != null) {
         return cached;
      }
      String ch = n.data.sprite;
      com.badlogic.gdx.files.FileHandle file = com.badlogic.gdx.Gdx.files.local("mods/player/" + ch + "/walking.png");
      boolean mod = file.exists();
      if (!mod) {
         file = com.badlogic.gdx.Gdx.files.internal("player/" + ch + "-walking.png");
         if (!file.exists()) {
            file = com.badlogic.gdx.Gdx.files.internal("player/kris-walking.png");
            ch = "kris";
         }
      }
      com.badlogic.gdx.graphics.Color key1 = mainKey(ch, mod);
      com.badlogic.gdx.graphics.Color key2 = new com.badlogic.gdx.graphics.Color(1.0F, 0.80784315F, 0.28235295F, 1.0F);
      com.badlogic.gdx.graphics.Pixmap src = new com.badlogic.gdx.graphics.Pixmap(file);
      com.badlogic.gdx.graphics.Pixmap out = new com.badlogic.gdx.graphics.Pixmap(160, 16, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
      // same frame layout as the NPC sheets: idle down/up/left/right, down A/B, up A/B, left walk, right walk
      int[][] map = {{4, 0}, {5, 0}, {2, 0}, {3, 0}, {0, 0}, {0, 1}, {1, 0}, {1, 1}, {6, 0}, {7, 0}};
      for (int f = 0; f < map.length; f++) {
         int col = map[f][0];
         boolean flip = map[f][1] == 1;
         for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
               int sx = col * 16 + (flip ? 15 - x : x);
               if (sx >= src.getWidth() || y >= src.getHeight()) {
                  continue;
               }
               int px = src.getPixel(sx, y);
               com.badlogic.gdx.graphics.Color c = new com.badlogic.gdx.graphics.Color(px);
               if (c.equals(key1)) {
                  px = n.data.spriteColor;
               } else if (c.equals(key2)) {
                  px = n.data.spriteSkin;
               }
               out.drawPixel(f * 16 + x, y, px);
            }
         }
      }
      com.badlogic.gdx.graphics.Texture t = new com.badlogic.gdx.graphics.Texture(out);
      src.dispose();
      parentTextures.put(key, t);
      return t;
   }

   /** Same rule the game uses for which color in a character's sheet is the "your color" one. */
   static com.badlogic.gdx.graphics.Color mainKey(String ch, boolean mod) {
      java.util.List<String> red = java.util.Arrays.asList("gold", "mark", "red", "chase", "gloria", "hilbert", "lyra", "serena", "brendan", "summer");
      if (mod || red.contains(ch)) {
         return new com.badlogic.gdx.graphics.Color(0.972549F, 0.21960784F, 0.03137255F, 1.0F);
      }
      if (ch.equals("lunick")) {
         return new com.badlogic.gdx.graphics.Color(0.34901962F, 0.28235295F, 1.0F, 1.0F);
      }
      if (ch.equals("kellyn")) {
         return new com.badlogic.gdx.graphics.Color(0.9647059F, 0.03137255F, 0.003921569F, 1.0F);
      }
      return new com.badlogic.gdx.graphics.Color(0.3137255F, 0.28235295F, 0.972549F, 1.0F);
   }

   static Npcs.Npc willow(Game game) {
      for (Npcs.Npc n : game.map.npcs) {
         if (n.data.type.equals("willow")) {
            return n;
         }
      }
      return null;
   }

   static boolean needsStarter(Game game) {
      return game.player.generation >= 2 && game.player.pokemon.isEmpty() && !game.player.unlocks.contains("starter:" + game.player.generation);
   }

   /** Is this the NPC who hands out starters right now? */
   static boolean isProfessor(Game game, Npcs.Npc npc) {
      Npcs.Npc w = willow(game);
      return w != null ? npc == w : npc.data.type.equals("dad");
   }

   static String[] pickStarters(Game game) {
      Characters.loadTypeData();
      ArrayList<String> fire = new ArrayList<>();
      ArrayList<String> water = new ArrayList<>();
      ArrayList<String> grass = new ArrayList<>();
      for (String sp : Characters.starters) {
         if (!Characters.playable(sp)) {
            continue;
         }
         for (String t : Tournaments.types(sp)) {
            if (t.equals("FIRE")) {
               fire.add(sp);
            } else if (t.equals("WATER")) {
               water.add(sp);
            } else if (t.equals("GRASS")) {
               grass.add(sp);
            }
         }
      }
      java.util.Random r = new java.util.Random(game.player.daysPassed * 7L + game.player.generation);
      return new String[]{
         fire.isEmpty() ? "cyndaquil" : fire.get(r.nextInt(fire.size())),
         water.isEmpty() ? "totodile" : water.get(r.nextInt(water.size())),
         grass.isEmpty() ? "chikorita" : grass.get(r.nextInt(grass.size())),
      };
   }

   /** The professor's starter choice, one at a time with YES/NO. */
   static Action starterTalk(Game game, Npcs.Npc npc, Action end) {
      String who = (npc.data.type.equals("willow") ? "PROFESSOR WILLOW" : npc.fullName()) + " - ";
      String[] s = pickStarters(game);
      return new DisplayText(game, Characters.T(who + (npc.data.type.equals("willow")
            ? "You're their kid?! Oh wow. Okay! I've got three POKéMON here. Pick one!"
            : "Here, kiddo. Three POKéMON. Pick one, and pick well.")), null, null,
         offer(game, who, s, 0, end));
   }

   static Action offer(Game game, String who, String[] s, int i, Action end) {
      String[] kinds = {"FIRE", "WATER", "GRASS"};
      String name = s[i].toUpperCase(Locale.ROOT);
      Action yes = new DisplayText.Clear(game, new RunCode(() -> {
         Pokemon pk = new Pokemon(s[i], 5, Pokemon.Generation.CRYSTAL);
         game.player.pokemon.add(pk);
         game.player.markCaught(pk);
         game.player.setCurrPokemon();
         game.player.unlocks.add("starter:" + game.player.generation);
         game.insertAction(new DisplayText(game, Characters.T("You got " + name + "!"), "fanfare1.ogg", null,
            new DisplayText(game, Characters.T(who + "Take good care of it. Your journey starts now: win 8 BADGES and enter the REGIONAL!"),
               null, null, end)));
      }, null));
      Action no = new DisplayText.Clear(game, new RunCode(() -> game.insertAction(offer(game, who, s, (i + 1) % 3, end)), null));
      return new DisplayText(game, Characters.T("Choose " + name + ", the " + kinds[i] + " type?"), null, true, false,
         new DrawYesNoMenu(null, yes, no));
   }
}
