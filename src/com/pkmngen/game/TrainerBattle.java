package com.pkmngen.game;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Trainer battles against villagers (their real, growing teams).
 *
 * The battle engine only ever handled one opposing Pokemon, and it queues both sides' attacks
 * up front, so swapping the opponent mid-turn is unsafe. Instead a trainer battle is a chain of
 * rounds: when the opponent's Pokemon faints and they have more, the round closes (exp and
 * evolutions happen as usual) and the next round opens straight away with "X sent out Y!".
 * You can't run, and balls are refused without being used.
 */
class TrainerBattle {
   Npcs.Npc npc;
   String name;
   ArrayList<Pokemon> team = new ArrayList<>();
   int index = 0;
   boolean firstRound = true;

   TrainerBattle(Npcs.Npc npc) {
      this.npc = npc;
      this.name = npc.fullName();
      for (String member : npc.data.team) {
         String[] p = member.split(":");
         try {
            this.team.add(new Pokemon(p[0], Integer.valueOf(p[1]), Pokemon.Generation.CRYSTAL));
         } catch (Exception e) {
            System.out.println("Couldn't build " + member + " for " + this.name);
         }
      }
   }

   static boolean canBattle(Npcs.Npc npc) {
      return npc.data.team != null && !npc.data.team.isEmpty();
   }

   static void start(Game game, Npcs.Npc npc) {
      Characters.daily(game, npc, false); // repairs unsupported team members
      TrainerBattle tb = new TrainerBattle(npc);
      if (tb.team.isEmpty()) {
         game.playerCanMove = true;
         return;
      }
      npc.data.lastBattleDay = game.player.daysPassed;
      game.battle.trainer = tb;
      game.playerCanMove = false;
      game.musicController.startBattle = "wild";
      game.battle.oppPokemon = tb.team.get(0);
      game.player.setCurrPokemon();
      game.insertAction(Battle.getIntroAction(game));
   }

   String introText(Pokemon opp) {
      String sent = this.name + " sent out " + opp.nickname.toUpperCase(Locale.ROOT) + "!";
      if (this.firstRound) {
         this.firstRound = false;
         return this.name + " wants to battle! " + sent;
      }
      return sent;
   }

   boolean hasNext() {
      for (int i = this.index + 1; i < this.team.size(); i++) {
         if (this.team.get(i).currentStats.get("hp") > 0) {
            return true;
         }
      }
      return false;
   }

   void nextRound(Game game) {
      System.out.println("[trainer] next round " + (this.index + 1));
      do {
         this.index++;
      } while (this.index < this.team.size() && this.team.get(this.index).currentStats.get("hp") <= 0);
      game.battle.oppPokemon = this.team.get(this.index);
      game.player.setCurrPokemon();
      game.insertAction(Battle.getIntroAction(game));
   }

   /** Appended after the last opposing Pokemon faints. */
   Action victory(Game game) {
      System.out.println("[trainer] victory vs " + this.name);
      Npcs.NpcData d = this.npc.data;
      d.wins++;
      d.friendship = Math.min(10, d.friendship + 1);
      if ("emma".equals(d.type) || "wood".equals(d.type)) {
         // they respect strength
         d.romance = Math.min(10, d.romance + ("partner".equals(d.relation) ? 0 : 1));
      }
      Characters.Def def = Characters.DEFS.get(d.type);
      String line = def != null && def.winLine != null ? def.name + " - " + def.winLine : this.name + " - You win!";
      return new DisplayText(game, Characters.T("You defeated " + this.name + "!"), "fanfare1.ogg", null,
         new DisplayText(game, Characters.T(line), null, null, new RunCode(() -> game.battle.trainer = null, null)));
   }

   /** Called when the player runs out of Pokemon. */
   void defeat(Game game) {
      System.out.println("[trainer] defeat vs " + this.name);
      Npcs.NpcData d = this.npc.data;
      d.losses++;
      Characters.Def def = Characters.DEFS.get(d.type);
      if (def != null && def.loseLine != null) {
         d.pendingLine = def.loseLine;
      }
      game.battle.trainer = null;
   }
}
