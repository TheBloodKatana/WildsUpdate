package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

/**
 * Everyone who isn't a core character:
 *  - TRAINER CLASSES ("t_" ids). Each morning a few wander the island; if you walk into their
 *    line of sight they challenge you, and they leave at nightfall. Beating a class once means
 *    one of them may later SETTLE in a free house: settlers live in town, keep a growing team
 *    and offer a rematch once a day.
 *  - JOBS (ball maker, blacksmith, miner, farmer, cook, carpenter, breeder): regular villagers
 *    (need a free bed) with trades or a service.
 *  - TOWNSFOLK ("f_" ids): filler residents of founded towns. They don't need a bed, wander
 *    the plaza, gossip, and once a week hand you some random junk. More houses, more people.
 */
class Trainers {
   static class Cls {
      String id;
      String title;
      int row;
      String[] types;
      String[] names;
      String[] spot;   // lines when they see you
      String[] win;    // when you beat them
      String[] chat;   // settled small talk

      Cls(String id, String title, int row, String[] types, String[] names) {
         this.id = id;
         this.title = title;
         this.row = row;
         this.types = types;
         this.names = names;
      }
   }

   static final HashMap<String, Cls> CLASSES = new HashMap<>();
   static final ArrayList<String> JOBS = new ArrayList<>();
   static final ArrayList<String> FOLK = new ArrayList<>();
   static final HashMap<String, Integer> ROWS = new HashMap<>();   // sprite rows (for reference / regenerating)

   static String[] a(String... s) {
      return s;
   }

   static Cls cls(String id, String title, int row, String[] types, String[] names, String[] spot, String[] win, String[] chat) {
      Cls c = new Cls("t_" + id, title, row, types, names);
      c.spot = spot;
      c.win = win;
      c.chat = chat;
      CLASSES.put(c.id, c);
      ROWS.put(c.id, row);
      return c;
   }

   static String[] ANY = {"NORMAL", "FIRE", "WATER", "GRASS", "ELECTRIC", "ICE", "FIGHTING", "POISON", "GROUND",
      "FLYING", "PSYCHIC", "BUG", "ROCK", "GHOST", "DRAGON", "DARK", "STEEL"};

