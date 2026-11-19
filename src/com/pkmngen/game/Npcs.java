package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
 * Island villagers (Terraria-style).
 *
 * - An NPC only moves in if there's a free BED in a house you built. Each morning, one NPC
 *   whose unlock condition is met may arrive and settle by that house's door.
 * - By day they wander near their door; at night they're home (hidden).
 * - Press A to talk. Talking once a day raises friendship; friendship unlocks gifts.
 * - Each type has a service: trades (shown in the crafting menu: what you give -> what you get),
 *   healing, boss hints, daily gifts.
 * - NPCs live per island and are saved with the map (MapSaveData.npcs).
 *
 * Sprites: assets/npc/<type>-walking.png (Crystal overworld sheet, 8 frames like the player).
 */
class Npcs {
   // ------------------------------------------------------------------ roster

   static class Type {
      String id;
      String title;
      String[] names;
      String[] lines;
      ArrayList<Player.Craft> trades = new ArrayList<>();

      Type(String id, String title, String[] names, String... lines) {
         this.id = id;
         this.title = title;
         this.names = names;
         this.lines = lines;
      }

      Type trade(String get, int getAmount, Object... give) {
         Player.Craft c = new Player.Craft(get, getAmount);
         for (int i = 0; i < give.length; i += 2) {
            c.requirements.add(new Player.Craft((String)give[i], (Integer)give[i + 1]));
         }
         this.trades.add(c);
         return this;
      }
   }

   static final LinkedHashMap<String, Type> TYPES = new LinkedHashMap<>();

   static {
      add(new Type("merchant", "MERCHANT", new String[]{"RAFI", "OTTO", "BRUNO", "LIAM"},
            "Shipwrecks bring all sorts of treasure. Care to barter?",
            "A fine island! Business will be good here.",
            "I trade fair. Mostly.")
         .trade("poké ball", 3, "copper ore", 2)
         .trade("great ball", 2, "iron ore", 1, "copper ore", 1)
         .trade("silky thread", 3, "log", 4)
         .trade("soft bedding", 1, "grass", 3, "log", 2)
         .trade("escape rope", 1, "grass", 2)
         .trade("berry juice", 2, "berry seed", 3)
         .trade("rare candy", 1, "nugget", 1));
      for (String id : new String[]{"emma", "wood", "flambar", "willow", "locke", "lavender", "bruno", "blue"}) {
         Characters.Def def = Characters.DEFS.get(id);
         add(new Type(id, def.name, new String[]{def.name}, def.greet));
      }
      add(new Type("nurse", "NURSE", new String[]{"JOY", "ANA", "MEI", "CLARA"},
            "Your POKéMON look tired. Let me help!",
            "Remember to rest, trainer.",
            "I used to work at a POKéMON CENTER."));
      add(new Type("hiker", "HIKER", new String[]{"KAI", "DOUG", "BORIS", "TOMAS"},
            "Caves are full of ore if you know where to dig!",
            "Deeper floors hide gold and crystal.",
            "Rock on, kid!")
         .trade("iron ore", 1, "copper ore", 3)
         .trade("gold ore", 1, "iron ore", 3)
         .trade("crystal ore", 1, "gold ore", 2)
         .trade("hard stone", 3, "copper ore", 1));
      add(new Type("fisher", "FISHER", new String[]{"SAM", "NICO", "PEDRO", "WADE"},
            "The sea's been generous lately.",
            "Patience is the best bait.",
            "Ever seen what washes up after a storm?")
         .trade("good rod", 1, "old rod", 1, "silky thread", 3)
         .trade("super rod", 1, "good rod", 1, "iron ore", 3)
         .trade("dive ball", 2, "great ball", 1, "hard shell", 1));
      add(new Type("scientist", "SCIENTIST", new String[]{"ELM", "VERA", "IGOR", "LENA"},
            "Fascinating specimens on this island!",
            "Crystals resonate with evolutionary energy.",
            "Bring me crystal ore and I'll make it worth your while.")
         .trade("water stone", 1, "crystal ore", 2)
         .trade("thunderstone", 1, "crystal ore", 2)
         .trade("fire stone", 1, "crystal ore", 2)
         .trade("leaf stone", 1, "crystal ore", 2)
         .trade("moon stone", 1, "crystal ore", 2)
         .trade("sun stone", 1, "crystal ore", 2));
      add(new Type("elder", "ELDER", new String[]{"OBA", "HUGO", "AMOS", "NAN"},
            "I have watched these islands for a long time.",
            "Mighty POKéMON grow stronger each time one falls.",
            "Be careful out there, young one."));
   }

