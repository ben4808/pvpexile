package Client;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import Game.ChatRecord.ChatLine;
import Game.Damage;
import Game.Damage.DamageGraphic;
import Game.Effect;
import Game.Game;
import Game.GameEvent.EventType;
import Game.FileData;
import Game.GameMap;
import Game.Item;
import Game.Message;
import Game.Monster;
import Game.Monster.MonsterState;
import Game.Player;
import Game.Player.PlayerState;
import Game.Spell;


public class ClientMessageHandler {
	public static void handleMessage(Message msg) {
		GameClient client = GameClient.inst();
		GameWindow window = client.getGameWindow();
		Game game = Game.inst();
		GameMap map = game.getMap();
		
		switch(msg.getType()) {
		case CONNECTED: {
			String username = client.getConnectWindow().getUsername();
			client.yourParty().setUsername(username);
			client.sendMessage(new Message(EventType.USERNAME).add(username));
		}
			break;
			
		case USERNAME_TAKEN:
			client.getConnectWindow().usernameTaken();
			break;
			
		case PLAYER: {
			int id = (int)msg.getObject(0);
			client.setPlayerId(id);
			game.nthPlayer(id).setState(PlayerState.NOT_READY);
			
			if(Game.DEBUG) {
				try { FileData.inst().loadParty(id, id == 0 ? "party1.txt" : "party2.txt"); }
				catch (Exception e) { System.out.println("Automatic party failed."); }
				client.sendMessage(new Message(EventType.READY).add(client.yourParty().getMonsters()));
			}
		}
			break;
			
		case OBSERVER: {
			int id = (int)msg.getObject(0);
			client.setPlayerId(id);
			JOptionPane.showMessageDialog(client.getPartyWindow(), "The game is full. You are connected as an observer only.");
		}
			break;
			
		case RECONNECTED: {
			int id = (int)msg.getObject(0);
			client.setPlayerId(id);
			game.nthPlayer(id).setState(PlayerState.PLAYING);
		}
			break;
			
		case PLAYER_RECONNECTED: {
			int id = (int)msg.getObject(0);
			game.nthPlayer(id).setState(PlayerState.PLAYING);
			window.addStatus("Player " + game.nthPlayer(id).getUsername() + " has been reconnected.");
		}
			break;
			
		case FULL_UPDATE: {
			Game newGame = (Game)msg.getObject(0);
			Game.setGame(newGame);
			// can only switch screens once we know whether game is running or not
			// FULL_UPDATE should now only be send on initial connection
			client.connected();  
			window.getMapPanel().snapToActive();
			
			for(ChatLine line : newGame.getChat().getLines()) {
				window.addChat(line.getName(), line.getMessage(), line.isPlayer());
			}
		}
			break;
			
		case NEW_PLAYER: {
			int id = (int)msg.getObject(0);
			String name = (String)msg.getObject(1);
			game.nthPlayer(id).setUsername(name);
			game.nthPlayer(id).setState(PlayerState.NOT_READY);
		}
			break;
			
		case PLAYER_LEFT: {
			int id = (int)msg.getObject(0);
			game.setPlayer(id, new Player());
		}
			break;
			
		case PLAYER_NOT_READY: {
			int id = (int)msg.getObject(0);
			game.nthPlayer(id).setState(PlayerState.NOT_READY);
			client.getPartyWindow().stopGameCountdown();
		}
			break;
			
		case PLAYER_PARTY: {
			int id = (int)msg.getObject(0);
			@SuppressWarnings("unchecked")
			ArrayList<Monster> party = (ArrayList<Monster>)msg.getObject(1);
			game.nthPlayer(id).setMonsters(party);
		}
			break;
			
		case PLAYER_READY: {
			int id = (int)msg.getObject(0);
			game.nthPlayer(id).setState(PlayerState.READY);
		}
			break;
			
		case MAP: {
			GameMap newMap = (GameMap)msg.getObject(0);
			game.setMap(newMap);
		}
			break;
			
		case WAITING_FOR_MAP:
			client.getPartyWindow().waitingForMap();
			break;
			
		case GAME_STARTING:
			client.getPartyWindow().startGameCountdown();
			break;
			
		case GAME_STARTED: {
			@SuppressWarnings("unchecked")
			ArrayList<Player> players = (ArrayList<Player>)msg.getObject(0);
			for(int team = 0; team < players.size(); team++) {
				Player player = players.get(team);
				game.setPlayer(team, player);
				for(int n=0; n < player.partySize(); n++) {
					Point p = player.nthMonster(n).getState().getLocation();
					map.setMonster(p, player.nthMonster(n));
				}
			}
			Game.inst().setRunning(true);
			client.getPartyWindow().stopGameCountdown();
			client.startGame();
		}
			break;
			
		case NO_ACTION: {
			int id = (int)msg.getObject(0);
			window.addStatus("Player " + game.nthPlayer(id).getUsername() + "'s turn has been skipped because no one can move.");
		}
			break;
			
		case START_TURN: {
			int id = (int)msg.getObject(0);
			Point active = (Point)msg.getObject(1);
			@SuppressWarnings("unchecked")
			ArrayList<MonsterState> states = (ArrayList<MonsterState>)msg.getObject(2);
			
			for(int i=0; i < states.size(); i++) {
				game.nthPlayer(id).nthMonster(i).setState(states.get(i));
			}
			
			// allow current splotches to finish
			if (window.getMapPanel().hasSplotches()) {
				try { Thread.sleep(DamageSplotch.DURATION); }
			    catch (InterruptedException e) {}
			}

			game.setTurn(id);
			game.setActivePoint(active);
			window.addStatus("Starting turn: " + game.getActivePlayer().getUsername());
			window.getMapPanel().snapToActive();
		}
			break;
			
		case FIELD_DISAPPEAR: {
			@SuppressWarnings("unchecked")
			ArrayList<Point> points = (ArrayList<Point>)msg.getObject(0);
			for (Point point : points) {
				map.setField(point, null);
			}
		}
			break;
			
		case POISON_DAMAGE: {
			@SuppressWarnings("unchecked")
			ArrayList<Effect> effects = (ArrayList<Effect>)msg.getObject(0);
			window.addStatus("Poison:");
			for(Effect effect : effects) {
				effect.apply();
				
				for (String line : effect.getStatusLines()) {
					window.addStatus(line, 1);
				}
				
				Damage damage = effect.getDamage();
				if(damage != null) {
					window.getMapPanel().doAmimation(null, effect.getPoint(), DamageGraphic.POISON, damage.getAmount());
				}
			}
		}
			break;
			
		case FIELD_DAMAGE: {
			@SuppressWarnings("unchecked")
			ArrayList<Effect> effects = (ArrayList<Effect>)msg.getObject(0);
			window.addStatus("Fields:");
			for(Effect effect : effects) {
				effect.apply();
				
				for (String line : effect.getStatusLines()) {
					window.addStatus(line, 1);
				}
				
				Damage damage = effect.getDamage();
				if(damage != null) {
					window.getMapPanel().doAmimation(null, effect.getPoint(), damage.getGraphic(), damage.getAmount());
				}
			}
		}
			break;
			
		case MONSTER_DIED: {
			Point p = (Point)msg.getObject(0);
			Monster monster = map.monsterAt(p);
			map.setMonster(p, null);
			map.setBlood(p, true);
			window.addStatus(monster.getName() + " dies.", 1);
		}
			break;
			
		case GAME_WINNER: {
			int id = (int)msg.getObject(0);
			game.setTurn(-1);
			window.addStatus(game.nthPlayer(id).getUsername() + " wins!");
			JOptionPane.showMessageDialog(client.getGameWindow(), id == client.getPlayerId() ? "VICTORY!" : "DEFEAT!");
		}
			break;
			
		case PLAYER_DISCONNECTED: {
			int id = (int)msg.getObject(0);
			game.nthPlayer(id).setState(PlayerState.DISCONNECTED);
			window.addStatus("Player " + game.nthPlayer(id).getUsername() + " has been disconnected.");
		}
			break;
			
		case CHAT_MESSAGE: {
			String name = (String)msg.getObject(0);
			String message = (String)msg.getObject(1);
			boolean isPlayer = (boolean)msg.getObject(2);
			
			game.getChat().addLine(name, message, isPlayer);
			window.addChat(name, message, isPlayer);
		}
			break;
			
		case SET_ACTIVE: {
			Point p = (Point)msg.getObject(0);
			game.setActivePoint(p);
			window.getMapPanel().snapToActive();
		}
			break;
			
		case MONSTER_STAND_READY: {
			Monster monster = map.monsterAt(game.getActivePoint());
			monster.getState().setActionPoints(0);
			window.addStatus(monster.getName() + " stands ready.");
		}
			break;
			
		case MOVE_MONSTER: {
			Point p1 = (Point)msg.getObject(0);
			Point p2 = (Point)msg.getObject(1);
			int ap = (int)msg.getObject(2);
			
			// in case the monster moved wasn't the active one
			if(p1.equals(game.getActivePoint()))
				game.setActivePoint(p2);
			
			Monster monst = map.monsterAt(p1);
			monst.getState().setActionPoints(ap);
			map.setMonster(p2, monst);
			map.setMonster(p1, null);
		}
			break;
			
		case SWITCH_MONSTER: {
			Point p1 = (Point)msg.getObject(0);
			int ap1 = (int)msg.getObject(1);
			Point p2 = (Point)msg.getObject(2);
			int ap2 = (int)msg.getObject(3);
			
			// in case the monster moved wasn't the active one
			if(p1.equals(game.getActivePoint()))
				game.setActivePoint(p2);
			
			Monster origMonst = map.monsterAt(p1);
			Monster otherMonst = map.monsterAt(p2);
			origMonst.getState().setActionPoints(ap1);
			otherMonst.getState().setActionPoints(ap2);
			map.setMonster(p2, origMonst);
			map.setMonster(p1, otherMonst);
		}
			break;
			
		case MELEE: {
			Point att = (Point)msg.getObject(0);
			int ap = (int)msg.getObject(1);
			Point target = (Point)msg.getObject(2);
			int damage = (int)msg.getObject(3);
			Monster attackMonst = map.monsterAt(att);
			Monster targetMonst = map.monsterAt(target);
			attackMonst.getState().setActionPoints(ap);
			targetMonst.getState().changeCurHealth(-damage);
			window.addStatus(attackMonst.getName() + " swings:");
			window.addStatus(damage > 0 ? targetMonst.getName() + " takes " + Integer.toString(damage) + "." : "Missed.", 1);
			window.getMapPanel().doAmimation(att, target, DamageGraphic.MELEE, damage, false);
		}
			break;
			
		case MONSTER_USE_POTION: {
			Point p = (Point)msg.getObject(0);
			int ap = (int)msg.getObject(1);
			int n = (int)msg.getObject(2);
			Effect effects = (Effect)msg.getObject(3);
			
			Monster monster = map.monsterAt(p);
			monster.getState().setActionPoints(ap);
			effects.apply();
			
			Item potion = monster.getItems().get(n);
			potion.decrementQuantity();
			
			window.addStatus(monster.getName() + " drinks " + potion.getName() + ".");
			for (String line : effects.getStatusLines()) {
				window.addStatus(line, 1);
			}
		}
			break;
			
		case MONSTER_USE_MISSILE: {
			Point att = (Point)msg.getObject(0);
			int ap = (int)msg.getObject(1);
			int n = (int)msg.getObject(2);
			Point target = (Point)msg.getObject(3);
			Effect effects = (Effect)msg.getObject(4);
			
			Monster attacker = map.monsterAt(att);
			attacker.getState().setActionPoints(ap);
			effects.apply();
			
			Item missile = attacker.getItems().get(n);
			missile.decrementQuantity();
			
			window.addStatus(attacker.getName() + " throws " + missile.getName() + ".");
			for (String line : effects.getStatusLines()) {
				window.addStatus(line, 1);
			}
			
			Damage damage = effects.getDamage();
			if(damage != null) {
				window.getMapPanel().doAmimation(att, target, damage.getGraphic(), damage.getAmount());
			}
		}
			break;
			
		case MONSTER_SPELL: {
			Spell spell = (Spell)msg.getObject(0);
			Point caster = (Point)msg.getObject(1);
			int newAP = (int)msg.getObject(2);
			int newSP = (int)msg.getObject(3);
			//Point target = (Point)msg.getObject(4);
			@SuppressWarnings("unchecked")
			ArrayList<Effect> effects = (ArrayList<Effect>)msg.getObject(5);
			
			Monster attackMonst = map.monsterAt(caster);
			attackMonst.getState().setActionPoints(newAP);
			attackMonst.getState().setSpellPoints(newSP);
			window.addStatus(attackMonst.getName() + " casts " + spell.getName() + ":");
			for (Effect effect : effects) {
				effect.apply();
				
				for (String line : effect.getStatusLines()) {
					window.addStatus(line, 1);
				}
				
				Damage damage = effect.getDamage();
				if(damage != null) {
					window.getMapPanel().doAmimation(caster, effect.getPoint(), damage.getGraphic(), damage.getAmount());
				}
			}
		}
			break;
			
		}
		
		client.repaintGame();
	}
}
