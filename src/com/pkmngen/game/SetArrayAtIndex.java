package com.pkmngen.game;

class SetArrayAtIndex extends Action {
   Object[] object;
   int index;
   Object setTo;

   public SetArrayAtIndex(Object[] object, int index, Object setTo, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.object = object;
      this.index = index;
      this.setTo = setTo;
   }

   @Override
   public void step(Game game) {
      if (this.object != null) {
         try {
            this.object[this.index] = this.setTo;
         } catch (IllegalArgumentException var3) {
            var3.printStackTrace();
         } catch (SecurityException var4) {
            var4.printStackTrace();
         }
      }

      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
