package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Pokedex screen. List view shows every species in the game in national order
 * (unseen ones as "-----"), with a ball marker for caught ones. A opens a detail
 * page (sprite, number, types) for seen species. B goes back.
 */
class DrawPokedex extends Menu {
   static class Entry {
      int number;
      String displayName;
      String id;
      String spritePath;
   }

   static ArrayList<Entry> entries;
   static final int VISIBLE = 7;

   public Action.Layer layer = Action.Layer.gui_104;
   Menu prevMenu;
   Sprite bg;
   Sprite ball;
   Sprite arrow;
   int cursor = 0;
   int scroll = 0;
   boolean detail = false;
   HashMap<String, Sprite> frontSprites = new HashMap<>();
   HashMap<String, String> typeText = new HashMap<>();
   int holdTimer = 0;

   public DrawPokedex(Menu prevMenu) {
      this.prevMenu = prevMenu;
      this.bg = new Sprite(TextureCache.get(Gdx.files.internal("menu/pokedex_bg1.png")), 0, 0, 160, 144);
      this.ball = new Sprite(TextureCache.get(Gdx.files.internal("menu/pokedex_ball1.png")), 0, 0, 8, 8);
      this.arrow = new Sprite(TextureCache.get(Gdx.files.internal("battle/arrow_right1.png")), 0, 0, 5, 7);
      loadEntries();
      Game.staticGame.player.syncDexWithOwnedPokemon(Game.staticGame);
   }

   static void loadEntries() {
      if (entries != null) {
         return;
      }
      entries = new ArrayList<>();
      ArrayList<String> lines;
      try {
         lines = Game.readLines(Gdx.files.internal("pokemon/pokedex.txt"));
      } catch (java.io.IOException ex) {
         lines = new ArrayList<>();
      }
      for (String line : lines) {
         if (line.startsWith("#") || line.trim().isEmpty()) {
            continue;
         }
         String[] parts = line.trim().split("\\|");
         Entry e = new Entry();
         e.number = Integer.valueOf(parts[0]);
         e.displayName = parts[1];
         e.id = parts[2];
         e.spritePath = "pokemon/" + parts[3];
         entries.add(e);
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

   void text(Game game, String s, float x, float top) {
      for (int i = 0; i < s.length(); i++) {
         com.pkmngen.game.util.SpriteProxy glyph = game.textDict.get(s.charAt(i));
         if (glyph == null) {
            glyph = game.textDict.get(' ');
         }
         game.uiBatch.draw(glyph, x + 8 * i, 144 - top - 8);
      }
   }

   static String pad3(int n) {
      String s = String.valueOf(n);
      while (s.length() < 3) {
         s = "0" + s;
      }
      return s;
   }

   Sprite frontSprite(Entry e) {
      if (!this.frontSprites.containsKey(e.id)) {
         Sprite sprite = null;
         FileHandle file = Gdx.files.internal(e.spritePath);
         if (file.exists()) {
            Texture tex = TextureCache.get(file);
            int w = tex.getWidth();
            sprite = new Sprite(tex, 0, 0, w, w);
         }
         this.frontSprites.put(e.id, sprite);
      }
      return this.frontSprites.get(e.id);
   }

   String types(Entry e) {
      if (!this.typeText.containsKey(e.id)) {
         String t = "";
         try {
            if (!Specie.species.containsKey(e.id)) {
               Specie.species.put(e.id, new Specie(e.id));
            }
            Specie specie = Specie.species.get(e.id);
            if (specie.types != null) {
               t = String.join("/", specie.types).toUpperCase(Locale.ROOT);
            }
         } catch (Exception ex) {
            t = "";
         }
         this.typeText.put(e.id, t);
      }
      return this.typeText.get(e.id);
   }

   @Override
   public void step(Game game) {
      Player player = game.player;
      game.uiBatch.draw(this.bg, 0, 0);
      this.text(game, "POKéDEX", 8, 5);

      if (!this.detail) {
         for (int row = 0; row < VISIBLE; row++) {
            int index = this.scroll + row;
            if (index >= entries.size()) {
               break;
            }
            Entry e = entries.get(index);
            float top = 24 + 14 * row;
            this.text(game, pad3(e.number), 16, top);
            if (player.dexCaught.contains(e.id)) {
               game.uiBatch.draw(this.ball, 44, 144 - top - 8);
            }
            this.text(game, player.dexSeen.contains(e.id) ? e.displayName : "-----", 56, top);
            if (index == this.cursor) {
               game.uiBatch.draw(this.arrow, 7, 144 - top - 8);
            }
         }
      } else {
         Entry e = entries.get(this.cursor);
         Sprite front = this.frontSprite(e);
         if (front != null) {
            float size = front.getRegionWidth();
            game.uiBatch.draw(front, 12 + (56 - size) / 2, 144 - 24 - 56 + (56 - size));
         }
         this.text(game, "No." + pad3(e.number), 76, 28);
         this.text(game, player.dexCaught.contains(e.id) ? "CAUGHT" : "SEEN", 76, 44);
         String[] t = this.types(e).split("/");
         for (int i = 0; i < t.length && i < 2; i++) {
            this.text(game, t[i], 76, 62 + 12 * i);
         }
         this.text(game, e.displayName, 12, 92);
      }

      this.text(game, "SEEN " + pad3(player.dexSeen.size()) + " OWN " + pad3(player.dexCaught.size()), 8, 124);

      if (this.disabled) {
         return;
      }

      if (this.detail) {
         if (InputProcessor.aJustPressed || InputProcessor.bJustPressed) {
            this.detail = false;
         } else if (InputProcessor.upJustPressed || InputProcessor.downJustPressed) {
            // flip to the previous / next seen species
            int dir = InputProcessor.upJustPressed ? -1 : 1;
            for (int i = this.cursor + dir; i >= 0 && i < entries.size(); i += dir) {
               if (player.dexSeen.contains(entries.get(i).id)) {
                  this.cursor = i;
                  break;
               }
            }
            this.keepCursorVisible();
         }
         return;
      }

      int move = 0;
      if (InputProcessor.upJustPressed) {
         move = -1;
      } else if (InputProcessor.downJustPressed) {
         move = 1;
      } else if (InputProcessor.leftJustPressed) {
         move = -VISIBLE;
      } else if (InputProcessor.rightJustPressed) {
         move = VISIBLE;
      } else if (InputProcessor.upPressed || InputProcessor.downPressed) {
         // hold to scroll quickly
         this.holdTimer++;
         if (this.holdTimer > 20 && this.holdTimer % 3 == 0) {
            move = InputProcessor.upPressed ? -1 : 1;
         }
      } else {
         this.holdTimer = 0;
      }
      if (move != 0) {
         this.cursor = Math.max(0, Math.min(entries.size() - 1, this.cursor + move));
         this.keepCursorVisible();
      }

      if (InputProcessor.aJustPressed) {
         if (player.dexSeen.contains(entries.get(this.cursor).id)) {
            this.detail = true;
         } else {
            game.insertAction(new PlayMusic("error1", null));
         }
      } else if (InputProcessor.bJustPressed) {
         game.actionStack.remove(this);
         if (this.prevMenu != null) {
            this.prevMenu.disabled = false;
            game.insertAction(this.prevMenu);
         } else {
            game.playerCanMove = true;
         }
      }
   }

   void keepCursorVisible() {
      if (this.cursor < this.scroll) {
         this.scroll = this.cursor;
      } else if (this.cursor >= this.scroll + VISIBLE) {
         this.scroll = this.cursor - VISIBLE + 1;
      }
   }
}
