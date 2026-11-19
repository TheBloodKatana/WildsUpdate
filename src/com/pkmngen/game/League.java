package com.pkmngen.game;

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
      int gymTowns = 0;
      for (Cities.CityData c : Cities.list(game)) {
         if (!Tournaments.leadersOf(game, c).isEmpty()) {
            gymTowns++;
         }
      }
      String status;
      if (Cities.list(game).isEmpty()) {
         status = "Found a town first. The CITY MANAGER can help.";
      } else if (!upcoming.isEmpty()) {
         status = upcoming + " Sign up in town that morning if you want in.";
      } else if (gymTowns < 8) {
         status = gymTowns + " of 8 GYM towns so far. Every town gets a GYM, and tournaments decide who leads them.";
      } else {
         status = "Eight GYM towns! Once a LEAGUE HALL stands in a CITY, the REGIONAL TOURNAMENT can begin.";
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
      if (!isLeader(npc)) {
         return null;
      }
      // Phase 1: a gym battle once a day, for glory. Phase 2: until you have the badge.
      if (game.player.generation < 2 ? npc.data.lastBattleDay == game.player.daysPassed : hasBadge(game, npc)) {
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
      return new DisplayText(game, Characters.T(npc.fullName() + " - " + gymLine(npc.data.type)), null, true, false,
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
