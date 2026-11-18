package com.pkmngen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import java.util.Locale;

class ServerBroadcast extends Action {
   int timeStep = 0;
   public Action.Layer layer = Action.Layer.map_1;

   public ServerBroadcast(final Game game) {
      super();
      game.server
         .addListener(
            new Listener() {
               public void received(final Connection connection, final Object object) {
                  Runnable runnable = new Runnable() {
                     @Override
                     public void run() {
                        try {
                           if (object instanceof Network.Login) {
                              Network.Login login = (Network.Login)object;
                              if (!game.players.containsKey(login.playerId)) {
                                 String playerId = login.playerId;
                                 Player player = new Player();
                                 Vector2 startLoc = game.map.edges.get(game.map.rand.nextInt(game.map.edges.size()));
                                 player.position.set(startLoc);
                                 player.spawnLoc.set(startLoc);
                                 player.type = Player.Type.REMOTE;
                                 player.network = player.new Network(player.position);
                                 player.network.connectionId = connection.getID();
                                 player.network.id = playerId;
                                 player.network.number = String.valueOf(game.players.keySet().size());
                                 player.setColor(login.color);
                                 player.network.tiles = game.map.overworldTiles;
                                 player.currPokemon = new Pokemon("machop", 6);
                                 player.pokemon.add(player.currPokemon);
                                 game.players.put(playerId, player);
                              }

                              Player player = game.players.get(login.playerId);
                              player.network.connectionId = connection.getID();
                              Network.PlayerData playerData = new Network.PlayerData(player);
                              connection.sendTCP(playerData);
                              if (game.battles.containsKey(player.network.id)) {
                                 Battle battle = game.battles.get(player.network.id);
                                 Pokemon oppPokemon = battle.oppPokemon;
                                 connection.sendTCP(new Network.BattleData(oppPokemon));
                              }

                              if (player.standingAction == null) {
                                 player.standingAction = new PlayerStanding(game, player, false, true);
                                 game.insertAction(player.standingAction);
                              }

                              Network.MapTiles mapTiles = new Network.MapTiles();

                              for (Vector2 position : player.network.loadingZone.allPositions()) {
                                 Tile tile = game.map.overworldTiles.get(position);
                                 if (tile != null) {
                                    mapTiles.tiles.add(new Network.TileData(tile));
                                    tile = game.map.interiorTiles.get(game.map.interiorTilesIndex).get(position);
                                    if (tile != null) {
                                       mapTiles.tiles.add(new Network.TileData(tile, game.map.interiorTilesIndex));
                                    }

                                    if (game.map.pokemon.containsKey(position)) {
                                       Pokemon pokemon = game.map.pokemon.get(position);
                                       connection.sendTCP(new Network.OverworldPokemonData(pokemon, position));
                                    }

                                    if (mapTiles.tiles.size() >= 16) {
                                       connection.sendTCP(mapTiles);
                                       mapTiles.tiles.clear();
                                    }
                                 }
                              }

                              mapTiles.dayTimer = CycleDayNight.dayTimer;
                              mapTiles.timeOfDay = game.map.timeOfDay;
                              connection.sendTCP(mapTiles);

                              for (Player otherPlayer : game.players.values()) {
                                 if (otherPlayer != player && player.network.loadingZone.contains(otherPlayer.position)) {
                                    Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(otherPlayer);
                                    connection.sendTCP(serverPlayerData);
                                    serverPlayerData = new Network.ServerPlayerData(player);
                                    game.server.sendToTCP(otherPlayer.network.connectionId, serverPlayerData);
                                 }
                              }
                           } else if (object instanceof Network.Logout) {
                              Network.Logout logout = (Network.Logout)object;
                              if (!game.players.containsKey(logout.playerId)) {
                                 System.out
                                    .println("Logout: Invalid player ID " + logout.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player playerx = game.players.get(logout.playerId);

                              for (Player otherPlayerx : game.players.values()) {
                                 if (otherPlayerx != playerx && otherPlayerx.network.loadingZone.contains(playerx.position)) {
                                    Network.Logout logoutPlayer = new Network.Logout(playerx.network.number);
                                    game.server.sendToTCP(otherPlayerx.network.connectionId, logoutPlayer);
                                 }
                              }
                           } else if (object instanceof Network.MovePlayer) {
                              Network.MovePlayer movePlayer = (Network.MovePlayer)object;
                              if (!game.players.containsKey(movePlayer.playerId)) {
                                 System.out
                                    .println(
                                       "MovePlayer: Invalid player ID " + movePlayer.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player playerx = game.players.get(movePlayer.playerId);
                              playerx.network.shouldMove = true;
                              playerx.network.dirFacing = movePlayer.dirFacing;
                              playerx.network.isRunning = movePlayer.isRunning;
                              Vector2 nextPos = playerx.position.cpy();
                              if (movePlayer.dirFacing.equals("up")) {
                                 nextPos.add(0.0F, 16.0F);
                              } else if (movePlayer.dirFacing.equals("down")) {
                                 nextPos.add(0.0F, -16.0F);
                              } else if (movePlayer.dirFacing.equals("right")) {
                                 nextPos.add(16.0F, 0.0F);
                              } else if (movePlayer.dirFacing.equals("left")) {
                                 nextPos.add(-16.0F, 0.0F);
                              }

                              for (Player otherPlayerxx : game.players.values()) {
                                 if (otherPlayerxx != playerx && otherPlayerxx.network.loadingZone.contains(nextPos)) {
                                    if (!otherPlayerxx.network.loadingZone.contains(playerx.position)) {
                                       Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(playerx);
                                       game.server.sendToTCP(otherPlayerxx.network.connectionId, serverPlayerData);
                                       serverPlayerData = new Network.ServerPlayerData(otherPlayerxx);
                                       game.server.sendToTCP(playerx.network.connectionId, serverPlayerData);
                                    }

                                    game.server
                                       .sendToTCP(
                                          otherPlayerxx.network.connectionId,
                                          new Network.MovePlayer(playerx.network.number, playerx.network.dirFacing, playerx.network.isRunning)
                                       );
                                 }
                              }
                           } else if (object instanceof Network.DoBattleAction) {
                              Network.DoBattleAction battleAction = (Network.DoBattleAction)object;
                              if (!game.players.containsKey(battleAction.playerId)) {
                                 System.out
                                    .println(
                                       "DoAttack: Invalid player id " + battleAction.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player playerx = game.players.get(battleAction.playerId);
                              if (!game.battles.containsKey(battleAction.playerId)) {
                                 System.out
                                    .println(
                                       "DoAttack: player ID not currently in battle "
                                          + battleAction.playerId
                                          + ", sent by: "
                                          + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Battle battle = game.battles.get(battleAction.playerId);
                              Network.BattleTurnData turnData = new Network.BattleTurnData();
                              if (battleAction.type == Battle.DoTurn.Type.SWITCH) {
                                 turnData.oppFirst = false;
                                 playerx.numFlees = 0;
                                 playerx.currPokemon = playerx.pokemon.get(battleAction.pokemonIndex);
                              } else if (battleAction.type == Battle.DoTurn.Type.ITEM) {
                                 turnData.oppFirst = false;
                                 playerx.numFlees = 0;
                                 turnData.itemName = battleAction.itemName;
                                 playerx.setItemAmount(turnData.itemName, playerx.getItemAmount(turnData.itemName) - 1);
                                 if (playerx.getItemAmount(turnData.itemName) <= 0) {
                                    playerx.removeItem(turnData.itemName);
                                 }

                                 if (turnData.itemName.contains("ball")) {
                                    turnData.numWobbles = Battle.gen2CalcIfCaught(game, battle.oppPokemon, battleAction.itemName);
                                    if (turnData.numWobbles == -1) {
                                       playerx.pokemon.add(battle.oppPokemon);
                                       Route var81 = game.map.tiles.get(playerx.position).routeBelongsTo;
                                    }
                                 }
                              } else if (battleAction.type == Battle.DoTurn.Type.RUN) {
                                 turnData.oppFirst = false;
                                 turnData.runSuccessful = battle.calcIfRunSuccessful(game, playerx);
                                 if (turnData.runSuccessful) {
                                    playerx.numFlees = 0;
                                 } else {
                                    playerx.numFlees++;
                                 }
                              } else {
                                 playerx.numFlees = 0;
                                 boolean found = false;

                                 for (int i = 0; i < playerx.currPokemon.attacks.length; i++) {
                                    if (playerx.currPokemon.attacks[i] != null
                                       && playerx.currPokemon.attacks[i].toLowerCase(Locale.ROOT).equals(battleAction.attack.toLowerCase(Locale.ROOT))) {
                                       found = true;
                                       break;
                                    }
                                 }

                                 if (!found) {
                                    System.out
                                       .println(
                                          "DoAttack: Invalid attack choice "
                                             + battleAction.attack
                                             + ", sent by: "
                                             + connection.getRemoteAddressTCP().toString()
                                       );
                                    throw new Exception();
                                 }

                                 turnData.playerAttack = battle.attacks.get(battleAction.attack.toLowerCase(Locale.ROOT));
                                 int yourSpeed = playerx.currPokemon.currentStats.get("speed");
                                 int oppSpeed = battle.oppPokemon.currentStats.get("speed");
                                 if (yourSpeed > oppSpeed) {
                                    turnData.oppFirst = false;
                                 } else if (yourSpeed < oppSpeed) {
                                    turnData.oppFirst = true;
                                 } else if (game.map.rand.nextInt(2) == 0) {
                                    turnData.oppFirst = true;
                                 }
                              }

                              if (battle.network.expectPlayerSwitch) {
                                 if (battleAction.type == Battle.DoTurn.Type.SWITCH) {
                                    battle.network.expectPlayerSwitch = false;
                                 }

                                 System.out.println("battle.network.expectPlayerSwitch check");
                                 throw new Exception();
                              }

                              String attackChoice = battle.oppPokemon.attacks[game.map.rand.nextInt(battle.oppPokemon.attacks.length)];
                              if (attackChoice == null) {
                                 attackChoice = "Struggle";
                              }

                              turnData.enemyAttack = battle.attacks.get(attackChoice.toLowerCase(Locale.ROOT));
                              if (!turnData.oppFirst) {
                                 int finalHealth = battle.oppPokemon.currentStats.get("hp");
                                 if (battleAction.type == Battle.DoTurn.Type.ATTACK) {
                                    int damage = Battle.gen2CalcDamage(playerx.currPokemon, turnData.playerAttack, battle.oppPokemon);
                                    int currHealth = battle.oppPokemon.currentStats.get("hp");
                                    finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                    battle.oppPokemon.currentStats.put("hp", finalHealth);
                                    turnData.playerAttack.damage = damage;
                                    if (finalHealth <= 0) {
                                       Route currRoute = game.map.tiles.get(playerx.position).routeBelongsTo;
                                       playerx.currPokemon.exp = playerx.currPokemon.exp + battle.calcFaintExp(1);

                                       while (
                                          playerx.currPokemon.level < 100
                                             && playerx.currPokemon.calcExpForLevel(playerx.currPokemon.level + 1) <= playerx.currPokemon.exp
                                       ) {
                                          playerx.currPokemon.level++;
                                       }

                                       for (int ix = 1; ix <= playerx.currPokemon.level; ix++) {
                                          if (Specie.gen2Evos.get(playerx.currPokemon.specie.name.toLowerCase(Locale.ROOT)).containsKey(String.valueOf(ix))) {
                                             String evolveTo = Specie.gen2Evos
                                                .get(playerx.currPokemon.specie.name.toLowerCase(Locale.ROOT))
                                                .get(String.valueOf(ix));
                                             playerx.currPokemon.evolveTo(evolveTo);
                                             break;
                                          }
                                       }
                                    }
                                 }

                                 if (battleAction.type == Battle.DoTurn.Type.ATTACK && finalHealth > 0
                                    || battleAction.type == Battle.DoTurn.Type.RUN && !turnData.runSuccessful
                                    || battleAction.type == Battle.DoTurn.Type.ITEM && turnData.numWobbles != -1) {
                                    int damage = Battle.gen2CalcDamage(battle.oppPokemon, turnData.enemyAttack, playerx.currPokemon);
                                    int currHealth = playerx.currPokemon.currentStats.get("hp");
                                    finalHealth = currHealth - damage > 0 ? currHealth - damage : 0;
                                    playerx.currPokemon.currentStats.put("hp", finalHealth);
                                    turnData.enemyAttack.damage = damage;
                                    if (finalHealth <= 0) {
                                       battle.network.expectPlayerSwitch = true;
                                    }
                                 }
                              } else {
                                 int damage = Battle.gen2CalcDamage(battle.oppPokemon, turnData.enemyAttack, playerx.currPokemon);
                                 int currHealth = playerx.currPokemon.currentStats.get("hp");
                                 int finalHealthx = currHealth - damage > 0 ? currHealth - damage : 0;
                                 playerx.currPokemon.currentStats.put("hp", finalHealthx);
                                 turnData.enemyAttack.damage = damage;
                                 if (finalHealthx <= 0) {
                                    battle.network.expectPlayerSwitch = true;
                                 } else {
                                    damage = Battle.gen2CalcDamage(playerx.currPokemon, turnData.playerAttack, battle.oppPokemon);
                                    currHealth = battle.oppPokemon.currentStats.get("hp");
                                    finalHealthx = currHealth - damage > 0 ? currHealth - damage : 0;
                                    battle.oppPokemon.currentStats.put("hp", finalHealthx);
                                    turnData.playerAttack.damage = damage;
                                    if (finalHealthx <= 0) {
                                       Route currRoute = game.map.tiles.get(playerx.position).routeBelongsTo;
                                       playerx.currPokemon.exp = playerx.currPokemon.exp + battle.calcFaintExp(1);

                                       while (
                                          playerx.currPokemon.level < 100
                                             && playerx.currPokemon.calcExpForLevel(playerx.currPokemon.level + 1) <= playerx.currPokemon.exp
                                       ) {
                                          playerx.currPokemon.level++;
                                       }

                                       for (int ixx = 1; ixx <= playerx.currPokemon.level; ixx++) {
                                          if (Specie.gen2Evos.get(playerx.currPokemon.specie.name.toLowerCase(Locale.ROOT)).containsKey(String.valueOf(ixx))) {
                                             String evolveTo = Specie.gen2Evos
                                                .get(playerx.currPokemon.specie.name.toLowerCase(Locale.ROOT))
                                                .get(String.valueOf(ixx));
                                             playerx.currPokemon.evolveTo(evolveTo);
                                             break;
                                          }
                                       }
                                    }
                                 }
                              }

                              if (battle.oppPokemon.currentStats.get("hp") <= 0
                                 || battleAction.type == Battle.DoTurn.Type.RUN && turnData.runSuccessful
                                 || battleAction.type == Battle.DoTurn.Type.ITEM && turnData.numWobbles == -1) {
                                 battle.oppPokemon.inBattle = false;
                                 playerx.canMove = true;
                                 playerx.numFlees = 0;
                                 game.battles.remove(playerx.network.id);
                              }

                              boolean hasAlivePokemon = false;

                              for (Pokemon pokemon : playerx.pokemon) {
                                 if (pokemon.currentStats.get("hp") > 0) {
                                    hasAlivePokemon = true;
                                    break;
                                 }
                              }

                              if (!hasAlivePokemon) {
                                 battle.oppPokemon.inBattle = false;
                                 playerx.canMove = true;
                                 playerx.numFlees = 0;
                                 game.battles.remove(playerx.network.id);
                                 playerx.position.set(playerx.spawnLoc);

                                 for (Pokemon pokemonx : playerx.pokemon) {
                                    pokemonx.currentStats.put("hp", pokemonx.maxStats.get("hp") / 2);
                                 }

                                 for (Player otherPlayerxxx : game.players.values()) {
                                    if (otherPlayerxxx != playerx && otherPlayerxxx.network.loadingZone.contains(playerx.position)) {
                                       Network.ServerPlayerData serverPlayerData = new Network.ServerPlayerData(playerx);
                                       game.server.sendToTCP(otherPlayerxxx.network.connectionId, serverPlayerData);
                                    }
                                 }
                              }

                              System.out.println("Sending turn data.");
                              game.server.sendToTCP(playerx.network.connectionId, turnData);
                           } else if (object instanceof Network.TileData) {
                              Network.TileData tileData = (Network.TileData)object;
                              if (!game.map.tiles.containsKey(tileData.pos)) {
                                 System.out
                                    .println(
                                       "TileData: Invalid tile position "
                                          + tileData.pos.toString()
                                          + ", sent by: "
                                          + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              if (game.map.tiles.get(tileData.pos).isSolid) {
                                 throw new Exception();
                              }

                              for (Player playerxx : game.players.values()) {
                                 if (playerxx.position.equals(tileData.pos)) {
                                    throw new Exception();
                                 }
                              }

                              Tile oldTile = game.map.tiles.get(tileData.pos);
                              Tile newTile = new Tile(tileData.tileName, tileData.tileNameUpper, tileData.pos.cpy(), true, oldTile.routeBelongsTo);
                              newTile.hasItem = tileData.hasItem;
                              newTile.hasItemAmount = tileData.hasItemAmount;
                              game.map.tiles.put(tileData.pos.cpy(), newTile);
                              game.map.adjustSurroundingTiles(newTile);

                              for (Player playerxxx : game.players.values()) {
                                 if (playerxxx.network.loadingZone.contains(tileData.pos)) {
                                    game.server.sendToTCP(playerxxx.network.connectionId, tileData);
                                 }
                              }
                           } else if (object instanceof Network.UseHM) {
                              Network.UseHM useHM = (Network.UseHM)object;
                              if (!game.players.containsKey(useHM.playerId)) {
                                 System.out.println("UseHM: Invalid player id " + useHM.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player playerxxxx = game.players.get(useHM.playerId);
                              if (!playerxxxx.pokemon.get(useHM.pokemonIndex).hms.contains(useHM.hm)) {
                                 System.out
                                    .println(
                                       "UseHM: Invalid HM "
                                          + useHM.hm
                                          + " for index "
                                          + useHM.pokemonIndex
                                          + ", sent by: "
                                          + connection.getRemoteAddressTCP().toString()
                                    );
                              }

                              if (useHM.hm.equals("STOP")) {
                                 playerxxxx.currFieldMove = "";
                              } else if (useHM.hm.equals("SWITCH")) {
                                 Pokemon movePokemon = playerxxxx.pokemon.get(useHM.pokemonIndex);
                                 Pokemon movePokemon2 = playerxxxx.pokemon.get(useHM.movePos);
                                 playerxxxx.pokemon.remove(useHM.pokemonIndex);
                                 playerxxxx.pokemon.add(useHM.pokemonIndex, movePokemon2);
                                 playerxxxx.pokemon.remove(useHM.movePos);
                                 playerxxxx.pokemon.add(useHM.movePos, movePokemon);
                                 if (useHM.pokemonIndex == 0) {
                                    playerxxxx.currPokemon = movePokemon2;
                                 }

                                 if (useHM.movePos == 0) {
                                    playerxxxx.currPokemon = movePokemon;
                                 }
                              } else if (!useHM.hm.equals("BUILD")) {
                                 if (useHM.hm.equals("CUT")) {
                                    Vector2 pos = playerxxxx.facingPos(useHM.dirFacing);
                                    Tile currTile = playerxxxx.network.tiles.get(pos);
                                    if (currTile.isCuttable) {
                                       Action action = new CutTreeAnim(game, game.map.overworldTiles.get(pos), null);
                                       game.map.interiorTiles.get(game.map.interiorTilesIndex).remove(currTile.position.cpy());
                                       if (!currTile.items().isEmpty()) {
                                          for (String item : currTile.items().keySet()) {
                                             if (playerxxxx.hasItem(item)) {
                                                int currQuantity = playerxxxx.getItemAmount(item);
                                                playerxxxx.setItemAmount(item, currQuantity + currTile.items().get(item));
                                             } else {
                                                playerxxxx.setItemAmount(item, currTile.items().get(item));
                                             }
                                          }

                                          currTile.items().clear();
                                       }

                                       game.insertAction(action);
                                    }
                                 } else if (useHM.hm.equals("HEADBUTT")) {
                                    playerxxxx.currFieldMove = "HEADBUTT";
                                 } else if (useHM.hm.equals("JUMP")) {
                                    playerxxxx.currFieldMove = "RIDE";
                                 }
                              }

                              for (Player otherPlayerxxxx : game.players.values()) {
                                 if (playerxxxx != otherPlayerxxxx && otherPlayerxxxx.network.loadingZone.contains(playerxxxx.position)) {
                                    useHM.playerId = playerxxxx.network.number;
                                    game.server.sendToTCP(otherPlayerxxxx.network.connectionId, useHM);
                                 }
                              }
                           } else if (object instanceof Network.UseItem) {
                              Network.UseItem useItem = (Network.UseItem)object;
                              if (!game.players.containsKey(useItem.playerId)) {
                                 System.out
                                    .println("UseItem: Invalid player id " + useItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player playerxxxxx = game.players.get(useItem.playerId);
                              if (!playerxxxxx.hasItem(useItem.item)) {
                                 System.out
                                    .println(
                                       "UseItem: None of this item in inventory: " + useItem.item + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Vector2 pos = playerxxxxx.facingPos(useItem.dirFacing);
                              if (useItem.item.contains("apricorn")) {
                                 game.insertAction(new PlantTree(pos, null));
                              }

                              playerxxxxx.setItemAmount(useItem.item, playerxxxxx.getItemAmount(useItem.item) - 1);
                              if (playerxxxxx.getItemAmount(useItem.item) <= 0) {
                                 playerxxxxx.removeItem(useItem.item);
                              }

                              for (Player otherPlayerxxxxx : game.players.values()) {
                                 if (playerxxxxx != otherPlayerxxxxx && otherPlayerxxxxx.network.loadingZone.contains(playerxxxxx.position)) {
                                    useItem.playerId = playerxxxxx.network.number;
                                    game.server.sendToTCP(otherPlayerxxxxx.network.connectionId, useItem);
                                 }
                              }
                           } else if (object instanceof Network.Sleep) {
                              Network.Sleep sleep = (Network.Sleep)object;
                              if (!game.players.containsKey(sleep.playerId)) {
                                 System.out.println("UseHM: Invalid player id " + sleep.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player playerxxxxxx = game.players.get(sleep.playerId);
                              playerxxxxxx.isSleeping = sleep.isSleeping;
                              playerxxxxxx.canMove = !sleep.isSleeping;
                              if (!playerxxxxxx.isSleeping) {
                                 playerxxxxxx.spawnLoc = playerxxxxxx.position.cpy();
                                 int ixxx = 0;

                                 for (Pokemon pokemonx : playerxxxxxx.pokemon) {
                                    Network.PokemonData pokemonData = new Network.PokemonData(pokemonx, ixxx);
                                    connection.sendTCP(pokemonData);
                                    ixxx++;
                                 }
                              }
                           } else if (object instanceof Network.Craft) {
                              Network.Craft craft = (Network.Craft)object;
                              if (!game.players.containsKey(craft.playerId)) {
                                 System.out.println("Craft: Invalid player id " + craft.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player playerxxxxxx = game.players.get(craft.playerId);
                              if (playerxxxxxx.hasCraftRequirements(Player.crafts, craft.craftIndex, craft.amount)) {
                                 playerxxxxxx.craftItem(Player.crafts, craft.craftIndex, craft.amount);
                              }
                           } else if (object instanceof Network.DropItem) {
                              Network.DropItem dropItem = (Network.DropItem)object;
                              if (!game.players.containsKey(dropItem.playerId)) {
                                 System.out
                                    .println("DropItem: Invalid player id " + dropItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player playerxxxxxx = game.players.get(dropItem.playerId);
                              playerxxxxxx.setItemAmount(dropItem.itemName, playerxxxxxx.getItemAmount(dropItem.itemName) - dropItem.amount);
                              if (playerxxxxxx.getItemAmount(dropItem.itemName) <= 0) {
                                 playerxxxxxx.removeItem(dropItem.itemName);
                              }
                           } else if (object instanceof Network.DropPokemon) {
                              Network.DropPokemon dropPokemon = (Network.DropPokemon)object;
                              if (!game.players.containsKey(dropPokemon.playerId)) {
                                 System.out
                                    .println(
                                       "DropPokemon: Invalid player id " + dropPokemon.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player playerxxxxxx = game.players.get(dropPokemon.playerId);
                              if (dropPokemon.pickingUp) {
                                 Vector2 posx = dropPokemon.pos;
                                 Pokemon pokemonx = game.map.pokemon.get(posx);
                                 if (playerxxxxxx.pokemon.size() < 6 && pokemonx != null && playerxxxxxx.network.loadingZone.contains(posx)) {
                                    playerxxxxxx.pokemon.add(pokemonx);
                                    game.map.pokemon.remove(posx);
                                    game.actionStack.remove(pokemonx.standingAction);
                                 }

                                 for (Player otherPlayerxxxxxx : game.players.values()) {
                                    if (otherPlayerxxxxxx != playerxxxxxx && playerxxxxxx.network.loadingZone.contains(posx)) {
                                       game.server.sendToTCP(otherPlayerxxxxxx.network.connectionId, new Network.OverworldPokemonData(pokemonx, posx, true));
                                    }
                                 }
                              } else {
                                 Vector2 posx = playerxxxxxx.facingPos(dropPokemon.dirFacing);
                                 Pokemon pokemonx = playerxxxxxx.pokemon.get(dropPokemon.index);
                                 pokemonx.position = posx;
                                 playerxxxxxx.pokemon.remove(dropPokemon.index);
                                 if (dropPokemon.index == 0) {
                                    for (Pokemon currPokemon : playerxxxxxx.pokemon) {
                                       if (currPokemon.currentStats.get("hp") > 0) {
                                          playerxxxxxx.currPokemon = currPokemon;
                                          break;
                                       }
                                    }
                                 }

                                 pokemonx.mapTiles = playerxxxxxx.network.tiles;
                                 pokemonx.canMove = true;
                                 game.insertAction(pokemonx.new Standing());

                                 for (Player otherPlayerxxxxxxx : game.players.values()) {
                                    if (otherPlayerxxxxxxx != playerxxxxxx && playerxxxxxx.network.loadingZone.contains(posx)) {
                                       game.server.sendToTCP(otherPlayerxxxxxxx.network.connectionId, new Network.OverworldPokemonData(pokemonx, posx));
                                    }
                                 }
                              }
                           } else if (object instanceof Network.PausePokemon) {
                              Network.PausePokemon pausePokemon = (Network.PausePokemon)object;
                              if (!game.players.containsKey(pausePokemon.playerId)) {
                                 System.out
                                    .println(
                                       "DropPokemon: Invalid player id " + pausePokemon.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player playerxxxxxx = game.players.get(pausePokemon.playerId);
                              if (playerxxxxxx.network.loadingZone.contains(pausePokemon.position) && game.map.pokemon.containsKey(pausePokemon.position)) {
                                 Pokemon pokemonx = game.map.pokemon.get(pausePokemon.position);
                                 pokemonx.canMove = !pausePokemon.shouldPause;
                              }

                              if (pausePokemon.shouldPause) {
                                 connection.sendTCP(pausePokemon);
                              }
                           } else if (object instanceof Network.PickupItem) {
                              Network.PickupItem pickupItem = (Network.PickupItem)object;
                              if (!game.players.containsKey(pickupItem.playerId)) {
                                 System.out
                                    .println(
                                       "PickupItem: Invalid player id " + pickupItem.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player playerxxxxxxx = game.players.get(pickupItem.playerId);
                              Vector2 posx = new Vector2(0.0F, 0.0F);
                              if (pickupItem.dirFacing.equals("right")) {
                                 posx = new Vector2(playerxxxxxxx.position.cpy().add(16.0F, 0.0F));
                              } else if (pickupItem.dirFacing.equals("left")) {
                                 posx = new Vector2(playerxxxxxxx.position.cpy().add(-16.0F, 0.0F));
                              } else if (pickupItem.dirFacing.equals("up")) {
                                 posx = new Vector2(playerxxxxxxx.position.cpy().add(0.0F, 16.0F));
                              } else if (pickupItem.dirFacing.equals("down")) {
                                 posx = new Vector2(playerxxxxxxx.position.cpy().add(0.0F, -16.0F));
                              }

                              Tile tile = game.map.tiles.get(posx);
                              if (tile != null && tile.hasItem != null) {
                                 tile.pickUpItem(playerxxxxxxx);
                                 pickupItem.pos = posx;
                                 connection.sendTCP(pickupItem);
                                 Network.TileData tileDatax = new Network.TileData(tile);

                                 for (Player otherPlayerxxxxxxxx : game.players.values()) {
                                    if (playerxxxxxxx != otherPlayerxxxxxxxx && playerxxxxxxx.network.loadingZone.contains(tileDatax.pos)) {
                                       game.server.sendToTCP(playerxxxxxxx.network.connectionId, tileDatax);
                                    }
                                 }
                              }
                           } else if (object instanceof Network.LearnMove) {
                              Network.LearnMove learnMove = (Network.LearnMove)object;
                              if (!game.players.containsKey(learnMove.playerId)) {
                                 System.out
                                    .println("DropItem: Invalid player id " + learnMove.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString());
                                 throw new Exception();
                              }

                              Player playerxxxxxxxx = game.players.get(learnMove.playerId);
                              Pokemon pokemonx = playerxxxxxxxx.pokemon.get(learnMove.pokemonIndex);
                              learnMove.moveName = learnMove.moveName.toLowerCase(Locale.ROOT);
                              boolean foundMove = false;

                              for (int ixxx = 0; ixxx <= pokemonx.level; ixxx++) {
                                 if (pokemonx.learnSet.containsKey(ixxx)) {
                                    for (String attack : pokemonx.learnSet.get(ixxx)) {
                                       if (attack.toLowerCase(Locale.ROOT).equals(learnMove.moveName)) {
                                          foundMove = true;
                                          break;
                                       }
                                    }

                                    if (foundMove) {
                                       break;
                                    }
                                 }
                              }

                              if (foundMove) {
                                 pokemonx.attacks[learnMove.replaceIndex] = learnMove.moveName;
                              }
                           } else if (object instanceof Network.BattleData) {
                              Network.BattleData battleData = (Network.BattleData)object;
                              if (!battleData.pokemonData.name.equals("ghost")) {
                                 System.out
                                    .println(
                                       "BattleData: Invalid encounter for "
                                          + battleData.pokemonData.name
                                          + ", sent by: "
                                          + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              if (!game.players.containsKey(battleData.playerId)) {
                                 System.out
                                    .println(
                                       "BattleData: Invalid player id " + battleData.playerId + ", sent by: " + connection.getRemoteAddressTCP().toString()
                                    );
                                 throw new Exception();
                              }

                              Player playerxxxxxxxx = game.players.get(battleData.playerId);
                              playerxxxxxxxx.setCurrPokemon();
                              playerxxxxxxxx.canMove = false;
                              game.battles.put(playerxxxxxxxx.network.id, new Battle());
                              game.battles.get(playerxxxxxxxx.network.id).oppPokemon = new Pokemon(battleData.pokemonData);
                           }
                        } catch (Exception var14) {
                           var14.printStackTrace();
                        }

                        synchronized (this) {
                           this.notify();
                        }
                     }
                  };
                  Gdx.app.postRunnable(runnable);

                  try {
                     synchronized (runnable) {
                        runnable.wait();
                     }
                  } catch (InterruptedException var7) {
                     var7.printStackTrace();
                  }
               }
            }
         );
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }
}
