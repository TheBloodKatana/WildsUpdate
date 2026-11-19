package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * The core characters (designed by the project owner): Emma, Flambar, Wood, Willow, Bruno,
 * Lavender, Locke, Blue (Red is a boss/event character, not a villager).
 *
 * They are villagers (see Npcs) with extra layers:
 * - a 64x64 portrait shown above the text box while they talk,
 * - a team that grows over time: every morning they may catch a Pokemon (from their type
 *   affinity, or anything for Emma/Wood) and their team levels up alongside your catch cap,
 * - gifts (A -> "Give a gift?"), friendship 0-10 and, for Emma/Wood/Lavender, romance 0-10,
 * - one personal quest each, offered once friendship reaches 3,
 * - romance: at romance 10 + their quest done they confess; saying yes makes them your partner.
 *
 * Battles (rival fights, gyms, Elite Four) hook in once trainer battles exist. See
 * docs/CHARACTERS.md for the full design.
 */
class Characters {
   static class Def {
      String id;
      String name;
      boolean romanceable;
      String[] affinity;        // types; empty = can catch anything
      String extraType;         // starting second Pokemon type (Emma DARK, Wood FIGHTING)
      boolean starter;
      String[] likes;           // loved gifts
      String[] greet;           // friendship 0-4
      String[] friendly;        // friendship 5+
      String[] crush;           // romance 5+ (romanceable only)
      String[] partner;         // once partners
      String questIntro;
      String questDone;
      String confession;
      String battleAsk;
      String winLine;   // shown when YOU win
      String loseLine;  // said next time you talk, after they beat you

      Def(String id, String name, boolean romanceable) {
         this.id = id;
         this.name = name;
         this.romanceable = romanceable;
      }
   }

   static final LinkedHashMap<String, Def> DEFS = new LinkedHashMap<>();

   static Def def(String id, String name, boolean rom) {
      Def d = new Def(id, name, rom);
      DEFS.put(id, d);
      return d;
   }

