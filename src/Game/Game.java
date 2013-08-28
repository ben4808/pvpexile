package Game;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;


@SuppressWarnings("serial")
public class Game implements Serializable {
	public static boolean DEBUG = true;
	public static String DEBUG_MAP = "testmap.txt";
	
	public static int MAX_PLAYERS = 2;
	
	private GameMap map;
	private HashMap<Integer, Player> players;
	private ChatRecord chat;
	
	private int turn;
	private Point activePoint;
	private boolean running;
	
	private static Game singleton = null;
	
	public static Game inst() {
		if(singleton == null) singleton = new Game();
		return singleton;
	}
	
	public static void setGame(Game newGame) {
		singleton = newGame;
	}
	
	private Game() {
		map = null;
		players = new HashMap<Integer, Player>();
		chat = new ChatRecord();
		turn = 0;
		activePoint = null;
		running = false;
		
		for(int i=0; i < MAX_PLAYERS; i++) {
			players.put(i, new Player());
		}
	}
	
	public GameMap getMap() { return map; }
	public Player nthPlayer(int n) { return players.get(n); }
	public Player getActivePlayer() { return players.get(turn); }
	public Monster getActiveMonster() { return map.monsterAt(activePoint); }
	public ChatRecord getChat() { return chat; }
	public int getTurn() { return turn; }
	public Point getActivePoint() { return activePoint; }
	public boolean isRunning() { return running; }
	
	public void setMap(GameMap m) { map = m; }
	public void setPlayer(int i, Player p) { players.put(i, p); }
	public void setTurn(int t) { turn = t; }
	public void setActivePoint(Point p) { activePoint = (Point)p.clone(); }
	public void setRunning(boolean r) { running = r; }
}