   static void add(Type t) {
      TYPES.put(t.id, t);
   }

   static boolean isTradeList(ArrayList<Player.Craft> list) {
      for (Type t : TYPES.values()) {
         if (t.trades == list) {
            return true;
         }
      }
      return false;
   }

   /** When each type is willing to come. */
   static boolean unlocked(Game game, String id) {
      Player p = game.player;
      if (Characters.isCore(id)) {
         return Characters.unlocked(game, id);
      }
      switch (id) {
         case "merchant": return true;
         case "nurse": return p.dexCaught.size() >= 10;
         case "hiker": return p.visitedCave;
         case "fisher": return p.hasItem("raft") || p.hasItem("old rod") || p.hasItem("good rod") || p.hasItem("super rod");
         case "scientist": return p.dexSeen.size() >= 30;
         case "elder": return p.bossesBeaten >= 1;
         default: return false;
      }
   }

   // ------------------------------------------------------------------ data

   static class NpcData {
      String type;
      String name;
      float x;
      float y;
      float homeX;
      float homeY;
      float bedX;
      float bedY;
      int bedLayer;
      int friendship;
      int lastTalkDay = -1;
      int lastGiftDay = -1;
      // Core characters (see Characters.java)
      ArrayList<String> team;
      int romance;
      String relation;
      int questStage;
      int questBase;
      int lastGiftTakenDay = -1;
      int lastRomanceDay = -1;
      int lastConfessionDay = -1;
      String lastCatch;
      String lastEvolved;
      int leg;

      NpcData() {
      }
   }

   static class Npc {
      NpcData data;
      Vector2 position;
      Vector2 target;
      String dir = "down";
      int moveTimer = 0;
      int idleTimer = 60;

      Npc(NpcData data) {
         this.data = data;
         this.position = new Vector2(data.x, data.y);
         this.target = this.position.cpy();
      }

      Type type() {
         return TYPES.get(this.data.type);
      }

      String fullName() {
         if (Characters.isCore(this.data.type)) {
            return this.type().title;
         }
         return this.type().title + " " + this.data.name;
      }

      Vector2 home() {
         return new Vector2(this.data.homeX, this.data.homeY);
      }
   }

   // ------------------------------------------------------------------ world

   static boolean awake(Game game) {
      return !game.map.timeOfDay.equals("night");
   }

   static boolean visible(Game game) {
      return awake(game) && game.map.tiles == game.map.overworldTiles;
   }

   static Npc at(Game game, Vector2 pos) {
      if (game.map.npcs == null || !visible(game)) {
         return null;
      }
      for (Npc n : game.map.npcs) {
         if (n.target.epsilonEquals(pos, 1.0F) || n.position.epsilonEquals(pos, 1.0F)) {
            return n;
         }
      }
      return null;
   }

   /** Called on map load and each morning (newDay). Maybe brings a new villager. */
   static void update(Game game, boolean newDay) {
      if (game.map.npcs == null) {
         game.map.npcs = new ArrayList<>();
      }
      for (Npc n : game.map.npcs) {
         // back out of the house every morning
         n.position.set(n.home());
         n.target.set(n.home());
         Characters.daily(game, n, newDay);
      }
      if (!newDay && !game.map.npcs.isEmpty()) {
         return;
      }
      tryArrival(game, !newDay);
   }

   static String pendingText = null;

   static void tryArrival(Game game, boolean quiet) {
      String next = null;
      for (String id : TYPES.keySet()) {
         boolean present = false;
         for (Npc n : game.map.npcs) {
            if (n.data.type.equals(id)) {
               present = true;
               break;
            }
         }
         if (!present && unlocked(game, id)) {
            next = id;
            break;
         }
      }
      if (next == null) {
         return;
      }
      Tile[] home = findFreeHome(game);
      if (home == null) {
         return;
      }
      NpcData d = new NpcData();
      Type t = TYPES.get(next);
      d.type = next;
      d.name = t.names[Game.rand.nextInt(t.names.length)];
      Tile bed = home[0];
      Tile outside = home[1];
      d.bedX = bed.position.x;
      d.bedY = bed.position.y;
      d.bedLayer = (int)home[2].position.x;
      d.homeX = outside.position.x;
      d.homeY = outside.position.y;
      d.x = d.homeX;
      d.y = d.homeY;
      Npc npc = new Npc(d);
      game.map.npcs.add(npc);
      Characters.daily(game, npc, false);
      System.out.println("NPC arrived: " + npc.fullName() + " at " + outside.position);
      pendingText = npc.fullName() + " arrived on the island and moved into one of your houses!";
   }