   static {
      cls("youngster", "YOUNGSTER", 14, a("NORMAL", "BUG"), a("JOEY", "MIKEY", "BEN", "ALBERT", "IAN"),
         a("Hey! We locked eyes! That means we battle!", "My RATTATA is in the top percentage!"),
         a("Aww, I almost had you!", "Okay, you're pretty good."),
         a("I'm gonna be the very best. Someday.", "I keep shorts on all year. They're comfy!"));
      cls("lass", "LASS", 6, a("NORMAL", "GRASS"), a("DANA", "KRISE", "CONNIE", "ELLEN"),
         a("You look strong! Let's find out!", "Don't you dare go easy on me!"),
         a("Hmph. Next time!", "You're kind of amazing."),
         a("This town is so cute. I love it here.", "Do you think my POKéMON look stylish?"));
      cls("bugcatcher", "BUG CATCHER", 16, a("BUG"), a("WADE", "DON", "ARNIE", "BENNY"),
         a("I caught a bunch of bugs! Wanna see?", "Bugs are the best! I'll prove it!"),
         a("My bugs need a nap now.", "Wow... you're like a bug expert."),
         a("I found a cool beetle under a rock today.", "Shh. Something's in the grass."));
      cls("camper", "CAMPER", 17, a("GROUND", "ROCK", "NORMAL"), a("TODD", "ROLAND", "IVAN", "BARRY"),
         a("Out here it's survival of the fittest!", "I've been camping for days. I need a battle!"),
         a("Guess I'll go pitch my tent.", "You'd survive out here."),
         a("Nothing beats sleeping under the stars.", "Want some trail mix? It's mostly sand."));
      cls("picnicker", "PICNICKER", 9, a("GRASS", "WATER", "NORMAL"), a("LIZ", "GINA", "ERIN", "TIFFANY"),
         a("Perfect weather for a battle!", "Let's battle before lunch!"),
         a("I lost my appetite...", "Good battle! Want a sandwich?"),
         a("I packed too much food again.", "The beach is the best picnic spot."));
      cls("birdkeeper", "BIRD KEEPER", 63, a("FLYING"), a("VANCE", "ROD", "TOBY", "PERRY"),
         a("My birds spotted you from way up there!", "Let's see how you handle the sky!"),
         a("My birds are grounded.", "You clipped my wings."),
         a("The birds sing differently in the morning.", "I once saw something huge fly over the sea."));
      cls("blackbelt", "BLACK BELT", 25, a("FIGHTING"), a("KIYO", "KENJI", "NOB", "LUNG"),
         a("Hwaa! Face me!", "My fists are ready! Are yours?"),
         a("I must train harder!", "Your spirit is strong."),
         a("One hundred push-ups before breakfast.", "Strength comes from discipline."));
      cls("cooltrainer", "COOLTRAINER", 24, ANY, a("GAVEN", "ALLEN", "REENA", "QUINN", "MEGAN"),
         a("You're one of the good ones. Show me.", "I only battle the best. Prove you are."),
         a("Impressive. Truly.", "I underestimated you."),
         a("I'm training for the LEAGUE.", "I heard the GYM LEADERS here are tough."));
      cls("beauty", "BEAUTY", 10, a("WATER", "NORMAL", "GRASS"), a("VICTORIA", "SAMANTHA", "JULIA", "OLIVIA"),
         a("Battling is how I stay beautiful!", "Don't mess up my hair, okay?"),
         a("Ugh, I'm sweating.", "You're cute when you win."),
         a("The sea breeze is wonderful for my skin.", "Have you seen my reflection? It's lovely."));
      cls("gentleman", "GENTLEMAN", 54, a("NORMAL", "FIRE"), a("PRESTON", "EDWARD", "ALFRED", "GREGORY"),
         a("Ah, a young trainer. Shall we?", "A battle would be most refreshing."),
         a("Splendid! Simply splendid.", "You've beaten an old gentleman fairly."),
         a("I came here for the quiet life.", "In my day, we had no POKéDEX."));
      cls("supernerd", "SUPER NERD", 27, a("ELECTRIC", "POISON", "STEEL"), a("STAN", "ERIC", "GREGG", "JOVAN"),
         a("My calculations say I'll win!", "I've run the numbers. Battle me!"),
         a("My data was wrong?!", "I need to recalibrate."),
         a("The crystals in the caves give off weird readings.", "Want to see my rock collection? No? Okay."));
      cls("pokemaniac", "POKéMANIAC", 52, ANY, a("LARRY", "ANDREW", "CALVIN", "SHANE"),
         a("Ooh, your POKéMON! Let me see them up close!", "I love rare POKéMON! Battle me!"),
         a("Your POKéMON are so cool...", "Can I have your autograph? For your POKéMON?"),
         a("I've seen so many POKéMON on this island!", "Have you found anything rare? Tell me!"));
      cls("psychic", "PSYCHIC", 47, a("PSYCHIC", "GHOST"), a("NATHAN", "RICHARD", "MARK", "GREG"),
         a("I foresaw this battle.", "My mind is ready. Is yours?"),
         a("I didn't see that coming.", "The future is clouded."),
         a("I sense a strong presence on this island.", "You'll find something rare soon. Maybe."));
      cls("sage", "SAGE", 22, a("GRASS", "GHOST", "PSYCHIC"), a("CHOW", "NICO", "JIN", "TROY"),
         a("Show me the bond with your POKéMON.", "Let us test your spirit."),
         a("Your bond is true.", "I have much to learn."),
         a("Patience is the root of strength.", "Even the smallest sprout reaches for the sun."));
      cls("grunt", "ROCKET GRUNT", 29, a("POISON", "DARK"), a("GRUNT"),
         a("Hey! This island is ours now!", "Hand over your POKéMON, kid!"),
         a("Blast it!", "This isn't over!"),
         a("I quit. This town has better food.", "Don't tell anyone I used to be a ROCKET."));
      cls("biker", "BIKER", 45, a("POISON", "FIRE"), a("REESE", "DWAYNE", "HARRIS", "ZEKE"),
         a("Outta my way! Or battle!", "You lookin' at me?"),
         a("Man... wiped out.", "Respect."),
         a("No roads here. Worst island ever. I love it.", "Seen my bike? Me neither."));
   }

