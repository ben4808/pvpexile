package Server;

import java.awt.Point;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import Game.BadFileFormatException;
import Game.Damage;
import Game.Damage.DamageGraphic;
import Game.Effect;
import Game.Effect.EffectType;
import Game.Effect.SingleEffect;
import Game.Field;
import Game.FileData;
import Game.Game;
import Game.GameMap;
import Game.Message;
import Game.GameEvent.EventType;
import Game.Monster;
import Game.Monster.MonsterState;
import Game.Player;
import Game.Player.PlayerState;


public class GameServer {	
	private ServerSocket serverSocket;
	private HashMap<Integer, Connection> connections;
	
	private Timer startGameTimer;
	
	private static GameServer singleton = null;
	
	public static GameServer inst() {
		if(singleton == null) singleton = new GameServer();
		return singleton;
	}
	
	private GameServer() {
		connections = new HashMap<Integer, Connection>();
		startGameTimer = null;
		
		try {
			FileData.inst().loadTerrain();
		} 
		catch (IOException e1) { System.out.println("ERROR: Could not find terrain file."); }
		catch (BadFileFormatException e2) { System.out.println("ERROR: Terrain file in incorrect format."); }
		
		try {
			FileData.inst().loadItems();
		} 
		catch (IOException e1) { System.out.println("ERROR: Could not find items file."); }
		catch (BadFileFormatException e2) { System.out.println("ERROR: Items file in incorrect format."); }
	}
	
	public Connection getConnection(int i) {
		return connections.containsKey(i) ? connections.get(i): null;
	}
	
	public void insertConnection(int i, Connection conn) {
		connections.put(i, conn);
	}
	
	public boolean usernameTaken(String name) {
		for(int i : connections.keySet()) {
			if(connections.get(i).getUsername().equals(name))
				return true;
		}
		return false;
	}
	
	public void sendAll(Message msg) {
		for(int i : connections.keySet()) {
			if(i < Game.MAX_PLAYERS && Game.inst().nthPlayer(i).getState() == PlayerState.DISCONNECTED) 
				continue;
			connections.get(i).sendMessage(msg);
		}
	}
	
	public void start(int port) {
		try {
			serverSocket = new ServerSocket(port);
			new Thread(new ServerThread()).start();
		} catch (IOException e) {
			ServerWindow.inst().serverErrorOccurred();
		}
	}
	
	public class ServerThread implements Runnable {
		public void run() {
			while(true) {
				try {
					Socket socket = serverSocket.accept();
					Connection connection = new Connection(socket);
					ConnectionThread newThread = new ConnectionThread(connection);
					new Thread(newThread).start();
					connection.sendMessage(new Message(EventType.CONNECTED));
				} 
				catch (IOException e) {
					ServerWindow.inst().serverErrorOccurred();
				}
			}
		}
	}
	
	public void disconnected(Connection conn) {
		int id = conn.getId();
		if(!Game.inst().isRunning()) {
			connections.remove(id);
			if(conn.isPlayer()) {
				Game.inst().setPlayer(id, new Player());
				sendAll(new Message(EventType.PLAYER_LEFT).add(id));
				ServerWindow.inst().updatePlayerList();
			}
			return;
		}
		
		if(!conn.isPlayer()) {
			connections.remove(id);
			return;
		}
		
		Game.inst().nthPlayer(id).setState(PlayerState.DISCONNECTED);
		sendAll(new Message(EventType.PLAYER_DISCONNECTED).add(id));
		ServerWindow.inst().updatePlayerList();
	}
	
	public void checkForGameStart() {
		Game game = Game.inst();
		for(int i=0; i < Game.MAX_PLAYERS; i++) {
			Player player = game.nthPlayer(i);
			if(player == null || player.getState() != PlayerState.READY)
					return;
		}
		
		if(game.getMap() == null) {
			this.sendAll(new Message(EventType.WAITING_FOR_MAP));
			return;
		}
		
		this.sendAll(new Message(EventType.GAME_STARTING));
		
		if(Game.DEBUG) {
			initializeGame();
		}
		else {
			startGameTimer = new Timer();
			startGameTimer.schedule(new TimerTask() {
				public void run() {
					initializeGame();
				}
			}, 5*1000);
		}
	}
	
	public void cancelGameStart() {
		if(startGameTimer != null)
			startGameTimer.cancel();
	}
	
	public void initializeGame() {
		Game game = Game.inst();
		game.setRunning(true);
		
		ArrayList<Player> players = new ArrayList<Player>();
		for(int i=0; i < Game.MAX_PLAYERS; i++) {
			game.nthPlayer(i).setState(PlayerState.PLAYING);
			game.getMap().placeParty(i);
			players.add(game.nthPlayer(i));
		}

		sendAll(new Message(EventType.GAME_STARTED).add(players));
		initializeNextTurn(-1);
	}
	
