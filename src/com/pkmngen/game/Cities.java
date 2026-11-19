package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Towns. The CITY MANAGER watches where you build: three or more houses close together make a
 * potential settlement, and talking to the manager lets you found it (pick a name, a sign goes
 * up at its center). Settlements grow with the houses around them:
 *   VILLAGE 3+ houses (1 gym slot), TOWN 6+ (2), CITY 10+ (3).
 * The town center is the "plaza" villagers gather at in their routines, and the LEAGUE
 * DIRECTOR puts gyms in founded towns.
 */
class Cities {
   static final float RADIUS = 12 * 16;   // houses this close to the center belong to it
   static final int[] TIER_HOUSES = {3, 6, 10};
   static final String[] TIERS = {"VILLAGE", "TOWN", "CITY"};
   static final String[] NAMES = {"CEDAR", "CORAL", "MAPLE", "DRIFT", "EMBER", "MISTY", "STONE", "AZURE",
      "SAGE", "PEBBLE", "DUSK", "TIDE", "FERN", "AMBER", "GALE", "LUMEN"};

   static class CityData {
      String name;      // base name, e.g. CEDAR
      float x;
      float y;
      int tier;         // highest tier announced so far (0 village, 1 town, 2 city)
      int foundedDay;
      ArrayList<String> succession = new ArrayList<>();   // next in line for a gym slot (npc keys)
      String lastWinner;
      int lastTourneyDay = -1;
      boolean leagueHall = false;   // the LEAGUE HALL stands here (needs a CITY)
      float hallX;
      float hallY;

      CityData() {
      }
   }

   static ArrayList<CityData> list(Game game) {
      if (game.map.cities == null) {
         game.map.cities = new ArrayList<>();
      }
      return game.map.cities;
   }

   static String title(CityData c) {
      return c.name + " " + TIERS[c.tier];
   }

   static int tierFor(int houses) {
      int t = -1;
      for (int i = 0; i < TIER_HOUSES.length; i++) {
         if (houses >= TIER_HOUSES[i]) {
            t = i;
         }
      }
      return t;
   }

   static int gymSlots(CityData c) {
      return c.tier + 1;
   }

   // ------------------------------------------------------------------ houses

   static ArrayList<Vector2> doorCache = new ArrayList<>();
   static long doorCacheTime = 0;
   static Object doorCacheMap = null;

   /** Overworld doors of houses the player built (cached for a few seconds). */
   static ArrayList<Vector2> houseDoors(Game game) {
      long now = System.currentTimeMillis();
      if (now - doorCacheTime < 5000 && doorCacheMap == game.map.overworldTiles) {
         return doorCache;
      }
      doorCacheTime = now;
      doorCacheMap = game.map.overworldTiles;
      doorCache = new ArrayList<>();
      for (Tile t : game.map.overworldTiles.values()) {
         if (t.nameUpper.startsWith("house") && t.nameUpper.contains("door")) {
            doorCache.add(t.position.cpy());
         }
      }
      return doorCache;
   }

   static int housesIn(Game game, CityData c) {
      int n = 0;
      Vector2 center = new Vector2(c.x, c.y);
      for (Vector2 d : houseDoors(game)) {
         if (d.dst(center) <= RADIUS) {
            n++;
         }
      }
      return n;
   }

   static CityData cityAt(Game game, Vector2 p) {
      for (CityData c : list(game)) {
         if (p.dst(c.x, c.y) <= RADIUS) {
            return c;
         }
      }
      return null;
   }

   static CityData nearest(Game game, Vector2 p) {
      CityData best = null;
      for (CityData c : list(game)) {
         if (p.dst(c.x, c.y) < 30 * 16 && (best == null || p.dst2(c.x, c.y) < p.dst2(best.x, best.y))) {
            best = c;
         }
      }
      return best;
   }

   /** Center of the biggest cluster of 3+ houses that isn't part of a town yet, or null. */
   static Vector2 potential(Game game) {
      ArrayList<Vector2> free = new ArrayList<>();
      for (Vector2 d : houseDoors(game)) {
         if (cityAt(game, d) == null) {
            free.add(d);
         }
      }
      Vector2 bestCenter = null;
      int bestCount = 0;
      for (Vector2 d : free) {
         Vector2 sum = new Vector2();
         int n = 0;
         for (Vector2 o : free) {
            if (o.dst(d) <= RADIUS) {
               sum.add(o);
               n++;
            }
         }
         if (n >= TIER_HOUSES[0] && n > bestCount) {
            bestCount = n;
            sum.scl(1.0F / n);
            bestCenter = new Vector2(sum.x - sum.x % 16, sum.y - sum.y % 16);
         }
      }
      // Don't let a new town's center sit inside an existing one.
      if (bestCenter != null && cityAt(game, bestCenter) != null) {
         return null;
      }
      return bestCenter;
   }

   /** Houses near the best "almost" spot (for hints): returns {count, x, y} or null. */
   static int[] almost(Game game) {
      int best = 0;
      Vector2 at = null;
      for (Vector2 d : houseDoors(game)) {
         if (cityAt(game, d) != null) {
            continue;
         }
         int n = 0;
         for (Vector2 o : houseDoors(game)) {
            if (o.dst(d) <= RADIUS && cityAt(game, o) == null) {
               n++;
            }
         }
         if (n > best) {
            best = n;
            at = d;
         }
      }
      return at == null ? null : new int[]{best, (int)at.x, (int)at.y};
   }

   /** A founded town that has grown into the next tier since it was last announced. */
   static CityData grown(Game game) {
      for (CityData c : list(game)) {
         if (tierFor(housesIn(game, c)) > c.tier) {
            return c;
         }
      }
      return null;
   }

