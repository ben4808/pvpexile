package Game;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

import Game.FileData.TerrainType;


@SuppressWarnings("serial")
public class GameMap implements Serializable {
	private String name;
	private ArrayList<Point> startPoints;
	private Square[][] squares;
	
	public GameMap(String n, int width, int height, ArrayList<Point> start) {
		name = n;
		startPoints = start;
		squares = new Square[width][height];
		for(int i=0; i<width; i++) {
			for(int j=0; j<height; j++)
				squares[i][j] = new Square();
		}
	}
	
	public String getName() { return name; }
	public int getWidth() { return squares.length; }
	public int getHeight() { return squares[0].length; }
	
	public TerrainType terrainAt(Point p) { 
		return pointInBounds(p) ? squares[p.x][p.y].getTerrain() : null;
	}
	
	public boolean isBloodAt(Point p) {
		return pointInBounds(p) ? squares[p.x][p.y].isBlood() : null;
	}
	
	public Monster monsterAt(Point p) { 
		return pointInBounds(p) ? squares[p.x][p.y].getMonster() : null;
	}
	
	public Field fieldAt(Point p) { 
		return pointInBounds(p) ? squares[p.x][p.y].getField() : null;
	}
	
	public void setTerrain(Point p, TerrainType t) {
		if(!pointInBounds(p)) return;
		squares[p.x][p.y].setTerrain(t);
	}
	
	public void setBlood(Point p, boolean b) {
		if(!pointInBounds(p)) return;
		squares[p.x][p.y].setBlood(b);
	}
	
	public void setMonster(Point p, Monster m) {
		if(!pointInBounds(p)) return;
		squares[p.x][p.y].setMonster(m);
		if(m != null)
			m.getState().setLocation(p);
	}
	
	public void setField(Point p, Field f) {
		if(!pointInBounds(p)) return;
		squares[p.x][p.y].setField(f);
	}
	
	public boolean pointInBounds (Point p) {
		return p.x >= 0 && p.y >= 0 && p.x < getWidth() && p.y < getHeight();
	}
	
	public void placeParty(int team) {
		Player player = Game.inst().nthPlayer(team);
		Point point = (Point)startPoints.get(team).clone();
		int dir = team == 0 ? 1 : -1;
		int partySize = player.partySize();
		int curMonster = 0;
		
		for(int rowSize = 1; curMonster < partySize; rowSize++) {
			for(int rowMember=0; rowMember < rowSize; rowMember++) {
				if(curMonster >= partySize) break;
				Monster monster = player.nthMonster(curMonster);
				setMonster(new Point(point.x, point.y), monster);
				point.x += 2;
				curMonster += 1;
			}
			point.y += dir;
			point.x -= rowSize*2 + 1;
		}
	}
	
	public class Square implements Serializable {
		private TerrainType terrain;
		boolean blood;
		private Monster monster;
		private Field field;
		
		public Square() {
			terrain = null;
			blood = false;
			monster = null;
			field = null;
		}
		
		public TerrainType getTerrain() { return terrain; }
		public boolean isBlood() { return blood; }
		public Monster getMonster() { return monster; }
		public Field getField() { return field; }
		
		public void setTerrain(TerrainType t) { terrain = t; }
		public void setBlood(boolean b) { blood = b; }
		public void setMonster(Monster m) { monster = m; }
		public void setField(Field f) { field = f; }
	}
}
