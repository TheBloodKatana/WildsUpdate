package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Local tournaments (docs/PROGRESSION.md sections 3-5, 7, 8).
 *
 *  - A town that gets a gym slot nobody holds gets an INAUGURAL tournament 3 days later.
 *  - Each season one gym town (drawn from this cycle's pool) hosts a tournament on a random day.
 *    Once every town has hosted, the pool refills (minus the last host).
 *  - Alerts 3 days before and on the morning, both saying where. Results that evening.
 *  - 8 entrants: the town's leaders, you (if you sign up that morning in town), core characters
 *    from the town, then its succession list, settled trainers, townsfolk.
 *  - Single elimination at noon. Your matches are real battles (healed between rounds), everything
 *    else is simulated. Gym slots go to the best finishers who aren't you; the rest become the
 *    town's succession list, used when a slot opens later.
 */
class Tournaments {
   static final int ANNOUNCE_DAYS = 3;
   static final int SIZE = 8;

   static class Sched {
      String town;
      int day;
      boolean inaugural;

      Sched() {
      }
   }

   /** Per island, saved in MapSaveData. */
   static class State {
      ArrayList<String> pool = new ArrayList<>();
      String lastHost;
      ArrayList<Sched> scheduled = new ArrayList<>();
      int plannedSeason = -1;   // absolute season number (daysPassed / 28) already planned

      State() {
      }
   }

   static State state(Game game) {
      if (game.map.tourney == null) {
         game.map.tourney = new State();
      }
      return game.map.tourney;
   }

   static String key(Npcs.Npc n) {
      return n.data.type + ":" + n.data.name;
   }

   static Npcs.Npc byKey(Game game, String k) {
      for (Npcs.Npc n : game.map.npcs) {
         if (key(n).equals(k)) {
            return n;
         }
      }
      return null;
   }

   static Cities.CityData city(Game game, String name) {
      for (Cities.CityData c : Cities.list(game)) {
         if (c.name.equals(name)) {
            return c;
         }
      }
      return null;
   }

   /** Section 3: the founded town nearest an NPC's home (townsfolk: the town they live in). */
   static Cities.CityData homeTown(Game game, Npcs.Npc n) {
      if (Trainers.isFolk(n.data.type) && n.data.gymCity != null) {
         return city(game, n.data.gymCity);
      }
      if (Trainers.isWanderer(n)) {
         return null;
      }
      Cities.CityData best = null;
      Vector2 h = n.home();
      for (Cities.CityData c : Cities.list(game)) {
         if (h.dst(c.x, c.y) < 60 * 16 && (best == null || h.dst2(c.x, c.y) < h.dst2(best.x, best.y))) {
            best = c;
         }
      }
      return best;
   }

   static ArrayList<Npcs.Npc> leadersOf(Game game, Cities.CityData c) {
      ArrayList<Npcs.Npc> out = new ArrayList<>();
      for (Npcs.Npc n : game.map.npcs) {
         if (League.isLeader(n) && c.name.equals(n.data.gymCity)) {
            out.add(n);
         }
      }
      return out;
   }

   static boolean holdsRole(Npcs.Npc n) {
      return n.data.role != null;
   }

   // ------------------------------------------------------------------ scheduling

   static Sched scheduledFor(Game game, String town) {
      for (Sched s : state(game).scheduled) {
         if (s.town.equals(town)) {
            return s;
         }
      }
      return null;
   }

   static Sched today(Game game) {
      for (Sched s : state(game).scheduled) {
         if (s.day == game.player.daysPassed) {
            return s;
         }
      }
      return null;
   }

   static void schedule(Game game, String town, int day, boolean inaugural) {
      if (scheduledFor(game, town) != null) {
         return;
      }
      Sched s = new Sched();
      s.town = town;
      s.day = day;
      s.inaugural = inaugural;
      state(game).scheduled.add(s);
      state(game).pool.remove(town);
      Cities.CityData c = city(game, town);
      String where = c != null ? Cities.title(c) : town;
      int inDays = day - game.player.daysPassed;
      if (inaugural) {
         Alerts.push(game, "New GYM in " + where + "! A tournament for its leader starts in " + inDays + " days at the plaza.");
      }
   }

   /** Morning: announcements, inaugural tournaments for empty gyms, seasonal planning, vacancies. */
   static void onLoad(Game game) {
      if (game.map.npcs == null) {
         return;
      }
      fillVacancies(game);
      planSeason(game);
   }

   static void onNewDay(Game game) {
      if (game.map.npcs == null) {
         return;
      }
      State st = state(game);
      int day = game.player.daysPassed;
      fillVacancies(game);
      planSeason(game);
      announce(game);
      st.scheduled.removeIf(s -> s.day < day || city(game, s.town) == null);
      todayEntrants = null;
      signedUp = false;
      askedDay = -1;
   }

   static void planSeason(Game game) {
      State st = state(game);
      int day = game.player.daysPassed;
      int seasonNo = day / Calendar.SEASON_DAYS;
      if (st.plannedSeason != seasonNo && !Cities.list(game).isEmpty()) {
         st.plannedSeason = seasonNo;
         ArrayList<String> towns = new ArrayList<>();
         for (Cities.CityData c : Cities.list(game)) {
            towns.add(c.name);
         }
         st.pool.retainAll(towns);
         if (st.pool.isEmpty()) {
            st.pool.addAll(towns);
            if (st.pool.size() > 1 && st.lastHost != null) {
               st.pool.remove(st.lastHost);
            }
         }
         ArrayList<String> choices = new ArrayList<>(st.pool);
         choices.removeIf(t -> scheduledFor(game, t) != null);
         if (!choices.isEmpty()) {
            Random r = new Random(seasonNo * 101L + day);
            String host = choices.get(r.nextInt(choices.size()));
            int ds = Calendar.dayOfSeason(game);
            int last = Calendar.seasonIndex(game) == 3 ? Calendar.SEASON_DAYS - 7 : Calendar.SEASON_DAYS - 2;
            int first = Math.min(last, Math.max(ds + ANNOUNCE_DAYS, 4));
            int pick = first + r.nextInt(Math.max(1, last - first + 1));
            schedule(game, host, day + (pick - ds), false);
         }
      }
   }

   static void announce(Game game) {
      State st = state(game);
      int day = game.player.daysPassed;
      for (Sched s : st.scheduled) {
         Cities.CityData c = city(game, s.town);
         if (c == null) {
            continue;
         }
         String where = Cities.title(c);
         String name = where + " TOURNAMENT";
         if (s.day - day == ANNOUNCE_DAYS && !s.inaugural) {
            Alerts.push(game, "The " + name + " starts in 3 days at " + where + " plaza!");
         } else if (s.day == day) {
            Alerts.push(game, "The " + name + " starts TODAY at noon at " + where + " plaza!");
         }
      }
   }

   /** Section 7: town size changes, missing leaders. Gyms are never left empty if anyone can fill them. */
   static void fillVacancies(Game game) {
      for (Cities.CityData c : Cities.list(game)) {
         ArrayList<Npcs.Npc> leaders = leadersOf(game, c);
         int slots = Cities.gymSlots(c);
         // too many leaders (town shrank): the most recent appointee steps down to the top of the line
         while (leaders.size() > slots) {
            Npcs.Npc out = leaders.remove(leaders.size() - 1);
            out.data.role = null;
            out.data.gymCity = null;
            c.succession.add(0, key(out));
            Alerts.push(game, out.fullName() + " stepped down as a GYM LEADER of " + Cities.title(c) + ".");
         }
         // free slots: next in line, else a tournament
         while (leaders.size() < slots) {
            Npcs.Npc next = null;
            while (!c.succession.isEmpty() && next == null) {
               Npcs.Npc n = byKey(game, c.succession.remove(0));
               if (n != null && !holdsRole(n) && eligibleFor(game, n, c)) {
                  next = n;
               }
            }
            if (next == null) {
               if (scheduledFor(game, c.name) == null && (running == null || running.town != c)) {
                  schedule(game, c.name, game.player.daysPassed + ANNOUNCE_DAYS, true);
               }
               break;
            }
            appoint(game, next, c);
            leaders.add(next);
            Alerts.push(game, next.fullName() + " is now a GYM LEADER of " + Cities.title(c) + "!");
         }
      }
   }

   static void appoint(Game game, Npcs.Npc n, Cities.CityData c) {
      boolean[] used = new boolean[4];
      for (Npcs.Npc o : leadersOf(game, c)) {
         if (o != n && o.data.gymSlot < used.length) {
            used[o.data.gymSlot] = true;
         }
      }
      int slot = 0;
      while (slot < used.length - 1 && used[slot]) {
         slot++;
      }
      n.data.role = "gym";
      n.data.gymCity = c.name;
      n.data.gymSlot = slot;
      ensureTeam(game, n);
   }

   static boolean eligibleFor(Game game, Npcs.Npc n, Cities.CityData c) {
      if (Trainers.isWanderer(n) || "citymanager".equals(n.data.type) || "director".equals(n.data.type)) {
         return false;
      }
      if (holdsRole(n) && !(League.isLeader(n) && c.name.equals(n.data.gymCity))) {
         return false;
      }
      Cities.CityData home = homeTown(game, n);
      return home != null && home.name.equals(c.name);
   }

   static boolean fighter(Npcs.Npc n) {
      String t = n.data.type;
      return Characters.isCore(t) || Trainers.isTrainer(t) || Trainers.isFolk(t);
   }

   // ------------------------------------------------------------------ entrants

   static ArrayList<Npcs.Npc> todayEntrants;
   static boolean signedUp = false;
   static int askedDay = -1;
   static boolean devStartNow = false;

   static ArrayList<Npcs.Npc> entrants(Game game, Cities.CityData c) {
      ArrayList<Npcs.Npc> out = new ArrayList<>();
      HashSet<Npcs.Npc> seen = new HashSet<>();
      java.util.function.Consumer<Npcs.Npc> add = n -> {
         if (n != null && out.size() < SIZE && !seen.contains(n) && fighter(n) && eligibleFor(game, n, c)) {
            seen.add(n);
            out.add(n);
         }
      };
      for (Npcs.Npc n : leadersOf(game, c)) {
         add.accept(n);
      }
      for (Npcs.Npc n : game.map.npcs) {        // core characters first, as asked
         if (Characters.isCore(n.data.type)) {
            add.accept(n);
         }
      }
      for (String k : c.succession) {
         add.accept(byKey(game, k));
      }
      ArrayList<Npcs.Npc> rest = new ArrayList<>();
      for (Npcs.Npc n : game.map.npcs) {
         if (Trainers.isTrainer(n.data.type) || Trainers.isFolk(n.data.type)) {
            rest.add(n);
         }
      }
      rest.sort((a, b) -> power(game, b) - power(game, a));
      for (Npcs.Npc n : rest) {
         if (Trainers.isTrainer(n.data.type)) {
            add.accept(n);
         }
      }
      for (Npcs.Npc n : rest) {
         add.accept(n);
      }
      for (Npcs.Npc n : out) {
         ensureTeam(game, n);
      }
      return out;
   }

   static void ensureTeam(Game game, Npcs.Npc n) {
      if (n.data.team != null && !n.data.team.isEmpty()) {
         return;
      }
      if (Characters.isCore(n.data.type)) {
         Characters.daily(game, n, false);
         return;
      }
      if (Trainers.isTrainer(n.data.type)) {
         Trainers.daily(game, n, false);
         return;
      }
      // townsfolk: a couple of modest Pokemon
      Random r = new Random(key(n).hashCode());
      n.data.team = new ArrayList<>();
      int lv = Math.max(3, game.player.catchLevelCap() - 15 + r.nextInt(5));
      for (int i = 0; i < 2; i++) {
         String sp = Characters.randomOfType(Trainers.ANY[r.nextInt(Trainers.ANY.length)]);
         n.data.team.add(Characters.evolve(sp, lv) + ":" + lv);
      }
   }

   /** Routines hook: entrants spend tournament day at the host plaza. */
   static Cities.CityData attending(Game game, Npcs.Npc n) {
      Sched s = today(game);
      if (s == null) {
         return null;
      }
      Cities.CityData c = city(game, s.town);
      if (c == null) {
         return null;
      }
      if (todayEntrants == null) {
         todayEntrants = entrants(game, c);
      }
      return todayEntrants.contains(n) ? c : null;
   }

   // ------------------------------------------------------------------ simulation (section 8)

   static HashMap<String, String[]> typesOf;

   static String[] types(String species) {
      if (typesOf == null) {
         typesOf = new HashMap<>();
         Characters.loadTypeData();
         for (String type : Characters.byType.keySet()) {
            for (String sp : Characters.byType.get(type)) {
               String[] old = typesOf.get(sp);
               typesOf.put(sp, old == null ? new String[]{type} : new String[]{old[0], type});
            }
         }
      }
      return typesOf.getOrDefault(species, new String[]{"NORMAL"});
   }

   static final HashMap<String, String> BEATS = new HashMap<>();
   static {
      BEATS.put("FIRE", "GRASS ICE BUG STEEL");
      BEATS.put("WATER", "FIRE GROUND ROCK");
      BEATS.put("GRASS", "WATER GROUND ROCK");
      BEATS.put("ELECTRIC", "WATER FLYING");
      BEATS.put("ICE", "GRASS GROUND FLYING DRAGON");
      BEATS.put("FIGHTING", "NORMAL ICE ROCK DARK STEEL");
      BEATS.put("POISON", "GRASS");
      BEATS.put("GROUND", "FIRE ELECTRIC POISON ROCK STEEL");
      BEATS.put("FLYING", "GRASS FIGHTING BUG");
      BEATS.put("PSYCHIC", "FIGHTING POISON");
      BEATS.put("BUG", "GRASS PSYCHIC DARK");
      BEATS.put("ROCK", "FIRE ICE FLYING BUG");
      BEATS.put("GHOST", "PSYCHIC GHOST");
      BEATS.put("DRAGON", "DRAGON");
      BEATS.put("DARK", "PSYCHIC GHOST");
      BEATS.put("STEEL", "ICE ROCK");
   }

   static int power(Game game, Npcs.Npc n) {
      int p = 0;
      if (n.data.team != null) {
         for (String m : n.data.team) {
            p += Integer.valueOf(m.split(":")[1]);
         }
      }
      return p;
   }

   /** -1..1: how well a's types hit b's, averaged over every pairing. */
   static float typeEdge(Npcs.Npc a, Npcs.Npc b) {
      float sum = 0;
      int n = 0;
      for (String ma : a.data.team) {
         for (String mb : b.data.team) {
            String[] ta = types(ma.split(":")[0]);
            String[] tb = types(mb.split(":")[0]);
            int s = 0;
            for (String x : ta) {
               for (String y : tb) {
                  if (BEATS.getOrDefault(x, "").contains(y)) {
                     s++;
                  }
                  if (BEATS.getOrDefault(y, "").contains(x)) {
                     s--;
                  }
               }
            }
            sum += Math.signum(s);
            n++;
         }
      }
      return n == 0 ? 0 : sum / n;
   }

   static float score(Game game, Npcs.Npc a, Npcs.Npc b, Random r) {
      return power(game, a) + 15 * typeEdge(a, b) + r.nextInt(41);
   }

   // ------------------------------------------------------------------ running one

   static Running running;

   static class Running {
      Cities.CityData town;
      Sched sched;
      ArrayList<Npcs.Npc> alive = new ArrayList<>();   // null = the player
      ArrayList<Object> ranking = new ArrayList<>();    // eliminated, worst first (Npc or null for player)
      HashMap<Object, Float> lastScore = new HashMap<>();
      int round = 1;
      boolean playerIn;
      Npcs.Npc playerOpp;          // waiting for this battle
      boolean battling = false;
      Boolean lastPlayerWon = null;
      ArrayList<String> log = new ArrayList<>();
      Random r = new Random();
   }

   static boolean playerHere(Game game, Cities.CityData c) {
      return game.map.tiles == game.map.overworldTiles && game.player.position.dst(c.x, c.y) <= Cities.RADIUS * 1.5F;
   }

   /** Called every frame by the NPC driver. */
   static void step(Game game) {
      Sched s = today(game);
      if (s == null && running == null) {
         return;
      }
      if (running == null) {
         Cities.CityData c = city(game, s.town);
         if (c == null) {
            return;
         }
         String period = Routines.period(game);
         // morning sign-up when you're in town
         if (period.equals("morning") && askedDay != game.player.daysPassed && playerHere(game, c) && game.playerCanMove
            && game.battle.drawAction == null && hasFighters(game)) {
            askedDay = game.player.daysPassed;
            game.playerCanMove = false;
            Action yes = new DisplayText.Clear(game, new RunCode(() -> {
               signedUp = true;
               game.insertAction(new DisplayText(game, Characters.T("You're signed up! Be at the plaza by noon."), "fanfare1.ogg", null,
                  new SetField(game, "playerCanMove", true, null)));
            }, null));
            Action no = new DisplayText.Clear(game, new SetField(game, "playerCanMove", true, null));
            game.insertAction(new DisplayText(game, Characters.T("The " + Cities.title(c) + " TOURNAMENT starts at noon. Sign up?"),
               null, true, false, new DrawYesNoMenu(null, yes, no)));
            return;
         }
         if ((devStartNow || !period.equals("morning")) && game.battle.drawAction == null && game.playerCanMove) {
            devStartNow = false;
            begin(game, s, c);
         }
         return;
      }
      advance(game);
   }

   static boolean hasFighters(Game game) {
      for (Pokemon p : game.player.pokemon) {
         if (!p.isEgg) {
            return true;
         }
      }
      return false;
   }

   static void begin(Game game, Sched s, Cities.CityData c) {
      Running run = new Running();
      run.town = c;
      run.sched = s;
      run.alive.addAll(entrants(game, c));
      state(game).scheduled.remove(s);
      state(game).lastHost = c.name;
      if (signedUp) {
         if (playerHere(game, c)) {
            run.playerIn = true;
            if (run.alive.size() >= SIZE) {
               run.alive.remove(run.alive.size() - 1);   // the weakest entrant makes room for you
            }
            run.alive.add(null);
         } else {
            Alerts.push(game, "You missed the " + Cities.title(c) + " TOURNAMENT! It went on without you.");
         }
      }
      signedUp = false;
      if (run.alive.size() < 2) {
         Npcs.Npc only = run.alive.isEmpty() ? null : run.alive.get(0);
         if (only != null && !League.isLeader(only) && leadersOf(game, c).size() < Cities.gymSlots(c)) {
            appoint(game, only, c);
            Alerts.push(game, "Nobody else showed up! " + only.fullName() + " becomes a GYM LEADER of " + Cities.title(c) + " unopposed.");
         } else {
            Alerts.push(game, "The " + Cities.title(c) + " TOURNAMENT was called off. Not enough trainers.");
         }
         return;
      }
      java.util.Collections.shuffle(run.alive, run.r);
      running = run;
      System.out.println("[tourney] " + c.name + " entrants: " + names(run.alive));
   }

   static String names(List<Npcs.Npc> l) {
      StringBuilder sb = new StringBuilder();
      for (Npcs.Npc n : l) {
         sb.append(n == null ? "YOU" : n.fullName()).append(", ");
      }
      return sb.toString();
   }

   static void advance(Game game) {
      Running run = running;
      if (run.battling) {
         if (run.lastPlayerWon == null || game.battle.drawAction != null || !game.playerCanMove) {
            return;   // battle (or its aftermath) still going
         }
         // the player's battle finished
         Npcs.Npc opp = run.playerOpp;
         if (run.lastPlayerWon) {
            run.alive.remove(opp);
            run.ranking.add(opp);
            run.lastScore.put(opp, (float)power(game, opp));
         } else {
            run.alive.remove(null);
            run.ranking.add(null);
            run.lastScore.put(null, 0F);
            run.playerIn = false;
         }
         run.battling = false;
         run.playerOpp = null;
         run.lastPlayerWon = null;
         if (run.alive.size() == 1 || run.alive.isEmpty()) {
            finish(game);
            return;
         }
         // rest of the round already simulated? (they were, before the player's match)
         run.round++;
         return;
      }
      if (game.battle.drawAction != null || !game.playerCanMove) {
         return;
      }
      if (run.alive.size() <= 1) {
         finish(game);
         return;
      }
      // play one round: simulate everything except the player's pair
      ArrayList<Npcs.Npc> next = new ArrayList<>();
      Npcs.Npc playerOpp = null;
      for (int i = 0; i < run.alive.size(); i += 2) {
         if (i + 1 >= run.alive.size()) {
            next.add(run.alive.get(i));   // bye
            continue;
         }
         Npcs.Npc a = run.alive.get(i);
         Npcs.Npc b = run.alive.get(i + 1);
         if (a == null || b == null) {
            playerOpp = a == null ? b : a;
            next.add(null);
            next.add(playerOpp);   // placeholder pair, resolved by the battle
            continue;
         }
         float sa = score(game, a, b, run.r);
         float sb = score(game, b, a, run.r);
         if (power(game, a) - power(game, b) >= 60) {
            sa = Float.MAX_VALUE;
         } else if (power(game, b) - power(game, a) >= 60) {
            sb = Float.MAX_VALUE;
         }
         Npcs.Npc win = sa >= sb ? a : b;
         Npcs.Npc lose = win == a ? b : a;
         next.add(win);
         run.ranking.add(lose);
         run.lastScore.put(lose, Math.min(sa, sb));
         run.log.add(win.fullName() + " beat " + lose.fullName());
      }
      run.alive = next;
      if (playerOpp != null) {
         startPlayerMatch(game, run, playerOpp);
         return;
      }
      run.round++;
      if (run.alive.size() <= 1) {
         finish(game);
      }
   }

   static void startPlayerMatch(Game game, Running run, Npcs.Npc opp) {
      run.battling = true;
      run.playerOpp = opp;
      run.lastPlayerWon = null;
      for (Pokemon p : game.player.pokemon) {
         p.currentStats.put("hp", p.maxStats.get("hp"));
         p.status = null;
      }
      String round = run.alive.size() <= 2 ? "FINAL" : run.alive.size() <= 4 ? "SEMIFINAL" : "ROUND " + run.round;
      game.playerCanMove = false;
      game.insertAction(new DisplayText(game, Characters.T(Cities.title(run.town) + " TOURNAMENT - " + round + "! You vs "
         + opp.fullName() + "! Your team was healed."), "fanfare1.ogg", null,
         new RunCode(() -> {
            TrainerBattle.start(game, opp);
            if (game.battle.trainer != null) {
               game.battle.trainer.tourney = true;
            } else {
               run.lastPlayerWon = true;   // they had no team: walkover
            }
         }, null)));
   }

   /** TrainerBattle hooks. */
   static void playerWon(Game game) {
      if (running != null && running.battling) {
         running.lastPlayerWon = true;
      }
   }

   static void playerLost(Game game) {
      if (running != null && running.battling) {
         running.lastPlayerWon = false;
      }
   }

   static void finish(Game game) {
      Running run = running;
      running = null;
      Cities.CityData c = run.town;
      boolean youWon = run.alive.size() == 1 && run.alive.get(0) == null;
      if (!run.alive.isEmpty()) {
         run.ranking.add(run.alive.get(0));
      }
      ArrayList<Object> order = new ArrayList<>(run.ranking);
      java.util.Collections.reverse(order);   // best first
      ArrayList<Npcs.Npc> finishers = new ArrayList<>();
      for (Object o : order) {
         if (o instanceof Npcs.Npc) {
            finishers.add((Npcs.Npc)o);
         }
      }
      String champ = youWon ? "You" : finishers.isEmpty() ? "Nobody" : finishers.get(0).fullName();
      c.lastWinner = champ;
      c.lastTourneyDay = game.player.daysPassed;
      Alerts.push(game, champ + " won the " + Cities.title(c) + " TOURNAMENT!");
      // gym slots
      int slots = Cities.gymSlots(c);
      ArrayList<Npcs.Npc> oldLeaders = leadersOf(game, c);
      ArrayList<Npcs.Npc> newLeaders = new ArrayList<>();
      for (Npcs.Npc n : finishers) {
         if (newLeaders.size() < slots) {
            newLeaders.add(n);
         }
      }
      for (Npcs.Npc n : oldLeaders) {
         if (!newLeaders.contains(n)) {
            n.data.role = null;
            if (!Trainers.isFolk(n.data.type)) {
               n.data.gymCity = null;
            }
            n.data.pendingLine = "I lost my GYM... Don't look at me like that.";
            Alerts.push(game, n.fullName() + " lost the " + Cities.title(c) + " GYM!");
         }
      }
      for (Npcs.Npc n : newLeaders) {
         boolean wasLeader = oldLeaders.contains(n);
         appoint(game, n, c);
         if (!wasLeader) {
            n.data.pendingLine = "I'm a GYM LEADER now! Come challenge me at noon in " + Cities.title(c) + ".";
         }
         Alerts.push(game, n.fullName() + (wasLeader ? " keeps " : " takes ") + "a " + Cities.title(c) + " GYM!");
      }
      c.succession.clear();
      for (Npcs.Npc n : finishers) {
         if (!newLeaders.contains(n)) {
            c.succession.add(key(n));
         }
      }
      if (champ.equals("You")) {
         game.player.unlocks.add("trophy:" + c.name + ":" + Calendar.year(game));
         game.player.addItem("rare candy", 3);
         game.player.addItem("nugget", 2);
         game.player.townRep.merge(c.name, 3, Integer::sum);
         game.playerCanMove = false;
         game.insertAction(new DisplayText(game, Characters.T("You won the " + Cities.title(c) + " TOURNAMENT! You got a TROPHY, 3 RARE CANDY and 2 NUGGETS."),
            "fanfare1.ogg", null, new SetField(game, "playerCanMove", true, null)));
      }
      System.out.println("[tourney] " + c.name + " done. Leaders " + names(newLeaders) + " line " + c.succession + " log " + run.log);
   }

   static String status(Game game) {
      StringBuilder sb = new StringBuilder();
      for (Sched s : state(game).scheduled) {
         Cities.CityData c = city(game, s.town);
         if (c == null) {
            continue;
         }
         int in = s.day - game.player.daysPassed;
         if (sb.length() > 0) {
            sb.append(" ");
         }
         sb.append("The ").append(Cities.title(c)).append(" TOURNAMENT is ").append(in == 0 ? "today" : in == 1 ? "tomorrow" : "in " + in + " days").append(".");
      }
      return sb.toString();
   }

   static String upper(String s) {
      return s.toUpperCase(Locale.ROOT);
   }
}