   static {
      Def d = def("emma", "EMMA", true);
      d.battleAsk = "You again? Fine. Let me crush you.";
      d.winLine = "Tch! You got lucky. Next time, you're mine.";
      d.loseLine = "Ha! Told you. Stick to catching bugs.";
      d.starter = true;
      d.extraType = "DARK";
      d.affinity = new String[0];
      d.likes = new String[]{"dusk ball", "black glasses", "rare candy", "nugget"};
      d.greet = new String[]{"Oh, it's you. Still catching weaklings?", "Bet my team's already stronger than yours.",
         "Don't get comfortable on MY island."};
      d.friendly = new String[]{"You're not as bad as I thought. Don't let it go to your head.",
         "Heh. You actually kept up today.", "Next time we battle, I'm not holding back."};
      d.crush = new String[]{"...Why are you staring? Ugh, forget it.", "I didn't train all day just to impress you. Obviously.",
         "You're the only one here worth beating, you know."};
      d.partner = new String[]{"Partner or not, I'm still beating you someday.", "I saved you the best fishing spot. Don't make it weird."};
      d.questIntro = "Prove you're not all talk. Catch a DARK type POKéMON and show me.";
      d.questDone = "Huh. Not bad. Fine, you've earned my respect. A little.";
      d.confession = "I... hate losing to you. But I'd hate it more if you weren't around. Stay with me?";

      d = def("flambar", "FLAMBAR", false);
      d.battleAsk = "Let's have a good battle! No holding back!";
      d.winLine = "You were amazing! I'm so proud of you.";
      d.loseLine = "Don't be sad! You fought really well.";
      d.affinity = new String[]{"FIRE"};
      d.likes = new String[]{"charcoal", "fire stone", "berry juice", "soft bedding"};
      d.greet = new String[]{"Hi! Did you eat today? You should eat!", "My POKéMON keep me warm at night.",
         "Be careful out there, okay?"};
      d.friendly = new String[]{"I made extra berry juice. For you!", "You've gotten so strong. I'm proud of you!",
         "In a battle I don't go easy. Out of one, I'm all yours to talk to."};
      d.questIntro = "Could you bring me 10 CHARCOAL? I want to build a hearth so nobody's ever cold here.";
      d.questDone = "A real hearth! Thank you. This island feels like home now.";

      d = def("wood", "WOOD", true);
      d.battleAsk = "Spar with me. It'll toughen you up.";
      d.winLine = "...Good. You're stronger than I thought.";
      d.loseLine = "Get up. Train harder. I'll be here.";
      d.starter = true;
      d.extraType = "FIGHTING";
      d.affinity = new String[0];
      d.likes = new String[]{"black belt", "protein", "rare candy", "hard stone"};
      d.greet = new String[]{"Stay behind me if something big shows up.", "Train hard. The wilds don't forgive.",
         "Hmph. You again."};
      d.friendly = new String[]{"Anyone who messes with this island deals with me.", "You fight well. I respect that.",
         "If a BOSS gives you trouble, call me."};
      d.crush = new String[]{"...I worry about you when you go off alone.", "You don't have to be tough all the time. I've got you.",
         "I keep a spot by the fire for you."};
      d.partner = new String[]{"Nobody's touching you while I'm around.", "Rest. I'll keep watch tonight."};
      d.questIntro = "There's a mighty POKéMON out there. Beat a BOSS and I'll know you can handle yourself.";
      d.questDone = "You did it. ...I was worried. Don't tell anyone I said that.";
      d.confession = "I've fought a lot of things. Nothing scares me like losing you. Be with me?";

      d = def("willow", "WILLOW", false);
      d.battleAsk = "Battle test! For science!";
      d.winLine = "Fascinating! My hypothesis was wrong!";
      d.loseLine = "Data collected! Sorry, was that rude?";
      d.affinity = new String[]{"ELECTRIC"};
      d.likes = new String[]{"magnet", "metal coat", "thunderstone", "thin paper"};
      d.greet = new String[]{"Did you know MAGNEMITE can-- wait, where was I?", "I have a new theory! It's probably wrong!",
         "Ooh! What's that in your bag?"};
      d.friendly = new String[]{"You're my favorite lab partner. You're my only lab partner. Still counts!",
         "I rewired my PC to talk to PIKACHU. It mostly says PIKA.", "Electricity is just tiny lightning. Isn't that great?"};
      d.questIntro = "Help me build a generator! I need 4 MAGNET and 2 METAL COAT.";
      d.questDone = "IT WORKS! Okay it's mostly sparks. But they're GOOD sparks!";

      d = def("bruno", "BRUNO", false);
      d.battleAsk = "Think you can take me? Prove it.";
      d.winLine = "...Hmph. You've got teeth after all.";
      d.loseLine = "Pathetic. Come back when you're worth my time.";
      d.affinity = new String[]{"DARK"};
      d.likes = new String[]{"black glasses", "dusk ball", "nugget"};
      d.greet = new String[]{"Keep walking, kid. This is my turf.", "You look like easy prey.",
         "One wrong move and my team eats you alive."};
      d.friendly = new String[]{"...You've got guts. I'll give you that.", "Hmph. Maybe you're not prey after all.",
         "If anyone else threatens you, they answer to me."};
      d.questIntro = "Think you're tough? Beat 2 BOSSES. Then we'll talk.";
      d.questDone = "Two of them. Fine. You've earned your place here.";

      d = def("lavender", "LAVENDER", true);
      d.battleAsk = "Let's play! The ghosts want to watch.";
      d.winLine = "Hehe, you're so strong... the spirits are cheering!";
      d.loseLine = "Don't worry. They only haunt the losers a little.";
      d.affinity = new String[]{"GHOST"};
      d.likes = new String[]{"spell tag", "dusk ball", "thin paper", "moon stone"};
      d.greet = new String[]{"Hehe... the spirits like you.", "Did you hear that? ...No? Good.",
         "I talk to ghosts. They talk back. Isn't that nice?"};
      d.friendly = new String[]{"I told my GASTLY all about you. It giggled.", "The graveyard is lovely at midnight. Come with me sometime?",
         "You're not scared of me. That's... rare."};
      d.crush = new String[]{"My heart goes all cold and fluttery around you. Like a ghost!",
         "If I haunted someone forever... I'd pick you. Is that romantic?", "Stay a little longer. Please?"};
      d.partner = new String[]{"The ghosts promised to leave you alone at night. I asked nicely.",
         "Forever and ever and ever. Hehe."};
      d.questIntro = "Some lost spirits are wandering. Catch 3 different GHOST types so they're not lonely?";
      d.questDone = "They're happy now! You're so kind... I'll remember this. Forever.";
      d.confession = "I've been dead set on you since we met. Hehe. Will you be mine?";

      d = def("locke", "LOCKE", false);
      d.battleAsk = "Show me your roots.";
      d.winLine = "Strong roots. Good.";
      d.loseLine = "Grow some more. Then return.";
      d.affinity = new String[]{"GRASS", "GROUND", "ROCK", "BUG"};
      d.likes = new String[]{"miracle seed", "hard stone", "silver powder", "damp soil"};
      d.greet = new String[]{"The earth remembers everything.", "Watch your step. Bugs live there.",
         "A good island needs strong roots."};
      d.friendly = new String[]{"You look after the land. I respect that.", "Plant something today. It helps.",
         "The rocks here are older than all of us."};
      d.questIntro = "Catch 4 different BUG or GRASS types. I want to know you understand this island.";
      d.questDone = "Good. You see it now. The island is alive.";

      d = def("blue", "BLUE", false);
      d.battleAsk = "Let's see what you've got, rookie!";
      d.winLine = "What?! ...Okay, that was a good one.";
      d.loseLine = "Smell ya later! Better luck next time.";
      d.affinity = new String[0];
      d.likes = new String[]{"rare candy", "nugget", "ultra ball"};
      d.greet = new String[]{"Smell ya later! ...Kidding. I'm sticking around.", "This place needs a POKéMON LEAGUE. And a champion. Me.",
         "Gather strong trainers here and I'll show you how a LEAGUE works."};
      d.friendly = new String[]{"Not bad, rookie. Not bad.", "Build gyms, find leaders. That's how it starts.",
         "Someday you might even beat me. Someday."};
      d.questIntro = "Get 5 people living on this island. Then we can talk about a LEAGUE.";
      d.questDone = "A real village! Okay, I'm impressed. Next up: GYMS. Found a town and a LEAGUE DIRECTOR will show up.";
   }

