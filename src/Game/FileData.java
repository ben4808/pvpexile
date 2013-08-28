package Game;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Game.ItemEffect;
import Game.ItemEffect.ItemEffectType;
import Game.Item.ItemType;


@SuppressWarnings("serial")
public class FileData implements Serializable {
	public static final String DATA_DIRECTORY = System.getProperty("user.dir") + "\\assets\\data\\";
	
	private HashMap<Character, TerrainType> terrain;
	private HashMap<Integer, Item> items;
	
	private static FileData singleton = null;
	public static FileData inst() {
		if(singleton == null) singleton = new FileData();
		return singleton;
	}
	
	public FileData() {
		terrain = new HashMap<Character, TerrainType>();
		items = new HashMap<Integer, Item>();
	}
	
	public class TerrainType implements Serializable {
		private String name;
		private int graphic;
		private boolean solid;
		private boolean opaque;
		
		public TerrainType(String n, int g, boolean s, boolean o) {
			name = n;
			graphic = g;
			solid = s;
			opaque = o;
		}
		
		public String getName() { return name; }
		public int getGraphic() { return graphic; }
		public boolean isSolid() { return solid; }
		public boolean isOpaque() { return opaque; }
	}
	
	public TerrainType getTerrain(char c) { return terrain.containsKey(c) ? terrain.get(c) : null; }
	
	public void loadTerrain() throws IOException, BadFileFormatException {
		Scanner scanner = new Scanner(new File(DATA_DIRECTORY + "terrain.txt"));
		
		while(scanner.hasNext()) {
			String line = scanner.nextLine().trim();
			if(line.length() == 0 || line.charAt(0) == '#')
				continue;
			
			String[] tokens = line.split(",");
			for(int i=0; i<tokens.length; i++) { tokens[i] = tokens[i].trim(); }
			if(tokens.length != 5 || tokens[0].length() != 1) throw new BadFileFormatException();
			char symbol = tokens[0].charAt(0);
			String name = tokens[1];
			int graphicId;
			boolean solid;
			boolean opaque;
			try {
				graphicId = Integer.parseInt(tokens[2]);
				solid = Integer.parseInt(tokens[3]) == 1;
				opaque = Integer.parseInt(tokens[4]) == 1;
			}
			catch(NumberFormatException e) {
				throw new BadFileFormatException();
			}
				
			terrain.put(symbol, new TerrainType(name, graphicId, solid, opaque));
		}
	}
	
	public Item getItem(int id) { return items.containsKey(id) ? items.get(id) : null; }
	
	public void loadItems() throws IOException, BadFileFormatException {
		Scanner scanner = new Scanner(new File(DATA_DIRECTORY + "items.txt"));
		
		while(scanner.hasNext()) {
			String line = scanner.nextLine().trim();
			if(line.length() == 0 || line.charAt(0) == '#') continue;
			
			String[] tokens = line.split(",");
			for(int i=0; i<tokens.length; i++) { tokens[i] = tokens[i].trim(); }
			
			if(tokens.length != 5 && tokens.length != 6) throw new BadFileFormatException();
			int id;
			String name;
			int graphic;
			int typeId;
			int weight;
			int quantity;
			ArrayList<ItemEffect> effects;
			try {
				id = Integer.parseInt(tokens[0]);
				name = tokens[1];
				graphic = Integer.parseInt(tokens[2]);
				typeId = Integer.parseInt(tokens[3]);
				weight = Integer.parseInt(tokens[4]);
				quantity = tokens.length == 6 ? Integer.parseInt(tokens[5]) : 1;
			}
			catch(NumberFormatException e) {
				throw new BadFileFormatException();
			}
			
			ItemType type = null;
			switch(typeId) {
			case 0: type = ItemType.ONE_HAND_WEAPON; break;
			case 1: type = ItemType.TWO_HAND_WEAPON; break;
			case 2: type = ItemType.PLATE; break;
			case 3: type = ItemType.HELM; break;
			case 4: type = ItemType.SHIELD; break;
			case 5: type = ItemType.AMULET; break;
			case 6: type = ItemType.POTION; break;
			case 7: type = ItemType.MISSILE; break;
			case 8: type = ItemType.NO_TYPE; break;
			}
			
			effects = new ArrayList<ItemEffect>();
			while(true) {
				if(!scanner.hasNext()) break;
				String effectLine = scanner.nextLine().trim();
				if(effectLine.length() == 0) break;
				if(effectLine.charAt(0) == '#') continue;
				effectLine = effectLine.replace(" ", "");
				String[] effectTokens = effectLine.split(",");
				if(effectTokens.length != 2) throw new BadFileFormatException();
				
				int effectId;
				int amount;
				try {
					effectId = Integer.parseInt(effectTokens[0]);
					amount = Integer.parseInt(effectTokens[1]);
				}
				catch(NumberFormatException e) {
					throw new BadFileFormatException();
				}
				
				ItemEffectType effectType = null;
				switch(effectId) {
				case 0: effectType = ItemEffectType.MELEE_DAMAGE; break;
				case 1: effectType = ItemEffectType.MELEE_BONUS; break;
				case 2: effectType = ItemEffectType.POISON; break;
				case 3: effectType = ItemEffectType.CURSE; break;
				case 4: effectType = ItemEffectType.SLOW; break;
				case 5: effectType = ItemEffectType.ARMOR; break;
				case 6: effectType = ItemEffectType.RESIST_FIRE; break;
				case 7: effectType = ItemEffectType.RESIST_MAGIC; break;
				case 8: effectType = ItemEffectType.RESIST_COLD; break;
				case 9: effectType = ItemEffectType.HEAL; break;
				case 10: effectType = ItemEffectType.ENERGY; break;
				case 11: effectType = ItemEffectType.CURE_POISON; break;
				case 12: effectType = ItemEffectType.BLESS; break;
				case 13: effectType = ItemEffectType.HASTE; break;
				}
				
				effects.add(new ItemEffect(effectType, amount));
			}
				
			items.put(id, new Item(name, id, graphic, type, weight, quantity, effects));
		}
	}
	
