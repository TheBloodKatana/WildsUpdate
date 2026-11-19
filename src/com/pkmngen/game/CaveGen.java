package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 * Procedural caves inside mountains.
 *
 * Entrances ("cave_door1" objects) are placed on rocky mountain ground once per world. Each cave
 * has FLOORS floors stored as interior layers (base, base-1, base-2 ...), generated the first
 * time the player walks in and saved with the map afterwards. Floors are linked by ladders
 * (stairs tiles), deeper floors have stronger Pokemon and richer ore.
 *
 * Tiles come from the GSC Dark Cave / Mt. Mortar tileset (assets/tiles/cave3_*.png).
 */
class CaveGen {
   static final int MAX_CAVES = 4;
   static final int FLOORS = 3;
   static final int W = 30;
   static final int H = 24;
   static final int MIN_SPACING = 640; // px between entrances

   static int baseIndex(int caveNumber) {
      return 88 - FLOORS * caveNumber;
   }

   static String key(Vector2 pos) {
      return (int)pos.x + "," + (int)pos.y;
   }

   static boolean isEntrance(Game game, Tile tile) {
      return tile != null && tile.nameUpper.equals("cave_door1") && game.player.caveEntrances.containsKey(key(tile.position));
   }

   // ------------------------------------------------------------------ entrances

   static void placeEntrances(Game game) {
      if (game.player.cavesPlaced || game.map.overworldTiles == null) {
         return;
      }
      ArrayList<Tile> candidates = new ArrayList<>();
      for (Tile tile : game.map.overworldTiles.values()) {
         if (tile.routeBelongsTo == null || !tile.routeBelongsTo.name.startsWith("mountain")) {
            continue;
         }
         if (!tile.name.startsWith("mountain") || tile.isSolid || tile.isLedge || !tile.nameUpper.equals("")) {
            continue;
         }
         Tile below = game.map.overworldTiles.get(tile.position.cpy().add(0.0F, -16.0F));
         if (below == null || below.isSolid || below.isLedge || below.isWater || !below.nameUpper.equals("")) {
            continue;
         }
         candidates.add(tile);
      }
      Collections.shuffle(candidates, new Random(game.map.rand.nextLong()));
      ArrayList<Tile> chosen = new ArrayList<>();
      for (Tile tile : candidates) {
         boolean farEnough = true;
         for (Tile other : chosen) {
            if (other.position.dst(tile.position) < MIN_SPACING) {
               farEnough = false;
               break;
            }
         }
         if (farEnough) {
            chosen.add(tile);
            if (chosen.size() >= MAX_CAVES) {
               break;
            }
         }
      }
      for (int i = 0; i < chosen.size(); i++) {
         Tile tile = chosen.get(i);
         tile.nameUpper = "cave_door1";
         tile.init();
         game.player.caveEntrances.put(key(tile.position), i);
         System.out.println("Cave " + i + " entrance at " + tile.position);
      }
      game.player.cavesPlaced = true;
   }

   /** Returns the interior index for this entrance, generating the cave on first visit. */
   static int enter(Game game, Tile door) {
      int number = game.player.caveEntrances.get(key(door.position));
      int base = baseIndex(number);
      if (game.map.getInteriorLayer(base).isEmpty()) {
         int level = door.routeBelongsTo != null ? door.routeBelongsTo.level : 20;
         generate(game, door.position.cpy(), base, level, new Random(door.position.hashCode() * 31L + number));
      }
      return base;
   }

   // ------------------------------------------------------------------ generation

   static void generate(Game game, Vector2 entrance, int base, int level, Random rng) {
      Vector2 start = entrance.cpy();
      for (int f = 0; f < FLOORS; f++) {
         start = generateFloor(game, base - f, f, start, level, rng);
      }
   }

