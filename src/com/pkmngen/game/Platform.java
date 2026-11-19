package com.pkmngen.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import java.io.File;

/**
 * The few things that differ between the PC and Android builds.
 *  - Paths: on PC, saves live next to the game (relative paths). On Android the working folder
 *    isn't writable, so relative paths go to the app's private storage instead.
 *  - Dialogs: the PC build pops Swing dialogs; Android has no Swing, so it logs instead.
 *    Swing is reached by reflection so Android never loads it.
 */
public class Platform {
   public static boolean android() {
      return Gdx.app != null && Gdx.app.getType() == Application.ApplicationType.Android;
   }

   /** PC test mode: run with "phone" for a tall window with the touch controls. */
   public static boolean phoneLayout = false;

   /** Touch controls on screen: Android, or the PC phone preview. */
   public static boolean touchUi() {
      return android() || phoneLayout;
   }

   /** A save/log path that works on this platform. */
   public static String path(String p) {
      if (!android() || p == null || new File(p).isAbsolute()) {
         return p;
      }
      String base = Gdx.files.getLocalStoragePath();
      return base.endsWith("/") ? base + p : base + "/" + p;
   }

   public static File file(String p) {
      return new File(path(p));
   }

   public static void alert(String message) {
      if (android()) {
         Gdx.app.error("PokeWilds", message);
         return;
      }
      try {
         Class<?> pane = Class.forName("javax.swing.JOptionPane");
         pane.getMethod("showMessageDialog", Class.forName("java.awt.Component"), Object.class).invoke(null, null, message);
      } catch (Throwable e) {
         System.err.println(message);
      }
   }

   /** YES/NO dialog on PC (returns true for YES). On Android: false, so nothing gets overwritten. */
   public static boolean confirm(String message, String title) {
      if (android()) {
         return false;
      }
      try {
         Class<?> pane = Class.forName("javax.swing.JOptionPane");
         Object r = pane.getMethod("showConfirmDialog", Class.forName("java.awt.Component"), Object.class, String.class, int.class)
            .invoke(null, null, message, title, 0);
         return ((Integer)r) == 0;
      } catch (Throwable e) {
         return false;
      }
   }
}
