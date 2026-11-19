package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.pkmngen.game.util.TextureCache;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

/**
 * The calendar, seasons, weather and legendary events.
 *
 * Year = 4 seasons x 28 days. Seasons tint the world (warm summer, orange fall, cold winter) and
 * change the weather odds. Weather is rolled a day ahead (so there's always a forecast) and shows
 * up differently per biome: rain turns to snow in the tundra and never reaches the desert, fog
 * stays off the volcano, heat only bakes hot places. Rain and heat carry into battles as RAIN DANCE
 * and SUNNY DAY. No ordinary storms.
 *
 * Legendary events are rare: a few days' warning, then an area around a spot is taken over by a
 * legendary's weather (downpour, drought, blizzard, thunderstorm...) with the legendary itself in
 * the middle as a boss. Beat it, catch it, or wait the few days until it leaves. A caught
 * legendary doesn't cause events again (until the torch is passed).
 */
class Calendar {
   static final int SEASON_DAYS = 28;
   static final int YEAR_DAYS = SEASON_DAYS * 4;
   static final String[] SEASONS = {"SPRING", "SUMMER", "FALL", "WINTER"};

   static final String CLEAR = "clear";
   static final String RAIN = "rain";
   static final String SNOW = "snow";
   static final String FOG = "fog";
   static final String HEAT = "heat";

   // ------------------------------------------------------------------ dates

   static String mapId(Game game) {
      return game.map.currMapId == null ? "0,0" : game.map.currMapId;
   }

   static int dayOfYear(Game game) {
      return game.player.daysPassed % YEAR_DAYS;
   }

   static int seasonIndex(Game game) {
      return dayOfYear(game) / SEASON_DAYS;
   }

   static String season(Game game) {
      return SEASONS[seasonIndex(game)];
   }

   static int dayOfSeason(Game game) {
      return dayOfYear(game) % SEASON_DAYS + 1;
   }

   static int year(Game game) {
      return game.player.daysPassed / YEAR_DAYS + 1;
   }

   static String dateText(Game game) {
      return season(game) + " day " + dayOfSeason(game) + ", year " + year(game);
   }

   // ------------------------------------------------------------------ events (saved in PlayerData)

   static class Event {
      String legend;      // species id
      String kind;        // rain / heat / snow / storm / fog
      String title;       // "an endless DOWNPOUR"
      String mapKey;      // which island
      float x;
      float y;
      int warnDay;
      int startDay;
      int endDay;
      boolean spawned;

      Event() {
      }
   }

   static final String[][] LEGENDS = {
      // species, area weather, title, where it likes to appear, season (-1 any)
      {"kyogre", RAIN, "an endless DOWNPOUR", "ocean,beach", "-1"},
      {"groudon", HEAT, "a scorching DROUGHT", "desert,volcano,savanna", "1"},
      {"zapdos", "storm", "a raging THUNDERSTORM", "mountain,savanna", "-1"},
      {"articuno", SNOW, "a howling BLIZZARD", "snow,mountain", "3"},
      {"moltres", HEAT, "a blazing HEAT WAVE", "volcano,desert", "1"},
      {"suicune", RAIN, "a strange, pure RAIN", "wooded_lake,forest,beach", "0"},
      {"raikou", "storm", "a crackling STORM", "savanna,forest", "-1"},
      {"entei", HEAT, "a smoldering HEAT", "volcano,mountain", "-1"},
      {"lugia", "storm", "a monstrous SEA STORM", "ocean,beach", "2"},
   };

   static final int EVENT_RADIUS = 22 * 16;
   static final int EVENT_DAYS = 3;
   static final int WARN_DAYS = 2;

   // ------------------------------------------------------------------ hooks

   static boolean loaded = false;