   // ------------------------------------------------------------------ registering with Npcs

   static void register() {
      for (Cls c : CLASSES.values()) {
         String[] lines = c.chat;
         Npcs.add(new Npcs.Type(c.id, c.title, c.names, lines));
      }
      job("ballmaker", "BALL MAKER", 32, a("KURT", "MAPLE", "ROWE", "IVY"),
            "Bring me apricorns and I'll carve you something special.",
            "Every ball has a personality, you know.",
            "My grandpa taught me this craft.")
         .trade("fast ball", 1, "white apricorn", 1)
         .trade("heavy ball", 1, "black apricorn", 1)
         .trade("level ball", 1, "red apricorn", 1)
         .trade("lure ball", 1, "blue apricorn", 1)
         .trade("love ball", 1, "pink apricorn", 1)
         .trade("friend ball", 1, "green apricorn", 1)
         .trade("moon ball", 1, "yellow apricorn", 1)
         .trade("ultra ball", 1, "great ball", 1, "iron ore", 2);
      job("blacksmith", "BLACKSMITH", 8, a("BRANT", "HILDA", "TORVAL", "ODA"),
            "Steel and fire. That's all you need.",
            "Bring ore, get tools. Simple.",
            "My anvil's older than this island. Probably.")
         .trade("iron pick", 1, "iron ore", 3, "log", 2)
         .trade("iron axe", 1, "iron ore", 3, "log", 2)
         .trade("iron shovel", 1, "iron ore", 2, "log", 2)
         .trade("metal coat", 1, "iron ore", 4)
         .trade("iron ore", 1, "copper ore", 3);
      job("miner", "MINER", 20, a("DIGBY", "ROCKO", "GARRET", "OPAL"),
            "The deeper you dig, the better it gets.",
            "Watch your head down there.",
            "I can hear the ore singing. Don't laugh.")
         .trade("iron ore", 2, "log", 6)
         .trade("gold ore", 1, "iron ore", 2, "log", 3)
         .trade("helix fossil", 1, "crystal ore", 3)
         .trade("dome fossil", 1, "crystal ore", 3)
         .trade("claw fossil", 1, "crystal ore", 3)
         .trade("root fossil", 1, "crystal ore", 3)
         .trade("big pearl", 1, "gold ore", 2);
      job("farmer", "FARMER", 30, a("HANK", "ROSA", "EARL", "MAE"),
            "Good soil here. Real good soil.",
            "Up with the sun, down with the sun.",
            "My MILTANK says hi.")
         .trade("cheri berry", 2, "berry seed", 1, "grass", 1)
         .trade("pecha berry", 2, "berry seed", 1, "grass", 1)
         .trade("rawst berry", 2, "berry seed", 1, "grass", 1)
         .trade("chesto berry", 2, "berry seed", 1, "grass", 1)
         .trade("moomoo milk", 1, "grass", 5)
         .trade("berry seed", 3, "log", 1);
      job("cook", "COOK", 65, a("REMY", "PAULA", "GUS", "NONNA"),
            "Nobody adventures on an empty stomach!",
            "Taste this. No, really. Taste it.",
            "Secret ingredient? Love. And salt.")
         .trade("berry juice", 2, "cheri berry", 1, "pecha berry", 1)
         .trade("moomoo milk", 2, "berry juice", 2)
         .trade("honey", 1, "berry juice", 1, "grass", 2)
         .trade("lum berry", 1, "berry juice", 3);
      job("carpenter", "CARPENTER", 33, a("JOSEPH", "BEA", "TIMBER", "HAL"),
            "Measure twice, cut once.",
            "Your houses are crooked. Love the spirit though.",
            "Need wood? I've got wood.")
         .trade("log", 4, "copper ore", 1)
         .trade("soft bedding", 2, "silky thread", 1, "grass", 2)
         .trade("log", 2, "grass", 4);
      job("breeder", "BREEDER", 37, a("MARGE", "WILMA", "DOT", "HETTY"),
            "Oh, your POKéMON are darling!",
            "I used to run a DAY-CARE, you know.",
            "Leave them with me any time, dear.");

      folk("gramps", "GRAMPS", 7);
      folk("lady", "LADY", 41);
      folk("kid", "KID", 26);
      folk("teacher", "TEACHER", 19);
      folk("gossip", "GOSSIP", 39);
      folk("tourist", "TOURIST", 42);
      folk("dj", "DJ", 31);
      folk("glutton", "GLUTTON", 11);
      folk("oldman", "OLD MAN", 58);
      folk("teen", "TEEN", 60);
      folk("mom", "MOM", 57);
      folk("boy", "BOY", 67);
   }

