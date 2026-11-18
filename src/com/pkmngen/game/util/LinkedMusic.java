package com.pkmngen.game.util;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.pkmngen.game.util.audio.AudioLoader;
import gme.VGMPlayer;

public class LinkedMusic implements Music {
   Music music1;
   Music music2;
   Music currMusic;
   public static VGMPlayer gbsPlayer = new VGMPlayer(44100);
   public LinkedMusic.Type type = LinkedMusic.Type.NORMAL;

   public LinkedMusic(String m1, String m2) {
      if (!m1.contains("pkmnmansion1") && !m2.contains("pkmnmansion1")) {
         this.music1 = AudioLoader.loadMusic(m1 + ".ogg");
         this.music1.setLooping(false);
         this.currMusic = this.music1;
         if (!m2.equals("")) {
            this.music1.setOnCompletionListener(new OnCompletionListener() {
               public void onCompletion(Music music) {
                  LinkedMusic.this.music2.play();
                  LinkedMusic.this.currMusic = LinkedMusic.this.music2;
               }
            });
            this.music2 = AudioLoader.loadMusic(m2 + ".ogg");
            this.music2.setLooping(true);
            this.music2.play();
            this.music2.pause();
         }
      } else {
         this.type = LinkedMusic.Type.GME;

         try {
            gbsPlayer.loadFile("DMG-APEE-USA.gbs", "DMG-APEE-USA.gbs");
            gbsPlayer.startTrack(38, 100);
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }
   }

   public void play() {
      if (this.type == LinkedMusic.Type.GME) {
         try {
            if (!gbsPlayer.isPlaying()) {
               gbsPlayer.play();
            }
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      } else {
         this.currMusic.play();
      }
   }

   public void pause() {
      if (this.type == LinkedMusic.Type.GME) {
         try {
            if (gbsPlayer.isPlaying()) {
               gbsPlayer.pause();
            }
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      } else {
         this.currMusic.pause();
      }
   }

   public void stop() {
      if (this.type == LinkedMusic.Type.GME) {
         try {
            gbsPlayer.stop();
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      } else {
         if (this.music2 != null) {
            this.music2.stop();
         }

         this.music1.stop();
         this.currMusic = this.music1;
      }
   }

   public boolean isPlaying() {
      return this.type == LinkedMusic.Type.GME ? gbsPlayer.isPlaying() : this.currMusic.isPlaying();
   }

   public void setLooping(boolean isLooping) {
      if (this.type == LinkedMusic.Type.GME) {
         System.out.println("hi");
      } else {
         this.currMusic.setLooping(isLooping);
      }
   }

   public boolean isLooping() {
      return this.type == LinkedMusic.Type.GME ? true : this.currMusic.isLooping();
   }

   public void setVolume(float volume) {
      if (this.type == LinkedMusic.Type.GME) {
         try {
            gbsPlayer.setVolume(volume);
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      } else {
         if (this.music2 != null) {
            this.music2.setVolume(volume);
         }

         this.music1.setVolume(volume);
      }
   }

   public float getVolume() {
      return this.type == LinkedMusic.Type.GME ? (float)gbsPlayer.getVolume() : this.currMusic.getVolume();
   }

   public void setPan(float pan, float volume) {
      if (this.type != LinkedMusic.Type.GME) {
         this.currMusic.setPan(pan, volume);
      }
   }

   public void setPosition(float position) {
      if (this.type != LinkedMusic.Type.GME) {
         this.currMusic.setPosition(position);
      }
   }

   public float getPosition() {
      return this.type == LinkedMusic.Type.GME ? 0.0F : this.currMusic.getPosition();
   }

   public void dispose() {
      if (this.type != LinkedMusic.Type.GME) {
         this.music1.dispose();
         if (this.music2 != null) {
            this.music2.dispose();
         }
      }
   }

   public void setOnCompletionListener(OnCompletionListener listener) {
      if (this.type != LinkedMusic.Type.GME) {
         this.currMusic.setOnCompletionListener(listener);
      }
   }

   public static enum Type {
      NORMAL,
      GME;
   }
}
