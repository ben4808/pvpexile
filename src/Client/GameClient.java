package Client;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

import Game.BadFileFormatException;
import Game.FileData;
import Game.Game;
import Game.Message;
import Game.Player;
import Game.Spell;


public class GameClient {
	private ConnectWindow connectWindow;
	private PartyWindow partyWindow;
	private GameWindow gameWindow;
	
	private Socket socket;
	private int playerId;
	
	private Point attackingPoint;
	
	private Point castingPoint;
	private Spell castingSpell;
	private int itemUsed;

	private static GameClient singleton = null;
	
	public static GameClient inst() {
		if(singleton == null) singleton = new GameClient();
		return singleton;
	}
	
	private GameClient() {
		connectWindow = new ConnectWindow();
		partyWindow = new PartyWindow();
		gameWindow = new GameWindow();
		socket = null;
		
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
		
		connectWindow.setVisible(true);
	}
	
	public ConnectWindow getConnectWindow() { return connectWindow; }
	public PartyWindow getPartyWindow() { return partyWindow; }
	public GameWindow getGameWindow() { return gameWindow; }
	
	public boolean isObserver() { return playerId >= Game.MAX_PLAYERS; }
	public void setPlayerId(int n) { playerId = n; }
	public int getPlayerId() { return playerId; }
	public String getUsername() { return Game.inst().nthPlayer(playerId).getUsername(); }
	public Player yourParty() { return Game.inst().nthPlayer(playerId); }
	public Player otherParty() { return Game.inst().nthPlayer(1-playerId); }
	public boolean isMyTurn() { return Game.inst().getTurn() == getPlayerId(); }
	
	public Point getAttackingPoint() { return attackingPoint; }
	public void setAttackingPoint(Point p) { attackingPoint = p; }
	
	public Point getCastingPoint() { return castingPoint; }
	public Spell getCastingSpell() { return castingSpell; }
	public int getItemUsed() { return itemUsed; }
	
	public void setCastingPoint(Point p) { castingPoint = p; }
	public void setCastingSpell (Spell s) { castingSpell = s; }
	public void setItemUsed(int i) { itemUsed = i; }
	
	public void connectToServer(String hostname, int port) throws IOException {
		Socket sock = new Socket(hostname, port);
		new Thread(new SocketThread(sock)).start();
		socket = sock;
	}
	
	public void connected() {
		connectWindow.setVisible(false);
		if(Game.inst().isRunning()) {
			partyWindow.setVisible(false);
			gameWindow.setVisible(true);
		}
		else {
			gameWindow.setVisible(false);
			partyWindow.initialize();
		}
	}
	
	public void disconnected() {
		// don't exit on username taken disconnection
		if(!connectWindow.isVisible()) {
			JOptionPane.showMessageDialog(gameWindow, "You have been disconnected.");
			System.exit(0);
		}
	}
	
	public void sendMessage(Message msg) {
		try {
			new ObjectOutputStream(socket.getOutputStream()).writeObject(msg);
		} catch (IOException e) {
			System.out.println("Error sending message.");
		}
	}
	
	public void startGame() {
		partyWindow.setVisible(false);
		gameWindow.setVisible(true);
	}
	
	public void repaintGame() {
		if(Game.inst().isRunning())
			gameWindow.repaint();
		else
			partyWindow.repaint();
	}
}
