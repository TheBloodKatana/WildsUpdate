package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Banner alerts at the top of the screen: "A BUG CATCHER is wandering near the forest",
 * "Winter starts in 3 days", "Rain is coming", tournament announcements... They queue up,
 * slide in, stay a few seconds and slide out. Held back during battles and while text is up.
 */
class Alerts {
   static final ArrayDeque<String> queue = new ArrayDeque<>();
   static final ArrayList<String> history = new ArrayList<>();   // last alerts, newest last
   static Driver driver;
   /** False while a world is loading, so re-spawning saved things doesn't spam alerts. */
   static boolean ready = false;

   static final String[][] BIOMES = {
      {"deep_forest", "the DEEP FOREST"}, {"forest", "the FOREST"}, {"beach", "the BEACH"}, {"desert", "the DESERT"},
      {"oasis", "the OASIS"}, {"graveyard", "the GRAVEYARD"}, {"mountain", "the MOUNTAINS"}, {"ocean", "the SEA"},
      {"savanna", "the SAVANNA"}, {"snow", "the TUNDRA"}, {"volcano", "the VOLCANO"}, {"wooded_lake", "the WOODED LAKE"},
      {"ruins", "the RUINS"}, {"sand_pit", "the SAND PIT"}, {"cave", "a CAVE"},
   };

   /** "the FOREST", "CEDAR VILLAGE", ... for a spot on the overworld. */
   static String where(Game game, com.badlogic.gdx.math.Vector2 pos) {
      Cities.CityData c = Cities.cityAt(game, pos);
      if (c != null) {
         return Cities.title(c);
      }
      Tile t = game.map.overworldTiles.get(pos);
      if (t != null && t.routeBelongsTo != null && t.routeBelongsTo.name != null) {
         for (String[] b : BIOMES) {
            if (t.routeBelongsTo.name.startsWith(b[0])) {
               return b[1];
            }
         }
      }
      return "the wilds";
   }

   static void push(Game game, String text) {
      if (text == null || text.isEmpty()) {
         return;
      }
      String t = Characters.T(text);
      if (queue.contains(t)) {
         return;
      }
      queue.add(t);
      history.add(t);
      while (history.size() > 20) {
         history.remove(0);
      }
      System.out.println("[alert] " + text);
      if (driver == null || !game.actionStack.contains(driver)) {
         driver = new Driver();
         game.insertAction(driver);
      }
   }

   /** Word-wraps to lines of at most width characters. */
   static ArrayList<String> wrap(String text, int width) {
      ArrayList<String> lines = new ArrayList<>();
      StringBuilder line = new StringBuilder();
      for (String word : text.split(" ")) {
         if (line.length() > 0 && line.length() + 1 + word.length() > width) {
            lines.add(line.toString());
            line = new StringBuilder();
         }
         if (line.length() > 0) {
            line.append(' ');
         }
         line.append(word);
      }
      if (line.length() > 0) {
         lines.add(line.toString());
      }
      return lines;
   }

   static class Driver extends Action {
      public Action.Layer layer = Action.Layer.gui_120;
      String current;
      ArrayList<String> lines;
      int timer = 0;
      Texture dark;
      Texture white;

      static final int SHOW = 360;
      static final int SLIDE = 12;

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      boolean blocked(Game game) {
         return game.battle != null && game.battle.drawAction != null;
      }

      @Override
      public void step(Game game) {
         if (this.dark == null) {
            this.dark = TextureCache.get(Gdx.files.internal("npc/px_dark.png"));
            this.white = TextureCache.get(Gdx.files.internal("npc/px_white.png"));
         }
         if (this.current == null) {
            if (queue.isEmpty() || blocked(game)) {
               return;
            }
            this.current = queue.poll();
            this.lines = wrap(this.current, 18);
            while (this.lines.size() > 4) {
               this.lines.remove(this.lines.size() - 1);
            }
            this.timer = SHOW;
         }
         int h = this.lines.size() * 8 + 8;
         // slide down from above the screen, then back up
         int elapsed = SHOW - this.timer;
         float offset = 0;
         if (elapsed < SLIDE) {
            offset = h * (1 - elapsed / (float)SLIDE);
         } else if (this.timer < SLIDE) {
            offset = h * (1 - this.timer / (float)SLIDE);
         }
         float top = 144 + offset;
         float y0 = top - h;
         game.uiBatch.draw(this.dark, 0, y0, 160, h);
         game.uiBatch.draw(this.white, 1, y0 + 1, 158, h - 2);
         for (int i = 0; i < this.lines.size(); i++) {
            char[] cs = this.lines.get(i).toCharArray();
            for (int j = 0; j < cs.length; j++) {
               Object glyph = game.textDict.get(cs[j]);
               if (glyph == null) {
                  glyph = game.textDict.get(' ');
               }
               if (glyph != null) {
                  game.uiBatch.draw((com.pkmngen.game.util.SpriteProxy)glyph, 8 + j * 8, top - 12 - i * 8);
               }
            }
         }
         if (--this.timer <= 0) {
            this.current = null;
         }
      }
   }
}
