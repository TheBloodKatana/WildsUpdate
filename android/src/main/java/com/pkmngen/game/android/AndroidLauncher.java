package com.pkmngen.game.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.pkmngen.game.Game;
import com.pkmngen.leaks.LeakTracer;

/** Starts PokeWilds on Android: portrait, full screen, touch controls (see DrawMobileControls). */
public class AndroidLauncher extends AndroidApplication {
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Game.leakTracer = LeakTracer.NoOp.INSTANCE;
      AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
      config.useImmersiveMode = true;
      config.useAccelerometer = false;
      config.useCompass = false;
      config.useGyroscope = false;
      config.useWakelock = true;
      initialize(new Game(new String[0], 1), config);
   }
}
