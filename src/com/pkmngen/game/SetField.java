package com.pkmngen.game;

class SetField extends Action {
   Object object;
   String field;
   Object setTo;

   public SetField(Object object, String field, Object setTo, Action nextAction) {
      super();
      this.nextAction = nextAction;
      this.object = object;
      this.field = field;
      this.setTo = setTo;
   }

   @Override
   public void step(Game game) {
      if (this.object != null) {
         try {
            this.object.getClass().getField(this.field).set(this.object, this.setTo);
         } catch (IllegalArgumentException var3) {
            var3.printStackTrace();
         } catch (IllegalAccessException var4) {
            var4.printStackTrace();
         } catch (NoSuchFieldException var5) {
            var5.printStackTrace();
         } catch (SecurityException var6) {
            var6.printStackTrace();
         }
      }

      game.actionStack.remove(this);
      game.insertAction(this.nextAction);
   }
}