   static Npcs.Type job(String id, String title, int row, String[] names, String... lines) {
      Npcs.Type t = new Npcs.Type(id, title, names, lines);
      Npcs.add(t);
      JOBS.add(id);
      ROWS.put(id, row);
      return t;
   }

   static void folk(String id, String title, int row) {
      Npcs.add(new Npcs.Type("f_" + id, title, FOLK_NAMES, "Hi!"));
      FOLK.add("f_" + id);
      ROWS.put("f_" + id, row);
   }

   static final String[] FOLK_NAMES = {"ANNA", "BILL", "CORA", "DEV", "EVE", "FINN", "GIL", "HAZEL", "IGGY", "JUNE",
      "KIP", "LOLA", "MOE", "NELL", "OSSIE", "PIA", "RUBY", "SAL", "TIM", "UMA", "VIC", "WREN"};

   static boolean isTrainer(String type) {
      return type.startsWith("t_");
   }

   static boolean isFolk(String type) {
      return type.startsWith("f_");
   }

   static boolean isWanderer(Npcs.Npc npc) {
      return npc.data.wanderDay >= 0;
   }

   /** Who may move into a free bed. */
   static boolean unlocked(Game game, String id) {
      Player p = game.player;
      boolean town = !Cities.list(game).isEmpty();
      if (isTrainer(id)) {
         return town && p.unlocks.contains("beat:" + id);
      }
      switch (id) {
         case "ballmaker": return town || p.dexCaught.size() >= 15;
         case "blacksmith": return town && (p.visitedCave || p.hasItem("iron ore"));
         case "miner": return p.visitedCave && p.dexSeen.size() >= 20;
         case "farmer": return town;
         case "cook": return town && p.daysPassed >= 5;
         case "carpenter": return Cities.houseDoors(game).size() >= 4;
         case "breeder": return p.dexCaught.size() >= 20;
         default: return false;
      }
   }

   /** Default routine for anyone not listed in Routines.SCHEDULE. */
   static String[] schedule(Npcs.Npc npc) {
      String t = npc.data.type;
      if (isWanderer(npc)) {
         return a("home", "home", "home", "inside");
      }
      if (isFolk(t)) {
         return a("plaza", "wander", "plaza", "inside");
      }
      if (isTrainer(t)) {
         return a("grass", "wander", "plaza", "inside");
      }
      switch (t) {
         case "miner": return a("cave", "cave", "home", "inside");
         case "farmer": return a("grass", "grass", "home", "inside");
         case "blacksmith": return a("home", "home", "plaza", "inside");
         case "ballmaker": return a("trees", "home", "plaza", "inside");
         default: return a("home", "plaza", "home", "inside");
      }
   }

   // ------------------------------------------------------------------ teams

   static int trainerLevel(Game game, Random r) {
      return Math.max(3, game.player.catchLevelCap() - 10 + r.nextInt(6));
   }

   static ArrayList<String> makeTeam(Game game, Cls c, int size, int level) {
      ArrayList<String> team = new ArrayList<>();
      for (int i = 0; i < size; i++) {
         String type = c.types[Game.rand.nextInt(c.types.length)];
         int lv = Math.max(2, level - Game.rand.nextInt(3));
         String sp = Characters.randomOfType(type);
         team.add(Characters.evolve(sp, lv) + ":" + lv);
      }
      return team;
   }