	public void loadMap(String filename) throws IOException, BadFileFormatException {
		Scanner scanner = new Scanner(new File(DATA_DIRECTORY + filename));
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<Point> startPoints = new ArrayList<Point>();
		String name = null;
		int width = 0, height = 0;
		int state = 0;
		
		while(scanner.hasNext()) {
			String line = scanner.nextLine().trim();
			if(line.length() == 0) {
				state++;
				continue;
			}
			if (line.charAt(0) == '#') continue;
			
			switch(state) {
			case 0:
				name = line;
				break;
				
			case 1:
				if(line.length() == 0) throw new BadFileFormatException();
				if(width == 0) width = line.length();
				if(line.length() != width) throw new BadFileFormatException();
				lines.add(line);
				height++;
				break;
				
			case 2: case 3:
				line = line.replace(" ", "");
				String[] tokens = line.split(",");
				if(tokens.length != 2) throw new BadFileFormatException();
				int x, y;
				try {
					x = Integer.parseInt(tokens[0]);
					y = Integer.parseInt(tokens[1]);
				}
				catch(NumberFormatException e) {
					throw new BadFileFormatException();
				}
				
				startPoints.add(new Point(x, y));
				break;
				
			default:
				throw new BadFileFormatException();
			}
		}
		
		GameMap map = new GameMap(name, width, height, startPoints);
		for (int row = 0; row < height; row++) {
			String line = lines.get(row);
			for(int col=0; col < width; col++) {
				if(!terrain.containsKey(line.charAt(col))) throw new BadFileFormatException();
				map.setTerrain(new Point(col, row), terrain.get(line.charAt(col)));
			}
		}
		
		Game.inst().setMap(map);
	}
	
	public void loadParty(int id, String filename) throws IOException, BadFileFormatException {	
		Game.inst().nthPlayer(id).setMonsters(new ArrayList<Monster>());
		
		Scanner scanner = new Scanner(new File(DATA_DIRECTORY + filename));
		int num = 0;
		while(scanner.hasNext()) {
			String name = scanner.nextLine().trim();
			if(name.length() == 0 || name.charAt(0) == '#') continue;
			
			String line2 = scanner.nextLine().trim();
			if(line2 == null || line2.length() == 0) throw new BadFileFormatException();
			line2 = line2.replace(" ", "");
			String[] tokens2 = line2.split(",");
			if(tokens2.length != 5) throw new BadFileFormatException();
			int graphic, health, attack, defense, magic;
			try {
				graphic = Integer.parseInt(tokens2[0]);
				health = Integer.parseInt(tokens2[1]);
				attack = Integer.parseInt(tokens2[2]);
				defense = Integer.parseInt(tokens2[3]);
				magic = Integer.parseInt(tokens2[4]);
			}
			catch(NumberFormatException e) {
				throw new BadFileFormatException();
			}
			
			Monster monster = new Monster(name, graphic, health, attack, defense, magic, id, num);
			num++;
			
			String line3 = scanner.nextLine().trim();
			if(line3 == null || line3.length() == 0) continue;
			line3 = line3.replace(" ", "");
			String[] tokens3 = line3.split(",");
			for(String token : tokens3) {
				try {
					int itemId = Integer.parseInt(token);
					monster.addItem(new Item(getItem(itemId)));
				}
				catch(NumberFormatException e) {
					throw new BadFileFormatException();
				}
			}
			Game.inst().nthPlayer(id).addMonster(monster);
		}
	}
}