   /** Converts normal apostrophes to the GSC font's glyphs: 's -> one glyph, n't -> one glyph, other ' -> plain mark. */
   static String T(String s) {
      if (s == null) {
         return null;
      }
      return s.replace("n't", "nì").replace("'s", "\u0001").replace("'", "\u0092").replace("\u0001", "'");
   }

   static String upper(String s) {
      return s == null ? "" : s.toUpperCase(Locale.ROOT);
   }

   /** First letter upper case. */
   static String cap(String s) {
      return s == null || s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
   }

   static boolean isCore(String id) {
      return DEFS.containsKey(id);
   }

   /** Arrival conditions (in addition to a free bed). */
   static boolean unlocked(Game game, String id) {
      Player p = game.player;
      switch (id) {
         case "emma": return p.daysPassed >= 1;
         case "wood": return p.daysPassed >= 2;
         case "flambar": return countCaughtOfType(p, "FIRE") >= 1;
         case "willow": return hasPc(game);
         case "locke": return p.dexSeen.size() >= 15;
         case "lavender": return countCaughtOfType(p, "GHOST") >= 1;
         case "bruno": return p.bossesBeaten >= 1;
         case "blue": return game.map.npcs != null && game.map.npcs.size() >= 4;
         default: return false;
      }
   }

   static boolean hasPc(Game game) {
      for (java.util.HashMap<com.badlogic.gdx.math.Vector2, Tile> layer : game.map.interiorTiles) {
         if (layer == null) {
            continue;
         }
         for (Tile t : layer.values()) {
            if (t.nameUpper.equals("pc1")) {
               return true;
            }
         }
      }
      return false;
   }

   // ------------------------------------------------------------------ type data

   static HashMap<String, List<String>> byType;
   static List<String> starters;
   static HashMap<String, List<String>> evolutions;
   static java.util.HashSet<String> baseForms;

