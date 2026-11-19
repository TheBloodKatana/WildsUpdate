# Progression

How a save goes from a castaway on a beach to a League with a Champion, then to the next
generation. Status: **design** unless marked **[built]**. Companion to `CHARACTERS.md`.

---

## 1. The big picture

| Phase | You play | Goal | Catch level cap rises from |
|---|---|---|---|
| **1. Survivor** | Your character | Survive, build islands and towns, beat bosses, set up the League, win the regional tournament | Bosses beaten [built] |
| **2. The journey** | Your kid | Classic Pokemon journey through the region you built: badges, trainers, Elite Four, Champion | Badges |
| **3+. Next generations** | The kid's kid (optional) | Same as phase 2, in an older world | Badges |

Phase 1 ends when you win the regional tournament and choose to **pass the torch**.

---

## 2. The calendar

- **1 year = 4 seasons x 28 days = 112 days.** Spring (days 1-28), Summer (29-56), Fall (57-84), Winter (85-112).
- **Local tournaments:** one per season, in one gym town (see section 4 for which).
- **Regional tournament:** once a year, on the **last day of winter** (day 112).
- A local tournament never falls in the last 7 days of winter, so the regional always has the stage to itself.

### Announcements
Every tournament gets two alert banners (and a bulletin board post in every town):

| When | Banner |
|---|---|
| 3 days before | `The CEDAR TOWN TOURNAMENT starts in 3 days at CEDAR TOWN plaza!` |
| Morning of | `The CEDAR TOWN TOURNAMENT starts TODAY at CEDAR TOWN plaza!` |
| Regional, 7 days before | `The REGIONAL TOURNAMENT starts in 7 days at the LEAGUE HALL in CORAL CITY!` |
| Regional, morning of | `The REGIONAL TOURNAMENT starts TODAY at the LEAGUE HALL in CORAL CITY!` |

Results get a third banner that evening, e.g. `WOOD keeps his gym in CEDAR TOWN!` or `YOUNGSTER JOEY takes over the CEDAR TOWN gym!`.

---

## 3. Every NPC has a home town

The rest of this doc depends on one rule: **each NPC belongs to exactly one town**, the founded town
nearest to their bed (townsfolk already store theirs). An NPC whose home is not near any town belongs
to no town and can't enter local tournaments until a town is founded near them.

A villager's home town only changes if they move house, or if a new town is founded that's closer.

---

## 4. Which town hosts the local tournament

Each season, one gym town hosts a local tournament on a random day of that season. Towns take turns in
a **cycle**, in random order:

1. At the start of a cycle, every town that has at least one gym goes into the **pool**.
2. Each season, a random town is drawn from the pool, hosts, and leaves the pool.
3. When the pool is empty, the cycle resets: every gym town goes back in, **except the town that
   hosted last**, so no town hosts twice in a row across a reset. It goes back in after the next draw.
4. A town that gets its first gym mid-cycle joins the current pool right away.
5. A town that loses all its gyms (shrank below village size) leaves the pool.

Example with 5 gym towns: seasons go Town 1, Town 5, Town 3, Town 2, Town 4, then a reset that can
pick anyone except Town 4.

> With one local tournament per season, a full cycle of 8 gyms takes 2 in-game years. If that feels
> too slow, `LOCALS_PER_SEASON` can be set to 2 (one every 14 days) so 8 gyms cycle once a year.

---

## 5. Local tournaments

### Who enters (8 entrants)
In priority order until there are 8:
1. The host town's current **Gym Leaders** (a village has 1, a town 2, a city 3).
2. **You**, if you talk to the host at the plaza before noon on the day.
3. The previous local tournament's **runner-up list** for this town (see succession).
4. **Core characters** whose home town is this one and who hold no League role.
5. **Settled trainers** from this town, strongest first.
6. **Townsfolk** from this town, strongest first. (Townsfolk get small teams for this.)

Not allowed: Elite Four members and the Champion (their spot is decided only at the regional), and
leaders of *other* towns (they defend in their own town).

If fewer than 4 can enter, the tournament is skipped, the town still leaves the pool, and the Director
keeps whoever holds the gym.

### How it runs
- Single elimination, 8 to 4 to 2 to 1, from noon to evening at the plaza. Entrants walk there in the morning.
- **Your matches** are real trainer battles. Your team is healed between rounds. If you black out, you're out.
- **Every other match is simulated** (section 8). If you're not there, the whole thing is simulated.

### Results
- The finishing order is: winner, runner-up, the two semifinal losers (better simulated score first),
  then the quarterfinal losers.
- The town's **gym slots go to the best finishers who aren't you**, top down.
- The rest of that order becomes the town's **succession list**, which is used when a slot opens up mid-season (section 7).
- **If you win:** a trophy for your house, a rare prize, and town reputation. In phase 1 you can't
  hold a gym, so it goes to the next finisher.
- A leader who loses their slot goes back to being a regular villager, with some sulking dialogue for a week.

---

## 6. The regional tournament

### Requirements
- **8 or more towns with a Gym Leader.**
- A **LEAGUE HALL**: a building the Director asks you to build in a **CITY** (10+ houses). That's
  where the regional happens and where the Elite Four and Champion spend their noons.

### Who enters (16 entrants)
1. The **Champion** and the **Elite Four**, seeded so they can't meet in round 1.
2. **One Gym Leader from every gym town** (in a town with several, the one who finished best last time).
3. **You**, if you talk to the Director at the League Hall before noon.
4. The rest is filled with the strongest **trainers who hold no role**, from any island.