   /** Each morning: settlers train up a bit, sometimes catch something new. */
   static void daily(Game game, Npcs.Npc npc, boolean newDay) {
      Cls c = CLASSES.get(npc.data.type);
      if (c == null) {
         return;
      }
      if (npc.data.team == null || npc.data.team.isEmpty()) {
         npc.data.team = makeTeam(game, c, 2, trainerLevel(game, new Random()));
         return;
      }
      if (!newDay) {
         return;
      }
      int target = Math.max(5, game.player.catchLevelCap() - 4);
      for (int i = 0; i < npc.data.team.size(); i++) {
         String[] m = npc.data.team.get(i).split(":");
         int lv = Integer.valueOf(m[1]);
         if (lv < target) {
            lv++;
            npc.data.team.set(i, Characters.evolve(m[0], lv) + ":" + lv);
         }
      }
      if (npc.data.team.size() < 4 && Game.rand.nextInt(5) == 0) {
         npc.data.team.addAll(makeTeam(game, c, 1, target - 3));
      }
   }

   // ------------------------------------------------------------------ wanderers

   static int wandererCount(Game game) {
      int ring = Islands.ring(game.map.currMapId);
      return Math.min(5, 2 + ring);
   }

   /** Morning (and map load): yesterday's wanderers leave, new ones show up. Townsfolk move in. */
   static void update(Game game, boolean newDay) {
      ArrayList<Npcs.Npc> npcs = game.map.npcs;
      int today = game.player.daysPassed;
      npcs.removeIf(n -> n.data.wanderDay >= 0 && n.data.wanderDay != today);
      int have = 0;
      for (Npcs.Npc n : npcs) {
         if (n.data.wanderDay == today) {
            have++;
         }
      }
      if (game.map.overworldTiles == null || game.map.overworldTiles.isEmpty()) {
         return;
      }
      ArrayList<Vector2> keys = new ArrayList<>(game.map.overworldTiles.keySet());
      Random r = new Random(today * 977L + (game.map.currMapId == null ? 0 : game.map.currMapId.hashCode()));
      ArrayList<String> ids = new ArrayList<>(CLASSES.keySet());
      java.util.Collections.sort(ids);
      for (int tries = 0; have < wandererCount(game) && tries < 400; tries++) {
         Vector2 p = keys.get(r.nextInt(keys.size()));
         Tile t = game.map.overworldTiles.get(p);
         if (t == null || !Routines.standable(game, p) || Cities.cityAt(game, p) != null
            || p.dst(game.player.position) < 14 * 16 || game.map.pokemon.containsKey(p)) {
            continue;
         }
         // wanderers like open ground: grass, sand, paths
         if (!(t.nameUpper.contains("grass") || t.name.contains("grass") || t.name.contains("sand") || t.name.contains("path"))) {
            continue;
         }
         Cls c = CLASSES.get(ids.get(r.nextInt(ids.size())));
         Npcs.NpcData d = new Npcs.NpcData();
         d.type = c.id;
         d.name = c.names[r.nextInt(c.names.length)];
         d.x = d.homeX = p.x;
         d.y = d.homeY = p.y;
         d.bedLayer = -1;
         d.wanderDay = today;
         d.team = makeTeam(game, c, 1 + r.nextInt(3), trainerLevel(game, r));
         Npcs.Npc npc = new Npcs.Npc(d);
         npc.dir = new String[]{"up", "down", "left", "right"}[r.nextInt(4)];
         npcs.add(npc);
         have++;
         if (Alerts.ready) {
            Alerts.push(game, "A " + c.title + " is wandering " + Alerts.where(game, p) + " today.");
         }
      }
      spawnFolk(game);
   }

   /** Called every frame for wanderers: challenge the player if they walk into view. */
   static boolean checkSight(Game game, Npcs.Npc n) {
      if (!isWanderer(n) || n.data.lastBattleDay == game.player.daysPassed || !game.playerCanMove
         || game.battle.drawAction != null || n.moveTimer > 0 || game.player.pokemon.isEmpty()) {
         return false;
      }
      boolean canFight = false;
      for (Pokemon p : game.player.pokemon) {
         canFight |= !p.isEgg && p.currentStats.get("hp") > 0;
      }
      if (!canFight) {
         return false;
      }
      Vector2 step = dirVec(n.dir);
      Vector2 p = n.position.cpy();
      for (int i = 1; i <= 4; i++) {
         p.add(step);
         if (p.epsilonEquals(game.player.position, 1.0F)) {
            challenge(game, n);
            return true;
         }
         if (!Routines.standable(game, p)) {
            return false;
         }
      }
      return false;
   }