   static void loadTypeData() {
      if (byType != null) {
         return;
      }
      byType = new HashMap<>();
      starters = new ArrayList<>();
      evolutions = new HashMap<>();
      baseForms = new java.util.HashSet<>();
      try {
         for (String line : Game.readLines(Gdx.files.internal("npc/evolutions.txt"))) {
            if (line.startsWith("#") || !line.contains("|")) {
               continue;
            }
            String[] parts = line.trim().split("\\|");
            evolutions.put(parts[0], Arrays.asList(parts[1].split(",")));
         }
         for (String line : Game.readLines(Gdx.files.internal("npc/base_forms.txt"))) {
            if (!line.trim().isEmpty()) {
               baseForms.addAll(Arrays.asList(line.trim().split(",")));
            }
         }
         for (String line : Game.readLines(Gdx.files.internal("npc/types.txt"))) {
            if (line.startsWith("#") || !line.contains("|")) {
               continue;
            }
            String[] parts = line.trim().split("\\|");
            byType.put(parts[0], Arrays.asList(parts[1].split(",")));
         }
         for (String line : Game.readLines(Gdx.files.internal("npc/starters.txt"))) {
            if (!line.trim().isEmpty()) {
               starters.addAll(Arrays.asList(line.trim().split(",")));
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   static int countCaughtOfType(Player p, String type) {
      loadTypeData();
      int n = 0;
      List<String> list = byType.get(type);
      if (list == null) {
         return 0;
      }
      for (String s : list) {
         if (p.dexCaught.contains(s)) {
            n++;
         }
      }
      return n;
   }

   static HashMap<String, Boolean> playableCache = new HashMap<>();

   /** Some species have data files but no complete sprites/stats; building one throws. */
   static boolean playable(String species) {
      return playableCache.computeIfAbsent(species, sp -> {
         try {
            new Pokemon(sp, 5, Pokemon.Generation.CRYSTAL);
            return true;
         } catch (Throwable e) {
            System.out.println("[npc] skipping unsupported species " + sp);
            return false;
         }
      });
   }

   static String randomOfType(String type) {
      loadTypeData();
      List<String> list = byType.get(type);
      if (list == null || list.isEmpty()) {
         return "rattata";
      }
      // Only first-stage Pokemon; they evolve as they level up.
      ArrayList<String> base = new ArrayList<>();
      for (String s : list) {
         if (baseForms.contains(s)) {
            base.add(s);
         }
      }
      if (base.isEmpty()) {
         base.addAll(list);
      }
      java.util.Collections.shuffle(base, Game.rand);
      for (String s : base) {
         if (playable(s)) {
            return s;
         }
      }
      return "rattata";
   }

   static String randomFor(Def def) {
      loadTypeData();
      String type;
      if (def.affinity.length == 0) {
         ArrayList<String> types = new ArrayList<>(byType.keySet());
         type = types.get(Game.rand.nextInt(types.size()));
      } else {
         type = def.affinity[Game.rand.nextInt(def.affinity.length)];
      }
      return randomOfType(type);
   }

   // ------------------------------------------------------------------ daily team growth

   static int teamLevel(Game game) {
      return Math.max(5, game.player.catchLevelCap() - 3);
   }

   static void daily(Game game, Npcs.Npc npc, boolean newDay) {
      Def def = DEFS.get(npc.data.type);
      if (def == null) {
         return;
      }
      Npcs.NpcData d = npc.data;
      if (d.team == null) {
         d.team = new ArrayList<>();
      }
      int target = teamLevel(game);
      // Swap out anything this build can't actually field (older saves, incomplete species).
      for (int i = 0; i < d.team.size(); i++) {
         String[] m = d.team.get(i).split(":");
         if (!playable(m[0])) {
            String type = def.extraType != null && i == 1 ? def.extraType : null;
            d.team.set(i, (type != null ? randomOfType(type) : randomFor(def)) + ":" + m[1]);
         }
      }
      if (d.team.isEmpty()) {
         loadTypeData();
         if (def.starter) {
            String starter = starters.get(Game.rand.nextInt(starters.size()));
            d.team.add((playable(starter) ? starter : "chikorita") + ":" + Math.max(5, target - 2));
            d.team.add(randomOfType(def.extraType) + ":" + Math.max(5, target - 3));
         } else {
            d.team.add(randomFor(def) + ":" + Math.max(5, target - 2));
            d.team.add(randomFor(def) + ":" + Math.max(5, target - 3));
         }
         return;
      }
      if (!newDay) {
         return;
      }
      // Level everyone toward the island's pace.
      for (int i = 0; i < d.team.size(); i++) {
         String[] m = d.team.get(i).split(":");
         int lv = Integer.valueOf(m[1]);
         if (lv < target) {
            lv = Math.min(target, lv + 1 + Game.rand.nextInt(3));
         }
         String species = evolve(m[0], lv);
         if (!species.equals(m[0])) {
            d.lastEvolved = m[0] + ">" + species;
         }
         d.team.set(i, species + ":" + lv);
      }
      // Maybe catch something new.
      if (Game.rand.nextInt(100) < 35) {
         String caught = randomFor(def) + ":" + Math.max(5, target - 4);
         if (d.team.size() < 6) {
            d.team.add(caught);
         } else {
            int weakest = 0;
            for (int i = 1; i < d.team.size(); i++) {
               if (Integer.valueOf(d.team.get(i).split(":")[1]) < Integer.valueOf(d.team.get(weakest).split(":")[1])) {
                  weakest = i;
               }
            }
            if (weakest != 0) {
               d.team.set(weakest, caught);
            }
         }
         d.lastCatch = caught.split(":")[0];
      }
   }

   /** Applies every evolution this level qualifies for (random branch, e.g. Eevee). */
   static String evolve(String species, int level) {
      loadTypeData();
      for (int guard = 0; guard < 3; guard++) {
         List<String> evos = evolutions.get(species);
         if (evos == null) {
            break;
         }
         ArrayList<String> ready = new ArrayList<>();
         for (String e : evos) {
            String[] p = e.split(":");
            if (level >= Integer.valueOf(p[1]) && playable(p[0])) {
               ready.add(p[0]);
            }
         }
         if (ready.isEmpty()) {
            break;
         }
         species = ready.get(Game.rand.nextInt(ready.size()));
      }
      return species;
   }

   static final HashMap<String, String[]> TEAM_CHAT = new HashMap<>();
   static {
      TEAM_CHAT.put("emma", new String[]{"%s is itching for a rematch. Just saying.", "Bet my %s could take your whole team.", "%s and I trained all morning. You?"});
      TEAM_CHAT.put("wood", new String[]{"%s keeps watch with me. Good partner.", "%s and I train every day. You should too.", "Nobody messes with you while %s and I are around."});
      TEAM_CHAT.put("flambar", new String[]{"%s loves sitting by the campfire. Me too!", "I made %s a little scarf. Don't laugh!", "%s gets so serious before a battle. Like me, I guess."});
      TEAM_CHAT.put("willow", new String[]{"Did you know %s can... wait, I forgot. It was cool though!", "I'm charting %s's sparks. For science!", "%s zapped my notes again. Totally worth it."});
      TEAM_CHAT.put("locke", new String[]{"%s found something under a rock today. Good eye.", "The soil here suits %s.", "%s doesn't talk much either. We get along."});
      TEAM_CHAT.put("lavender", new String[]{"%s keeps hiding in my shadow. Isn't that cute?", "%s whispered something to me last night. Hee hee.", "Want to hold %s? It only bites a little."});
      TEAM_CHAT.put("bruno", new String[]{"%s hasn't lost in a while. Want to change that?", "Keep your distance. %s bites.", "%s and I don't do friendly."});
      TEAM_CHAT.put("blue", new String[]{"%s's been helping me scout the island.", "Even %s thinks this place needs more houses.", "%s is the reason I'm still in one piece."});
   }

   /** A natural remark about their strongest Pokemon, or null. */
   static String teamChat(Npcs.Npc npc) {
      if (npc.data.team == null || npc.data.team.isEmpty()) {
         return null;
      }
      String best = null;
      int bestLv = -1;
      for (String m : npc.data.team) {
         String[] p = m.split(":");
         int lv = Integer.valueOf(p[1]);
         if (lv > bestLv) {
            bestLv = lv;
            best = p[0];
         }
      }
      String[] lines = TEAM_CHAT.getOrDefault(npc.data.type, new String[]{"%s and I are doing great!"});
      return String.format(pick(lines), best.toUpperCase(Locale.ROOT).replace("_", " "));
   }

   static String teamText(Npcs.Npc npc) {
      StringBuilder sb = new StringBuilder();
      for (String m : npc.data.team) {
         String[] p = m.split(":");
         if (sb.length() > 0) {
            sb.append(", ");
         }
         sb.append(p[0].toUpperCase(Locale.ROOT).replace("_", " ")).append(" LV").append(p[1]);
      }
      return sb.toString();
   }

   // ------------------------------------------------------------------ talking

   static String pick(String[] lines) {
      return lines[Game.rand.nextInt(lines.length)];
   }

   static Action talk(Game game, Npcs.Npc npc, Action end) {
      Def def = DEFS.get(npc.data.type);
      Npcs.NpcData d = npc.data;
      Player player = game.player;
      daily(game, npc, false);
      String name = def.name + " - ";

      // Line by relationship.
      String line;
      if ("partner".equals(d.relation) && def.partner != null) {
         line = pick(def.partner);
      } else if (def.romanceable && d.romance >= 5 && def.crush != null) {
         line = pick(def.crush);
      } else if (d.friendship >= 5) {
         line = pick(def.friendly);
      } else {
         line = pick(def.greet);
      }
      // Now and then they mention their partner Pokemon or what they're up to instead.
      int roll = Game.rand.nextInt(10);
      if (roll < 3) {
         String chat = teamChat(npc);
         if (chat != null) {
            line = chat;
         }
      } else if (roll < 6) {
         String r = Routines.line(game, npc);
         if (r != null) {
            line = r;
         }
      }
      if (d.lastEvolved != null) {
         String[] ev = d.lastEvolved.toUpperCase(Locale.ROOT).replace("_", " ").split(">");
         line = "My " + ev[0] + " evolved into " + ev[1] + "! " + ("emma".equals(d.type) ? "Try to keep up." : "Amazing, right?");
         d.lastEvolved = null;
      } else if (d.lastCatch != null && Game.rand.nextInt(3) == 0) {
         line = "I caught a " + d.lastCatch.toUpperCase(Locale.ROOT).replace("_", " ") + " recently. "
            + ("emma".equals(d.type) ? "Jealous?" : "Isn't it great?");
         d.lastCatch = null;
      }

      // Romance grows a little with daily talks once you're good friends.
      boolean firstToday = d.lastRomanceDay != player.daysPassed;
      if (def.romanceable && firstToday && d.friendship >= 5) {
         d.lastRomanceDay = player.daysPassed;
         d.romance = Math.min(10, d.romance + 1);
      }

      if (d.pendingLine != null) {
         line = d.pendingLine;
         d.pendingLine = null;
      }
      Action next = battlePrompt(game, npc, end);
      next = giftPrompt(game, npc, next);
      next = questStep(game, npc, next);
      next = confessionStep(game, npc, next);
      Action first = new DisplayText(game, T(name + line), null, null, next);
      game.insertAction(new Portrait(def.id));
      return first;
   }

   /** Once a day: "Battle me?" (needs at least one usable POKéMON). */
   static Action battlePrompt(Game game, Npcs.Npc npc, Action end) {
      Def def = DEFS.get(npc.data.type);
      boolean canFight = false;
      for (Pokemon p : game.player.pokemon) {
         canFight |= !p.isEgg && p.currentStats.get("hp") > 0;
      }
      Action gym = canFight ? League.gymPrompt(game, npc, end) : null;
      if (gym != null) {
         return gym;
      }
      if (!canFight || !TrainerBattle.canBattle(npc) || npc.data.lastBattleDay == game.player.daysPassed || def.battleAsk == null) {
         return end;
      }
      Action yes = new DisplayText.Clear(game, new RunCode(() -> TrainerBattle.start(game, npc), null));
      return new DisplayText(game, T(def.name + " - " + def.battleAsk), null, true, false,
         new DrawYesNoMenu(null, yes, new DisplayText.Clear(game, end)));
   }

   static Action giftPrompt(Game game, Npcs.Npc npc, Action end) {
      Def def = DEFS.get(npc.data.type);
      if (npc.data.lastGiftTakenDay == game.player.daysPassed) {
         return end;
      }
      Action open = new RunCode(() -> {
         giftTarget = npc;
         game.player.isCrafting = true;
         game.insertAction(new DrawCraftsMenu.Intro(null, 9,
            new DrawCraftsMenu(game, giftList(def), new SetField(game.player, "isCrafting", false, end))));
      }, null);
      return new DisplayText(game, T("Give " + def.name + " a gift?"), null, true, false,
         new DrawYesNoMenu(null, new DisplayText.Clear(game, open), new DisplayText.Clear(game, end)));
   }

   // ------------------------------------------------------------------ gifts

   static HashMap<String, ArrayList<Player.Craft>> giftLists = new HashMap<>();
   static Npcs.Npc giftTarget;

   static ArrayList<Player.Craft> giftList(Def def) {
      return giftLists.computeIfAbsent(def.id, k -> {
         ArrayList<Player.Craft> list = new ArrayList<>();
         for (String item : def.likes) {
            Player.Craft c = new Player.Craft(item, 1);
            c.requirements.add(new Player.Craft(item, 1));
            list.add(c);
         }
         return list;
      });
   }

   static boolean isGiftList(ArrayList<Player.Craft> list) {
      return giftLists.containsValue(list);
   }

   /** Consumes the gift and returns the reaction text. */
   static String receiveGift(Game game, Player.Craft craft, int amount) {
      Player player = game.player;
      for (Player.Craft req : craft.requirements) {
         player.addItem(req.name, -req.amount * amount);
         if (player.getItemAmount(req.name) != null && player.getItemAmount(req.name) <= 0) {
            player.getItemsDict().remove(req.name);
         }
      }
      Npcs.Npc npc = giftTarget;
      if (npc == null) {
         return "...";
      }
      Def def = DEFS.get(npc.data.type);
      npc.data.lastGiftTakenDay = player.daysPassed;
      npc.data.friendship = Math.min(10, npc.data.friendship + 2);
      if (def.romanceable) {
         npc.data.romance = Math.min(10, npc.data.romance + 1);
      }
      switch (def.id) {
         case "emma": return def.name + " - ...For me? I mean. Obviously. Thanks.";
         case "wood": return def.name + " - This is... thank you. I'll put it to good use.";
         case "lavender": return def.name + " - Ooh! I'll keep it forever. Even after forever!";
         case "bruno": return def.name + " - Tch. ...Fine. I'll take it.";
         case "willow": return def.name + " - FOR SCIENCE! Thank you thank you!";
         case "flambar": return def.name + " - You're so sweet! Thank you!";
         default: return def.name + " - Thanks! I love it.";
      }
   }

   // ------------------------------------------------------------------ quests

   /** questStage: 0 = not offered, 1 = active, 2 = done. */
   static Action questStep(Game game, Npcs.Npc npc, Action next) {
      Def def = DEFS.get(npc.data.type);
      Npcs.NpcData d = npc.data;
      if (d.questStage == 2 || d.friendship < 3) {
         return next;
      }
      if (d.questStage == 0) {
         d.questStage = 1;
         d.questBase = game.player.bossesBeaten;
         return new DisplayText(game, T(def.name + " - " + def.questIntro), null, null, next);
      }
      if (questComplete(game, npc)) {
         d.questStage = 2;
         d.friendship = Math.min(10, d.friendship + 3);
         if (def.romanceable) {
            d.romance = Math.min(10, d.romance + 2);
         }
         String reward = questReward(game, def.id);
         return new DisplayText(game, T(def.name + " - " + def.questDone), "fanfare1.ogg", null,
            new DisplayText(game, T(reward), null, null, next));
      }
      // Only bring it up now and then, and not word for word every time.
      if (Game.rand.nextInt(3) != 0) {
         return next;
      }
      String[] nudges = {"Don't forget that favor, okay?", "Still thinking about what I asked you?", "No rush on that favor. Well... some rush."};
      return new DisplayText(game, T(def.name + " - " + pick(nudges)), null, null, next);
   }

   static boolean questComplete(Game game, Npcs.Npc npc) {
      return questComplete(game, npc, false);
   }

   static boolean questComplete(Game game, Npcs.Npc npc, boolean dryRun) {
      Player p = game.player;
      switch (npc.data.type) {
         case "emma": return countCaughtOfType(p, "DARK") >= 1;
         case "wood": return p.bossesBeaten > npc.data.questBase;
         case "bruno": return p.bossesBeaten >= npc.data.questBase + 2;
         case "lavender": return countCaughtOfType(p, "GHOST") >= 3;
         case "locke": return countCaughtOfType(p, "BUG") + countCaughtOfType(p, "GRASS") >= 4;
         case "blue": return game.map.npcs.size() >= 5;
         case "flambar": return dryRun ? has(p, "charcoal", 10) : take(p, "charcoal", 10);
         case "willow": return has(p, "magnet", 4) && has(p, "metal coat", 2) && (dryRun || take(p, "magnet", 4) && take(p, "metal coat", 2));
         default: return false;
      }
   }

   static boolean has(Player p, String item, int n) {
      Integer have = p.getItemAmount(item);
      return have != null && have >= n;
   }

   static boolean take(Player p, String item, int n) {
      if (!has(p, item, n)) {
         return false;
      }
      p.addItem(item, -n);
      if (p.getItemAmount(item) <= 0) {
         p.getItemsDict().remove(item);
      }
      return true;
   }

   static String questReward(Game game, String id) {
      String[][] rewards = {
         {"emma", "dusk ball:5"}, {"wood", "rare candy:2"}, {"bruno", "nugget:2"}, {"lavender", "spell tag:1"},
         {"locke", "miracle seed:1"}, {"blue", "ultra ball:5"}, {"flambar", "fire stone:1"}, {"willow", "thunderstone:1"},
      };
      for (String[] r : rewards) {
         if (r[0].equals(id)) {
            String[] g = r[1].split(":");
            game.player.addItem(g[0], Integer.valueOf(g[1]));
            return "Received " + g[1] + " " + g[0].toUpperCase(Locale.ROOT) + "!";
         }
      }
      return "";
   }

   /** True when talking to this character would give you something new (shows "!"). */
   static boolean hasNews(Game game, Npcs.Npc npc) {
      Def def = DEFS.get(npc.data.type);
      Npcs.NpcData d = npc.data;
      if (d.questStage == 0 && d.friendship >= 3) {
         return true;
      }
      if (d.questStage == 1 && questComplete(game, npc, true)) {
         return true;
      }
      if (def.romanceable && !"partner".equals(d.relation) && d.romance >= 10 && d.friendship >= 10 && d.questStage == 2
         && (d.lastConfessionDay < 0 || game.player.daysPassed - d.lastConfessionDay >= 7)) {
         return true;
      }
      if (League.isLeader(npc) && !League.hasBadge(game, npc)) {
         return true;
      }
      return d.lastEvolved != null;
   }

   // ------------------------------------------------------------------ romance

   static Action confessionStep(Game game, Npcs.Npc npc, Action next) {
      Def def = DEFS.get(npc.data.type);
      Npcs.NpcData d = npc.data;
      if (!def.romanceable || "partner".equals(d.relation) || d.romance < 10 || d.friendship < 10 || d.questStage != 2) {
         return next;
      }
      if (d.lastConfessionDay >= 0 && game.player.daysPassed - d.lastConfessionDay < 7) {
         return next;
      }
      // Only one partner at a time.
      for (Npcs.Npc other : game.map.npcs) {
         if (other != npc && "partner".equals(other.data.relation)) {
            return next;
         }
      }
      d.lastConfessionDay = game.player.daysPassed;
      Action yes = new DisplayText.Clear(game, new RunCode(() -> d.relation = "partner",
         new DisplayText(game, T(def.name + " - Really?! ...Then it's settled. You're stuck with me."), "fanfare1.ogg", null, next)));
      Action no = new DisplayText.Clear(game, new RunCode(() -> d.romance = 7,
         new DisplayText(game, T(def.name + " - Oh. ...Okay. Forget I said anything."), null, null, next)));
      return new DisplayText(game, T(def.name + " - " + def.confession), null, true, false, new DrawYesNoMenu(null, yes, no));
   }

   // ------------------------------------------------------------------ portrait

   /** Draws a 64x64 portrait above the text box until the conversation ends. */
   static class Portrait extends Action {
      public Action.Layer layer = Action.Layer.gui_104;
      Sprite sprite;
      Sprite frame;
      Sprite white;
      int wait = 4;

      Portrait(String id) {
         this.sprite = new Sprite(TextureCache.get(Gdx.files.internal("npc/portraits/" + id + ".png")), 0, 0, 64, 64);
         this.frame = new Sprite(TextureCache.get(Gdx.files.internal("npc/px_dark.png")), 0, 0, 1, 1);
         this.white = new Sprite(TextureCache.get(Gdx.files.internal("npc/px_white.png")), 0, 0, 1, 1);
      }

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         if (this.wait > 0) {
            this.wait--;
         } else if (game.playerCanMove || game.battle.trainer != null) {
            game.actionStack.remove(this);
            return;
         }
         if (game.player.isCrafting) {
            return;
         }
         // 1px dark frame, then the portrait, sitting right on top of the 48px text box
         float x = 4;
         float y = 48;
         game.uiBatch.draw(this.frame, x - 1, y - 1, 66, 66);
         game.uiBatch.draw(this.white, x, y, 64, 64);
         game.uiBatch.draw(this.sprite, x, y);
      }
   }
}
