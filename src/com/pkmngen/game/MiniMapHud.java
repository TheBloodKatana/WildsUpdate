package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;

/**
 * Corner minimap (N toggles it) built from the game's own explored-map picture, plus pins:
 * towns (red), cave entrances you've uncovered (brown), a legendary event (purple), your bed
 * (blue) and you (blinking white). M opens the full map, which shows the same pins and town names.
 */
class MiniMapHud extends Action {
   public Action.Layer layer = Action.Layer.gui_130;
   static MiniMapHud instance;
   static boolean visible = true;

   static final int SIZE = 44;
   Texture mapTexture;
   int refresh = 0;
   Texture white;
   Texture dark;
   int blink = 0;

   static void ensure(Game game) {
      if (instance == null || !game.actionStack.contains(instance)) {
         instance = new MiniMapHud();
         game.insertAction(instance);
      }
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   /** Overworld world position -> pixel in game.map.minimap (row 0 = top). */
   static int px(Game game, float x) {
      return (int)(x - game.map.bottomLeft.x) / 8;
   }

   static int py(Game game, float y) {
      return (int)(game.map.topRight.y - y) / 8;
   }

   @Override
   public void step(Game game) {
      if (game.map == null || game.player == null || game.map.minimap == null) {
         return;
      }
      if (this.white == null) {
         this.white = TextureCache.get(Gdx.files.internal("npc/px_white.png"));
         this.dark = TextureCache.get(Gdx.files.internal("npc/px_dark.png"));
      }
      boolean overworld = game.map.tiles == game.map.overworldTiles;
      // keys work wherever you can walk
      if (game.playerCanMove && overworld && (game.battle == null || game.battle.drawAction == null)) {
         if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            visible = !visible;
         }
         if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.playerCanMove = false;
            DrawMiniMap full = new DrawMiniMap(game, (Menu)null);
            full.fromKey = true;
            game.insertAction(new DrawMiniMap.Intro(null, 9, full));
            return;
         }
      }
      if (!visible || !overworld || !game.playerCanMove || (game.battle != null && game.battle.drawAction != null)) {
         return;
      }
      if (this.mapTexture == null || this.refresh-- <= 0) {
         if (this.mapTexture != null) {
            this.mapTexture.dispose();
         }
         this.mapTexture = new Texture(game.map.minimap);
         this.refresh = 90;
      }
      int cx = px(game, game.player.position.x);
      int cy = py(game, game.player.position.y);
      float x0 = 160 - SIZE - 3;
      float y0 = 144 - SIZE - 3;
      game.uiBatch.draw(this.dark, x0 - 1, y0 - 1, SIZE + 2, SIZE + 2);
      game.uiBatch.draw(this.mapTexture, x0, y0, SIZE, SIZE, cx - SIZE / 2, cy - SIZE / 2, SIZE, SIZE, false, false);
      Color saved = new Color(game.uiBatch.getColor());
      // pins
      for (Cities.CityData c : Cities.list(game)) {
         pin(game, x0, y0, cx, cy, c.x, c.y, new Color(0.9F, 0.15F, 0.15F, 1), 3);
      }
      String prefix = CaveGen.prefix(game);
      for (String k : game.player.caveEntrances.keySet()) {
         if (k.endsWith("placed") || !k.startsWith(prefix) || (prefix.isEmpty() && k.contains("|"))) {
            continue;
         }
         String[] xy = k.substring(prefix.length()).split(",");
         try {
            float wx = Float.valueOf(xy[0]);
            float wy = Float.valueOf(xy[1]);
            if (explored(game, wx, wy)) {
               pin(game, x0, y0, cx, cy, wx, wy, new Color(0.55F, 0.35F, 0.15F, 1), 2);
            }
         } catch (Exception ignored) {
         }
      }
      Calendar.Event e = Calendar.activeEvent(game);
      if (e != null) {
         pin(game, x0, y0, cx, cy, e.x, e.y, new Color(0.6F, 0.2F, 0.9F, 1), 3);
      }
      pin(game, x0, y0, cx, cy, game.player.spawnLoc.x, game.player.spawnLoc.y, new Color(0.2F, 0.4F, 1.0F, 1), 2);
      if (this.blink++ % 40 < 28) {
         game.uiBatch.setColor(Color.WHITE);
         game.uiBatch.draw(this.dark, x0 + SIZE / 2 - 2, y0 + SIZE / 2 - 2, 4, 4);
         game.uiBatch.draw(this.white, x0 + SIZE / 2 - 1, y0 + SIZE / 2 - 1, 2, 2);
      }
      game.uiBatch.setColor(saved);
   }

   static boolean explored(Game game, float wx, float wy) {
      int x = px(game, wx);
      int y = py(game, wy);
      if (x < 0 || y < 0 || x >= game.map.minimap.getWidth() || y >= game.map.minimap.getHeight()) {
         return false;
      }
      int rgba = game.map.minimap.getPixel(x, y);
      return (rgba & 0xFFFFFF00) != 0;
   }

   void pin(Game game, float x0, float y0, int cx, int cy, float wx, float wy, Color color, int size) {
      int dx = px(game, wx) - cx;
      int dy = cy - py(game, wy);
      if (Math.abs(dx) > SIZE / 2 - 2 || Math.abs(dy) > SIZE / 2 - 2) {
         return;
      }
      game.uiBatch.setColor(color);
      game.uiBatch.draw(this.white, x0 + SIZE / 2 + dx - size / 2, y0 + SIZE / 2 + dy - size / 2, size, size);
   }
}
