# PokeWilds — private dev build

PRIVATE. Not for distribution. Rebuilt from the official PokeWilds v0.8.11
release (by SheerSt) for personal modding only.

## Layout
- `src/` — game source (Java, package `com.pkmngen.game`)
- `assets/` — sprites, sounds, music, maps, data (packed into the jar)
- `libs/runtime-libs.jar` — libGDX, LWJGL, Kryo, etc. (prebuilt)
- `game-files/` — settings.txt, mods/, launchers copied next to the jar
- `Build.java` — build script, needs only a JDK 17+

## Build & run
    java Build.java          # builds dist/pokewilds.jar
    java Build.java run      # builds, then launches the game
    java Build.java run dev  # launches in dev mode

## Where things are
- Entry point: `src/com/pkmngen/game/desktop/DesktopLauncher.java`
- Main loop/state: `Game.java`
- World generation: `GenIsland1.java`, `GenForest2.java`, `PkmnMap.java`
- Tiles and field interactions: `Tile.java`
- Player movement/overworld: `Player.java`, `PlayerStanding.java`
- Battles: `Battle.java`, `Attack.java`
- Pokémon data/logic: `Pokemon.java`, `Specie.java`
- Menus: `DrawPokemonMenu.java`, `DrawSetupMenu.java`, ...