   static void onLoad(Game game) {
      if (game.player.weatherToday == null) {
         game.player.weatherToday = CLEAR;
         game.player.weatherTomorrow = roll(game, new Random(), seasonIndex(game));
      }
      if (driver == null || !game.actionStack.contains(driver)) {
         driver = new Driver();
         game.insertAction(driver);
      }
      if (overlay == null || !game.actionStack.contains(overlay)) {
         overlay = new Overlay();
         game.insertAction(overlay);
      }
      loaded = true;
      Torch.applyNames(game);
      Event e = activeEvent(game);
      if (e != null) {
         ensureLegend(game, e);
      }
   }

   static void onNewDay(Game game) {
      Player p = game.player;
      Random r = new Random();
      p.weatherToday = p.weatherTomorrow != null ? p.weatherTomorrow : CLEAR;
      int tomorrowSeason = ((p.daysPassed + 1) % YEAR_DAYS) / SEASON_DAYS;
      p.weatherTomorrow = roll(game, r, tomorrowSeason);

      // season changes
      int ds = dayOfSeason(game);
      if (ds == 1) {
         Alerts.push(game, season(game) + " has begun! " + dateText(game) + ".");
      } else if (ds == SEASON_DAYS - 2) {
         Alerts.push(game, SEASONS[(seasonIndex(game) + 1) % 4] + " starts in 3 days.");
      }
      if (!p.weatherToday.equals(CLEAR)) {
         Alerts.push(game, "Today on this island - " + weatherName(p.weatherToday) + ".");
      }

      // legendary events
      Event e = activeEvent(game);
      for (Event ev : new ArrayList<>(p.legendEvents)) {
         if (p.daysPassed > ev.endDay) {
            endEvent(game, ev, "left");
         }
      }
      for (Event ev : p.legendEvents) {
         if (!ev.mapKey.equals(mapId(game))) {
            continue;
         }
         if (p.daysPassed == ev.startDay) {
            Alerts.push(game, Characters.upper(ev.legend) + " has arrived! " + Characters.cap(ev.title) + " hits " + Alerts.where(game, new Vector2(ev.x, ev.y)) + ".");
            ensureLegend(game, ev);
         } else if (p.daysPassed < ev.startDay) {
            Alerts.push(game, "Strange weather is gathering over " + Alerts.where(game, new Vector2(ev.x, ev.y)) + "...");
         }
      }
      if (e == null && p.legendEvents.isEmpty() && p.daysPassed >= 10 && r.nextInt(100) < 3) {
         planEvent(game, r);
      }
   }

   /** Called every frame (cheap): notices when the event's legendary is gone. */
   static void checkEvent(Game game) {
      Event e = activeEvent(game);
      if (e == null || !e.spawned || game.battle.drawAction != null) {
         return;
      }
      for (Pokemon pk : game.map.pokemon.values()) {
         if (pk.isBoss && pk.specie.name.equalsIgnoreCase(e.legend)) {
            return;
         }
      }
      boolean caught = game.player.dexCaught.contains(Player.dexId(e.legend));
      if (caught) {
         game.player.legendsDone.add(e.legend);
      }
      endEvent(game, e, caught ? "caught" : "beaten");
   }

   static void endEvent(Game game, Event e, String how) {
      game.player.legendEvents.remove(e);
      String where = Alerts.where(game, new Vector2(e.x, e.y));
      String name = Characters.upper(e.legend);
      switch (how) {
         case "caught": Alerts.push(game, "You caught " + name + "! The skies over " + where + " are clearing."); break;
         case "beaten": Alerts.push(game, name + " fled. The skies over " + where + " are clearing."); break;
         default: {
            Alerts.push(game, name + " has moved on. The weather over " + where + " is calming down.");
            for (Vector2 k : new ArrayList<>(game.map.pokemon.keySet())) {
               Pokemon pk = game.map.pokemon.get(k);
               if (pk != null && pk.isBoss && pk.specie.name.equalsIgnoreCase(e.legend)) {
                  game.map.pokemon.remove(k);
                  if (pk.standingAction != null) {
                     game.actionStack.remove(pk.standingAction);
                  }
               }
            }
         }
      }
   }