   static Vector2 dirVec(String dir) {
      switch (dir) {
         case "up": return new Vector2(0, 16);
         case "down": return new Vector2(0, -16);
         case "left": return new Vector2(-16, 0);
         default: return new Vector2(16, 0);
      }
   }

   static void challenge(Game game, Npcs.Npc n) {
      Cls c = CLASSES.get(n.data.type);
      game.playerCanMove = false;
      n.data.lastBattleDay = game.player.daysPassed;
      n.idleTimer = 600;
      game.player.dirFacing = Player.oppDirs.get(n.dir);
      String line = c.spot[Game.rand.nextInt(c.spot.length)];
      game.insertAction(new Npcs.Bang(n, 50,
         new DisplayText(game, Characters.T(n.fullName() + " - " + line), null, null,
            new RunCode(() -> TrainerBattle.start(game, n), null))));
   }

   static String winLine(Npcs.Npc npc) {
      Cls c = CLASSES.get(npc.data.type);
      if (c == null) {
         return "You win!";
      }
      return c.win[Game.rand.nextInt(c.win.length)];
   }

   /** After you beat a trainer: a small prize, and their class can now settle in your towns. */
   static Action prize(Game game, Npcs.Npc npc, Action next) {
      if (!isTrainer(npc.data.type)) {
         return next;
      }
      game.player.unlocks.add("beat:" + npc.data.type);
      String[] prizes = {"poké ball:2", "great ball:1", "berry juice:1", "stardust:1", "copper ore:2", "iron ore:1", "berry seed:2"};
      String[] g = prizes[Game.rand.nextInt(prizes.length)].split(":");
      game.player.addItem(g[0], Integer.valueOf(g[1]));
      return new DisplayText(game, Characters.T(npc.fullName() + " gave you " + g[1] + " " + g[0].toUpperCase(Locale.ROOT) + "."),
         null, null, next);
   }

   // ------------------------------------------------------------------ townsfolk

   static void spawnFolk(Game game) {
      ArrayList<Npcs.Npc> npcs = game.map.npcs;
      for (Cities.CityData c : Cities.list(game)) {
         int want = Math.min(6, Math.max(1, Cities.housesIn(game, c) / 2));
         int have = 0;
         ArrayList<String> used = new ArrayList<>();
         for (Npcs.Npc n : npcs) {
            if (isFolk(n.data.type) && c.name.equals(n.data.gymCity)) {
               have++;
               used.add(n.data.type);
            }
         }
         Random r = new Random(c.name.hashCode() * 31L + have);
         while (have < want) {
            ArrayList<String> pool = new ArrayList<>(FOLK);
            pool.removeAll(used);
            if (pool.isEmpty()) {
               pool = new ArrayList<>(FOLK);
            }
            String type = pool.get(r.nextInt(pool.size()));
            Vector2 home = Routines.nearestStandable(game,
               new Vector2(c.x + (r.nextInt(9) - 4) * 16, c.y + (r.nextInt(9) - 4) * 16), 4);
            if (home == null) {
               break;
            }
            Npcs.NpcData d = new Npcs.NpcData();
            d.type = type;
            d.name = FOLK_NAMES[r.nextInt(FOLK_NAMES.length)];
            d.x = d.homeX = home.x;
            d.y = d.homeY = home.y;
            d.bedLayer = -1;
            d.gymCity = c.name;   // which town they belong to
            npcs.add(new Npcs.Npc(d));
            used.add(type);
            have++;
         }
      }
   }

   static final String[] JUNK = {"grass:3", "log:2", "berry seed:1", "stardust:1", "poké ball:1", "honey:1",
      "moomoo milk:1", "escape rope:1", "copper ore:1", "cheri berry:1", "silky thread:1", "pecha berry:1"};

   static String[] JUNK_LINES = {
      "I found this behind my house. No idea what it is. Here!",
      "Cleaning out my stuff. You want this? Take it.",
      "This was in my shoe. Don't ask. It's yours now.",
      "My cousin sent me this. I don't need it.",
      "Free stuff! Well, free junk. Same thing.",
      "I won this in a bet. Long story. Here.",
   };