   /** Builds one floor whose start cell sits on world position {@code start}. Returns the ladder-down position. */
   static Vector2 generateFloor(Game game, int index, int depth, Vector2 start, int level, Random rng) {
      boolean[][] floor = new boolean[W][H];
      int sx = W / 2;
      int sy = depth == 0 ? 1 : H / 2;
      float ox = start.x - sx * 16;
      float oy = start.y - sy * 16;

      // Random walk with a little momentum, plus the odd small chamber.
      int target = (int)(W * H * (0.27F + 0.03F * depth));
      int count = 0;
      int x = sx;
      int y = sy;
      int dir = 1;
      int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
      if (depth == 0) {
         for (int i = 1; i <= 4; i++) {
            if (!floor[sx][i]) {
               floor[sx][i] = true;
               count++;
            }
         }
         y = 4;
      }
      for (int steps = 0; count < target && steps < 40000; steps++) {
         if (!floor[x][y]) {
            floor[x][y] = true;
            count++;
         }
         if (rng.nextInt(100) < 3) {
            for (int i = -1; i <= 1; i++) {
               for (int j = -1; j <= 1; j++) {
                  int cx = x + i;
                  int cy = y + j;
                  if (cx > 1 && cy > 1 && cx < W - 2 && cy < H - 2 && !floor[cx][cy]) {
                     floor[cx][cy] = true;
                     count++;
                  }
               }
            }
         }
         if (rng.nextInt(100) < 25) {
            dir = rng.nextInt(4);
         }
         int nx = x + dirs[dir][0];
         int ny = y + dirs[dir][1];
         if (nx < 1 || ny < 2 || nx > W - 2 || ny > H - 2) {
            dir = rng.nextInt(4);
            continue;
         }
         x = nx;
         y = ny;
      }
      // Smooth: fill-in walls surrounded by floor.
      for (int i = 2; i < W - 2; i++) {
         for (int j = 2; j < H - 2; j++) {
            if (!floor[i][j] && neighbours8(floor, i, j) >= 6) {
               floor[i][j] = true;
            }
         }
      }

      // Distances from the start, pick the ladder at the far end.
      int[][] dist = bfs(floor, null, sx, sy);
      int lx = sx;
      int ly = sy;
      for (int i = 0; i < W; i++) {
         for (int j = 0; j < H; j++) {
            if (dist[i][j] > dist[lx][ly]) {
               lx = i;
               ly = j;
            }
         }
      }
      boolean hasLadder = depth < FLOORS - 1;

      // Ores and plain boulders, never cutting the floor in two.
      String[][] objects = new String[W][H];
      boolean[][] blocked = new boolean[W][H];
      int ores = 5 + 3 * depth;
      int boulders = 4;
      ArrayList<int[]> cells = new ArrayList<>();
      for (int i = 0; i < W; i++) {
         for (int j = 0; j < H; j++) {
            boolean nearStart = Math.abs(i - sx) + Math.abs(j - sy) <= 2;
            boolean nearLadder = Math.abs(i - lx) + Math.abs(j - ly) <= 1;
            if (floor[i][j] && !nearStart && !nearLadder) {
               cells.add(new int[]{i, j});
            }
         }
      }
      Collections.shuffle(cells, rng);
      // Dead ends first: they look like natural alcoves.
      cells.sort((a, b) -> Integer.compare(neighbours4(floor, a[0], a[1]), neighbours4(floor, b[0], b[1])));
      int floorCount = reachable(dist);
      int blockedCount = 0;
      for (int[] c : cells) {
         if (ores <= 0 && boulders <= 0) {
            break;
         }
         blocked[c[0]][c[1]] = true;
         if (reachable(bfs(floor, blocked, sx, sy)) != floorCount - blockedCount - 1) {
            blocked[c[0]][c[1]] = false;
            continue;
         }
         blockedCount++;
         if (ores > 0) {
            objects[c[0]][c[1]] = pickOre(depth, rng);
            ores--;
         } else {
            objects[c[0]][c[1]] = "rock_cave1";
            boulders--;
         }
      }

      // Write tiles.
      Route route = new Route("cave" + (depth + 1), level + 4 * depth);
      java.util.HashMap<Vector2, Tile> layer = game.map.getInteriorLayer(index);
      for (int i = 0; i < W; i++) {
         for (int j = 0; j < H; j++) {
            Vector2 pos = new Vector2(ox + i * 16, oy + j * 16);
            String name = null;
            String upper = "";
            if (floor[i][j]) {
               name = "cave3_floor1";
               if (i == sx && j == sy && depth > 0) {
                  name = "cave3_stairs_up1";
               } else if (hasLadder && i == lx && j == ly) {
                  name = "cave3_stairs_down1";
               } else if (objects[i][j] != null) {
                  upper = objects[i][j];
               }
            } else if (depth == 0 && i == sx && j == sy - 1) {
               name = "cave3_entrance1";
            } else if (neighbours8(floor, i, j) > 0) {
               name = rng.nextInt(10) < 7 ? "cave3_wall1" : "cave3_wall2";
            }
            if (name != null) {
               layer.put(pos, new Tile(name, upper, pos, true, route));
            }
         }
      }
      return new Vector2(ox + lx * 16, oy + ly * 16);
   }

   static String pickOre(int depth, Random rng) {
      int r = rng.nextInt(100);
      if (depth == 0) {
         return r < 60 ? "rock_ore_copper1" : r < 95 ? "rock_ore_iron1" : "rock_ore_gold1";
      } else if (depth == 1) {
         return r < 25 ? "rock_ore_copper1" : r < 70 ? "rock_ore_iron1" : r < 95 ? "rock_ore_gold1" : "rock_ore_crystal1";
      }
      return r < 35 ? "rock_ore_iron1" : r < 75 ? "rock_ore_gold1" : "rock_ore_crystal1";
   }

   static int neighbours4(boolean[][] g, int x, int y) {
      int n = 0;
      if (x > 0 && g[x - 1][y]) n++;
      if (y > 0 && g[x][y - 1]) n++;
      if (x < W - 1 && g[x + 1][y]) n++;
      if (y < H - 1 && g[x][y + 1]) n++;
      return n;
   }

   static int neighbours8(boolean[][] g, int x, int y) {
      int n = 0;
      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 1; j++) {
            int cx = x + i;
            int cy = y + j;
            if ((i != 0 || j != 0) && cx >= 0 && cy >= 0 && cx < W && cy < H && g[cx][cy]) {
               n++;
            }
         }
      }
      return n;
   }

   static int[][] bfs(boolean[][] floor, boolean[][] blocked, int sx, int sy) {
      int[][] dist = new int[W][H];
      for (int[] row : dist) {
         java.util.Arrays.fill(row, -1);
      }
      ArrayDeque<int[]> queue = new ArrayDeque<>();
      dist[sx][sy] = 0;
      queue.add(new int[]{sx, sy});
      int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
      while (!queue.isEmpty()) {
         int[] c = queue.poll();
         for (int[] d : dirs) {
            int nx = c[0] + d[0];
            int ny = c[1] + d[1];
            if (nx < 0 || ny < 0 || nx >= W || ny >= H || !floor[nx][ny] || dist[nx][ny] >= 0) {
               continue;
            }
            if (blocked != null && blocked[nx][ny]) {
               continue;
            }
            dist[nx][ny] = dist[c[0]][c[1]] + 1;
            queue.add(new int[]{nx, ny});
         }
      }
      return dist;
   }

   static int reachable(int[][] dist) {
      int n = 0;
      for (int[] row : dist) {
         for (int d : row) {
            if (d >= 0) {
               n++;
            }
         }
      }
      return n;
   }
}
