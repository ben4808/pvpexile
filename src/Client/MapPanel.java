package Client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import Game.Damage.DamageGraphic;
import Game.Field;
import Game.FileData.TerrainType;
import Game.Game;
import Game.GameEvent.CursorDir;
import Game.GameMap;
import Game.Message;
import Game.Monster;
import Game.GameEvent.EventType;
import Game.Spell;


@SuppressWarnings("serial")
public class MapPanel extends JPanel {
	
	public static final int VIEW_WIDTH = 19, VIEW_HEIGHT = 19;
	public static final int SQ_WIDTH = 28, SQ_HEIGHT = 36;
	public static final Color TEAM1_COLOR = Color.blue, TEAM2_COLOR = new Color(0x990000);
	
	private Point centerSq;
	private Point cursorPx;
	
	private Point attackingPoint;
	private HashMap<Integer, DamageSplotch> damages;
	private int splotchId;
	
	private ArrayList<Point> targetedSquares;
	private Point targetLineStart;
	private Point targetLineEnd;

	public MapPanel() {
		centerSq = new Point(0, 0);
		cursorPx = new Point(0, 0);
		
		attackingPoint = null;
		damages = new HashMap<Integer, DamageSplotch>();
		splotchId = 0;
		
		this.setSize(VIEW_WIDTH*SQ_WIDTH, VIEW_HEIGHT*SQ_HEIGHT);
		new MapInputHandler();
		this.setFocusable(true);
	}
	
	public boolean hasSplotches() { return damages.size() > 0; }
	
	public void setTargetedSquares(ArrayList<Point> squares, Point start, Point end) {
		targetedSquares = squares;
		targetLineStart = start;
		targetLineEnd = end;
	}
	
	public void clearTargetedSquares() {
		targetedSquares = null;
		targetLineStart = null;
		targetLineEnd = null;
	}
	
	public void snapToActive() {
		Point point = Game.inst().getActivePoint();
		if(point != null) {
			centerSq = (Point)Game.inst().getActivePoint().clone();
			repaint();
		}
	}
	
	private boolean isVisibleFromActive(Point p) {
		GameMap map = Game.inst().getMap();
		
		Point curSquare = new Point(cursorPx.x / SQ_WIDTH, cursorPx.y / SQ_HEIGHT);
		Point mapSquare = null;
		while(true) {
			mapSquare = dispSquareToMapSquare(curSquare);
			if(mapSquare.equals(Game.inst().getActivePoint())) break;
			TerrainType terrain = map.terrainAt(mapSquare);
			if(terrain == null || terrain.isOpaque()) return false;
			
			Point squareCenter = new Point(curSquare.x*SQ_WIDTH + SQ_WIDTH/2, curSquare.y*SQ_HEIGHT + SQ_HEIGHT/2);
			CursorDir dir = getCursorDirection(squareCenter, true);
			
			switch(dir) {
			case N: curSquare.y++; break;
			case NE: curSquare.x--; curSquare.y++; break;
			case E: curSquare.x--; break;
			case SE: curSquare.x--; curSquare.y--; break;
			case S: curSquare.y--; break;
			case SW: curSquare.x++; curSquare.y--; break;
			case W: curSquare.x++; break;
			case NW: curSquare.x++; curSquare.y++; break;
			}
		}
		return true;
	}
	
	private int distanceFromActive(Point p) {
		Point curSquare = new Point(cursorPx.x / SQ_WIDTH, cursorPx.y / SQ_HEIGHT);
		Point mapSquare = dispSquareToMapSquare(curSquare);
		Point activePoint = Game.inst().getActivePoint();
		int dx = Math.abs(mapSquare.x - activePoint.x);
		int dy = Math.abs(mapSquare.y - activePoint.y);
		double dist = Math.sqrt(dx*dx + dy*dy);
		return (int)Math.floor(dist);
	}
	
	private boolean canCast (Spell spell, Point p) {
		if(distanceFromActive(p) > spell.getRange()) return false;
		if(spell.needsVision() && !isVisibleFromActive(p)) return false;
		return true;
	}
	