   /** Returns {bed tile, walkable tile outside the house door, Tile(layerIndex,0) as a carrier} or null. */
   static Tile[] findFreeHome(Game game) {
      for (int layer = 100; layer < game.map.interiorTiles.size(); layer++) {
         java.util.HashMap<Vector2, Tile> tiles = game.map.interiorTiles.get(layer);
         if (tiles == null) {
            continue;
         }
         for (Tile tile : tiles.values()) {
            if (!tile.nameUpper.startsWith("house_bed1")) {
               continue;
            }
            boolean taken = false;
            for (Npc n : game.map.npcs) {
               if (n.data.bedLayer == layer && n.data.bedX == tile.position.x && n.data.bedY == tile.position.y) {
                  taken = true;
                  break;
               }
            }
            if (taken) {
               continue;
            }
            Tile outside = outsideDoor(game, tile.position);
            if (outside != null) {
               return new Tile[]{tile, outside, new Tile("blank", new Vector2(layer, 0))};
            }
         }
      }
      return null;
   }

   /** Nearest overworld house door to this spot; returns the walkable tile just below it. */
   static Tile outsideDoor(Game game, Vector2 near) {
      Tile best = null;
      float bestDst = Float.MAX_VALUE;
      for (int i = -10; i <= 10; i++) {
         for (int j = -10; j <= 10; j++) {
            Vector2 p = near.cpy().add(i * 16, j * 16);
            Tile t = game.map.overworldTiles.get(p);
            if (t == null || !t.nameUpper.contains("door")) {
               continue;
            }
            Tile below = game.map.overworldTiles.get(p.cpy().add(0.0F, -16.0F));
            if (below != null && !below.isSolid && !below.isWater) {
               float d = p.dst2(near);
               if (d < bestDst) {
                  bestDst = d;
                  best = below;
               }
            }
         }
      }
      return best;
   }

   static boolean walkable(Game game, Npc npc, Vector2 p) {
      Tile t = game.map.overworldTiles.get(p);
      if (t == null || t.isSolid || t.isWater || t.isLedge || t.nameUpper.contains("door") || t.name.contains("door")) {
         return false;
      }
      if (p.dst(npc.home()) > 80.0F || p.epsilonEquals(game.player.position, 1.0F) || game.map.pokemon.containsKey(p)) {
         return false;
      }
      for (Npc other : game.map.npcs) {
         if (other != npc && (other.target.epsilonEquals(p, 1.0F) || other.position.epsilonEquals(p, 1.0F))) {
            return false;
         }
      }
      return true;
   }

   // ------------------------------------------------------------------ talking

   /** If an NPC stands at pos, start a conversation and return true. */
   static boolean talk(Game game, Vector2 pos) {
      Npc npc = at(game, pos);
      if (npc == null || npc.moveTimer > 0) {
         return false;
      }
      Player player = game.player;
      npc.dir = Player.oppDirs.get(player.dirFacing);
      npc.idleTimer = 240;
      Type type = npc.type();
      boolean firstToday = npc.data.lastTalkDay != player.daysPassed;
      if (firstToday) {
         npc.data.lastTalkDay = player.daysPassed;
         npc.data.friendship = Math.min(10, npc.data.friendship + 1);
      }
      if (Characters.isCore(npc.data.type)) {
         game.playerCanMove = false;
         game.insertAction(Characters.talk(game, npc, new SetField(game, "playerCanMove", true, null)));
         return true;
      }
      String line = type.lines[Game.rand.nextInt(type.lines.length)];
      String hearts = npc.data.friendship >= 8 ? " We're close friends now!" : "";
      game.playerCanMove = false;
      Action end = new SetField(game, "playerCanMove", true, null);
      Action service = service(game, npc, end);
      game.insertAction(new DisplayText(game, Characters.T(npc.fullName() + " - " + line + hearts), null, null, service));
      return true;
   }

