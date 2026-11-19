# Core characters

Designed by the project owner. Portraits (64x64) are in `assets/npc/portraits/`, overworld sprites
in `assets/npc/<id>-walking.png`. Code: `Characters.java` (personality, teams, gifts, quests,
romance) on top of `Npcs.java` (arrival, housing, wandering).

Legend: **[built]** works now, **[battles]** needs trainer battles (next milestone), **[later]** planned.

## Shared systems

| System | How it works |
|---|---|
| Arrival **[built]** | Like every villager: needs a free BED in a house you built. One arrival per morning. |
| Portrait **[built]** | 64x64 portrait above the text box whenever they talk. |
| Team **[built]** | Starts with 2 first-stage Pokemon. Every morning: levels rise toward (catch cap - 3); 35% chance to catch a new first-stage one from their affinity (Emma/Wood/Blue: anything). Members evolve when they reach the level (stone/trade/friendship evolutions use stand-in levels 32/38/25; branching ones like Eevee pick at random). Max 6, weakest replaced. They tell you about catches and evolutions. |
| "!" marker **[built]** | A blinking "!" bubble over any villager with something for you: a quest to offer or turn in, a gift ready, an evolution to brag about, or a confession. |
| Friendship 0-10 **[built]** | +1 first talk each day, +2 per gift (one gift per day), +3 for finishing their quest. |
| Romance 0-10 **[built]** | Emma, Wood, Lavender only. +1 per daily talk once friendship is 5+, +1 per gift, +2 for their quest. |
| Gifts **[built]** | After talking: "Give a gift?" opens a GIVE A GIFT menu listing the things they love. |
| Quest **[built]** | One personal quest, offered once friendship reaches 3. Completed by talking to them again. |
| Confession **[built]** | Romance 10 + friendship 10 + quest done: they confess. YES = partner (one at a time). NO = romance drops to 7, they may try again after a week. |
| Rival battles **[built]** | Their real team (above) is what you fight. |
| League roles **[battles]** | Any of them can become a Gym Leader, Elite Four member or Champion (see bottom). |

## The characters

### 1. Emma (romanceable, rival-adjacent)
- **Arrives:** day 1+. **Team:** a starter + a Dark type; catches anything.
- **Personality:** taunts you, extremely competitive. Lines warm up from insults -> grudging respect -> flustered denial.
- **Loves:** Dusk Ball, Black Glasses, Rare Candy, Nugget.
- **Quest "Prove It" [built]:** catch a Dark type. Reward: 5 Dusk Balls.
- **Rival arc [battles]:** challenges you every few days ("Rematch?"). Her win/loss record vs you is tracked; she trains harder (team catches up faster) after each loss.
- **Romance arc:** quest -> "Moonlit Race" [later]: meet her on the beach at night and reach a far marker before her -> confession.
- **As partner [later]:** still challenges you, but her daily gift is a Dark-type Egg-chance item / Rare Candy; she joins you in double battles against bosses.

### 2. Flambar (not romanceable)
- **Arrives:** once you've caught a Fire type. **Team:** Fire types.
- **Personality:** sweet and caring; turns serious and focused in battle.
- **Loves:** Charcoal, Fire Stone, Berry Juice, Soft Bedding.
- **Quest "Hearth" [built]:** bring 10 Charcoal. Reward: Fire Stone.
- **Later:** after the quest, a hearth tile in her house heals your team overnight when you sleep nearby; natural Fire Gym Leader.

### 3. Wood (romanceable, rival-adjacent)
- **Arrives:** day 2+. **Team:** a starter + a Fighting type; catches anything.
- **Personality:** fierce and protective.
- **Loves:** Black Belt, Protein, Rare Candy, Hard Stone.
- **Quest "Guardian" [built]:** beat a boss after accepting. Reward: 2 Rare Candy.
- **Rival arc [battles]:** "training spars" rather than taunts; if you lose to a boss, he challenges you next morning "to toughen you up".
- **Romance arc:** quest -> "Night Watch" [later]: stay out past midnight with him near the village while ghosts roam -> confession.
- **As partner [later]:** aggressive wild Pokemon near your house are driven off; he occasionally fights a wild battle for you (tag-in).