	public void initializeNextTurn(int turn) {
		int nextTurn = (turn + 1) % Game.MAX_PLAYERS;
		
		Game game = Game.inst();
		Player player = game.nthPlayer(nextTurn);
		
		ArrayList<Effect> poisonEffects = new ArrayList<Effect>();
		ArrayList<Effect> fieldEffects = new ArrayList<Effect>();
		ArrayList<MonsterState> states = new ArrayList<MonsterState>();
		ArrayList<Point> fieldDisappearance = new ArrayList<Point>();
		ArrayList<Point> poisonDeadMonsters = new ArrayList<Point>();
		ArrayList<Point> fieldDeadMonsters = new ArrayList<Point>();
		
		GameMap map = game.getMap();
		Random rand = new Random();
		for(int y=0; y < map.getHeight(); y++) {
			for(int x=0; x < map.getWidth(); x++) {
				Point point = new Point(x, y);
				Field field = map.fieldAt(point);
				if(field != null && rand.nextInt(100) < field.getDisappearChance()) {
					map.setField(point, null);
					fieldDisappearance.add(point);
				}
			}
		}
		
		for(int i=0; i < player.partySize(); i++) {
			Monster monster = player.nthMonster(i);
			MonsterState state = monster.getState();
			Point point = state.getLocation();
			if(!state.isDead()) {
				if(state.getPoison() > 0) {
					Effect effect = new Effect(point);
					int damage = Damage.calculatePoisonDamage(monster);
					effect.addSingleEffect(new SingleEffect(EffectType.DAMAGE, new Damage(DamageGraphic.POISON, damage)));
					effect.apply();
					if(state.isDead()) poisonDeadMonsters.add(point);
					poisonEffects.add(effect);
				}
				
				if(game.getMap().fieldAt(point) != null) {
					Effect effect = new Effect(point);
					Field field = game.getMap().fieldAt(point);
					int damage = Damage.calculateFieldDamage(monster, field);
					effect.addSingleEffect(new SingleEffect(EffectType.DAMAGE, new Damage(field.getDamageGraphic(), damage)));
					effect.apply();
					if(state.isDead()) fieldDeadMonsters.add(point);
					fieldEffects.add(effect);
				}
				
				if(!state.isDead()) {
					state.setSpeed(Math.max(state.getSpeed() - 1, monster.getBaseSpeed()));
					state.setPoison(Math.max(state.getPoison() - 1, 0));
					if(state.getBlessed() != monster.getBaseBlessed())
						state.setBlessed(state.getBlessed() < monster.getBaseBlessed() ? state.getBlessed() + 1 : state.getBlessed() - 1);
					state.setActionPoints(state.getSpeed());
					state.changeCurHealth(2);
					state.changeSpellPoints(1);
				}
			}
			states.add(state);
		}
		
		int firstActiveId = player.nextMovableMonster(-1);
		boolean canMove = true;
		if(firstActiveId == -1) {
			sendAll(new Message(EventType.NO_ACTION).add(nextTurn).add(states));
			canMove = false;
		}
		
		if(canMove) {
			Point activePoint = player.nthMonster(firstActiveId).getState().getLocation();
			game.setTurn(nextTurn);
			game.setActivePoint(activePoint);
			sendAll(new Message(EventType.START_TURN).add(nextTurn).add(activePoint).add(states));
		}
		
		if(fieldDisappearance.size() > 0)
			sendAll(new Message(EventType.FIELD_DISAPPEAR).add(fieldDisappearance));
		if(poisonEffects.size() > 0)
			sendAll(new Message(EventType.POISON_DAMAGE).add(poisonEffects));
		for(Point p : poisonDeadMonsters) {
			sendDeadMonster(p);
		}
		if(fieldEffects.size() > 0)
			sendAll(new Message(EventType.FIELD_DAMAGE).add(fieldEffects));
		for(Point p : fieldDeadMonsters) {
			sendDeadMonster(p);
		}
		
		if(!canMove) {
			initializeNextTurn(nextTurn);
		}
	}
	
	public void sendDeadMonster(Point p) {
		GameMap map = Game.inst().getMap();
		Monster monster = map.monsterAt(p);
		map.setBlood(p, true);
		map.setMonster(p, null);
		sendAll(new Message(EventType.MONSTER_DIED).add(p));
		if (Game.inst().nthPlayer(monster.getTeam()).isAllDead())
			sendAll(new Message(EventType.GAME_WINNER).add(1 - monster.getTeam()));
	}
}
