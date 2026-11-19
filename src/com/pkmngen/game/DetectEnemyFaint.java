package com.pkmngen.game;

import java.util.Locale;

class DetectEnemyFaint extends Action {
   public Action.Layer layer = Action.Layer.gui_129;
   public boolean checkEndOfBattle = true;

   public DetectEnemyFaint(Action nextAction) {
      this(true, nextAction);
   }

   public DetectEnemyFaint(boolean checkEndOfBattle, Action nextAction) {
      super();
      this.checkEndOfBattle = checkEndOfBattle;
      this.nextAction = nextAction;
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
      game.actionStack.remove(this);
      if (game.battle.oppPokemon.currentStats.get("hp") <= 0) {
         Action nextAction = new EnemyFaint(
            game,
            new RemoveDisplayText(
               new DisplayText.Clear(
                  game,
                  new WaitFrames(
                     game, 3, new DisplayText(game, "Enemy " + game.battle.oppPokemon.nickname.toUpperCase(Locale.ROOT) + " fainted!", null, null, null)
                  )
               )
            )
         );
         if (game.battle.oppPokemon.isBoss) {
            nextAction.append(new DisplayText(game, Boss.onDefeated(game, game.battle.oppPokemon), "fanfare1.ogg", null, null));
         }
         String drop = Drops.roll(game, game.battle.oppPokemon);
         if (drop != null) {
            nextAction.append(new DisplayText(game, drop, "fanfare1.ogg", null, null));
         }

         if (this.checkEndOfBattle) {
            nextAction.append(new CheckEndOfBattle(null));
         } else {
            nextAction.append(this.nextAction);
         }

         game.insertAction(nextAction);
      } else {
         Action rage = Boss.checkRage(game, this.nextAction);
         game.insertAction(rage != null ? rage : this.nextAction);
      }
   }
}
