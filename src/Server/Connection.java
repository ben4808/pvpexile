package Server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Game.Game;
import Game.Message;


public class Connection {
	private Socket socket;
	private String ipAddress;
	
	private int playerId;
	private String username;
	
	public Connection(Socket s) {
		System.out.println("Connection created, IP: " + s.getRemoteSocketAddress().toString());
		
		setSocket(s);
		playerId = -1;
	}
	
	public Socket getSocket() { return socket; }
	public String getIP() { return ipAddress; }
	public int getId() { return playerId; }
	public boolean isPlayer() { return playerId >= 0 && playerId < Game.MAX_PLAYERS; }
	public String getUsername() { return username; }
	
	public void setSocket(Socket s) { 
		socket = s; 
		ipAddress = formatIP(s);
	}
	public void setId(int i) { playerId = i; }
	public void setUsername(String n) { username = n; }
	
	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			System.out.println("Error disconnecting player.");
		}
	}

	public void sendMessage(Message msg) {
		try {
			new ObjectOutputStream(socket.getOutputStream()).writeObject(msg);
		} catch (IOException e) {
			System.out.println("Error sending message.");
		}
	}
	
	public static String formatIP (Socket s) {
		String address = s.getRemoteSocketAddress().toString();
		return address.split(":")[0];
	}
}