   static Event activeEvent(Game game) {
      String key = mapId(game);
      for (Event e : game.player.legendEvents) {
         if (e.mapKey.equals(key) && game.player.daysPassed >= e.startDay && game.player.daysPassed <= e.endDay) {
            return e;
         }
      }
      return null;
   }

   static void planEvent(Game game, Random r) {
      Player p = game.player;
      ArrayList<String[]> options = new ArrayList<>();
      int season = seasonIndex(game);
      for (String[] l : LEGENDS) {
         int s = Integer.valueOf(l[4]);
         if (p.legendsDone.contains(l[0]) || (s >= 0 && s != season) || !Characters.playable(l[0])) {
            continue;
         }
         options.add(l);
      }
      if (options.isEmpty()) {
         return;
      }
      String[] l = options.get(r.nextInt(options.size()));
      // pick a spot in one of its biomes, away from towns
      ArrayList<Tile> spots = new ArrayList<>();
      for (Tile t : game.map.overworldTiles.values()) {
         if (t.routeBelongsTo == null || t.routeBelongsTo.name == null || t.isSolid || t.isWater || !t.nameUpper.equals("")) {
            continue;
         }
         for (String b : l[3].split(",")) {
            if (t.routeBelongsTo.name.startsWith(b) && Cities.cityAt(game, t.position) == null) {
               spots.add(t);
               break;
            }
         }
      }
      if (spots.isEmpty()) {
         return;
      }
      Tile t = spots.get(r.nextInt(spots.size()));
      Event e = new Event();
      e.legend = l[0];
      e.kind = l[1];
      e.title = l[2];
      e.mapKey = mapId(game);
      e.x = t.position.x;
      e.y = t.position.y;
      e.warnDay = p.daysPassed;
      e.startDay = p.daysPassed + WARN_DAYS;
      e.endDay = e.startDay + EVENT_DAYS - 1;
      p.legendEvents.add(e);
      Alerts.push(game, "Strange weather is gathering over " + Alerts.where(game, t.position) + "...");
   }

   static void ensureLegend(Game game, Event e) {
      for (Pokemon pk : game.map.pokemon.values()) {
         if (pk.isBoss && pk.specie.name.equalsIgnoreCase(e.legend)) {
            e.spawned = true;
            return;
         }
      }
      if (e.spawned) {
         return; // it was here and is gone; checkEvent handles that
      }
      Tile t = game.map.overworldTiles.get(new Vector2(e.x, e.y));
      if (t == null) {
         return;
      }
      int level = Math.min(100, Boss.bossLevel(game.player) + 10);
      boolean wasReady = Alerts.ready;
      Alerts.ready = false;   // the event has its own announcement
      Boss.spawnSpecies(game, t, t.routeBelongsTo != null ? t.routeBelongsTo.name : "legend", e.legend, level, false);
      Alerts.ready = wasReady;
      e.spawned = true;
   }

   // ------------------------------------------------------------------ weather

   static String roll(Game game, Random r, int season) {
      int n = r.nextInt(100);
      switch (season) {
         case 0: return n < 25 ? RAIN : n < 35 ? FOG : CLEAR;                 // spring: showers
         case 1: return n < 30 ? HEAT : n < 42 ? RAIN : CLEAR;                // summer: hot, the odd shower
         case 2: return n < 22 ? FOG : n < 42 ? RAIN : CLEAR;                 // fall: foggy and wet
         default: return n < 40 ? SNOW : n < 52 ? FOG : CLEAR;                // winter: snow
      }
   }

   static String weatherName(String w) {
      switch (w) {
         case RAIN: return "RAIN";
         case SNOW: return "SNOW";
         case FOG: return "FOG";
         case HEAT: return "a HEAT WAVE";
         case "storm": return "a THUNDERSTORM";
         default: return "clear skies";
      }
   }

