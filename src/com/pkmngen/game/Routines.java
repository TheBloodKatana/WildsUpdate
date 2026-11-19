package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.Random;

/**
 * Daily routines. The day is split into MORNING, NOON, EVENING and NIGHT; for each part every
 * villager type has a kind of place it likes to be (tall grass, the shore, trees, rocks, a cave
 * mouth, the town plaza, home...). When the part of the day changes the villager walks there
 * (or quietly appears there if neither spot is on screen) and hangs around it. "inside" means
 * they stay in their house and aren't on the map.
 */
class Routines {
   static final String[] PERIODS = {"morning", "noon", "evening", "night"};

   /** type -> spot kind for morning, noon, evening, night. */
   static final HashMap<String, String[]> SCHEDULE = new HashMap<>();
   /** "type:spot" -> things they say while there. */
   static final HashMap<String, String[]> LINES = new HashMap<>();

   static void sched(String type, String morning, String noon, String evening, String night) {
      SCHEDULE.put(type, new String[]{morning, noon, evening, night});
   }

   static void lines(String type, String spot, String... l) {
      LINES.put(type + ":" + spot, l);
   }

   static {
      // Core characters
      sched("emma", "grass", "plaza", "shore", "inside");
      lines("emma", "grass", "Morning training. Don't distract me. Unless you want to lose.", "First one out here every day. Obviously.");
      lines("emma", "plaza", "Just showing everyone who the strongest trainer is.", "Bored. Entertain me. Or battle me.");
      lines("emma", "shore", "Race you to the end of the beach. Loser buys dinner.", "The sea's nice at this hour. ...What? I can like things.");

      sched("wood", "trees", "home", "plaza", "home");
      lines("wood", "trees", "Chopping logs is the best workout there is.", "Gotta stay strong. Somebody has to protect this place.");
      lines("wood", "home", "Just keeping an eye on things.", "Night watch. Go get some sleep, I've got this.", "Hear that? ...Never mind. Just the wind.");
      lines("wood", "plaza", "Everyone's safe and sound. Good.", "You look tired. Eat something, okay?");

      sched("flambar", "home", "plaza", "trees", "inside");
      lines("flambar", "home", "I made breakfast! There's extra if you're hungry.", "Good morning! Did you sleep well?");
      lines("flambar", "plaza", "I'm checking in on everybody today.", "If anyone needs anything, I'm right here!");
      lines("flambar", "trees", "Collecting firewood for tonight. Want to sit by the fire later?", "Evenings are my favorite. Everything glows.");

      sched("willow", "inside", "wander", "plaza", "home");
      lines("willow", "wander", "I had an idea! ...Then I had a different idea. I'm following both.", "Don't mind me, just following a sound. Or a smell. Unclear!");
      lines("willow", "plaza", "Want to hear about my theory? It's mostly sparks.", "I wired the town sign to glow. Don't tell anyone.");
      lines("willow", "home", "Stargazing! That one's a satellite. Or a ZAPDOS. Probably a satellite.", "Can't sleep, too many ideas.");

      sched("locke", "grass", "rocks", "trees", "inside");
      lines("locke", "grass", "Shh. Bugs are out this time of day.", "The grass tells you a lot, if you listen.");
      lines("locke", "rocks", "Good stone here.", "The ground's restless today.");
      lines("locke", "trees", "Old trees. Older than all of us.", "Sit. Listen for a while.");

      sched("lavender", "inside", "home", "trees", "wander");
      lines("lavender", "home", "Mornings are too bright... I'm still waking up.", "Oh! You found me. Hee hee.");
      lines("lavender", "trees", "The shadows are getting longer. My favorite part of the day.", "Something's watching us from the trees. Isn't that nice?");
      lines("lavender", "wander", "Midnight walk! The spirits are friendlier now.", "You're out late too? We're the same, you and me.", "Don't look behind you. ...Kidding! Maybe.");

      sched("bruno", "rocks", "cave", "plaza", "trees");
      lines("bruno", "rocks", "Training. Leave.", "These rocks hit back harder than you do.");
      lines("bruno", "cave", "Strong POKéMON live down there. I'm stronger.", "Going in? Don't come crying to me.");
      lines("bruno", "plaza", "Just watching. Who's weak. Who's not.", "Keep walking.");
      lines("bruno", "trees", "Night's when the real trainers come out.", "You shouldn't be out here. ...Neither should I.");

      sched("blue", "plaza", "wander", "home", "inside");
      lines("blue", "plaza", "Planning the next part of town. We need more houses!", "Morning! Got big plans today.");
      lines("blue", "wander", "Scouting the island. There's room for a whole city out here.", "Smell that? That's progress.");
      lines("blue", "home", "Long day. Good day, though.", "Tomorrow we build even more. Get some rest.");

      // Villagers
      sched("merchant", "plaza", "plaza", "home", "inside");
      lines("merchant", "plaza", "Fresh goods! Well, fresh-ish.", "Business is booming today!");
      sched("nurse", "home", "plaza", "home", "inside");
      lines("nurse", "plaza", "Just making my rounds. Everyone looks healthy!");
      sched("hiker", "rocks", "cave", "home", "inside");
      lines("hiker", "cave", "Heading in for some ore. Wish me luck!");
      lines("hiker", "rocks", "Nothing like a morning climb!");
      sched("fisher", "shore", "shore", "shore", "inside");
      lines("fisher", "shore", "They're biting today!", "Shh... you'll scare the fish.");
      sched("scientist", "grass", "wander", "home", "inside");
      lines("scientist", "grass", "Taking samples. Don't step on the samples.", "Fascinating behaviour in this grass!");
      sched("elder", "home", "plaza", "home", "inside");
      lines("elder", "plaza", "I like watching the town grow. Reminds me of when I was young.");
      sched("citymanager", "plaza", "wander", "plaza", "inside");
      lines("citymanager", "wander", "Surveying. A good town starts with good land.", "Measuring things. It's more exciting than it sounds.");
      sched("director", "plaza", "plaza", "home", "inside");
      lines("director", "plaza", "A LEAGUE needs strong trainers and a strong town.", "I'm always watching for talent.");
   }