### 4. Willow (not romanceable)
- **Arrives:** once a PC exists in any house. **Team:** Electric types.
- **Personality:** nerdy, spontaneous, rambling.
- **Loves:** Magnet, Metal Coat, Thunderstone, Thin Paper.
- **Quest "Spark Lab" [built]:** bring 4 Magnets + 2 Metal Coats. Reward: Thunderstone.
- **Later:** her lab upgrades your Pokedex (shows where species live) and PC (more boxes); Electric Gym Leader.

### 5. Red (boss NPC, not a villager)
- **Later [battles]:** a silent figure who appears on a far island (ring 3+) after the Elite Four is formed. The hardest battle in the game; his team is at the level cap + 10. Beating him unlocks the last catch-cap raise.

### 6. Blue (quest + boss NPC)
- **Arrives:** once 4 villagers live on the island. **Team:** anything, strong.
- **Quest "Village" [built]:** have 5 villagers. Reward: 5 Ultra Balls.
- **League questline [battles]:** Blue is how the League starts: build a Gym (new building), assign a leader, repeat x8, then he oversees the Elite Four, and finally challenges you as Champion before Red appears.

### 7. Bruno (not romanceable)
- **Arrives:** after your first boss. **Team:** Dark types.
- **Personality:** threatening. Softens only after you prove yourself.
- **Loves:** Black Glasses, Dusk Ball, Nugget.
- **Quest "Earn It" [built]:** beat 2 more bosses. Reward: 2 Nuggets.
- **Later [battles]:** ambushes you at night once a week for a battle until his quest is done; natural Elite Four member.

### 8. Lavender (romanceable)
- **Arrives:** once you've caught a Ghost type. **Team:** Ghost types.
- **Personality:** cute and scary.
- **Loves:** Spell Tag, Dusk Ball, Thin Paper, Moon Stone.
- **Quest "Lost Spirits" [built]:** catch 3 different Ghost types. Reward: Spell Tag.
- **Romance arc:** quest -> "Midnight Graveyard" [later]: meet her in the graveyard biome at midnight -> confession.
- **As partner [later]:** the game's night ghost leaves you alone ("I asked nicely").

### 9. Locke (not romanceable)
- **Arrives:** once you've seen 15 species. **Team:** Grass, Ground, Rock, Bug.
- **Personality:** calm, earthy, terse.
- **Loves:** Miracle Seed, Hard Stone, Silver Powder, Damp Soil.
- **Quest "Roots" [built]:** catch 4 Bug or Grass types. Reward: Miracle Seed.
- **Later:** apricorn and berry trees near his house grow twice as fast; Grass or Rock Gym Leader.

## League Director [built: appointing gym leaders + badges; Elite Four later]
A dedicated NPC who runs everything League-related, separate from Blue (who stays the quest giver and
rival/Champion figure). Arrives once trainer battles exist and Blue's "Village" quest is done.
- Unlocks the GYM building (BUILD menu) and certifies each gym you build.
- Lets you appoint leaders from your villagers (any core character, or generic ones) and pick each gym's type.
- Tracks badges; each badge raises the catch cap (alongside bosses).
- After 8 gyms: unlocks the Elite Four hall, appoint 4 members + a Champion.
- Schedules rematches and runs the post-game (Red's challenge).

## League potential [battles]
Any core character can take a slot. Natural fits: Flambar (Fire), Willow (Electric), Locke (Grass/Rock),
Lavender (Ghost), Bruno (Dark, Elite Four), Emma or Wood (Champion contenders / rival slots), Blue
(organiser, then Champion), Red (post-game). The player picks who goes where.