   /** Weather right where this spot is: island weather filtered by biome, or a legendary event's. */
   static String weatherAt(Game game, Vector2 pos) {
      if (game.map.tiles != game.map.overworldTiles) {
         return CLEAR;
      }
      Event e = activeEvent(game);
      if (e != null && pos.dst(e.x, e.y) <= EVENT_RADIUS) {
         return e.kind;
      }
      String w = game.player.weatherToday == null ? CLEAR : game.player.weatherToday;
      Tile t = game.map.overworldTiles.get(new Vector2(pos.x - pos.x % 16, pos.y - pos.y % 16));
      String biome = t != null && t.routeBelongsTo != null && t.routeBelongsTo.name != null ? t.routeBelongsTo.name : "";
      boolean cold = biome.startsWith("snow");
      boolean hot = biome.startsWith("desert") || biome.startsWith("volcano") || biome.startsWith("oasis") || biome.startsWith("sand_pit");
      boolean warm = hot || biome.startsWith("savanna") || biome.startsWith("beach");
      switch (w) {
         case RAIN: return cold ? SNOW : hot ? CLEAR : RAIN;
         case SNOW: return hot ? CLEAR : (warm && !cold) ? RAIN : SNOW;
         case FOG: return hot ? CLEAR : FOG;
         case HEAT: return warm ? HEAT : CLEAR;
         default: return cold && seasonIndex(game) == 3 ? SNOW : CLEAR;
      }
   }

   static String playerWeather(Game game) {
      return weatherAt(game, game.player.position);
   }

   /** At the start of a battle: overworld weather becomes battle weather. */
   static void applyToBattle(Game game) {
      if (game.player == null || game.map == null || game.battle == null) {
         return;
      }
      String w = playerWeather(game);
      if (w.equals(RAIN) || w.equals("storm")) {
         game.battle.weather = "rain dance";
         game.battle.weatherCounter = 999;
      } else if (w.equals(HEAT)) {
         game.battle.weather = "sunny day";
         game.battle.weatherCounter = 999;
      }
   }

   // ------------------------------------------------------------------ season tint + per-frame checks