   static String period(Game game) {
      if (game.map.timeOfDay.equals("night")) {
         return "night";
      }
      int t = CycleDayNight.dayTimer; // counts down from 18000 during the day
      return t > 12000 ? "morning" : t > 6000 ? "noon" : "evening";
   }

   static int periodIndex(Game game) {
      return java.util.Arrays.asList(PERIODS).indexOf(period(game));
   }

   /** Where this villager wants to be right now. */
   static String spot(Game game, Npcs.Npc npc) {
      String[] s = SCHEDULE.get(npc.data.type);
      int i = periodIndex(game);
      // Gym leaders hold court in their town at midday.
      if (i == 1 && "gym".equals(npc.data.role)) {
         return "gym";
      }
      if (s == null) {
         s = Trainers.schedule(npc);
      }
      if (s == null) {
         return i == 3 ? "inside" : "home";
      }
      // Willow is spontaneous: on any day she might go somewhere else entirely.
      if (npc.data.type.equals("willow") && i < 3 && !s[i].equals("inside")) {
         Random r = new Random(game.player.daysPassed * 7L + i);
         if (r.nextInt(3) == 0) {
            String[] any = {"grass", "shore", "trees", "rocks", "wander"};
            return any[r.nextInt(any.length)];
         }
      }
      return s[i];
   }

   static boolean outside(Game game, Npcs.Npc npc) {
      return game.map.tiles == game.map.overworldTiles && !spot(game, npc).equals("inside");
   }

   /** Something they'd say about what they're doing, or null. */
   static String line(Game game, Npcs.Npc npc) {
      String[] l = LINES.get(npc.data.type + ":" + spot(game, npc));
      return l == null ? null : l[Game.rand.nextInt(l.length)];
   }

   // ------------------------------------------------------------------ finding spots

   static boolean standable(Game game, Vector2 p) {
      Tile t = game.map.overworldTiles.get(p);
      return t != null && !t.isSolid && !t.isWater && !t.isLedge && !t.nameUpper.contains("door") && !t.name.contains("door");
   }

   static boolean near(Game game, Vector2 p, java.util.function.Predicate<Tile> what) {
      for (int[] d : new int[][]{{16, 0}, {-16, 0}, {0, 16}, {0, -16}}) {
         Tile t = game.map.overworldTiles.get(p.cpy().add(d[0], d[1]));
         if (t != null && what.test(t)) {
            return true;
         }
      }
      return false;
   }