	private boolean canThrow (Point p) {
		return distanceFromActive(p) <= 7 && isVisibleFromActive(p);
	}
	
	private boolean canTargetSquare(Point point) {
		GameClient client = GameClient.inst();
		GameMap map = Game.inst().getMap();
		
		if(!map.pointInBounds(point) || map.terrainAt(point).isSolid()) return false;
		if(client.getCastingSpell() != null && !canCast(client.getCastingSpell(), point)) 
			return false; // can't cast spell here
		if(client.getCastingSpell() == null && !canThrow(point))
			return false; // can't use missile here
		
		return true;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(Game.inst().getMap() != null)
			drawMap(g);
	}
	
	private void drawMap(Graphics go) {
		GameMap map = Game.inst().getMap();
		Graphics2D g = (Graphics2D)go;
		int mapWidth = map.getWidth(), mapHeight = map.getHeight();
		
		for(int y=0; y < VIEW_HEIGHT; y++) {
			for (int x=0; x < VIEW_WIDTH; x++) {
				Point dispPoint = new Point(x, y);
				Point mapPoint = dispSquareToMapSquare(dispPoint);
				Point tlPixel = new Point(SQ_WIDTH*x, SQ_HEIGHT*y);
				if(mapPoint.x < 0 || mapPoint.y < 0 || mapPoint.x >= mapWidth || mapPoint.y >= mapHeight) {
					g.setColor(Color.black);
					g.fillRect(tlPixel.x, tlPixel.y, SQ_WIDTH, SQ_HEIGHT);
					continue;
				}
				
				// terrain
				TerrainType terrain = map.terrainAt(mapPoint);
				Point srcPoint = Assets.terrainPoint(terrain.getGraphic());
				g.drawImage(Assets.terrainImg, tlPixel.x, tlPixel.y, tlPixel.x+SQ_WIDTH, tlPixel.y+SQ_HEIGHT, 
						srcPoint.x, srcPoint.y, srcPoint.x+SQ_WIDTH, srcPoint.y+SQ_HEIGHT, null);
				
				// blood
				if (map.isBloodAt(mapPoint)) {
					srcPoint = Assets.bloodPoint();
					g.drawImage(Assets.fieldImg, tlPixel.x, tlPixel.y, tlPixel.x+SQ_WIDTH, tlPixel.y+SQ_HEIGHT, 
							srcPoint.x, srcPoint.y, srcPoint.x+SQ_WIDTH, srcPoint.y+SQ_HEIGHT, null);
				}
				
				// field
				Field field = map.fieldAt(mapPoint);
				if(field != null) {
					srcPoint = Assets.fieldPoint(field);
					g.drawImage(Assets.fieldImg, tlPixel.x, tlPixel.y, tlPixel.x+SQ_WIDTH, tlPixel.y+SQ_HEIGHT, 
							srcPoint.x, srcPoint.y, srcPoint.x+SQ_WIDTH, srcPoint.y+SQ_HEIGHT, null);
				}
				
				// monster
				Monster monster = map.monsterAt(mapPoint);
				if(monster != null) {
					srcPoint = Assets.monsterPoint(monster.getGraphic(), mapPoint.equals(attackingPoint));
					g.drawImage(Assets.monsterImg, tlPixel.x, tlPixel.y, tlPixel.x+SQ_WIDTH, tlPixel.y+SQ_HEIGHT, 
							srcPoint.x, srcPoint.y, srcPoint.x+SQ_WIDTH, srcPoint.y+SQ_HEIGHT, null);
					
					Color teamColor = monster.getTeam() == 0 ? TEAM1_COLOR : TEAM2_COLOR;
					g.setColor(Color.lightGray);
					g.fillRect(tlPixel.x, tlPixel.y, SQ_WIDTH - 1, 3);
					g.setColor(teamColor);
					g.fillRect(tlPixel.x, tlPixel.y, SQ_WIDTH * monster.getState().getCurHealth() / monster.getMaxHealth() - 1, 3);
					
					Point active = Game.inst().getActivePoint();
					if(monster.getState().getLocation().equals(active))
						g.drawRect(tlPixel.x, tlPixel.y, SQ_WIDTH - 1, SQ_HEIGHT - 1);
				}
			}
		}
		
		// splotches
		for (int key : damages.keySet()) {
			DamageSplotch splotch = damages.get(key);
			Point dispPoint = mapSquareToDispSquare(splotch.getPoint());
			Point tlPixel = new Point(dispPoint.x * SQ_WIDTH, dispPoint.y * SQ_HEIGHT);
			Point srcPoint = Assets.damageFieldPoint(splotch.getGraphic(), splotch.getFrame());
			g.drawImage(Assets.fieldImg, tlPixel.x, tlPixel.y, tlPixel.x + SQ_WIDTH, tlPixel.y + SQ_HEIGHT, 
					srcPoint.x, srcPoint.y, srcPoint.x + SQ_WIDTH, srcPoint.y + SQ_HEIGHT, null);
			
			if(splotch.getAmount() > 0) {
				g.setFont(Assets.damageFont);
				int dmgX = 0;
				if(splotch.getAmount() >= 100) dmgX = tlPixel.x + 5;
				else if(splotch.getAmount() >= 10) dmgX = tlPixel.x + 8;
				else dmgX = tlPixel.x + 11;
				
				// draw damage with black outline for visibility
				String dmgStr = Integer.toString(splotch.getAmount());
				g.setColor(Color.black);
				g.drawString(dmgStr, dmgX - 1, tlPixel.y + 22 - 1);
				g.drawString(dmgStr, dmgX + 1, tlPixel.y + 22 + 1);
				g.drawString(dmgStr, dmgX + 1, tlPixel.y + 22 - 1);
				g.drawString(dmgStr, dmgX - 1, tlPixel.y + 22 + 1);
				g.setColor(Color.white);
				g.drawString(dmgStr, dmgX, tlPixel.y + 22);
			}
		}
		
		// target squares
		if(targetLineEnd == null || !canTargetSquare(targetLineEnd)) return;
		g.setStroke(new BasicStroke(2));
		g.setColor(new Color(0xEEEEEE));
		for(Point point : targetedSquares) {
			Point dispPoint = mapSquareToDispSquare(point);
			Point tlPixel = new Point(dispPoint.x * SQ_WIDTH, dispPoint.y * SQ_HEIGHT);
			g.drawRect(tlPixel.x + 1, tlPixel.y + 1, SQ_WIDTH - 1, SQ_HEIGHT - 1);
		}
		
		Point startPoint = mapSquareToDispSquare(targetLineStart);
		Point startTl = new Point(startPoint.x * SQ_WIDTH + SQ_WIDTH/2, startPoint.y * SQ_HEIGHT + SQ_HEIGHT/2);
		Point endPoint = mapSquareToDispSquare(targetLineEnd);
		Point endTl = new Point(endPoint.x * SQ_WIDTH + SQ_WIDTH/2, endPoint.y * SQ_HEIGHT + SQ_HEIGHT/2);
		g.drawLine(startTl.x, startTl.y, endTl.x, endTl.y);
	}
	