   static Color seasonTint(int season) {
      switch (season) {
         case 1: return new Color(1.0F, 0.97F, 0.88F, 1.0F);
         case 2: return new Color(1.0F, 0.87F, 0.74F, 1.0F);
         case 3: return new Color(0.87F, 0.92F, 1.0F, 1.0F);
         default: return new Color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   static Driver driver;
   static Overlay overlay;

   /** Map camera, runs before the map draws: applies the season tint to daytime colors. */
   static class Driver extends Action {
      public Action.Layer layer = Action.Layer.map_5000;
      int eveningAnnounced = -1;

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         if (game.map == null || game.player == null) {
            return;
         }
         Color c = game.mapBatch.getColor();
         boolean outside = game.map.tiles == game.map.overworldTiles && !game.map.currRoute.isDungeon;
         Color tint = seasonTint(seasonIndex(game));
         boolean isWhite = c.r > 0.99F && c.g > 0.99F && c.b > 0.99F;
         boolean isTint = Math.abs(c.r - tint.r) < 0.005F && Math.abs(c.g - tint.g) < 0.005F && Math.abs(c.b - tint.b) < 0.005F;
         boolean daytime = game.map.timeOfDay.equals("day") && !CycleDayNight.fadeToDay && !CycleDayNight.fadeToNight;
         if (outside && daytime && isWhite) {
            game.mapBatch.setColor(tint);
         } else if ((!outside || !daytime) && isTint && !isWhite && !(game.map.timeOfDay.equals("night"))) {
            game.mapBatch.setColor(Color.WHITE);
         }
         // evening forecast
         if (game.map.timeOfDay.equals("day") && CycleDayNight.dayTimer < 6000 && this.eveningAnnounced != game.player.daysPassed) {
            this.eveningAnnounced = game.player.daysPassed;
            String tm = game.player.weatherTomorrow;
            if (tm != null && !tm.equals(CLEAR)) {
               Alerts.push(game, "Forecast - " + weatherName(tm) + " tomorrow.");
            }
         }
         checkEvent(game);
      }
   }

   /** Screen-space rain / snow / fog, drawn over the map but under text and menus. */
   static class Overlay extends Action {
      public Action.Layer layer = Action.Layer.gui_139;
      Texture white;
      float[] px = new float[90];
      float[] py = new float[90];
      Random r = new Random();
      int t = 0;
      int flash = 0;

      @Override
      public String getCamera() {
         return "gui";
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         if (game.map == null || game.player == null || (game.battle != null && game.battle.drawAction != null)) {
            return;
         }
         if (this.white == null) {
            this.white = TextureCache.get(Gdx.files.internal("npc/px_white.png"));
            for (int i = 0; i < this.px.length; i++) {
               this.px[i] = this.r.nextInt(176);
               this.py[i] = this.r.nextInt(160);
            }
         }
         String w = playerWeather(game);
         if (w.equals(CLEAR)) {
            return;
         }
         this.t++;
         boolean night = game.map.timeOfDay.equals("night");
         Color saved = new Color(game.uiBatch.getColor());
         boolean heavy = activeEvent(game) != null && w.equals(activeEvent(game).kind);
         int count = heavy ? this.px.length : this.px.length * 2 / 3;
         switch (w) {
            case RAIN:
            case "storm": {
               game.uiBatch.setColor(0.1F, 0.15F, 0.3F, heavy ? 0.22F : 0.12F);
               game.uiBatch.draw(this.white, 0, 0, 160, 144);
               game.uiBatch.setColor(night ? 0.5F : 0.3F, night ? 0.6F : 0.4F, night ? 1.0F : 0.85F, 0.9F);
               for (int i = 0; i < count; i++) {
                  this.py[i] -= heavy ? 6 : 4;
                  this.px[i] -= 1.5F;
                  if (this.py[i] < -6) {
                     this.py[i] = 150 + this.r.nextInt(10);
                     this.px[i] = this.r.nextInt(176);
                  }
                  game.uiBatch.draw(this.white, this.px[i], this.py[i], 1, 7);
               }
               if (w.equals("storm")) {
                  if (this.flash <= 0 && this.r.nextInt(400) == 0) {
                     this.flash = 6;
                  }
                  if (this.flash > 0) {
                     this.flash--;
                     game.uiBatch.setColor(1, 1, 1, 0.5F);
                     game.uiBatch.draw(this.white, 0, 0, 160, 144);
                  }
               }
               break;
            }
            case SNOW: {
               game.uiBatch.setColor(1, 1, 1, night ? 0.6F : 0.9F);
               for (int i = 0; i < count; i++) {
                  this.py[i] -= heavy ? 1.4F : 0.6F;
                  this.px[i] += (float)Math.sin((this.t + i * 13) / 20.0) * 0.4F - (heavy ? 1.2F : 0);
                  if (this.py[i] < -2) {
                     this.py[i] = 146;
                     this.px[i] = this.r.nextInt(176);
                  }
                  int s = i % 3 == 0 ? 2 : 1;
                  game.uiBatch.draw(this.white, this.px[i], this.py[i], s, s);
               }
               if (heavy) {
                  game.uiBatch.setColor(1, 1, 1, 0.25F);
                  game.uiBatch.draw(this.white, 0, 0, 160, 144);
               }
               break;
            }
            case FOG: {
               float a = 0.28F + 0.05F * (float)Math.sin(this.t / 90.0);
               game.uiBatch.setColor(night ? 0.6F : 1, night ? 0.6F : 1, night ? 0.7F : 1, a);
               game.uiBatch.draw(this.white, 0, 0, 160, 144);
               break;
            }
            case HEAT: {
               float a = 0.10F + 0.04F * (float)Math.sin(this.t / 30.0);
               game.uiBatch.setColor(1.0F, 0.75F, 0.3F, heavy ? a * 2 : a);
               game.uiBatch.draw(this.white, 0, 0, 160, 144);
               break;
            }
         }
         game.uiBatch.setColor(saved);
      }
   }

   static HashSet<String> emptySet() {
      return new HashSet<>();
   }
}
