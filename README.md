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

## Added features (step 1)
- Survival tools: stone/iron axe, pick, shovel (campfire / kiln). Use from bag, Z to use, hold X to put away.
- PC (indoor build): one shared Pokemon storage across every PC.
- Healing machine (indoor build): full heal + status cure.
- Pokedex (campfire craft): seen/caught tracking, national order, detail pages.
- Balls: Repeat, Luxury, Hunter (kiln). Dive Ball bonus bug fixed.
- Catch level cap: wild Pokemon above it can't be caught (ball isn't used). Starts at LV20,
  +10 per boss beaten. Bosses are always cap+5, exempt from the cap, and every living boss
  levels up when you beat one.
- Bosses: one per biome (respawns 5 days after defeat), rare roaming
  bosses (mornings + 1/256 tall-grass encounters). 2x HP, rage at half HP, no fleeing,
  catchable below 1/4 HP, pulsing aura on the map. Each defeat unlocks the next locked recipe:
  iron tools, ultra balls, healing machine, repeat/hunter/luxury balls.
  Tuning knobs at the top of Boss.java.

- Caves: up to 4 entrances on rocky mountain ground per world. 3 procedurally generated floors
  each (GSC Dark Cave tiles), linked by ladders, generated on first visit and saved. Cave
  Pokemon get stronger per floor; floors hold copper/iron/gold/crystal ore rocks (SMASH; gold
  and crystal need an IRON PICK or a fully evolved Pokemon). Iron tools now cost iron ore;
  the kiln turns ore into metal coat, magnets, nuggets and evolution stones.

- Shipwreck loot: 1-2 crates wash up on seaside beach tiles each morning (max 6). Press A to
  open: logs plus 1-3 random finds (balls, ores, crafting materials, rare candy...).

- Islands: the game already generates a new island when you surf/fly past the map edge
  (saved as map x,y). Now: islands are gated by distance from home (|x|+|y|): RAFT (campfire)
  for 1 sea out, BOAT (kiln) for 2, SAILBOAT (kiln, needs deep-cave ore) for any; heading home
  is always allowed. Each island rolls 3 special species (rarer farther out) that replace 15%
  of its wild encounters; ring 3+ islands have a 35% chance of a legendary boss (birds/beasts).
  Caves, bosses and crates work per island.

- Villagers (Npcs.java): NPCs move in only if a house you built has a free BED; each morning
  one whose unlock condition is met may arrive and wander near that house's door by day (home at
  night). Press A to talk; daily talks raise friendship (gifts from 3). Roster: MERCHANT (always,
  barter), NURSE (10 caught, free heals), HIKER (visited a cave, ore upgrades), FISHER (raft or
  rod, rod upgrades), SCIENTIST (30 seen, crystal ore -> stones), ELDER (beat a boss, boss hints).
  Trades open in the crafting menu (TRADE MENU). Saved per island. Sprites: assets/npc/, all 69
  extracted Crystal overworld characters in art-reference/npc-rows/.

- Core characters (Characters.java, docs/CHARACTERS.md): Emma, Flambar, Wood, Willow, Bruno,
  Lavender, Locke, Blue arrive as villagers with 64x64 portraits, teams that catch Pokemon over
  time, gifts, friendship, romance (Emma/Wood/Lavender), personal quests and confessions.

## Dev mode keys (run with `dev`)
- F2: spawn the next villager on the faced tile (no house needed)
- F3: force a villager arrival check (needs a free bed)
- F4: jump next to the map's right edge (to test island travel)
- F5: place a shipwreck crate on the faced tile
- F6: jump to the nearest cave entrance
- F7: spawn a boss two tiles ahead
- F8: give a Pokedex, a SAILBOAT and 20 of each new ball
- F9 / F10: place a PC / healing machine on the faced tile
