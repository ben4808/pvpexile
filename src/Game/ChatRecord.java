package Game;

import java.io.Serializable;
import java.util.ArrayList;


@SuppressWarnings("serial")
public class ChatRecord implements Serializable {
	private ArrayList<ChatLine> lines;
	
	public ChatRecord() {
		lines = new ArrayList<ChatLine>();
	}
	
	public ArrayList<ChatLine> getLines() { return lines; }
	public void addLine(String name, String message, boolean isPlayer) {
		lines.add(new ChatLine(name, message, isPlayer));
	}
	
	public class ChatLine implements Serializable {
		String name;
		String message;
		boolean player;
		
		public ChatLine(String n, String m, boolean p) {
			name = n;
			message = m;
			player = p;
		}
		
		public String getName() { return name; }
		public String getMessage() { return message; }
		public boolean isPlayer() { return player; }
	}
}
