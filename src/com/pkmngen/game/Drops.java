package com.pkmngen.game;

import java.util.HashMap;
import java.util.Locale;

/**
 * Wild Pokemon have a 10% chance to drop an item when they faint, based on what they are:
 * bugs leave SILKY THREAD, birds SOFT FEATHER, fire types CHARCOAL, and so on. A few species have
 * their own drop (MAREEP wool, MILTANK milk, SHELLDER pearls...). Trainers' Pokemon drop nothing.
 */
class Drops {
   static final int CHANCE = 10;   // percent

   // type -> drops (first is the usual one, the rest rarer)
   static final HashMap<String, String[]> BY_TYPE = new HashMap<>();
   static final HashMap<String, String> BY_SPECIES = new HashMap<>();
   static {
      BY_TYPE.put("BUG", new String[]{"silky thread", "silky thread", "honey"});
      BY_TYPE.put("FLYING", new String[]{"soft feather"});
      BY_TYPE.put("FIRE", new String[]{"charcoal", "charcoal", "ashen soil"});
      BY_TYPE.put("WATER", new String[]{"damp soil", "big pearl"});
      BY_TYPE.put("GRASS", new String[]{"berry seed", "berry seed", "miracle seed"});
      BY_TYPE.put("ELECTRIC", new String[]{"magnet"});
      BY_TYPE.put("ICE", new String[]{"nevermeltice"});
      BY_TYPE.put("FIGHTING", new String[]{"binding band"});
      BY_TYPE.put("POISON", new String[]{"poison barb"});
      BY_TYPE.put("GROUND", new String[]{"soft sand", "soft sand", "dry soil"});
      BY_TYPE.put("ROCK", new String[]{"hard stone", "hard stone", "star piece"});
      BY_TYPE.put("GHOST", new String[]{"spell tag", "cursed soil"});
      BY_TYPE.put("PSYCHIC", new String[]{"psi energy"});
      BY_TYPE.put("DARK", new String[]{"dark energy"});
      BY_TYPE.put("DRAGON", new String[]{"dragon scale", "dragon scale", "dragon fang"});
      BY_TYPE.put("STEEL", new String[]{"metal coat"});
      BY_TYPE.put("NORMAL", new String[]{"soft wool", "moomoo milk"});
      for (String s : new String[]{"mareep", "flaaffy", "ampharos", "wooloo", "dubwool"}) {
         BY_SPECIES.put(s, "soft wool");
      }
      BY_SPECIES.put("miltank", "moomoo milk");
      BY_SPECIES.put("combee", "honey");
      BY_SPECIES.put("vespiquen", "honey");
      BY_SPECIES.put("teddiursa", "honey");
      BY_SPECIES.put("heracross", "honey");
      BY_SPECIES.put("shellder", "big pearl");
      BY_SPECIES.put("cloyster", "big pearl");
      BY_SPECIES.put("clamperl", "big pearl");
      BY_SPECIES.put("shuckle", "berry juice");
      BY_SPECIES.put("staryu", "stardust");
      BY_SPECIES.put("starmie", "star piece");
      BY_SPECIES.put("clefairy", "moon stone");
      BY_SPECIES.put("slowpoke", "sweet apple");
   }

   /** Rolls the drop for a fainted wild Pokemon. Adds it to the bag and returns the message, or null. */
   static String roll(Game game, Pokemon p) {
      if (p == null || game.battle.trainer != null || Game.rand.nextInt(100) >= CHANCE) {
         return null;
      }
      String item = BY_SPECIES.get(p.specie.name.toLowerCase(Locale.ROOT));
      if (item == null && p.types != null && !p.types.isEmpty()) {
         String type = p.types.get(Game.rand.nextInt(p.types.size())).toUpperCase(Locale.ROOT);
         String[] pool = BY_TYPE.get(type);
         if (pool != null) {
            item = pool[Game.rand.nextInt(pool.length)];
         }
      }
      if (item == null) {
         return null;
      }
      game.player.addItem(item, 1);
      return p.nickname.toUpperCase(Locale.ROOT) + " dropped " + item.toUpperCase(Locale.ROOT) + "!";
   }
}