   /** Townsfolk: gossip, and once a week some junk. */
   static Action folkTalk(Game game, Npcs.Npc npc, Action end) {
      String who = npc.fullName() + " - ";
      Action gym = League.gymPrompt(game, npc, end);
      if (gym != null) {
         return gym;
      }
      Action next = end;
      if (npc.data.lastGiftDay < 0 || game.player.daysPassed - npc.data.lastGiftDay >= 7) {
         npc.data.lastGiftDay = game.player.daysPassed;
         String[] g = JUNK[Game.rand.nextInt(JUNK.length)].split(":");
         game.player.addItem(g[0], Integer.valueOf(g[1]));
         next = new DisplayText(game, Characters.T(who + JUNK_LINES[Game.rand.nextInt(JUNK_LINES.length)]), null, null,
            new DisplayText(game, Characters.T("Got " + g[1] + " " + g[0].toUpperCase(Locale.ROOT) + "."), "fanfare1.ogg", null, end));
         return next;
      }
      return new DisplayText(game, Characters.T(who + gossip(game, npc)), null, null, end);
   }

   static String gossip(Game game, Npcs.Npc self) {
      ArrayList<String> out = new ArrayList<>();
      Player p = game.player;
      // About other villagers and what they get up to.
      for (Npcs.Npc n : game.map.npcs) {
         if (n == self || isWanderer(n) || isFolk(n.data.type)) {
            continue;
         }
         String name = n.fullName();
         String morning = Routines.SCHEDULE.containsKey(n.data.type) ? Routines.SCHEDULE.get(n.data.type)[0] : null;
         if ("grass".equals(morning)) {
            out.add("I always see " + name + " in the tall grass in the morning.");
         } else if ("rocks".equals(morning)) {
            out.add(name + " goes up the rocks every morning. Why?");
         } else if ("trees".equals(morning)) {
            out.add(name + " is always out by the trees early on.");
         }
         if ("partner".equals(n.data.relation)) {
            out.add("So... you and " + name + ", huh? Everyone's talking about it.");
         }
         if ("gym".equals(n.data.role)) {
            out.add("Have you challenged " + name + " yet? They run a GYM now!");
         }
         if (n.data.type.equals("lavender")) {
            out.add("I swear I saw LAVENDER walking around at midnight.");
         }
         if (n.data.type.equals("bruno")) {
            out.add("BRUNO glared at me today. I didn't even do anything!");
         }
         if (n.data.type.equals("willow")) {
            out.add("Something exploded near WILLOW's house again. Small explosion. Friendly one.");
         }
         if (n.data.type.equals("emma") && n.data.losses > 0) {
            out.add("EMMA keeps bragging she beat you. Is that true?");
         }
         if (n.data.type.equals("emma") && n.data.wins > 2) {
            out.add("EMMA was sulking by the water. Did you beat her again?");
         }
      }
      // About the town and the player.
      Cities.CityData c = Cities.cityAt(game, self.home());
      if (c != null) {
         out.add("I love living in " + Cities.title(c) + ". Best town around.");
         out.add("Did you know " + Cities.title(c) + " was founded on day " + c.foundedDay + "? I was here first. Basically.");
      }
      if (p.dexCaught.size() > 0) {
         out.add("You've caught " + p.dexCaught.size() + " kinds of POKéMON? That's like... a lot, right?");
      }
      if (p.bossesBeaten > 0) {
         out.add("Is it true you beat one of those giant POKéMON? Wild.");
      }
      if (!p.badges.isEmpty()) {
         out.add("Ooh, a BADGE! Can I touch it? No? Okay.");
      }
      // Pure nonsense.
      String t = self.data.type;
      String[] own;
      switch (t) {
         case "f_gramps": own = a("Back in my day this whole island was just sand.", "My knees can predict the weather. They say: sunny."); break;
         case "f_kid": own = a("I'm gonna catch a LEGENDARY when I grow up!", "I dug a hole. It's a pretty good hole."); break;
         case "f_teacher": own = a("Did you know a group of POKéMON is called... a party? I think.", "Homework is due tomorrow. Not yours. Theirs."); break;
         case "f_gossip": own = a("I heard someone talking to a MAGIKARP for an hour yesterday.", "I know everything that happens here. EVERYTHING."); break;
         case "f_tourist": own = a("Excuse me, where's the gift shop?", "I took 300 pictures of a PIDGEY."); break;
         case "f_dj": own = a("Listening to the sea. It's got a sick beat.", "I'm gonna start a radio station. Once someone invents radio here."); break;
         case "f_glutton": own = a("Have you tried berries? With other berries?", "I'm on a diet. A berry diet. Lots of berries."); break;
         case "f_oldman": own = a("I used to be a trainer like you. Then I got old.", "Zzz... huh? Oh, hello."); break;
         case "f_teen": own = a("This island has no signal. At all.", "Ugh, my mom wants me to go outside. I AM outside."); break;
         case "f_mom": own = a("Make sure you eat something, dear!", "Are you wearing clean socks? Adventurers forget."); break;
         case "f_boy": own = a("I'm not scared of GHOST types! ...Are there any around?", "Race you! ...Never mind, I'm tired."); break;
         default: own = a("Nice weather, huh?", "Just enjoying the day.");
      }
      for (String s : own) {
         out.add(s);
         out.add(s); // weight their own personality higher
      }
      return out.get(Game.rand.nextInt(out.size()));
   }

