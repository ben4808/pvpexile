package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import Game.Message;


public class SocketThread implements Runnable {
	private Socket socket;
	
	public SocketThread(Socket s) {
		socket = s;
	}

	@Override
	public void run() {
		Message msg = null;
		while (true) {   
			ObjectInputStream in = null;
			msg = null;
			try {
				in = new ObjectInputStream(socket.getInputStream());
				msg = (Message)in.readObject();
			} catch (ClassNotFoundException e) {
				System.out.println("ClassNotFoundException while trying to read in Message.");
				break;
			} catch (IOException e) {
				System.out.println("IOException while trying to read in Message at Client: " + e.getMessage());
				break;
			}
			if(msg == null) break;
			System.out.println("c " + msg.getType().toString());
		    ClientMessageHandler.handleMessage(msg);
		}
		
		GameClient.inst().disconnected();
	}
}