   static Action service(Game game, Npc npc, Action end) {
      Player player = game.player;
      Type type = npc.type();
      // Daily gift once friendship is high enough.
      String gift = null;
      if (npc.data.friendship >= 3 && player.daysPassed - npc.data.lastGiftDay >= 3) {
         gift = giftFor(npc.data.type);
      }
      Action after = end;
      if (gift != null) {
         npc.data.lastGiftDay = player.daysPassed;
         String[] g = gift.split(":");
         player.addItem(g[0], Integer.valueOf(g[1]));
         after = new DisplayText(game, Characters.T("You're a good friend. Take this! Got " + g[1] + " " + g[0].toUpperCase(Locale.ROOT) + "."), "fanfare1.ogg", null, end);
      }

      if (!type.trades.isEmpty()) {
         final Action afterTrade = after;
         Action open = new RunCode(() -> {
            game.player.isCrafting = true;
            game.insertAction(new DrawCraftsMenu.Intro(null, 9,
               new DrawCraftsMenu(game, type.trades, new SetField(game.player, "isCrafting", false, afterTrade))));
         }, null);
         return new DisplayText(game, Characters.T("Want to trade?"), null, true, false,
            new DrawYesNoMenu(null, new DisplayText.Clear(game, open), new DisplayText.Clear(game, after)));
      }
      if (npc.data.type.equals("nurse")) {
         return new DisplayText(game, Characters.T("Shall I heal your POKéMON?"), null, true, false,
            new DrawYesNoMenu(null, new DisplayText.Clear(game, new RunCode(() -> {
               for (Pokemon p : game.player.pokemon) {
                  if (!p.isEgg) {
                     p.currentStats.put("hp", p.maxStats.get("hp"));
                     p.status = null;
                  }
               }
            }, new DisplayText(game, Characters.T("Your POKéMON are fighting fit!"), "fanfare1.ogg", null, after))),
               new DisplayText.Clear(game, after)));
      }
      if (npc.data.type.equals("elder")) {
         return new DisplayText(game, Characters.T(bossHint(game)), null, null, after);
      }
      return after;
   }

   /** "!" over a villager's head when talking to them would give you something. */
   static boolean hasNews(Game game, Npc npc) {
      if (Characters.isCore(npc.data.type)) {
         return Characters.hasNews(game, npc);
      }
      return npc.data.friendship >= 3 && game.player.daysPassed - npc.data.lastGiftDay >= 3 && giftFor(npc.data.type) != null;
   }

   static String giftFor(String type) {
      String[][] gifts = {
         {"merchant", "nugget:1"},
         {"nurse", "berry juice:3"},
         {"hiker", "iron ore:2"},
         {"fisher", "dive ball:2"},
         {"scientist", "crystal ore:1"},
         {"elder", "rare candy:1"},
      };
      for (String[] g : gifts) {
         if (g[0].equals(type)) {
            return g[1];
         }
      }
      return null;
   }

   static String bossHint(Game game) {
      Pokemon best = null;
      for (Pokemon p : game.map.pokemon.values()) {
         if (p.isBoss && (best == null || p.position.dst2(game.player.position) < best.position.dst2(game.player.position))) {
            best = p;
         }
      }
      if (best == null) {
         return "No mighty POKéMON stir on this island right now.";
      }
      Vector2 d = best.position.cpy().sub(game.player.position);
      String ns = d.y > 160 ? "NORTH" : d.y < -160 ? "SOUTH" : "";
      String ew = d.x > 160 ? "EAST" : d.x < -160 ? "WEST" : "";
      String dir = (ns + (ns.isEmpty() || ew.isEmpty() ? "" : "-") + ew);
      if (dir.isEmpty()) {
         dir = "very close";
      } else {
         dir = "to the " + dir;
      }
      return "A mighty " + best.nickname.toUpperCase(Locale.ROOT) + " lurks " + dir + ". It is LV" + best.level + ".";
   }

   // ------------------------------------------------------------------ driver (AI + drawing)

   static Action driver = null;

   static void ensureDriver(Game game) {
      if (driver == null || !game.actionStack.contains(driver)) {
         driver = new Driver();
         game.insertAction(driver);
      }
   }

