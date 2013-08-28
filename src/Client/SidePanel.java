package Client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JPanel;

import Client.Assets.IconType;
import Game.*;
import Game.GameEvent.EventType;
import Game.Item.ItemType;
import Game.Monster.MonsterState;
import Game.Spell.SpellType;


@SuppressWarnings("serial")
public class SidePanel extends JPanel {
	private HashMap<Integer, Rectangle> itemUseRects;
	private HashMap<Spell, Rectangle> spellRects;
	
	private Rectangle spellTypeRect;
	private SpellType spellType;
	
	public SidePanel() {
		this.setSize(533, 500);
		this.addMouseListener(new SideMouseListener());
		itemUseRects = new HashMap<Integer, Rectangle>();
		spellRects = new HashMap<Spell, Rectangle>();
		spellTypeRect = new Rectangle(419, 223, 70, 15);
		spellType = SpellType.MAGE;
	}
	
	@Override
	public void paintComponent(Graphics go) {
		super.paintComponent(go);
		GameClient client = GameClient.inst();
		Game game = Game.inst();
		
		// game not properly initialized, wait until later
		if(game.getActivePoint() == null) return;
		
		Graphics2D g = (Graphics2D)go;
		
		// PARTIES
		for(int j = 0; j < 2; j++) {
			int team = (client.isObserver() || client.getPlayerId() == 0) ? j : 1-j;
			int pbx = 1 + 270*j, pby = 1, pbw = 260, pbh = 127;
			
			g.setColor(Color.white);
			g.fillRect(pbx, pby, pbw, pbh);
		
			Player player = game.nthPlayer(team);
			
			g.setColor(new Color(0x00, 0x00, 0x99));
			g.setStroke(new BasicStroke(2));
			g.drawRect(pbx, pby, pbw, pbh);
		
			g.setPaint(new GradientPaint(pbx + 1, pby + 1, new Color(0x9999FF), pbx + 1, pby + 21, Color.white));
			g.fillRect(pbx + 1, pby + 1, pbw - 2, 20);
			g.setColor(Color.black);
			g.setFont(Assets.headerFont);
			String headerStr;
			String uname = player.getUsername();
			if(client.isObserver())
				headerStr = "Player " + Integer.toString(team + 1) + " - " + uname;
			else
				headerStr = (j == 0 ? "Your Party - " + uname : "Opponent - " + uname);
			g.drawString(headerStr, pbx + 16, pby + 15);
		
			g.setColor(new Color(0xCCCCCC));
			g.fillRect(pbx + 1, pby + 21, pbw - 2, 15);
			g.setColor(Color.black);
			g.setFont(Assets.headerFont3);
			g.drawString("#", pbx + 10, pby + 32);
			g.drawString("Name", pbx + 40, pby + 32);
			g.drawString("HP", pbx + 165, pby + 32);
			g.drawString("SP", pbx + 200, pby + 32);
			g.drawString("AP", pbx + 230, pby + 32);
		
			g.setFont(Assets.infoFont);
		
			for(Monster monster : player.getMonsters()) {
				int i = monster.getTeamNumber();
				int y = pby+35 + 15*i;
			
				Monster activeMonster = game.getActiveMonster();
				if(activeMonster != null && team == game.getTurn() && activeMonster.getTeamNumber() == i) {
					g.setColor(new Color(0xFFFF66));
					g.fillRect(pbx + 1, y, pbw - 2, 15);
					g.setColor(Color.black);
				}
			
				g.drawString(Integer.toString(i+1), pbx + 10, y + 11);
				g.drawString(monster.getName(), pbx + 40, y + 11);
				
				MonsterState state = monster.getState();
				int iconSize = Assets.ICON_SIZE;
				if(state.getPoison() > 0) {
					Point srcPoint = Assets.iconPoint(IconType.POISON);
					g.drawImage(Assets.iconImage, pbx + 121, y + 2, pbx + 121 + iconSize, y + 2 + iconSize, 
							srcPoint.x, srcPoint.y, srcPoint.x + iconSize, srcPoint.y + iconSize, null);
				}
				if(state.getSpeed() != 4) {
					Point srcPoint = Assets.iconPoint(state.getSpeed() < 4 ? IconType.SLOWED : IconType.HASTED);
					g.drawImage(Assets.iconImage, pbx + 135, y + 2, pbx + 135 + iconSize, y + 2 + iconSize, 
							srcPoint.x, srcPoint.y, srcPoint.x + iconSize, srcPoint.y + iconSize, null);
				}
				if(state.getBlessed() != 5) {
					Point srcPoint = Assets.iconPoint(state.getBlessed() < 5 ? IconType.CURSED : IconType.BLESSED);
					g.drawImage(Assets.iconImage, pbx + 149, y + 2, pbx + 149 + iconSize, y + 2 + iconSize, 
							srcPoint.x, srcPoint.y, srcPoint.x + iconSize, srcPoint.y + iconSize, null);
				}
				
				if(monster.getState().isDead()) {
					g.drawString("DEAD", pbx + 165, y + 11);
				}
				else {
					g.drawString(Integer.toString(monster.getState().getCurHealth()), pbx + 165, y + 11);
					g.drawString(Integer.toString(monster.getState().getSpellPoints()), pbx + 200, y + 11);
					g.drawString(Integer.toString(monster.getState().getActionPoints()), pbx + 230, y + 11);
				}
			}
		}
		
		if(!client.isMyTurn()) return;
		if(game.getActiveMonster() == null) return; // weird situations where we might be in middle of a move
		
		int mbx = 1, mby = 135, mbw = 530, mbh = 364;
		
		g.setColor(Color.white);
		g.fillRect(mbx, mby, mbw, mbh);
		
		// ACTIVE MONSTER
		g.setColor(new Color(0x00, 0x00, 0x99));
		g.setStroke(new BasicStroke(2));
		g.drawRect(mbx, mby, mbw, mbh);
		
		g.setPaint(new GradientPaint(mbx + 1, mby + 1, new Color(0x9999FF), mbx + 1, mby + 21, Color.white));
		g.fillRect(mbx + 1, mby + 1, mbw - 2, 20);
		g.setColor(Color.black);
		g.setFont(Assets.headerFont);
		g.drawString("Active Monster", mbx + 16, mby + 15);
		
		Monster monster = game.getActiveMonster();
		MonsterState state = monster.getState();
		g.setFont(Assets.headerFont2);
		g.drawString(monster.getName(), mbx + 12, mby + 37);
	    g.drawString("Inventory", mbx + 12, mby + 100);
	    g.drawString("Spells", mbx + 380, mby + 100);
		
		int graphic = monster.getGraphic();
		int width = MapPanel.SQ_WIDTH, height = MapPanel.SQ_HEIGHT;
		Point srcPoint = Assets.monsterPoint(graphic, false);
		Point dstPoint = new Point(mbx + 16, mby + 45);
		g.drawImage(Assets.monsterImg, dstPoint.x, dstPoint.y, dstPoint.x+width, dstPoint.y+height, 
				srcPoint.x, srcPoint.y, srcPoint.x+width, srcPoint.y+height, null);
		
		g.setFont(Assets.infoFont);
		g.drawString("HP:", mbx + 49, mby + 53);
		g.drawString("SP:", mbx + 49, mby + 67);
		g.drawString("AP:", mbx + 49, mby + 81);
		g.drawString(Integer.toString(state.getCurHealth()) + " / " + Integer.toString(monster.getMaxHealth()), mbx + 174, mby + 53);
		g.drawString(Integer.toString(state.getSpellPoints()) + " / " + Integer.toString(monster.getBaseSpellPoints()), mbx + 174, mby + 67);
		g.drawString(Integer.toString(state.getActionPoints()), mbx + 69, mby + 81);
		
		Color teamColor = monster.getTeam() == 0 ? MapPanel.TEAM1_COLOR : MapPanel.TEAM2_COLOR;
		g.setColor(Color.lightGray);
		g.fillRect(mbx + 69, mby + 47, 100, 5);
		g.fillRect(mbx + 69, mby + 61, 100, 5);
		g.setColor(teamColor);
		g.fillRect(mbx + 69, mby + 47, 100 * state.getCurHealth() / monster.getMaxHealth(), 5);
		g.setColor(new Color(0x336633));
		g.fillRect(mbx + 69, mby + 61, monster.getBaseSpellPoints() == 0 ? 0 : 100 * state.getSpellPoints() / monster.getBaseSpellPoints(), 5);
		
		g.drawString("Att:", mbx + 230, mby + 53);
		g.drawString("Def:", mbx + 230, mby + 67);
		g.drawString("Mag:", mbx + 230, mby + 81);
		g.drawString(Integer.toString(monster.getAttack()), mbx + 260, mby + 53);
		g.drawString(Integer.toString(monster.getDefense()), mbx + 260, mby + 67);
		g.drawString(Integer.toString(monster.getMagic()), mbx + 260, mby + 81);
		
		g.drawString("Dmg:", mbx + 290, mby + 53);
		g.drawString("Arm:", mbx + 290, mby + 67);
		g.drawString("Enc:", mbx + 290, mby + 81);
		g.drawString(monster.getMeleeDamage().toString(), mbx + 320, mby + 53);
		g.drawString(Integer.toString(monster.getArmor()), mbx + 320, mby + 67);
		g.drawString(Integer.toString(monster.getEncumberance()), mbx + 320, mby + 81);
		
		g.drawString("Spd:", mbx + 370, mby + 53);
		g.drawString("Bsd:", mbx + 370, mby + 67);
		g.drawString("Poi:", mbx + 370, mby + 81);
		g.drawString(Integer.toString(state.getSpeed()), mbx + 400, mby + 53);
		g.drawString(Integer.toString(state.getBlessed()), mbx + 400, mby + 67);
		g.drawString(Integer.toString(state.getPoison()), mbx + 400, mby + 81);
		
		g.drawString("RF:", mbx + 430, mby + 53);
		g.drawString("RM:", mbx + 430, mby + 67);
		g.drawString("RC:", mbx + 430, mby + 81);
		g.drawString(Integer.toString(monster.getFireResist()), mbx + 460, mby + 53);
		g.drawString(Integer.toString(monster.getMagicResist()), mbx + 460, mby + 67);
		g.drawString(Integer.toString(monster.getColdResist()), mbx + 460, mby + 81);
		
		// ITEMS
		g.setColor(new Color(0xCCCCCC));
		g.fillRect(mbx + 9, mby + 107, 355, 15);
		g.setColor(Color.black);
		g.setFont(Assets.headerFont3);
		g.drawString("Name", mbx + 30, mby + 118);
		g.drawString("Wgt", mbx + 130, mby + 118);
		g.drawString("Qty", mbx + 160, mby + 118);
		g.drawString("Properties", mbx + 190, mby + 118);
		g.drawString("Use", mbx + 340, mby + 118);
		
		g.setFont(Assets.infoFont);
		
		itemUseRects.clear();
		int i = 0;
		for(Item item : monster.getItems()) {
			int y = mby+125 + 18*i;
			
			if(i%2 == 1) {
				g.setColor(new Color(0xEEEEEE));
				g.fillRect(mbx + 9, y - 1, 355, 18);
				g.setColor(Color.black);
			}
			
			srcPoint = Assets.objectPoint(item.getGraphic());
			dstPoint = new Point(mbx + 10, y);
			g.drawImage(Assets.objectImg, dstPoint.x, dstPoint.y, dstPoint.x+18, dstPoint.y+18, 
					srcPoint.x, srcPoint.y, srcPoint.x+18, srcPoint.y+18, null);
			
			g.drawString(item.getName(), mbx+30, y+10);
			g.drawString(Integer.toString(item.getTotalWeight()), mbx + 130, y+10);
			g.drawString(Integer.toString(item.getQuantity()), mbx + 160, y+10);
			g.drawString(item.getAbilityString(), mbx + 190, y+10);
			if(item.getQuantity() > 0 && (item.getType() == ItemType.MISSILE || item.getType() == ItemType.POTION)) {
				Point src = Assets.iconPoint(IconType.USE);
				int iconSize = Assets.ICON_SIZE;
				g.drawImage(Assets.iconImage, mbx + 345, y + 2, mbx + 345 + iconSize, y + 2 + iconSize, 
						src.x, src.y, src.x + iconSize, src.y + iconSize, null);
				itemUseRects.put(i, new Rectangle(mbx + 343, y, 20, 18));
			}
			i++;
		}
		
		// SPELLS
		g.setColor(new Color(0xCCCCCC));
		g.fillRect(mbx + 375, mby + 107, 145, 15);
		g.setColor(Color.black);
		g.setFont(Assets.headerFont3);
		g.drawString("Spell", mbx + 380, mby + 118);
		g.drawString("Lvl", mbx + 470, mby + 118);
		g.drawString("SP", mbx + 500, mby + 118);
		
		g.setColor(Color.blue);
		g.setFont(Assets.linkFont);
		String spellStr = spellType == SpellType.MAGE ? "To Priest ->" : "To Mage ->";
		g.drawString(spellStr, mbx+420, mby+100);
		g.setColor(Color.black);
		g.setFont(Assets.infoFont);
		
		g.setFont(Assets.infoFont);
		
		spellRects.clear();
		i = 0;
		for(Spell spell : Spell.values()) {
			if(spell.getSpellType() != spellType) continue;
			
			int y = mby+125 + 15*i;
			
			if(i%2 == 1) {
				g.setColor(new Color(0xEEEEEE));
				g.fillRect(mbx + 375, y - 2, 145, 15);
				g.setColor(Color.black);
			}
			
			if(monster.getMagic() >= spell.getLevel() && state.getSpellPoints() >= spell.getSpellPoints()) {
				g.setColor(Color.blue);
				g.setFont(Assets.linkFont);
				g.drawString(spell.getName(), mbx+380, y+8);
				spellRects.put(spell, new Rectangle(mbx + 378, y-2, 100, 15));
				g.setColor(Color.black);
				g.setFont(Assets.infoFont);
			}
			else
				g.drawString(spell.getName(), mbx+380, y+8);
			
			g.drawString(Integer.toString(spell.getLevel()), mbx + 470, y+8);
			g.drawString(Integer.toString(spell.getSpellPoints()), mbx + 500, y+8);
			
			i++;
		}
	}
	