   static boolean matches(Game game, String kind, Vector2 p, Tile t) {
      switch (kind) {
         case "grass": return t.nameUpper.contains("grass") && !t.nameUpper.contains("planted");
         case "shore": return near(game, p, n -> n.isWater);
         case "trees": return near(game, p, n -> n.nameUpper.contains("tree"));
         case "rocks": return near(game, p, n -> n.nameUpper.contains("rock") || n.name.contains("mountain"));
         default: return false;
      }
   }

   /** Picks today's spot of this kind (computed once per part of the day). */
   static Vector2 anchor(Game game, Npcs.Npc npc) {
      String kind = spot(game, npc);
      String key = game.player.daysPassed + ":" + periodIndex(game) + ":" + kind;
      if (key.equals(npc.anchorKey) && npc.anchor != null) {
         return npc.anchor;
      }
      npc.anchorKey = key;
      npc.anchor = find(game, npc, kind);
      return npc.anchor;
   }

   static Vector2 find(Game game, Npcs.Npc npc, String kind) {
      Vector2 home = npc.home();
      switch (kind) {
         case "home":
         case "inside":
            return home;
         case "gym": {
            for (Cities.CityData c : Cities.list(game)) {
               if (c.name.equals(npc.data.gymCity)) {
                  Vector2 p = nearestStandable(game, new Vector2(c.x + 32, c.y), 4);
                  if (p != null) {
                     return p;
                  }
               }
            }
            return home;
         }
         case "plaza": {
            Cities.CityData c = Cities.nearest(game, home);
            if (c != null) {
               Vector2 p = nearestStandable(game, new Vector2(c.x, c.y), 4);
               if (p != null) {
                  return p;
               }
            }
            return home;
         }
         case "cave": {
            Vector2 best = null;
            for (String k : game.player.caveEntrances.keySet()) {
               String prefix = CaveGen.prefix(game);
               if (k.endsWith("placed") || !k.startsWith(prefix) || (prefix.isEmpty() && k.contains("|"))) {
                  continue;
               }
               String[] xy = k.substring(prefix.length()).split(",");
               Vector2 cave = new Vector2(Float.valueOf(xy[0]), Float.valueOf(xy[1]));
               Vector2 p = nearestStandable(game, cave, 3);
               if (p != null && p.dst(home) < 24 * 16 && (best == null || p.dst2(home) < best.dst2(home))) {
                  best = p;
               }
            }
            return best != null ? best : find(game, npc, "rocks");
         }
         case "wander": {
            Random r = new Random(game.player.daysPassed * 31L + periodIndex(game) + npc.data.type.hashCode());
            for (int i = 0; i < 40; i++) {
               Vector2 p = home.cpy().add((r.nextInt(25) - 12) * 16, (r.nextInt(25) - 12) * 16);
               if (standable(game, p)) {
                  return p;
               }
            }
            return home;
         }
         default: {
            // Nearest matching tile within 20 tiles of home; ties broken per villager so they don't stack.
            Vector2 best = null;
            float bestScore = Float.MAX_VALUE;
            Random r = new Random(npc.data.type.hashCode() + game.player.daysPassed);
            for (int i = -20; i <= 20; i++) {
               for (int j = -20; j <= 20; j++) {
                  Vector2 p = home.cpy().add(i * 16, j * 16);
                  Tile t = game.map.overworldTiles.get(p);
                  if (t == null || !standable(game, p) || !matches(game, kind, p, t)) {
                     continue;
                  }
                  float score = p.dst(home) + r.nextInt(64);
                  if (score < bestScore) {
                     bestScore = score;
                     best = p;
                  }
               }
            }
            return best != null ? best : home;
         }
      }
   }

   static Vector2 nearestStandable(Game game, Vector2 c, int radius) {
      Vector2 snapped = new Vector2(c.x - c.x % 16, c.y - c.y % 16);
      for (int r = 0; r <= radius; r++) {
         for (int i = -r; i <= r; i++) {
            for (int j = -r; j <= r; j++) {
               if (Math.max(Math.abs(i), Math.abs(j)) != r) {
                  continue;
               }
               Vector2 p = snapped.cpy().add(i * 16, j * 16);
               if (standable(game, p)) {
                  return p;
               }
            }
         }
      }
      return null;
   }

   /** True if the player could see this tile right now. */
   static boolean onScreen(Game game, Vector2 p) {
      return Math.abs(p.x - game.player.position.x) < 12 * 16 && Math.abs(p.y - game.player.position.y) < 10 * 16;
   }
}
