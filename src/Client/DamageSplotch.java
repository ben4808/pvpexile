package Client;

import java.awt.Point;
import java.util.Timer;
import java.util.TimerTask;

import Game.Damage.DamageGraphic;

public class DamageSplotch {
	public static final int DURATION = 750;
	public static final int NUM_FRAMES = 8;
	
	private Point point;
	private DamageGraphic graphic;
	private int amount;
	private int frame;
	
	public DamageSplotch(Point p, DamageGraphic g, int a) {
		point = p;
		graphic = g;
		amount = a;
		frame = 0;
		
		if (g.isAnimated()) {
			int interval = DURATION / NUM_FRAMES;
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					frame++;
					GameClient.inst().getGameWindow().getMapPanel().repaint();
					if(frame >= NUM_FRAMES) this.cancel();
				}
			}, interval, interval);
		}
	}
	
	public Point getPoint() { return point; }
	public DamageGraphic getGraphic() { return graphic; }
	public int getAmount() { return amount; }
	public int getFrame() { return frame; }
}