   // ------------------------------------------------------------------ services

   /** Settler rematch / breeder day-care. Returns null if nothing special. */
   static Action service(Game game, Npcs.Npc npc, Action end) {
      String t = npc.data.type;
      Action gym = League.gymPrompt(game, npc, end);
      if (gym != null) {
         return gym;
      }
      if (isFolk(t)) {
         return folkTalk(game, npc, end);
      }
      if (isTrainer(t)) {
         if (isWanderer(npc)) {
            return end;
         }
         daily(game, npc, false);
         boolean canFight = false;
         for (Pokemon p : game.player.pokemon) {
            canFight |= !p.isEgg && p.currentStats.get("hp") > 0;
         }
         if (!canFight || npc.data.lastBattleDay == game.player.daysPassed) {
            return end;
         }
         Action yes = new DisplayText.Clear(game, new RunCode(() -> TrainerBattle.start(game, npc), null));
         return new DisplayText(game, Characters.T("Rematch? I've been training!"), null, true, false,
            new DrawYesNoMenu(null, yes, new DisplayText.Clear(game, end)));
      }
      if (t.equals("breeder")) {
         if (npc.data.lastBattleDay == game.player.daysPassed) {
            return end;
         }
         Pokemon low = null;
         for (Pokemon p : game.player.pokemon) {
            if (!p.isEgg && p.level < 100 && (low == null || p.level < low.level)) {
               low = p;
            }
         }
         if (low == null) {
            return end;
         }
         final Pokemon target = low;
         Action yes = new DisplayText.Clear(game, new RunCode(() -> {
            npc.data.lastBattleDay = game.player.daysPassed;
            int hp = target.currentStats.get("hp");
            int max = target.maxStats.get("hp");
            target.level++;
            target.exp = target.calcExpForLevel(target.level);
            target.calcMaxStats();
            target.currentStats.put("hp", Math.min(target.maxStats.get("hp"), hp + (target.maxStats.get("hp") - max)));
            game.insertAction(new DisplayText(game, Characters.T(target.nickname.toUpperCase(Locale.ROOT) + " grew to LV"
               + target.level + " with some TLC!"), "fanfare1.ogg", null, end));
         }, null));
         return new DisplayText(game, Characters.T("Want me to look after " + low.nickname.toUpperCase(Locale.ROOT) + " for a bit today?"),
            null, true, false, new DrawYesNoMenu(null, yes, new DisplayText.Clear(game, end)));
      }
      return null;
   }

   static boolean hasNews(Game game, Npcs.Npc npc) {
      String t = npc.data.type;
      if (isFolk(t)) {
         return npc.data.lastGiftDay < 0 || game.player.daysPassed - npc.data.lastGiftDay >= 7;
      }
      if (t.equals("breeder")) {
         return npc.data.lastBattleDay != game.player.daysPassed;
      }
      return false;
   }
}
