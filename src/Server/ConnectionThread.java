package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import Game.Message;


public class ConnectionThread implements Runnable {
	private Connection connection;
	
	public ConnectionThread(Connection c) {
		connection = c;
	}

	@Override
	public void run() {
		Socket socket = connection.getSocket();
		
		while (true) {   
			Message msg = null;
			try {
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				msg = (Message)in.readObject();
			} 
			catch (ClassNotFoundException e) {
				System.out.println("ERROR: ClassNotFoundException while trying to read in Message.");
			} 
			catch (IOException e) {
				GameServer.inst().disconnected(connection);
				break;
			}
			
			System.out.println("s " + msg.getType().toString());
		    ServerMessageHandler.handleMessage(connection, msg);
		}
	}
}
