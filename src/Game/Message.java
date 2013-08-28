package Game;

import java.io.Serializable;
import java.util.ArrayList;

import Game.GameEvent.EventType;


@SuppressWarnings("serial")
public class Message implements Serializable {
	private EventType type;
	private ArrayList<Object> data;
	
	public Message(EventType t) {
		type = t;
		data = new ArrayList<Object>();
	}
	
	public class Iterator {
		private int pos;
		
		public Iterator() { pos = 0; }
		public boolean hasNext() { return pos < data.size(); }
		public Object next() { return data.get(pos++); }
	}
	
	public EventType getType() { return type; }
	
	public Message add(Object o) { data.add(o); return this; }
	public Object getObject(int i) { return data.get(i); }
}