   static String suggestName(Game game, int attempt) {
      ArrayList<String> free = new ArrayList<>();
      for (String n : NAMES) {
         boolean used = false;
         for (CityData c : list(game)) {
            used |= c.name.equals(n);
         }
         if (!used) {
            free.add(n);
         }
      }
      if (free.isEmpty()) {
         return "NEW " + NAMES[attempt % NAMES.length];
      }
      java.util.Collections.shuffle(free, new java.util.Random(game.player.daysPassed * 13L + list(game).size()));
      return free.get(attempt % free.size());
   }

   static CityData found(Game game, Vector2 center, String name) {
      CityData c = new CityData();
      c.name = name;
      c.x = center.x;
      c.y = center.y;
      c.tier = Math.max(0, tierFor(housesIn(game, c)));
      c.foundedDay = game.player.daysPassed;
      list(game).add(c);
      placeSign(game, c);
      return c;
   }

   static void placeSign(Game game, CityData c) {
      Vector2 p = null;
      Vector2 center = new Vector2(c.x - c.x % 16, c.y - c.y % 16);
      for (int r = 1; r <= 4 && p == null; r++) {
         for (int[] d : new int[][]{{0, r}, {r, 0}, {-r, 0}, {0, -r}, {r, r}, {-r, r}, {r, -r}, {-r, -r}}) {
            Vector2 q = center.cpy().add(d[0] * 16, d[1] * 16);
            if (Routines.standable(game, q) && !q.epsilonEquals(game.player.position, 1.0F) && Npcs.at(game, q) == null) {
               p = q;
               break;
            }
         }
      }
      if (p == null) {
         return;
      }
      Tile old = game.map.overworldTiles.get(p);
      Tile sign = new TrainerTipsTile(p.cpy(), old != null ? old.routeBelongsTo : null, false,
         Characters.T(title(c) + " - Founded on day " + c.foundedDay + ". Home sweet home."));
      game.map.overworldTiles.put(p.cpy(), sign);
      game.map.refreshCache = true;
   }

   static void updateSign(Game game, CityData c) {
      for (int i = -4; i <= 4; i++) {
         for (int j = -4; j <= 4; j++) {
            Tile t = game.map.overworldTiles.get(new Vector2(c.x - c.x % 16 + i * 16, c.y - c.y % 16 + j * 16));
            if (t instanceof TrainerTipsTile && ((TrainerTipsTile)t).message.startsWith(c.name + " ")) {
               ((TrainerTipsTile)t).message = Characters.T(title(c) + " - Founded on day " + c.foundedDay + ". Home sweet home.");
               return;
            }
         }
      }
   }

   // ------------------------------------------------------------------ the City Manager

   static boolean hasNews(Game game) {
      return potential(game) != null || grown(game) != null;
   }

   static Action managerTalk(Game game, Npcs.Npc npc, Action end) {
      String who = npc.fullName() + " - ";
      CityData grew = grown(game);
      if (grew != null) {
         String before = title(grew);
         grew.tier = tierFor(housesIn(game, grew));
         updateSign(game, grew);
         String extra = gymSlots(grew) > 1 ? " It can hold " + gymSlots(grew) + " gyms now." : "";
         return new DisplayText(game, Characters.T(who + "Great news! " + before + " has grown into " + title(grew) + "!" + extra),
            "fanfare1.ogg", null, end);
      }
      Vector2 spot = potential(game);
      if (spot != null) {
         int houses = 0;
         for (Vector2 d : houseDoors(game)) {
            if (d.dst(spot) <= RADIUS && cityAt(game, d) == null) {
               houses++;
            }
         }
         return new DisplayText(game, Characters.T(who + "I've been looking around. Those " + houses
            + " houses over there could be a proper village!"), null, null, namePrompt(game, npc, spot, 0, end));
      }
      int[] a = almost(game);
      String tip;
      if (list(game).isEmpty()) {
         tip = a == null || a[0] == 0
            ? "Build a few houses close together and I'll help you turn them into a village."
            : "You've got " + a[0] + " house" + (a[0] == 1 ? "" : "s") + " near each other. " + (TIER_HOUSES[0] - a[0])
               + " more nearby and that's a village!";
      } else {
         CityData c = list(game).get(Game.rand.nextInt(list(game).size()));
         int h = housesIn(game, c);
         int next = c.tier + 1 < TIER_HOUSES.length ? TIER_HOUSES[c.tier + 1] : -1;
         tip = next < 0 ? title(c) + " is thriving. I'm proud of us."
            : title(c) + " is coming along. " + (next - h) + " more house" + (next - h == 1 ? "" : "s") + " and it'll be a "
               + TIERS[c.tier + 1] + ".";
      }
      return new DisplayText(game, Characters.T(who + tip), null, null, end);
   }

   static Action namePrompt(Game game, Npcs.Npc npc, Vector2 spot, int attempt, Action end) {
      String name = suggestName(game, attempt);
      Action yes = new DisplayText.Clear(game, new RunCode(() -> {
         CityData c = found(game, spot, name);
         Npcs.pendingText = null;
         game.insertAction(new DisplayText(game, Characters.T(title(c) + " is now on the map! I put up a sign."), "fanfare1.ogg", null, end));
      }, null));
      Action no = attempt >= 5
         ? new DisplayText.Clear(game, new DisplayText(game, Characters.T("Let's think about it. Come back any time."), null, null, end))
         : new DisplayText.Clear(game, namePrompt(game, npc, spot, attempt + 1, end));
      String ask = attempt == 0 ? "How about we call it " + name + "?" : "Then how about " + name + "?";
      return new DisplayText(game, Characters.T(ask), null, true, false, new DrawYesNoMenu(null, yes, no));
   }

   static String upper(String s) {
      return s.toUpperCase(Locale.ROOT);
   }
}
