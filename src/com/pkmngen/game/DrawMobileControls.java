package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;

/**
 * On-screen Game Boy controls for Android (portrait): D-pad bottom left, A and B bottom right,
 * START and SELECT in the middle, L and R above them. Every finger counts, so you can hold a
 * direction and press A at the same time. The phone's Back button also works as B.
 * SELECT opens the full map.
 *
 * The sprites' rectangles are what InputProcessor tests against (in UI coordinates).
 */
class DrawMobileControls extends Action {
   public static Sprite upArrowSprite = new Sprite();
   public static Sprite downArrowSprite = new Sprite();
   public static Sprite leftArrowSprite = new Sprite();
   public static Sprite rightArrowSprite = new Sprite();
   public static Sprite aSprite = new Sprite();
   public static Sprite bSprite = new Sprite();
   public static Sprite startSprite = new Sprite();
   public static Sprite selectSprite = new Sprite();
   public static Sprite lSprite = new Sprite();
   public static Sprite rSprite = new Sprite();

   static Texture circle;
   static Texture pill;
   static Texture square;
   static final Vector2 tmp = new Vector2();
   static boolean selectWasDown = false;

   public DrawMobileControls(Game game) {
      super();
      circle = makeCircle(26);
      pill = makePill(30, 12);
      square = makeSquare(22);
      Texture arrow = TextureCache.get(Gdx.files.internal("gb_arrow2.png"));
      upArrowSprite = new Sprite(arrow, 0, 0, 24, 24);
      downArrowSprite = new Sprite(arrow, 0, 0, 24, 24);
      downArrowSprite.flip(false, true);
      rightArrowSprite = new Sprite(arrow, 0, 0, 24, 24);
      rightArrowSprite.rotate90(true);
      leftArrowSprite = new Sprite(arrow, 0, 0, 24, 24);
      leftArrowSprite.rotate90(false);
      aSprite = new Sprite(circle);
      bSprite = new Sprite(circle);
      startSprite = new Sprite(pill);
      selectSprite = new Sprite(pill);
      lSprite = new Sprite(pill);
      rSprite = new Sprite(pill);
      layout(game);
   }

   static Texture plainTex;

   static Texture plain() {
      if (plainTex == null) {
         Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
         p.setColor(Color.WHITE);
         p.fill();
         plainTex = new Texture(p);
         p.dispose();
      }
      return plainTex;
   }

   static Texture makeCircle(int d) {
      Pixmap p = new Pixmap(d, d, Pixmap.Format.RGBA8888);
      p.setColor(0.15F, 0.15F, 0.2F, 1);
      p.fillCircle(d / 2, d / 2, d / 2 - 1);
      p.setColor(0.35F, 0.35F, 0.45F, 1);
      p.fillCircle(d / 2, d / 2, d / 2 - 3);
      Texture t = new Texture(p);
      p.dispose();
      return t;
   }

   static Texture makePill(int w, int h) {
      Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
      p.setColor(0.15F, 0.15F, 0.2F, 1);
      p.fillCircle(h / 2, h / 2, h / 2 - 1);
      p.fillCircle(w - h / 2 - 1, h / 2, h / 2 - 1);
      p.fillRectangle(h / 2, 1, w - h, h - 2);
      Texture t = new Texture(p);
      p.dispose();
      return t;
   }

   static Texture makeSquare(int s) {
      Pixmap p = new Pixmap(s, s, Pixmap.Format.RGBA8888);
      p.setColor(0.15F, 0.15F, 0.2F, 1);
      p.fillRectangle(0, 0, s, s);
      p.setColor(0.3F, 0.3F, 0.38F, 1);
      p.fillRectangle(2, 2, s - 4, s - 4);
      Texture t = new Texture(p);
      p.dispose();
      return t;
   }

   /** Bottom band below the game screen (UI coordinates, y < 0). */
   static float bandHeight(Game game) {
      float menuHeight = 160.0F * game.currScreen.y / Math.max(1, game.currScreen.x);
      return Math.max(60, (menuHeight - 144.0F) / 2.0F);
   }