	public CursorDir getCursorDirection(Point cursor, boolean useFactor) {
		Point topLeftSquare = new Point(centerSq.x - VIEW_HEIGHT/2, centerSq.y - VIEW_HEIGHT/2);
		Point activeSquare = Game.inst().getActivePoint();
		Point cursorSquare = dispSquareToMapSquare(new Point(cursor.x / SQ_WIDTH, cursor.y / SQ_HEIGHT));
		
		if(activeSquare == null || activeSquare.equals(cursorSquare)) {
			return CursorDir.ON_ACTIVE;
		}
		
		CursorDir cursorDir = null;
		Point activeCenterPixel = new Point((activeSquare.x-topLeftSquare.x)*SQ_WIDTH + SQ_WIDTH/2, (activeSquare.y-topLeftSquare.y)*SQ_HEIGHT + SQ_HEIGHT/2);
		double factor = useFactor ? SQ_HEIGHT / (double)SQ_WIDTH : 1;
		double line1 = 12*factor*(cursor.x - activeCenterPixel.x)/5 - activeCenterPixel.y;
		double line2 = -12*factor*(cursor.x - activeCenterPixel.x)/5 - activeCenterPixel.y;
		double line3 = 2*factor*(cursor.x - activeCenterPixel.x)/5 - activeCenterPixel.y;
		double line4 = -2*factor*(cursor.x - activeCenterPixel.x)/5 - activeCenterPixel.y;
		
		int cursY = -cursor.y;
		if (cursY >= line1 && cursY >= line2) cursorDir = CursorDir.N;
		else if (cursY >= line3 && cursY < line1) cursorDir = CursorDir.NE;
		else if (cursY >= line4 && cursY < line3) cursorDir = CursorDir.E;
		else if (cursY >= line2 && cursY < line4) cursorDir = CursorDir.SE;
		else if (cursY < line1 && cursY < line2) cursorDir = CursorDir.S;
		else if (cursY >= line1 && cursY < line3) cursorDir = CursorDir.SW;
		else if (cursY >= line3 && cursY < line4) cursorDir = CursorDir.W;
		else if (cursY >= line4 && cursY < line2) cursorDir = CursorDir.NW;
		return cursorDir;
	}
	
