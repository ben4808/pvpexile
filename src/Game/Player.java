package Game;

import java.io.Serializable;
import java.util.ArrayList;

import Game.Monster.MonsterState;


@SuppressWarnings("serial")
public class Player implements Serializable {
	public enum PlayerState {
		EMPTY, NOT_READY, READY, PLAYING, DISCONNECTED
	}
	
	private String username;
	private ArrayList<Monster> monsters;
	private PlayerState state;
	
	public Player() {
		username = "";
		monsters = new ArrayList<Monster>();
		state = PlayerState.EMPTY;
	}
	
	public int getTeam() { return monsters.get(0).getTeam(); }
	
	public String getUsername() { return username; }
	public Monster nthMonster(int n) { return monsters.get(n); }
	public ArrayList<Monster> getMonsters() { return monsters; }
	public PlayerState getState() { return state; }
	
	public void setUsername(String n) { username = n; }
	public void addMonster(Monster m) { monsters.add(m); }
	public void setMonsters(ArrayList<Monster> m) { monsters = m; }
	public void setState(PlayerState s) { state = s; }
	
	public int partySize() { return monsters.size(); }
	
	public int nextMovableMonster (int i) {
		int id = i;
		MonsterState state;
		do {
			id = (id+1) % partySize();
			state = nthMonster(id).getState();
		} while (id != i && (state.isDead() || state.getActionPoints() == 0));
		if(id == i) return -1;
		return id;
	}
	
	public boolean isAllDead() {
		boolean dead = true;
		for(Monster m : monsters) {
			if(!m.getState().isDead()) dead = false;
		}
		return dead;
	}
}