### Results
- **Champion:** the winner.
- **Elite Four:** the runner-up, the two semifinal losers, and the best quarterfinal loser.
- Anyone who was Champion or Elite Four and didn't place loses the title and goes back to being a free trainer.
- **If you win in phase 1, you're Champion**, and the game asks: **Pass the torch?**
  - **YES:** phase 2 starts (section 9).
  - **NOT YET:** keep playing as yourself, defend your title next year, and pass the torch whenever you win again.
- In phase 1 you can't take an Elite Four spot: if you place 2nd-5th, the spot goes to the next finisher.

---

## 7. Vacancies: who fills a spot when someone moves

### Core rules
- **One League role at a time.** Taking a higher role drops the lower one. The order is Champion, then Elite Four, then Gym Leader.
- **Badges belong to the gym, not the leader.** If a gym changes hands, the kid keeps that town's
  badge and can rematch the new leader for fun.
- **Gaps are filled the same day,** so no gym, Elite Four or Champion spot is ever empty.

### When each spot opens and who fills it

| Situation | What happens to the person | Who fills the spot |
|---|---|---|
| Leader loses the local tournament | Becomes a regular villager | The tournament's finishers (section 5) |
| Leader becomes Elite Four or Champion at the regional | Leaves the gym | Next on that town's **succession list** |
| Succession list is empty | n/a | The Director names an **ACTING LEADER**: the strongest eligible villager from that town. They hold the spot until that town's next local tournament |
| Nobody in the town is eligible at all | n/a | The gym stays **closed** (sign on it, gossip about it) until someone eligible moves in or the next local tournament |
| Elite Four or Champion doesn't place at the regional | Becomes a free trainer. Can enter their home town's next local tournament | Regional finishers (section 6) |
| Town shrinks (city to town, etc.) and has too many leaders | The lowest finisher steps down to the top of the succession list | Nobody; the slot is gone |
| Town grows and gets a new slot | n/a | Next on the succession list, or the Director appoints one if it's empty |
| A leader's house loses its bed | Keeps the role and stays at the gym town plaza until housed again. The Director nags you about it | n/a |
| A new town is founded closer to a leader's home | Their home town changes, but they **keep their current gym** until they lose it | n/a |

### Where people physically are
- **Gym Leaders:** their own routine, except **noon at their gym town** [built].
- **Elite Four and Champion:** **noon at the League Hall**, otherwise their own routine.
- **Tournament day overrides everyone's routine:** entrants walk to the host plaza in the morning and
  go home in the evening. Anyone entered in a tournament on another island is away all day; townsfolk mention it.
- **Spectators:** the host town's townsfolk and anyone with no role who lives within 30 tiles show up at the plaza.

---

## 8. Simulated battles

For matches you're not in, each side gets a score:

```
score = sum over team of (level x 1.0)          // raw strength
      + 15 x type edge                          // their affinity types vs the opponent's (-1, 0, +1 per matchup, averaged)
      + random(0, 40)                           // luck: upsets happen
```

The higher score wins. A gap of 60 or more is an automatic win, so a LV60 team never loses to LV12s.
Results are stored (who beat whom, and how close it was) so townsfolk can gossip about upsets and
characters can react ("I lost to a BUG CATCHER. Don't talk to me.").

---

## 9. Passing the torch (phase 2)

When you say YES:

1. **You become a core character: Dad** (or Mom, matching your character's sprite).
   - His team is **exactly the team that won the finals**. From then on it grows daily like everyone else's.
   - He lives in your house with your partner, if you have one. He gets a routine, dialogue, quests
     for the kid, and needs a portrait.
   - He follows **all the rules above**: he can lose the Champion title, drop to the Elite Four,
     take over a gym through a local tournament, or end up a free trainer. The kid might meet him anywhere.
2. **The kid** spawns in the family house with an empty bag, no Pokemon and their own empty PC.
   - **Professor Willow** offers 3 starters: one Fire, one Water and one Grass, picked at random from the starter pool.
   - Kid catch level cap: **LV15, +10 per badge**, LV100 once Champion.
   - The kid **can** hold a gym, an Elite Four spot or the Champion title by winning tournaments.
3. **Everything keeps running:** seasons, local tournaments, the regional, all simulated unless the kid takes part.
4. **The world ages:** every core character's team has grown, townsfolk get new lines about "back when
   your dad washed up here", and your partner becomes the kid's other parent.
5. **Rival:** if Emma, Wood or another core character has a child too (a partner of their own,
   simulated), that kid becomes the rival.
6. **Next generation:** if the kid wins the regional, they can pass the torch too.

---

## 10. What changes in the current build

- Badges **stop raising your character's catch cap in phase 1**. Only bosses raise it there; badges become phase 2.
- **Phase 1 gym battles stay** [built], but give friendship and a win record instead of badges.
- **The Director's job changes:** he appoints the *first* leader of each gym [built], then hosts tournaments
  that decide every leader after that.
- Townsfolk need small teams so they can enter.
- Not built yet: alert banners, seasons, the bulletin board, the League Hall, tournaments, and the phase 2 switch.

## 11. Build order

1. Alert banners (needed for announcements).
2. Seasons and the calendar.
3. Home towns, succession lists, and the vacancy rules (section 7).
4. Simulated battles and local tournaments (with you as an entrant).
5. League Hall, regional tournament, Elite Four, and Champion.
6. Passing the torch: Dad as a core character, the kid, Professor Willow.