	public Point mapSquareToDispSquare(Point p) {
		int x = p.x - centerSq.x + VIEW_WIDTH / 2;
		int y = p.y - centerSq.y + VIEW_HEIGHT / 2;
		return new Point(x, y);
	}
	
	public Point dispSquareToMapSquare(Point p) {
		int x = p.x + centerSq.x - VIEW_WIDTH / 2;
		int y = p.y + centerSq.y - VIEW_HEIGHT / 2;
		return new Point(x, y);
	}
	
	public class MapInputHandler {
		public MapInputHandler() {
			addMouseListener(new MapClickHandler());
			addMouseMotionListener(new MapMouseHandler());
			addKeyListener(new MapKeyHandler());
		}
		
		public class MapClickHandler implements MouseListener {

			@Override
			public void mouseClicked(MouseEvent e) {
				GameClient client = GameClient.inst();
				if(!client.isMyTurn()) return;
				
				if(client.getCastingPoint() != null) {
					Point target = dispSquareToMapSquare(new Point(cursorPx.x / SQ_WIDTH, cursorPx.y / SQ_HEIGHT));
					
					if(!canTargetSquare(target)) {
						client.getGameWindow().addStatus("Can't target this square. Cancelled.");
						client.setCastingPoint(null);
						client.setCastingSpell(null);
						clearTargetedSquares();
						return;
					}
					
					if(client.getCastingSpell() != null) {
						client.sendMessage(new Message(EventType.SPELL).add(client.getCastingSpell()).add(target));
					}
					else {
						client.sendMessage(new Message(EventType.USE_MISSILE).add(client.getItemUsed()).add(target));
					}
					
					client.setCastingPoint(null);
					client.setCastingSpell(null);
					clearTargetedSquares();
				}
				else {
					CursorDir cursorDir = getCursorDirection(cursorPx, false);
					if(cursorDir == CursorDir.ON_ACTIVE) {
						client.sendMessage(new Message(EventType.STAND_READY));
						return;
					}
				    processMove(cursorDir);
				}
			}

			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		}
		
		public class MapMouseHandler implements MouseMotionListener {
			@Override
			public void mouseMoved(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				cursorPx = new Point(x, y);
				
				clearTargetedSquares();
				
				GameClient client = GameClient.inst();
				if(client.getCastingPoint() != null) {
					targetLineStart = client.getCastingPoint();
					Point targetSquare = dispSquareToMapSquare(new Point(x / SQ_WIDTH, y / SQ_HEIGHT));
					targetLineEnd = targetSquare;
					if(client.getCastingSpell() != null) {
						targetedSquares = client.getCastingSpell().getEffectPoints(targetSquare);
					}
					else {
						ArrayList<Point> newList = new ArrayList<Point>();
						newList.add(targetSquare);
						targetedSquares = newList;
					}
				}
				repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {}
		}
		
