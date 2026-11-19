package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The POKéMON LEAGUE, run by the LEAGUE DIRECTOR (arrives once you've founded a town).
 * Each founded town has gym slots (village 1, town 2, city 3). The director appoints core
 * characters you've beaten and who trust you (friendship 5+) as Gym Leaders. Beat a Gym Leader
 * (their team is brought up to your catch cap) to earn their badge; every badge raises the
 * catch cap by CAP_PER_BADGE. Eight badges opens the Elite Four (next milestone).
 */
class League {
   static final int CAP_PER_BADGE = 5;
   static final int BADGES_FOR_E4 = 8;

   static final HashMap<String, String> BADGE = new HashMap<>();
   static final HashMap<String, String> SPECIALTY = new HashMap<>();
   static {
      BADGE.put("emma", "DUSK BADGE");       SPECIALTY.put("emma", "She'll battle anyone, and she hates losing.");
      BADGE.put("wood", "GUARD BADGE");      SPECIALTY.put("wood", "He'd make a tough wall for challengers.");
      BADGE.put("flambar", "HEARTH BADGE");  SPECIALTY.put("flambar", "Kind, but her FIRE types are no joke.");
      BADGE.put("willow", "SPARK BADGE");    SPECIALTY.put("willow", "Chaotic, brilliant, ELECTRIC.");
      BADGE.put("locke", "ROOT BADGE");      SPECIALTY.put("locke", "He knows this island's POKéMON better than anyone.");
      BADGE.put("lavender", "WISP BADGE");   SPECIALTY.put("lavender", "Her GHOST types give challengers nightmares.");
      BADGE.put("bruno", "FANG BADGE");      SPECIALTY.put("bruno", "Brutal. Exactly what a gym needs.");
      BADGE.put("blue", "PIONEER BADGE");    SPECIALTY.put("blue", "He built half this place. Seems fair.");
   }

   static boolean isLeader(Npcs.Npc npc) {
      return "gym".equals(npc.data.role);
   }

   static boolean isE4(Npcs.Npc npc) {
      return "e4".equals(npc.data.role);
   }

   static boolean isChampion(Npcs.Npc npc) {
      return "champion".equals(npc.data.role);
   }

   static final int GYM_TOWNS_FOR_REGIONAL = 8;
   static final String[][] HALL_COST = {{"log", "40"}, {"iron ore", "20"}, {"gold ore", "6"}};

   static Cities.CityData hallCity(Game game) {
      for (Cities.CityData c : Cities.list(game)) {
         if (c.leagueHall) {
            return c;
         }
      }
      return null;
   }

   static int gymTowns(Game game) {
      int n = 0;
      for (Cities.CityData c : Cities.list(game)) {
         if (!Tournaments.leadersOf(game, c).isEmpty()) {
            n++;
         }
      }
      return n;
   }

   static Npcs.Npc champion(Game game) {
      for (Npcs.Npc n : game.map.npcs) {
         if (isChampion(n)) {
            return n;
         }
      }
      return null;
   }

   static ArrayList<Npcs.Npc> eliteFour(Game game) {
      ArrayList<Npcs.Npc> out = new ArrayList<>();
      for (Npcs.Npc n : game.map.npcs) {
         if (isE4(n)) {
            out.add(n);
         }
      }
      out.sort((a, b) -> a.data.gymSlot - b.data.gymSlot);
      return out;
   }

   /** Title shown in battles: "LEADER WOOD", "ELITE FOUR BRUNO", "CHAMPION DAD". */
   static String titled(Npcs.Npc npc) {
      if (isLeader(npc)) {
         return "LEADER " + npc.fullName();
      }
      if (isE4(npc)) {
         return "ELITE FOUR " + npc.fullName();
      }
      if (isChampion(npc)) {
         return "CHAMPION " + npc.fullName();
      }
      return npc.fullName();
   }

   static boolean hasHallMaterials(Game game) {
      for (String[] c : HALL_COST) {
         if (!Characters.has(game.player, c[0], Integer.valueOf(c[1]))) {
            return false;
         }
      }
      return true;
   }

   static String hallCostText() {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < HALL_COST.length; i++) {
         sb.append(i == 0 ? "" : i == HALL_COST.length - 1 ? " and " : ", ").append(HALL_COST[i][1]).append(" ").append(HALL_COST[i][0].toUpperCase(java.util.Locale.ROOT));
      }
      return sb.toString();
   }

   /** Two statues and a sign at the city plaza. */
   static void buildHall(Game game, Cities.CityData c) {
      for (String[] cost : HALL_COST) {
         Characters.take(game.player, cost[0], Integer.valueOf(cost[1]));
      }
      Vector2 center = new Vector2(c.x - c.x % 16, c.y - c.y % 16);
      Vector2 spot = null;
      for (int r = 2; r <= 6 && spot == null; r++) {
         for (int dx = -r; dx <= r && spot == null; dx++) {
            Vector2 p = center.cpy().add(dx * 16, -r * 16);
            Vector2 l = p.cpy().add(-16, 0);
            Vector2 rr = p.cpy().add(16, 0);
            if (Routines.standable(game, p) && Routines.standable(game, l) && Routines.standable(game, rr)
               && Npcs.at(game, p) == null && !p.epsilonEquals(game.player.position, 1)) {
               spot = p;
            }
         }
      }
      if (spot == null) {
         spot = center.cpy().add(0, -32);
      }
      Tile base = game.map.overworldTiles.get(spot);
      Route route = base != null ? base.routeBelongsTo : null;
      for (int side : new int[]{-16, 16}) {
         Vector2 p = spot.cpy().add(side, 0);
         Tile old = game.map.overworldTiles.get(p);
         game.map.overworldTiles.put(p, new Tile(old != null ? old.name : "green1", "statue1", p, true, route));
      }
      game.map.overworldTiles.put(spot.cpy(), new TrainerTipsTile(spot.cpy(), route, false,
         Characters.T("POKEMON LEAGUE HALL - " + Cities.title(c) + ". The REGIONAL TOURNAMENT is held here on the last day of WINTER.")));
      game.map.refreshCache = true;
      c.leagueHall = true;
      c.hallX = spot.x;
      c.hallY = spot.y - 16;
      Alerts.push(game, "The POKEMON LEAGUE HALL now stands in " + Cities.title(c) + "!");
   }


   /** Badges belong to the gym (town + slot), not to whoever leads it. */
   static String badgeOf(Npcs.Npc npc) {
      if (npc.data.gymCity == null) {
         return null;
      }
      return npc.data.gymCity + (npc.data.gymSlot > 0 ? " " + (npc.data.gymSlot + 1) : "") + " BADGE";
   }

   static boolean hasBadge(Game game, Npcs.Npc npc) {
      return game.player.generation < 2 || game.player.badges.contains(badgeOf(npc));
   }

   static int leadersIn(Game game, Cities.CityData c) {
      int n = 0;
      for (Npcs.Npc o : game.map.npcs) {
         if (isLeader(o) && c.name.equals(o.data.gymCity)) {
            n++;
         }
      }
      return n;
   }

   static Cities.CityData cityWithRoom(Game game) {
      for (Cities.CityData c : Cities.list(game)) {
         if (leadersIn(game, c) < Cities.gymSlots(c)) {
            return c;
         }
      }
      return null;
   }

   static ArrayList<Npcs.Npc> candidates(Game game) {
      ArrayList<Npcs.Npc> out = new ArrayList<>();
      for (Npcs.Npc o : game.map.npcs) {
         if (Characters.isCore(o.data.type) && BADGE.containsKey(o.data.type) && o.data.role == null
            && TrainerBattle.canBattle(o) && o.data.wins >= 1 && o.data.friendship >= 5) {
            out.add(o);
         }
      }
      return out;
   }

   static boolean hasNews(Game game, Npcs.Npc director) {
      return director.data.questStage == 0;
   }

   static Action directorTalk(Game game, Npcs.Npc npc, Action end) {
      String who = npc.fullName() + " - ";
      if (npc.data.questStage == 0) {
         npc.data.questStage = 1;
         return say(game, who + "So you're the one building all this. I run POKéMON LEAGUES.",
            say(game, who + "Every town you found can host a GYM. I'll appoint the leaders, you earn their BADGES.",
            say(game, who + "Each BADGE lets you catch stronger POKéMON. Collect eight and we'll talk about an ELITE FOUR.",
            directorTalk(game, npc, end))));
      }
      String upcoming = Tournaments.status(game);
      int gymTowns = gymTowns(game);
      Cities.CityData hall = hallCity(game);
      // Champion in phase 1 (or later): offer to pass the torch.
      if (game.player.championYear >= 0) {
         Action yes = new DisplayText.Clear(game, new RunCode(() -> Torch.pass(game), null));
         return new DisplayText(game, Characters.T(who + "CHAMPION! Whenever you're ready, you can pass the torch to the next generation. Now?"),
            null, true, false, new DrawYesNoMenu(null, yes, new DisplayText.Clear(game,
               say(game, who + "Take your time. Defend that title.", end))));
      }
      // No hall yet: build one in a CITY.
      if (hall == null) {
         Cities.CityData city = null;
         for (Cities.CityData c : Cities.list(game)) {
            if (c.tier >= 2) {
               city = c;
            }
         }
         if (city != null && gymTowns >= 1) {
            final Cities.CityData target = city;
            if (!hasHallMaterials(game)) {
               return say(game, who + Cities.title(city) + " is big enough for a LEAGUE HALL. Bring me " + hallCostText() + " and I'll build it.", end);
            }
            Action yes = new DisplayText.Clear(game, new RunCode(() -> {
               buildHall(game, target);
               game.insertAction(say(game, who + "Done! The REGIONAL TOURNAMENT will be held here every year, on the last day of WINTER.", end));
            }, null));
            return new DisplayText(game, Characters.T(who + "Build the LEAGUE HALL in " + Cities.title(city) + "? It takes " + hallCostText() + "."),
               null, true, false, new DrawYesNoMenu(null, yes, new DisplayText.Clear(game, end)));
         }
      }
      String status;
      if (Cities.list(game).isEmpty()) {
         status = "Found a town first. The CITY MANAGER can help.";
      } else if (!upcoming.isEmpty()) {
         status = upcoming + " Sign up in town that morning if you want in.";
      } else if (gymTowns < GYM_TOWNS_FOR_REGIONAL) {
         status = gymTowns + " of 8 GYM towns so far. Every town gets a GYM, and tournaments decide who leads them."
            + (hall == null ? " A LEAGUE HALL needs a CITY of 10 houses." : "");
      } else if (hall == null) {
         status = "Eight GYM towns! Grow one into a CITY (10 houses) so I can build the LEAGUE HALL.";
      } else {
         int left = Calendar.YEAR_DAYS - 1 - Calendar.dayOfYear(game);
         status = "The REGIONAL TOURNAMENT is at the LEAGUE HALL in " + Cities.title(hall) + " in " + left + " days. Win it and you're CHAMPION.";
      }
      return say(game, who + status, end);
   }

   static Action appointPrompt(Game game, Cities.CityData city, ArrayList<Npcs.Npc> cands, int i, Action end) {
      if (i >= cands.size()) {
         return say(game, Characters.T("Take your time deciding."), end);
      }
      Npcs.Npc c = cands.get(i);
      String name = c.fullName();
      Action yes = new DisplayText.Clear(game, new RunCode(() -> {
         c.data.role = "gym";
         c.data.gymCity = city.name;
         game.insertAction(new DisplayText(game, Characters.T(name + " is now a GYM LEADER of " + Cities.title(city)
            + "! Beat them to earn the " + badgeOf(c) + "."), "fanfare1.ogg", null, end));
      }, null));
      Action no = new DisplayText.Clear(game, appointPrompt(game, city, cands, i + 1, end));
      return new DisplayText(game, Characters.T("What about " + name + "? " + SPECIALTY.get(c.data.type)), null, true, false,
         new DrawYesNoMenu(null, yes, no));
   }

   static Action say(Game game, String text, Action next) {
      return new DisplayText(game, Characters.T(text), null, null, next);
   }

   /** Leader's challenge prompt, or null if they've no gym battle for you. */
   static Action gymPrompt(Game game, Npcs.Npc npc, Action end) {
      if (!isLeader(npc) && !isE4(npc) && !isChampion(npc)) {
         return null;
      }
      // Gyms: once a day in phase 1 (for glory), until you have the badge in phase 2.
      // Elite Four and Champion: once a day, always.
      boolean phase1 = game.player.generation < 2;
      if (isLeader(npc) ? (phase1 ? npc.data.lastBattleDay == game.player.daysPassed : hasBadge(game, npc))
         : npc.data.lastBattleDay == game.player.daysPassed) {
         return null;
      }
      boolean canFight = false;
      for (Pokemon p : game.player.pokemon) {
         canFight |= !p.isEgg && p.currentStats.get("hp") > 0;
      }
      if (!canFight) {
         return null;
      }
      Action yes = new DisplayText.Clear(game, new RunCode(() -> TrainerBattle.start(game, npc, true), null));
      String line = isLeader(npc) ? gymLine(npc.data.type)
         : isChampion(npc) ? "So you want to face the CHAMPION. I won't go easy. Ready?"
         : "An ELITE FOUR battle? You'd better be ready.";
      return new DisplayText(game, Characters.T(titled(npc) + " - " + line), null, true, false,
         new DrawYesNoMenu(null, yes, new DisplayText.Clear(game, end)));
   }

   static String gymLine(String type) {
      switch (type) {
         case "emma": return "Gym battle? Finally, a real reason to beat you. Ready?";
         case "wood": return "As a GYM LEADER I won't hold back. You ready?";
         case "flambar": return "This is my GYM now, so I have to be serious. Ready?";
         case "willow": return "Official GYM battle! I've been planning this for days. Ready?";
         case "locke": return "Earn the ROOT BADGE. Ready?";
         case "lavender": return "Want my BADGE? You'll have to get past my friends first. Ready?";
         case "bruno": return "My GYM. My rules. Ready to get hurt?";
         default: return "Let's see if you deserve a BADGE. Ready?";
      }
   }

   static Action awardBadge(Game game, Npcs.Npc npc, Action next) {
      // Phase 1 (the survivor): gym battles are for glory. Badges are the kid's journey.
      if (game.player.generation < 2) {
         return new DisplayText(game, Characters.T("You beat GYM LEADER " + npc.fullName() + "! Word will get around."), null, null, next);
      }
      String badge = badgeOf(npc);
      if (badge == null || game.player.badges.contains(badge)) {
         return next;
      }
      game.player.badges.add(badge);
      return new DisplayText(game, Characters.T("You received the " + badge + "!"), "fanfare1.ogg", null,
         new DisplayText(game, Characters.T("You can now catch POKéMON up to LV" + game.player.catchLevelCap() + "."), null, null, next));
   }
}