   static class Driver extends Action {
      public Action.Layer layer = Action.Layer.map_131;
      HashMap<String, Texture> textures = new HashMap<>();
      TextureRegion region = new TextureRegion();
      TextureRegion bang;
      int checkTimer = 0;
      java.util.HashSet<Npc> news = new java.util.HashSet<>();

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      Driver() {
         this.bang = new TextureRegion(TextureCache.get(Gdx.files.internal("emotes.png")), 0, 0, 16, 16);
      }

      Texture tex(String type) {
         return this.textures.computeIfAbsent(type, t -> TextureCache.get(Gdx.files.internal("npc/" + t + "-walking.png")));
      }

      @Override
      public void step(Game game) {
         if (game.map == null || game.map.npcs == null) {
            return;
         }
         if (pendingText != null && game.playerCanMove && game.battle.drawAction == null) {
            game.playerCanMove = false;
            game.insertAction(new DisplayText(game, Characters.T(pendingText), "fanfare1.ogg", null, new SetField(game, "playerCanMove", true, null)));
            pendingText = null;
         }
         if (!visible(game)) {
            return;
         }
         for (Npc n : game.map.npcs) {
            this.move(game, n);
            int frame = Arrays.asList("down", "up", "left", "right").indexOf(n.dir);
            if (n.moveTimer > 4 && n.moveTimer < 12) {
               if (n.dir.equals("down")) {
                  frame = 4 + n.data.leg;
               } else if (n.dir.equals("up")) {
                  frame = 6 + n.data.leg;
               } else {
                  frame = n.dir.equals("left") ? 8 : 9;
               }
            }
            this.region.setTexture(this.tex(n.data.type));
            this.region.setRegion(frame * 16, 0, 16, 16);
            game.mapBatch.draw(this.region, n.position.x, n.position.y + 4.0F);
            if (this.news.contains(n) && (this.checkTimer / 20) % 2 == 0) {
               game.mapBatch.draw(this.bang, n.position.x, n.position.y + 20.0F);
            }
         }
         // Re-check who has news a couple of times a second (cheap, but not every frame).
         if (this.checkTimer++ % 30 == 0) {
            this.news.clear();
            for (Npc n : game.map.npcs) {
               if (hasNews(game, n)) {
                  this.news.add(n);
               }
            }
         }
      }

      void move(Game game, Npc n) {
         if (n.moveTimer > 0) {
            n.moveTimer--;
            Vector2 step = n.target.cpy().sub(n.position);
            if (step.len() > 1.0F) {
               step.nor();
            }
            n.position.add(step);
            if (n.moveTimer == 0) {
               n.position.set(n.target);
               n.data.x = n.position.x;
               n.data.y = n.position.y;
            }
            return;
         }
         // Stand still while the player is right next to us (so they can talk).
         if (n.position.dst2(game.player.position) <= 16 * 16 + 1) {
            n.idleTimer = Math.max(n.idleTimer, 90);
            return;
         }
         if (--n.idleTimer > 0 || !game.playerCanMove) {
            return;
         }
         n.idleTimer = 120 + Game.rand.nextInt(180);
         String[] dirs = {"up", "down", "left", "right"};
         String dir = dirs[Game.rand.nextInt(4)];
         n.dir = dir;
         Vector2 next = n.position.cpy();
         if (dir.equals("up")) {
            next.y += 16;
         } else if (dir.equals("down")) {
            next.y -= 16;
         } else if (dir.equals("left")) {
            next.x -= 16;
         } else {
            next.x += 16;
         }
         if (walkable(game, n, next)) {
            n.target.set(next);
            n.moveTimer = 16;
            n.data.leg = 1 - n.data.leg;
         }
      }
   }

   // ------------------------------------------------------------------ save / load

   static ArrayList<NpcData> save(Game game) {
      ArrayList<NpcData> out = new ArrayList<>();
      if (game.map.npcs != null) {
         for (Npc n : game.map.npcs) {
            n.data.x = n.target.x;
            n.data.y = n.target.y;
            out.add(n.data);
         }
      }
      return out;
   }

   static ArrayList<Npc> load(List<NpcData> data) {
      ArrayList<Npc> out = new ArrayList<>();
      if (data != null) {
         for (NpcData d : data) {
            if (TYPES.containsKey(d.type)) {
               out.add(new Npc(d));
            }
         }
      }
      return out;
   }
}