		public class MapKeyHandler implements KeyListener {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				int n = -1;
				switch(e.getKeyCode()) {
				case KeyEvent.VK_W:
					centerSq.y--;
					break;
				case KeyEvent.VK_S:
					centerSq.y++;
					break;
				case KeyEvent.VK_A:
					centerSq.x--;
					break;
				case KeyEvent.VK_D:
					centerSq.x++;
					break;
					
				case KeyEvent.VK_C:
					GameClient.inst().setCastingPoint(null);
					GameClient.inst().setCastingSpell(null);
					clearTargetedSquares();
					break;
					
				case KeyEvent.VK_U: case KeyEvent.VK_UP: processMove(CursorDir.N); break;
				case KeyEvent.VK_I: processMove(CursorDir.NE); break;
				case KeyEvent.VK_K: case KeyEvent.VK_RIGHT: processMove(CursorDir.E); break;
				case KeyEvent.VK_COMMA: processMove(CursorDir.SE); break;
				case KeyEvent.VK_M: case KeyEvent.VK_DOWN: processMove(CursorDir.S); break;
				case KeyEvent.VK_N: processMove(CursorDir.SW); break;
				case KeyEvent.VK_H: case KeyEvent.VK_LEFT: processMove(CursorDir.W); break;
				case KeyEvent.VK_Y: processMove(CursorDir.NW); break;
				
				case KeyEvent.VK_1: n = 0; break;
				case KeyEvent.VK_2: n = 1; break;
				case KeyEvent.VK_3: n = 2; break;
				case KeyEvent.VK_4: n = 3; break;
				case KeyEvent.VK_5: n = 4; break;
				case KeyEvent.VK_6: n = 5; break;
				}
				if(n >= 0 && GameClient.inst().isMyTurn())
					GameClient.inst().sendMessage(new Message(EventType.SWITCH_ACTIVE).add(n));
				repaint();
			}

			@Override
			public void keyReleased(KeyEvent e) {}
		}
	}
	
	public void processMove (CursorDir cursorDir) {
		Game game = Game.inst();
		GameClient client = GameClient.inst();
		if(!client.isMyTurn()) return;
		
		Point active = game.getActivePoint();
		if(client.getCastingPoint() != null) return;
		
		Point target = null;
		switch(cursorDir) {
		case N: target = new Point(active.x, active.y - 1); break;
		case NE: target = new Point(active.x + 1, active.y - 1); break;
		case E: target = new Point(active.x + 1, active.y); break;
		case SE: target = new Point(active.x + 1, active.y + 1); break;
		case S: target = new Point(active.x, active.y + 1); break;
		case SW: target = new Point(active.x - 1, active.y + 1); break;
		case W: target = new Point(active.x - 1, active.y); break;
		case NW: target = new Point(active.x - 1, active.y - 1); break;
		}
		
		GameMap map = game.getMap();
		if(target.x < 0 || target.y < 0 || target.x >= map.getWidth() || target.y >= map.getHeight()
				|| map.terrainAt(target).isSolid()) {
			client.getGameWindow().addStatus("Blocked: " + cursorDir.toString());
			return;
		}
		Monster targetMonster = map.monsterAt(target);
		if(targetMonster != null && targetMonster.getTeam() == game.getTurn() && targetMonster.getState().getActionPoints() == 0) {
			client.getGameWindow().addStatus("Can't switch with " + targetMonster.getName() + ".");
			return;
		}
	
		client.sendMessage(new Message(EventType.MOVE).add(cursorDir));
	}
	
	public void doAmimation (Point att, Point target, DamageGraphic graphic, int amount) {
		doAmimation (att, target, graphic, amount, true);
	}
	
	public void doAmimation (Point att, Point target, DamageGraphic graphic, int amount, boolean showIfZero) {
		if(att != null)
			attackingPoint = att;
		final int damage = amount;
		final DamageSplotch splotch = new DamageSplotch(target, graphic, damage);
		final int key = splotchId++;
		if(damage > 0 || showIfZero)
			damages.put(key, splotch);
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				attackingPoint = null;
				damages.remove(key);
				GameClient.inst().getGameWindow().getMapPanel().repaint();
			}
		}, DamageSplotch.DURATION);
		
		GameClient.inst().getGameWindow().getMapPanel().repaint();
	}
}
