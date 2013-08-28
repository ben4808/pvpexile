package Server;

import java.awt.Point;
import java.util.ArrayList;

import Game.*;
import Game.Effect.EffectType;
import Game.Effect.SingleEffect;
import Game.FileData.TerrainType;
import Game.Item.ItemType;
import Game.Monster.MonsterState;
import Game.Player.PlayerState;
import Game.GameEvent.*;

public class ServerMessageHandler {
	public static void handleMessage(Connection connection, Message msg) {
		GameServer server = GameServer.inst();
		ServerWindow window = ServerWindow.inst();
		Game game = Game.inst();
		GameMap map = game.getMap();
		int id = connection.getId();
		
		switch(msg.getType()) {
		case USERNAME: {
			String name = (String)msg.getObject(0);
			
			// reconnect lost player
			boolean found = false;
			for (int i=0; i < Game.MAX_PLAYERS; i++) {
				Connection conn = server.getConnection(i);
				if(conn == null) continue;
				if(conn != null && game.nthPlayer(i).getState() == PlayerState.DISCONNECTED && conn.getIP().equals(connection.getIP()) && 
						game.nthPlayer(i).getUsername().equals(name)) {
					connection.setId(i);
					connection.setUsername(name);
					server.insertConnection(i, connection);
					game.nthPlayer(i).setState(PlayerState.PLAYING);
					window.updatePlayerList();
					
					connection.sendMessage(new Message(EventType.RECONNECTED).add(i));
					connection.sendMessage(new Message(EventType.FULL_UPDATE).add(Game.inst()));
					server.sendAll(new Message(EventType.PLAYER_RECONNECTED).add(i));
					found = true;
					break;
				}
			}
			if(found) break;
			
			if(server.usernameTaken(name)) {
				connection.sendMessage(new Message(EventType.USERNAME_TAKEN));
				connection.disconnect();
				break;
			}
			
			// try to find a spot for new player
			int newId = -1;
			for (int i=0; ; i++) {
				if(server.getConnection(i) == null) {
					newId = i;
					break;
				}
			}
			
			connection.setId(newId);
			connection.setUsername(name);
			server.insertConnection(newId, connection);
			
			if(newId < Game.MAX_PLAYERS) {
				game.nthPlayer(newId).setUsername(name);
				game.nthPlayer(newId).setState(PlayerState.NOT_READY);
				window.updatePlayerList();
				connection.sendMessage(new Message(EventType.PLAYER).add(newId));
				connection.sendMessage(new Message(EventType.FULL_UPDATE).add(Game.inst()));
				server.sendAll(new Message(EventType.NEW_PLAYER).add(newId).add(name));
				break;
			}
			
			//no room, connect as observer
			connection.sendMessage(new Message(EventType.OBSERVER).add(newId));
			connection.sendMessage(new Message(EventType.FULL_UPDATE).add(Game.inst()));
		}
			break;
			
		case NOT_READY:
			game.nthPlayer(id).setState(PlayerState.NOT_READY);
			window.updatePlayerList();
			server.sendAll(new Message(EventType.PLAYER_NOT_READY).add(id));
			server.cancelGameStart();
			break;
		
		case READY: {
			@SuppressWarnings("unchecked")
			ArrayList<Monster> party = (ArrayList<Monster>)msg.getObject(0);
			game.nthPlayer(id).setMonsters(party);
			connection.sendMessage(new Message(EventType.PLAYER_PARTY).add(id).add(party));
			
			game.nthPlayer(id).setState(PlayerState.READY);
			window.updatePlayerList();
			server.sendAll(new Message(EventType.PLAYER_READY).add(id));
			server.checkForGameStart();
		}
			break;
			
		case CHAT: {
			String username = connection.getUsername();
			String message = (String)msg.getObject(0);
			game.getChat().addLine(username, message, connection.isPlayer());
			server.sendAll(new Message(EventType.CHAT_MESSAGE).add(username).add(message).add(connection.isPlayer()));
		}
			break;
			
		case SWITCH_ACTIVE: {
			int n = (int)msg.getObject(0);
			if(n >= game.getActivePlayer().partySize()) break;
			
			MonsterState state = game.getActivePlayer().nthMonster(n).getState();
			if(state.isDead() || state.getActionPoints() == 0) break;
			Point point = state.getLocation();
			game.setActivePoint(point);
			server.sendAll(new Message(EventType.SET_ACTIVE).add(point));
		}
			break;
			
		case STAND_READY: {
			Monster monster = game.getActiveMonster();
			monster.getState().setActionPoints(0);
			server.sendAll(new Message(EventType.MONSTER_STAND_READY));
			activateNextMember(monster.getTeamNumber());
		}
			break;
			
		case MOVE: {
			CursorDir dir = (CursorDir)msg.getObject(0);
			Point active = game.getActivePoint();
			
			Point target = null;
			switch(dir) {
			case N: target = new Point(active.x, active.y - 1); break;
			case NE: target = new Point(active.x + 1, active.y - 1); break;
			case E: target = new Point(active.x + 1, active.y); break;
			case SE: target = new Point(active.x + 1, active.y + 1); break;
			case S: target = new Point(active.x, active.y + 1); break;
			case SW: target = new Point(active.x - 1, active.y + 1); break;
			case W: target = new Point(active.x - 1, active.y); break;
			case NW: target = new Point(active.x - 1, active.y - 1); break;
			}
			
			// can't move off map
			if(target.x < 0 || target.y < 0 || target.x >= map.getWidth() || target.y >= map.getHeight())
				break;
			
			TerrainType targetTerrain = map.terrainAt(target);
			// can't move into solid terrain
			if(targetTerrain.isSolid())
				break;
			
			Monster activeMonster = map.monsterAt(active);
			Monster targetMonster = map.monsterAt(target);
			// open space
			if(targetMonster == null) {
				int ap = activeMonster.getState().getActionPoints();
				if(ap == 0) break;
				
				activeMonster.getState().setActionPoints(--ap);
				map.setMonster(active, null);
				map.setMonster(target, activeMonster);
				game.setActivePoint(target);
				
				Field targetField = map.fieldAt(target);
				ArrayList<Effect> effects = null;
				if(targetField != null) {
					effects = new ArrayList<Effect>();
					int damage = Damage.calculateFieldDamage(activeMonster, targetField);
					Effect effect = new Effect(target);
					effect.addSingleEffect(new SingleEffect(EffectType.DAMAGE, new Damage(targetField.getDamageGraphic(), damage)));
					effect.apply();
					effects.add(effect);
				}
				
				server.sendAll(new Message(EventType.MOVE_MONSTER).add(active).add(target).add(ap));
				if(effects != null) {
					server.sendAll(new Message(EventType.FIELD_DAMAGE).add(effects));
					if(activeMonster.getState().isDead())
						server.sendDeadMonster(activeMonster.getState().getLocation());
				}
			}
			// switch
			else if(targetMonster.getTeam() == game.getTurn()) {
				int activeAP = activeMonster.getState().getActionPoints();
				int targetAP = targetMonster.getState().getActionPoints();
				if(activeAP == 0 || targetAP == 0) break;
				
				activeMonster.getState().setActionPoints(--activeAP);
				targetMonster.getState().setActionPoints(--targetAP);
				map.setMonster(active, targetMonster);
				map.setMonster(target, activeMonster);
				game.setActivePoint(target);
				
				Field activeField = map.fieldAt(active);
				Field targetField = map.fieldAt(target);
				ArrayList<Effect> activeEffect = null;
				ArrayList<Effect> targetEffect = null;
				if(targetField != null) {
					int damage = Damage.calculateFieldDamage(activeMonster, targetField);
					activeEffect = Spell.simpleEffect(target, new SingleEffect(EffectType.DAMAGE, new Damage(targetField.getDamageGraphic(), damage)));
					activeEffect.get(0).apply();
				}
				if(activeField != null) {
					int damage = Damage.calculateFieldDamage(targetMonster, activeField);
					targetEffect = Spell.simpleEffect(active, new SingleEffect(EffectType.DAMAGE, new Damage(activeField.getDamageGraphic(), damage)));
					targetEffect.get(0).apply();
				}
			
				server.sendAll(new Message(EventType.SWITCH_MONSTER).add(active).add(activeAP).add(target).add(targetAP));
				if(activeEffect != null) {
					server.sendAll(new Message(EventType.FIELD_DAMAGE).add(activeEffect));
					if(activeMonster.getState().isDead())
						server.sendDeadMonster(activeMonster.getState().getLocation());
				}
				if(targetEffect != null) {
					server.sendAll(new Message(EventType.FIELD_DAMAGE).add(targetEffect));
					if(targetMonster.getState().isDead())
						server.sendDeadMonster(targetMonster.getState().getLocation());
				}
			}
			// melee
			else {
				int activeAP = activeMonster.getState().getActionPoints();
				if(activeAP == 0) break;
				activeAP = Math.max(0, activeAP - 4);
				activeMonster.getState().setActionPoints(activeAP);
				int damage = Damage.calculateMeleeDamage(activeMonster, targetMonster);
				targetMonster.deductHealth(damage);
				server.sendAll(new Message(EventType.MELEE).add(active).add(activeAP).add(target).add(damage));
				if(targetMonster.getState().isDead())
					server.sendDeadMonster(targetMonster.getState().getLocation());
			}
			
			if(activeMonster.getState().isDead() || activeMonster.getState().getActionPoints() == 0) {
				activateNextMember(activeMonster.getTeamNumber());
			}
		}
			break;
			
		case USE_POTION: {
			int n = (int)msg.getObject(0);
			Monster monster = game.getActiveMonster();
			if(monster.getItems().size() < n) break;
			Item potion = monster.getItems().get(n);
			if(potion == null || potion.getType() != ItemType.POTION || potion.getQuantity() == 0) break;
			
			int ap = monster.getState().getActionPoints();
			if(ap == 0) break;
			ap = Math.max(0, ap - 3);
			monster.getState().setActionPoints(ap);
			Effect effect = potion.effectOnMonster(monster);
			effect.apply();
			potion.decrementQuantity();
			server.sendAll(new Message(EventType.MONSTER_USE_POTION).add(effect.getPoint()).add(ap).add(n).add(effect));
			if(ap == 0) activateNextMember(monster.getTeamNumber());
		}
			break;
			
		case USE_MISSILE: {
			int n = (int)msg.getObject(0);
			Point target = (Point)msg.getObject(1);
			
			Monster monster = game.getActiveMonster();
			Monster targetMonster = game.getMap().monsterAt(target);
			
			if(monster.getItems().size() < n) break;
			Item missile = monster.getItems().get(n);
			if(missile == null || missile.getType() != ItemType.MISSILE || missile.getQuantity() == 0) break;
			
			int ap = monster.getState().getActionPoints();
			if(ap == 0) break;
			ap = Math.max(0, ap - 3);
			monster.getState().setActionPoints(ap);
			Effect effect;
			if(targetMonster != null) {
				effect = missile.effectOnMonster(targetMonster);
				effect.apply();
			}
			else {
				effect = new Effect(target);
			}
			missile.decrementQuantity();
			server.sendAll(new Message(EventType.MONSTER_USE_MISSILE).add(monster.getState().getLocation()).add(ap).add(n).add(target).add(effect));
			if(targetMonster != null && targetMonster.getState().isDead()) {
				server.sendDeadMonster(target);
			}
			if(ap == 0) activateNextMember(monster.getTeamNumber());
		}
			break;
			
		case SPELL: {
			Spell spell = (Spell)msg.getObject(0);
			Point target = (Point)msg.getObject(1);
			
			Monster caster = game.getActiveMonster();
			int ap = caster.getState().getActionPoints();
			int sp = caster.getState().getSpellPoints();
			if(ap == 0) activateNextMember(caster.getTeamNumber());
			if(ap == 0 || sp < spell.getSpellPoints() || spell.getLevel() > caster.getMagic()) break;
			ap = Math.max(0, ap - 6);
			caster.getState().setActionPoints(ap);
			sp = Math.max(0, sp - spell.getSpellPoints());
			caster.getState().setSpellPoints(sp);
			
			ArrayList<Effect> effects = spell.getEffects(caster, target);
			ArrayList<Point> deadPoints = new ArrayList<Point>();
			for(Effect effect : effects) {
				effect.apply();
				Monster monster = map.monsterAt(effect.getPoint());
				if(monster != null && monster.getState().isDead())
					deadPoints.add(effect.getPoint());
			}
			server.sendAll(new Message(EventType.MONSTER_SPELL).add(spell).add(caster.getState().getLocation()).add(ap).add(sp).add(target).add(effects));
			for(Point p : deadPoints) {
				server.sendDeadMonster(p);
			}
			if(ap == 0) activateNextMember(caster.getTeamNumber());
		}
			break;
		}
	}
	
	public static void activateNextMember(int id) {
		Game game = Game.inst();
		Player player = game.getActivePlayer();
		int i = id;
		do {
			i = (i + 1) % player.partySize();
			MonsterState state = player.nthMonster(i).getState();
			if (!state.isDead() && state.getActionPoints() > 0) {
				Point p = state.getLocation();
				game.setActivePoint(p);
				GameServer.inst().sendAll(new Message(EventType.SET_ACTIVE).add(p));
				return;
			}
		} while (i != id);
		
		// no monsters with action points, initialize next turn
		GameServer.inst().initializeNextTurn(game.getTurn());
	}
}