	public class SideMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			int x = e.getX(), y = e.getY();
			GameClient client = GameClient.inst();
			
			if(spellTypeRect.contains(x, y)) {
				spellType = spellType == SpellType.MAGE ? SpellType.PRIEST : SpellType.MAGE;
				repaint();
				return;
			}
			
			for(int i : itemUseRects.keySet()) {
				if(itemUseRects.get(i).contains(x, y)) {
					Monster monster = Game.inst().getActiveMonster();
					if(monster.getItems().get(i).getType() == ItemType.MISSILE) {
						client.setCastingPoint(monster.getState().getLocation());
						client.setItemUsed(i);
					}
					else if(monster.getItems().get(i).getType() == ItemType.POTION) {
						client.sendMessage(new Message(GameEvent.EventType.USE_POTION).add(i));
					}
				}
			}
			
			for(Spell spell : spellRects.keySet()) {
				if(spellRects.get(spell).contains(x, y)) {
					if(spell == Spell.LIGHT_HEAL_ALL) {
						client.sendMessage(new Message(EventType.SPELL).add(Spell.LIGHT_HEAL_ALL).add(new Point(0, 0)));
					}
					else {
						Monster monster = Game.inst().getActiveMonster();
						client.setCastingPoint(monster.getState().getLocation());
						client.setCastingSpell(spell);
					}
				}
			}
		}

		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
	}
}
