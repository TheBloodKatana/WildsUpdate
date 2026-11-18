The Pokemon crystal decompile has tutorials on how to use the disassembly format: https://github.com/pret/pokecrystal/wiki/Tutorials

**Custom Pokemon shiny colors** - below is the shiny palette found under mods/pokemon/pikachu/shiny.pal. This format is the same format used in asm romhacks:

	RGB 31, 17, 00  <-- lighter color
	RGB 20, 02, 11  <-- darker color

Modify the numbers to change r/g/b values. Values must be between 0 and 31 inclusive. 

To convert r/g/b value from 0-99 to this range use this formula: value*31/99
To convert r/g/b value from 0-255 to this range use this formula: value*31/255

(I can't math very well so lmk if that's incorrect.)


**Custom Pokemon Animation, Sprites and Cry**
 - front.png - front sprite animation, played from top to bottom. If you just want to use a single front sprite, that will also work fine.
 - back.png - back sprite.
 - overworld.png - overworld sprites. Use the same frame order as the example provided in mods/pokemon/pikachu/overworld.png.
 - cry.ogg - Pokemon cry in ogg format. All Pokemon cries available for download here: https://play.pokemonshowdown.com/audio/cries/
 - anim.asm - defines the front sprite animation and uses the same format as used by romhacks. I don't know of a good tutorial for this; if you know of one, feel free to post it in the discord. 

Note: the Pokemon that are loaded from Prism are weird and will require back.png to use the shiny colors. Additionally there needs to be a normal.pal file. Example under mods/gible/. Prism Pokemon lines that are currently in the game - numel, gible, cacnea, skorupi, feebas, trapinch, cranidos, shieldon, lileep, anorith, beldum, solrock, lunatone, duskull, sylveon, leafeon, glaceon, weavile, mamoswine, magmortar, mismagius, rhyperior.

Use the sample(s) given in the directory for reference. Let me know of any ideas for kinds of mods to support that you have :)


**Custom Stats and Learnsets**
 - base_stats.asm - Pokemon base stats in the disassembly format.
 - evos_attacks.asm - Pokemon evos and learnset in the disassembly format.
 - egg_moves.asm - Pokemon egg moves in the disassembly format.
 - Reference the crystal decompile for tutorials (link above).

**Custom Music**
 - Place an audio file named wild_battle.ogg in this directory. The audio must be in the .ogg format.
 - Add music files in the .ogg format to the music directory to mod game music. A full list of game music can be found here (you will need to match the file names exactly in order to replace that music file): https://github.com/SheerSt/pokemon-wilds/tree/main/music
