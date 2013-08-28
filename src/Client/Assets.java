package Client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import Game.Damage.DamageGraphic;
import Game.Field;


public class Assets {
	public static final int ICON_SIZE = 12;
	public enum IconType {
		POISON, BLESSED, CURSED, HASTED, SLOWED, USE
	}
	
	public static final String IMG_DIRECTORY = System.getProperty("user.dir") + "\\assets\\images\\";
	public static final int SQUARE_WIDTH = 28;
	public static final int SQUARE_HEIGHT = 36;
	
	public static BufferedImage terrainImg;
	public static BufferedImage monsterImg;
	public static BufferedImage fieldImg;
	public static BufferedImage objectImg;
	public static BufferedImage iconImage;
	
	public static Font damageFont;
	public static Font headerFont;
	public static Font headerFont2;
	public static Font headerFont3;
	public static Font infoFont;
	public static Font linkFont;
	
	static {
		try {
			terrainImg = ImageIO.read(new File(IMG_DIRECTORY + "TER1.BMP"));
			monsterImg = makeColorTransparent(ImageIO.read(new File(IMG_DIRECTORY + "PCS.BMP")), Color.white);
			fieldImg = makeColorTransparent(ImageIO.read(new File(IMG_DIRECTORY + "FIELDS.BMP")), Color.white);
			objectImg = makeColorTransparent(ImageIO.read(new File(IMG_DIRECTORY + "TINYOBJ.BMP")), Color.white);
			iconImage = makeColorTransparent(ImageIO.read(new File(IMG_DIRECTORY + "MIXED.BMP")), Color.white);
			
			damageFont = new Font(Font.SANS_SERIF, Font.BOLD, 12);
			headerFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
			headerFont2 = new Font(Font.SANS_SERIF, Font.BOLD, 12);
			headerFont3 = new Font(Font.SANS_SERIF, Font.BOLD, 10);
			infoFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
			linkFont = new Font(Font.SANS_SERIF, Font.ITALIC, 10);
		} catch (IOException e) {
			System.out.println("Error loading image.");
		}
	}
	
	private static BufferedImage makeColorTransparent(BufferedImage im, final Color color) {
    	ImageFilter filter = new RGBImageFilter() {

    		// the color we are looking for... Alpha bits are set to opaque
    		public int markerRGB = color.getRGB() | 0xFF000000;

    		public final int filterRGB(int x, int y, int rgb) {
    			if ((rgb | 0xFF000000) == markerRGB) {
    				// Mark the alpha bits as zero - transparent
    				return 0x00FFFFFF & rgb;
    			} else {
    				// nothing to do
    				return rgb;
    			}
    		}
    	};

    	ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
    	Image image = Toolkit.getDefaultToolkit().createImage(ip);
    	
    	BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g2 = bufferedImage.createGraphics();
    	g2.drawImage(image, 0, 0, null);
    	g2.dispose();

    	return bufferedImage;
    }
	
	public static Point terrainPoint(int id) {
		int x = (id % 10) * SQUARE_WIDTH;
		int y = (id / 10) * SQUARE_HEIGHT;
		return new Point(x, y);
	}
	
	public static Point bloodPoint() {
		return new Point(2*SQUARE_WIDTH, 3*SQUARE_HEIGHT);
	}
	
	public static Point fieldPoint(Field field) {
		int x=0;
		switch(field) {
		case FORCE: x = 0; break;
		case FIRE: x = 1; break;
		case ICE: x = 4; break;
		case BLADES: x = 5; break;
		}
		
		return new Point(x * SQUARE_WIDTH, 1 * SQUARE_HEIGHT);
	}
	
	public static Point monsterPoint(int id, boolean attacking) {
		int x = (id / 8) * SQUARE_WIDTH*2;
		int y = (id % 8) * SQUARE_HEIGHT;
		if(attacking) x += (5 * SQUARE_WIDTH*2);
		return new Point(x, y);
	}
	
	public static Point damageFieldPoint (DamageGraphic graphic, int frame) {
		int x=0, y=0;
		switch(graphic) {
		case FIRE: x = 0; y = 0; break;
		case MAGIC: x = 1; y = 0; break;
		case POISON: x = 2; y = 0; break;
		case MELEE: x = 3; y = 0; break;
		case COLD: x = 4; y = 0; break;
		
		case ANIM_FIRE: x = frame; y = 4; break;
		case ANIM_MAGIC1: x = frame; y = 5; break;
		case ANIM_MAGIC2: x = frame; y = 6; break;
		}
		
		return new Point(x * SQUARE_WIDTH, y * SQUARE_HEIGHT);
	}
	
	public static Point objectPoint (int id) {
		int x = (id % 10) * 18;
		int y = (id / 10) * 18;
		return new Point(x, y);
	}
	
	public static Point iconPoint (IconType type) {
		if(type == IconType.USE)
			return new Point(0, 12);
		
		int x=0, y=0;
		switch(type) {
		case POISON: x = 0; y = 0; break;
		case BLESSED: x = 2; y = 0; break;
		case CURSED: x = 0; y = 1; break;
		case HASTED: x = 0; y = 2; break;
		case SLOWED: x = 2; y = 2; break;
		}
		
		return new Point(x*ICON_SIZE, 55 + y*ICON_SIZE);
	}
}
