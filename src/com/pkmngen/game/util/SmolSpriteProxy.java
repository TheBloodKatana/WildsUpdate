package com.pkmngen.game.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;

public class SmolSpriteProxy extends SmolSprite {
   public static boolean inverseColors = false;
   public boolean darkenColors1 = false;
   public boolean darkenColors2 = false;
   public boolean darkenColors3 = false;
   public static boolean darkenAllColors1 = false;
   public static boolean darkenAllColors2 = false;
   public static boolean darkenAllColors3 = false;
   public static boolean confuseRayColors1 = false;
   public static boolean confuseRayColors2 = false;
   public boolean lightenColors1 = false;
   public boolean lightenColors2 = false;
   public static boolean lightenAllColors1 = false;
   public static boolean lightenAllColors2 = false;
   public Color color1 = null;
   public Color color2 = new Color();
   public Color black = Color.BLACK;
   Texture originalTexture;
   Texture inverseTexture;
   Texture darkenTexture1;
   Texture darkenTexture2;
   Texture darkenTexture3;
   Texture lightenTexture1;
   Texture lightenTexture2;
   Texture confuseRayTexture1;
   Texture confuseRayTexture2;

   @Override
   public void draw(Batch batch) {
      Texture texture = this.getTexture();
      if (SpriteProxy.inverseColors) {
         this.setTexture(this.inverseTexture);
         texture = this.inverseTexture;
      } else if (!SpriteProxy.inverseColors && texture == this.inverseTexture) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.darkenAllColors1 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture1);
         texture = this.darkenTexture1;
      } else if (!SpriteProxy.darkenAllColors1 && texture == this.darkenTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.darkenAllColors2 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture2);
         texture = this.darkenTexture2;
      } else if (!SpriteProxy.darkenAllColors2 && texture == this.darkenTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.darkenAllColors3 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture3);
         texture = this.darkenTexture3;
      } else if (!SpriteProxy.darkenAllColors3 && texture == this.darkenTexture3) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.lightenAllColors1 && texture == this.originalTexture) {
         this.setTexture(this.lightenTexture1);
         texture = this.lightenTexture1;
      } else if (!SpriteProxy.lightenAllColors1 && texture == this.lightenTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.lightenAllColors2 && texture == this.originalTexture) {
         this.setTexture(this.lightenTexture2);
         texture = this.lightenTexture2;
      } else if (!SpriteProxy.lightenAllColors2 && texture == this.lightenTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.lightenColors1 && texture == this.originalTexture) {
         this.setTexture(this.lightenTexture1);
         texture = this.lightenTexture1;
      } else if (!this.lightenColors1 && !SpriteProxy.lightenAllColors1 && texture == this.lightenTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.lightenColors2 && texture == this.originalTexture) {
         this.setTexture(this.lightenTexture2);
         texture = this.lightenTexture2;
      } else if (!this.lightenColors2 && !SpriteProxy.lightenAllColors2 && texture == this.lightenTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.confuseRayColors1 && texture == this.originalTexture) {
         this.setTexture(this.confuseRayTexture1);
         texture = this.confuseRayTexture1;
      } else if (!SpriteProxy.confuseRayColors1 && texture == this.confuseRayTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (SpriteProxy.confuseRayColors2 && texture == this.originalTexture) {
         this.setTexture(this.confuseRayTexture2);
         texture = this.confuseRayTexture2;
      } else if (!SpriteProxy.confuseRayColors2 && texture == this.confuseRayTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.darkenColors1 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture1);
         texture = this.darkenTexture1;
      } else if (!this.lightenColors1 && !SpriteProxy.darkenAllColors1 && texture == this.darkenTexture1) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.darkenColors2 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture2);
         texture = this.darkenTexture2;
      } else if (!this.darkenColors2 && !SpriteProxy.darkenAllColors2 && texture == this.darkenTexture2) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      if (this.darkenColors3 && texture == this.originalTexture) {
         this.setTexture(this.darkenTexture3);
         texture = this.darkenTexture3;
      } else if (!this.darkenColors3 && !SpriteProxy.darkenAllColors3 && texture == this.darkenTexture3) {
         this.setTexture(this.originalTexture);
         texture = this.originalTexture;
      }

      super.draw(batch);
   }

   public SmolSpriteProxy(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      this(null, texture, srcX, srcY, srcWidth, srcHeight);
   }

   public SmolSpriteProxy(Color color1, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      this(color1, new Color(), texture, srcX, srcY, srcWidth, srcHeight);
   }

   public SmolSpriteProxy(Color color1, Color color2, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      this(Color.BLACK, color1, new Color(), texture, srcX, srcY, srcWidth, srcHeight);
   }

   public SmolSpriteProxy(Color black, Color color1, Color color2, Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
      super(texture, srcX, srcY, srcWidth, srcHeight);
      this.color1 = color1;
      this.color2 = color2;
      this.black = black;
      this.originalTexture = texture;
      if (!TextureCache.effectsTextMap.containsKey(texture)) {
         Texture[] textures = new Texture[8];
         TextureData temp = texture.getTextureData();
         if (!temp.isPrepared()) {
            temp.prepare();
         }

         Pixmap currPixmap = temp.consumePixmap();
         Pixmap newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         int i = 0;

         for (int j = 0; j < texture.getHeight(); i++) {
            if (i > texture.getWidth()) {
               i = -1;
               j++;
            } else {
               Color color = new Color(currPixmap.getPixel(i, j));
               if ((color.r != 0.0F || color.g != 0.9843137F || color.b != 0.0F)
                  && (color.r != 0.0F || color.g != 0.0F || color.b != 0.0F)
                  && (color.r != 1.0F || color.g != 1.0F || color.b != 1.0F)) {
                  if (this.color1 == null) {
                     this.color1 = color;
                  } else if (color.r != this.color1.r || color.g != this.color1.g || color.b != this.color1.b) {
                     this.color2 = color;
                     break;
                  }
               }
            }
         }

         if (this.color1 != null && this.color2 != null && this.color1.r + this.color1.g + this.color1.b < this.color2.r + this.color2.g + this.color2.b) {
            Color tempColor = this.color1;
            this.color1 = this.color2;
            this.color2 = tempColor;
         }

         Color[] colors = new Color[]{this.color1, this.color2};
         TextureCache.colorsTextMap.put(texture, colors);
         int ix = 0;

         for (int jx = 0; jx < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               jx++;
            } else {
               Color color = new Color(currPixmap.getPixel(ix, jx));
               if (color.a == 0.0F) {
                  color = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (color.r == 0.0F && color.g == 0.0F && color.b == 0.0F) {
                  color = Color.WHITE;
               } else if (color.r == 1.0F && color.g == 1.0F && color.b == 1.0F) {
                  color = this.black;
               } else if (color.r == 1.0F && color.g == 0.9843137F && color.b == 1.0F) {
                  color = this.black;
               } else if (color.r == this.color1.r && color.g == this.color1.g && color.b == this.color1.b) {
                  color = this.color2;
               } else if (color.r == this.color2.r && color.g == this.color2.g && color.b == this.color2.b) {
                  color = this.color1;
               }

               if (color.a != 0.0F) {
                  newPixmap.drawPixel(ix, jx, Color.rgba8888(color.r, color.g, color.b, 1.0F));
               }
            }
         }

         textures[0] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int jxx = 0; jxx < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               jxx++;
            } else {
               Color colorx = new Color(currPixmap.getPixel(ix, jxx));
               if (colorx.a == 0.0F) {
                  colorx = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if ((colorx.r != 0.0F || colorx.g != 0.0F || colorx.b != 0.0F)
                  && (colorx.r != 1.0F || colorx.g != 1.0F || colorx.b != 1.0F)
                  && (colorx.r != 1.0F || colorx.g != 0.9843137F || colorx.b != 1.0F)
                  && this.color1 != null
                  && colorx.r == this.color2.r
                  && colorx.g == this.color2.g
                  && colorx.b == this.color2.b) {
                  colorx = this.black;
               }

               if (colorx.a != 0.0F) {
                  newPixmap.drawPixel(ix, jxx, Color.rgba8888(colorx.r, colorx.g, colorx.b, 1.0F));
               }
            }
         }

         textures[1] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int jxxx = 0; jxxx < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               jxxx++;
            } else {
               Color colorxx = new Color(currPixmap.getPixel(ix, jxxx));
               if (colorxx.a == 0.0F) {
                  colorxx = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if ((colorxx.r != 0.0F || colorxx.g != 0.0F || colorxx.b != 0.0F)
                  && (colorxx.r != 1.0F || colorxx.g != 1.0F || colorxx.b != 1.0F)
                  && (colorxx.r != 1.0F || colorxx.g != 0.9843137F || colorxx.b != 1.0F)) {
                  if (this.color1 != null && colorxx.r == this.color2.r && colorxx.g == this.color2.g && colorxx.b == this.color2.b) {
                     colorxx = this.black;
                  } else if (this.color1 != null && colorxx.r == this.color1.r && colorxx.g == this.color1.g && colorxx.b == this.color1.b) {
                     colorxx = this.color2;
                  }
               }

               if (colorxx.a != 0.0F) {
                  newPixmap.drawPixel(ix, jxxx, Color.rgba8888(colorxx.r, colorxx.g, colorxx.b, 1.0F));
               }
            }
         }

         textures[2] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int jxxxx = 0; jxxxx < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               jxxxx++;
            } else {
               Color colorxxx = new Color(currPixmap.getPixel(ix, jxxxx));
               if (colorxxx.a == 0.0F) {
                  colorxxx = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (colorxxx.r == 0.0F && colorxxx.g == 0.0F && colorxxx.b == 0.0F) {
                  colorxxx = this.color2;
               } else if ((colorxxx.r != 1.0F || colorxxx.g != 1.0F || colorxxx.b != 1.0F)
                  && (colorxxx.r != 1.0F || colorxxx.g != 0.9843137F || colorxxx.b != 1.0F)) {
                  if (this.color1 != null && colorxxx.r == this.color2.r && colorxxx.g == this.color2.g && colorxxx.b == this.color2.b) {
                     colorxxx = this.color1;
                  } else if (this.color1 != null && colorxxx.r == this.color1.r && colorxxx.g == this.color1.g && colorxxx.b == this.color1.b) {
                     colorxxx = Color.WHITE;
                  }
               }

               if (colorxxx.a != 0.0F) {
                  newPixmap.drawPixel(ix, jxxxx, Color.rgba8888(colorxxx.r, colorxxx.g, colorxxx.b, 1.0F));
               }
            }
         }

         textures[3] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int jxxxxx = 0; jxxxxx < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               jxxxxx++;
            } else {
               Color colorxxxx = new Color(currPixmap.getPixel(ix, jxxxxx));
               if (colorxxxx.a == 0.0F) {
                  colorxxxx = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (colorxxxx.r == 0.0F && colorxxxx.g == 0.0F && colorxxxx.b == 0.0F) {
                  colorxxxx = this.color1;
               } else if ((colorxxxx.r != 1.0F || colorxxxx.g != 1.0F || colorxxxx.b != 1.0F)
                  && (colorxxxx.r != 1.0F || colorxxxx.g != 0.9843137F || colorxxxx.b != 1.0F)) {
                  if (this.color1 != null && colorxxxx.r == this.color2.r && colorxxxx.g == this.color2.g && colorxxxx.b == this.color2.b) {
                     colorxxxx = Color.WHITE;
                  } else if (this.color1 != null && colorxxxx.r == this.color1.r && colorxxxx.g == this.color1.g && colorxxxx.b == this.color1.b) {
                     colorxxxx = Color.WHITE;
                  }
               }

               if (colorxxxx.a != 0.0F) {
                  newPixmap.drawPixel(ix, jxxxxx, Color.rgba8888(colorxxxx.r, colorxxxx.g, colorxxxx.b, 1.0F));
               }
            }
         }

         textures[4] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int jxxxxxx = 0; jxxxxxx < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               jxxxxxx++;
            } else {
               Color colorxxxxx = new Color(currPixmap.getPixel(ix, jxxxxxx));
               if (colorxxxxx.a == 0.0F) {
                  colorxxxxx = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (colorxxxxx.r == 0.0F && colorxxxxx.g == 0.0F && colorxxxxx.b == 0.0F) {
                  colorxxxxx = this.color1;
               } else if (colorxxxxx.r == 1.0F && colorxxxxx.g == 1.0F && colorxxxxx.b == 1.0F) {
                  colorxxxxx = this.black;
               } else if (colorxxxxx.r == 1.0F && colorxxxxx.g == 0.9843137F && colorxxxxx.b == 1.0F) {
                  colorxxxxx = this.black;
               } else if ((this.color1 == null || colorxxxxx.r != this.color2.r || colorxxxxx.g != this.color2.g || colorxxxxx.b != this.color2.b)
                  && this.color1 != null
                  && colorxxxxx.r == this.color1.r
                  && colorxxxxx.g == this.color1.g
                  && colorxxxxx.b == this.color1.b) {
                  colorxxxxx = Color.WHITE;
               }

               if (colorxxxxx.a != 0.0F) {
                  newPixmap.drawPixel(ix, jxxxxxx, Color.rgba8888(colorxxxxx.r, colorxxxxx.g, colorxxxxx.b, 1.0F));
               }
            }
         }

         textures[5] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int jxxxxxxx = 0; jxxxxxxx < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               jxxxxxxx++;
            } else {
               Color colorxxxxxx = new Color(currPixmap.getPixel(ix, jxxxxxxx));
               if (colorxxxxxx.a == 0.0F) {
                  colorxxxxxx = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if (colorxxxxxx.r == 0.0F && colorxxxxxx.g == 0.0F && colorxxxxxx.b == 0.0F) {
                  colorxxxxxx = this.color2;
               } else if (colorxxxxxx.r == 1.0F && colorxxxxxx.g == 1.0F && colorxxxxxx.b == 1.0F) {
                  colorxxxxxx = this.black;
               } else if (colorxxxxxx.r == 1.0F && colorxxxxxx.g == 0.9843137F && colorxxxxxx.b == 1.0F) {
                  colorxxxxxx = this.black;
               } else if (this.color1 != null && colorxxxxxx.r == this.color2.r && colorxxxxxx.g == this.color2.g && colorxxxxxx.b == this.color2.b) {
                  colorxxxxxx = Color.WHITE;
               } else if (this.color1 != null && colorxxxxxx.r == this.color1.r && colorxxxxxx.g == this.color1.g && colorxxxxxx.b == this.color1.b) {
               }

               if (colorxxxxxx.a != 0.0F) {
                  newPixmap.drawPixel(ix, jxxxxxxx, Color.rgba8888(colorxxxxxx.r, colorxxxxxx.g, colorxxxxxx.b, 1.0F));
               }
            }
         }

         textures[6] = TextureCache.get(newPixmap);
         newPixmap = new Pixmap(texture.getWidth(), texture.getHeight(), Format.RGBA8888);
         newPixmap.setColor(new Color(0.0F, 0.0F, 0.0F, 0.0F));
         newPixmap.fill();
         ix = 0;

         for (int jxxxxxxxx = 0; jxxxxxxxx < texture.getHeight(); ix++) {
            if (ix > texture.getWidth()) {
               ix = -1;
               jxxxxxxxx++;
            } else {
               Color colorxxxxxxx = new Color(currPixmap.getPixel(ix, jxxxxxxxx));
               if (colorxxxxxxx.a == 0.0F) {
                  colorxxxxxxx = new Color(0.0F, 0.0F, 0.0F, 0.0F);
               } else if ((colorxxxxxxx.r != 0.0F || colorxxxxxxx.g != 0.0F || colorxxxxxxx.b != 0.0F)
                  && (colorxxxxxxx.r != 1.0F || colorxxxxxxx.g != 1.0F || colorxxxxxxx.b != 1.0F)
                  && (colorxxxxxxx.r != 1.0F || colorxxxxxxx.g != 0.9843137F || colorxxxxxxx.b != 1.0F)) {
                  if (this.color1 != null && colorxxxxxxx.r == this.color2.r && colorxxxxxxx.g == this.color2.g && colorxxxxxxx.b == this.color2.b) {
                     colorxxxxxxx = this.black;
                  } else if (this.color1 != null && colorxxxxxxx.r == this.color1.r && colorxxxxxxx.g == this.color1.g && colorxxxxxxx.b == this.color1.b) {
                     colorxxxxxxx = this.black;
                  }
               }

               if (colorxxxxxxx.a != 0.0F) {
                  newPixmap.drawPixel(ix, jxxxxxxxx, Color.rgba8888(colorxxxxxxx.r, colorxxxxxxx.g, colorxxxxxxx.b, 1.0F));
               }
            }
         }

         textures[7] = TextureCache.get(newPixmap);
         TextureCache.effectsTextMap.put(texture, textures);
      } else {
         this.color1 = TextureCache.colorsTextMap.get(texture)[0];
         this.color2 = TextureCache.colorsTextMap.get(texture)[1];
      }

      this.inverseTexture = TextureCache.effectsTextMap.get(texture)[0];
      this.darkenTexture1 = TextureCache.effectsTextMap.get(texture)[1];
      this.darkenTexture2 = TextureCache.effectsTextMap.get(texture)[2];
      this.lightenTexture1 = TextureCache.effectsTextMap.get(texture)[3];
      this.lightenTexture2 = TextureCache.effectsTextMap.get(texture)[4];
      this.confuseRayTexture1 = TextureCache.effectsTextMap.get(texture)[5];
      this.confuseRayTexture2 = TextureCache.effectsTextMap.get(texture)[6];
      this.darkenTexture3 = TextureCache.effectsTextMap.get(texture)[7];
   }
}