   static void layout(Game game) {
      float band = bandHeight(game);
      float mid = -band / 2.0F - 4;
      // D-pad
      float cx = 34;
      upArrowSprite.setBounds(cx - 12, mid + 10, 24, 24);
      downArrowSprite.setBounds(cx - 12, mid - 34, 24, 24);
      leftArrowSprite.setBounds(cx - 34, mid - 12, 24, 24);
      rightArrowSprite.setBounds(cx + 10, mid - 12, 24, 24);
      // A / B
      aSprite.setBounds(126, mid + 2, 28, 28);
      bSprite.setBounds(96, mid - 14, 28, 28);
      // START / SELECT, L / R
      selectSprite.setBounds(48, -band + 4, 30, 12);
      startSprite.setBounds(82, -band + 4, 30, 12);
      lSprite.setBounds(4, -14, 30, 12);
      rSprite.setBounds(126, -14, 30, 12);
   }

   /** Is any finger on this button? (Also the mouse on PC, for testing.) */
   static boolean touched(Sprite s) {
      Game game = Game.staticGame;
      if (!Platform.touchUi() || game == null || game.currScreen == null) {
         return false;
      }
      Rectangle r = s.getBoundingRectangle();
      // a little slack around each button
      Rectangle hit = new Rectangle(r.x - 3, r.y - 3, r.width + 6, r.height + 6);
      float scale = 160.0F / game.currScreen.x;
      int offsetY = (int)((game.currScreen.y - 144.0F / scale) / 2.0F * scale);
      for (int i = 0; i < 10; i++) {
         if (!Gdx.input.isTouched(i)) {
            continue;
         }
         tmp.set(Gdx.input.getX(i) * scale, (game.currScreen.y - Gdx.input.getY(i)) * scale - offsetY);
         if (hit.contains(tmp)) {
            return true;
         }
      }
      return false;
   }

   static boolean backPressed() {
      return Platform.android() && Gdx.input.isKeyPressed(Input.Keys.BACK);
   }

   @Override
   public void firstStep(Game game) {
      Gdx.input.setCatchKey(Input.Keys.BACK, true);
      layout(game);
   }

   void button(Game game, Sprite s, String label) {
      boolean down = touched(s);
      s.setColor(down ? new Color(0.75F, 0.75F, 0.9F, 0.95F) : new Color(1, 1, 1, 0.8F));
      s.draw(game.uiBatch);
      if (label != null) {
         float x = s.getX() + s.getWidth() / 2 - label.length() * 4;
         float y = s.getY() + s.getHeight() / 2 - 4;
         for (int i = 0; i < label.length(); i++) {
            Object glyph = game.textDict.get(label.charAt(i));
            if (glyph != null) {
               game.uiBatch.draw((com.pkmngen.game.util.SpriteProxy)glyph, x + i * 8, y);
            }
         }
      }
   }

   @Override
   public void step(Game game) {
      layout(game);
      Color saved = new Color(game.uiBatch.getColor());
      // Game Boy body: solid panels above and below the 160x144 screen
      float band = bandHeight(game) + 4;
      game.uiBatch.setColor(0.12F, 0.12F, 0.16F, 1);
      game.uiBatch.draw(plain(), -20, -band - 40, 200, band + 40);
      game.uiBatch.draw(plain(), -20, 144, 200, band + 40);
      for (Sprite pad : new Sprite[]{upArrowSprite, downArrowSprite, leftArrowSprite, rightArrowSprite}) {
         game.uiBatch.setColor(1, 1, 1, 0.8F);
         game.uiBatch.draw(square, pad.getX() + 1, pad.getY() + 1, 22, 22);
         button(game, pad, null);
      }
      game.uiBatch.setColor(1, 1, 1, 0.8F);
      game.uiBatch.draw(square, 34 - 11, upArrowSprite.getY() - 22 + 1, 22, 22);   // D-pad center
      button(game, aSprite, "A");
      button(game, bSprite, "B");
      button(game, startSprite, null);
      button(game, selectSprite, null);
      button(game, lSprite, "L");
      button(game, rSprite, "R");
      game.uiBatch.setColor(saved);
      // SELECT: full map (only when you could walk)
      boolean sel = touched(selectSprite);
      if (sel && !selectWasDown && game.playerCanMove && game.map != null && game.map.tiles == game.map.overworldTiles
         && (game.battle == null || game.battle.drawAction == null)) {
         game.playerCanMove = false;
         DrawMiniMap full = new DrawMiniMap(game, (Menu)null);
         full.fromKey = true;
         game.insertAction(new DrawMiniMap.Intro(null, 9, full));
      }
      selectWasDown = sel;
   }

   @Override
   public String getCamera() {
      return "gui";
   }

   @Override
   public Action.Layer getLayer() {
      return Action.Layer.gui_0;
   }
}
