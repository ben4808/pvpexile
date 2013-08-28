package Game;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

import Game.ItemEffect.ItemEffectType;
import Game.Damage.DamageBonus;


@SuppressWarnings("serial")
public class Monster implements Serializable {
	public class MonsterState implements Serializable {
		private Point location;
		private boolean dead;
		private int actionPoints;
		private int curHealth;
		private int spellPoints;
		private int speed;
		private int poison;
		private int blessed;
		
		public MonsterState() {
			location = null;
			dead = false;
			actionPoints = 0;
			curHealth = Monster.this.getMaxHealth();
			spellPoints = Monster.this.getBaseSpellPoints();
			speed = Monster.this.getBaseSpeed();
			poison = 0;
			blessed = Monster.this.getBaseBlessed();
		}
		
		public void refreshState() {
			curHealth = Monster.this.getMaxHealth();
			spellPoints = Monster.this.getBaseSpellPoints();
			speed = Monster.this.getBaseSpeed();
			blessed = Monster.this.getBaseBlessed();
		}
		
		public Point getLocation() { return location; }
		public boolean isDead() { return dead; }
		public int getActionPoints() { return actionPoints; }
		public int getCurHealth() { return curHealth; }
		public int getSpellPoints() { return spellPoints; }
		public int getSpeed() { return speed; }
		public int getPoison() { return poison; }
		public int getBlessed() { return blessed; }
		
		public void setLocation(Point p) { location = (Point)p.clone(); }
		public void setDead(boolean b) { dead = b; }
		public void setActionPoints(int n) { actionPoints = n; }
		public void setCurHealth(int n) { curHealth = n; }
		public void setSpellPoints(int n) { spellPoints = n; }
		public void setSpeed(int n) { speed = n; }
		public void setBlessed(int n) { blessed = n; }
		public void setPoison(int n) { poison = n; }
		public void changeCurHealth(int n) { curHealth = Math.max(0, Math.min(curHealth + n, Monster.this.getMaxHealth())); }
		public void changeSpellPoints(int n) { spellPoints = Math.max(0, Math.min(spellPoints + n, Monster.this.getBaseSpellPoints())); }
		public void changeSpeed(int n) { speed = Math.max(0, Math.min(speed + n, 15)); }
		public void changeBlessed(int n) { blessed = Math.max(0, Math.min(blessed + n, 10)); }
		public void changePoison(int n) { poison = Math.max(0, Math.min(poison + n, 10)); }
	}
	
	// display attributes
	private String name;
	private int graphic;
	
	// monster attributes
	private int maxHealth;
	private int attack;
	private int defense;
	private int magic;
	
	private int team;
	private int teamNumber;
	private ArrayList<Item> items;
	
	private MonsterState state;
	
	public String getName() { return name; }
	public int getGraphic() { return graphic; }
	public int getMaxHealth() { return maxHealth; }
	public int getAttack() { return attack; }
	public int getDefense() { return defense; }
	public int getMagic() { return magic; }
	public int getTeam() { return team; }
	public int getTeamNumber() { return teamNumber; }
	public ArrayList<Item> getItems() { return items; }
	public MonsterState getState() { return state; }
	
	public void setState (MonsterState s) { state = s; }
	public void addItem (Item i) { items.add(i); state.refreshState(); }
	
	public Monster(String name, int g, int h, int a, int d, int m, int t, int n) {
		this.name = name;
		graphic = g;
		maxHealth = h;
		attack = a;
		defense = d;
		magic = m;
		team = t;
		teamNumber = n;
		items = new ArrayList<Item>();
		state = new MonsterState();
	}
	
	// calculated values
	public int getBaseSpellPoints() { 
		return magic / 2;
	}
	
	public int getBaseSpeed() {
		return Math.max(0, 4 - getEncumberance()) + getTotalItemEffect(ItemEffectType.HASTE);
	}
	
	public int getBaseBlessed() {
		return 5 + getTotalItemEffect(ItemEffectType.BLESS);
	}
	
	public int getMissChance() { 
		if(attack == 0) return 100;
		return Math.min(75, 500 / attack); 
	}
	
	public int getCritChance() {
		return attack;
	}
	
	public DamageBonus getMeleeDamage() {
		int damage = getTotalItemEffect(ItemEffectType.MELEE_DAMAGE);
		damage = (int)Math.floor(damage * getMeleeMultiplier());
		int bonus = getTotalItemEffect(ItemEffectType.MELEE_BONUS);
		bonus = (int)Math.floor(bonus * getMeleeMultiplier());
		return new DamageBonus(damage, bonus);
	}
	
	public double getMeleeMultiplier() {
		return ((100 + attack)/100.0)  * ((getState().getBlessed())/5.0);
	}
	
	public double getSpellMultiplier() {
		return (2 * magic)/100.0;
	}
	
	public int getArmor() {
		int armor = getTotalItemEffect(ItemEffectType.ARMOR);
		int defenseContrib = Math.max(0, (defense - 50)/2);
		return Math.min(armor + defenseContrib, 100);
	}
	
	public int getFireResist() {
		int fireResist = getTotalItemEffect(ItemEffectType.RESIST_FIRE);
		int defenseContrib = Math.max(0, (defense - 50)/2);
		return Math.min(fireResist + defenseContrib, 100);
	}
	
	public int getMagicResist() { 
		int magicResist = getTotalItemEffect(ItemEffectType.RESIST_MAGIC);
		int defenseContrib = Math.max(0, (defense - 50)/2);
		return Math.min(magicResist + defenseContrib, 100);
	}
	
	public int getColdResist() {
		int coldResist = getTotalItemEffect(ItemEffectType.RESIST_COLD);
		int defenseContrib = Math.max(0, (defense - 50)/2);
		return Math.min(coldResist + defenseContrib, 100);
	}
	
	public int getEncumberance() {
		int weight = 0;
		for(Item item : items) {
			weight += item.getWeight() * item.getQuantity();
		}
		return Math.max(0, (weight/50) - (defense/25));
	}
	
	private int getTotalItemEffect(ItemEffectType type) {
		int total = 0;
		for(Item item : items) {
			if(item.isUsable()) continue;
			for(ItemEffect effect : item.getEffects()) {
				if(effect.getType() == type)
					total += effect.getAmount();
			}
		}
		return total;
	}
	
	public void deductHealth(int n) { 
		if(n <= 0) return;
		if(state.getCurHealth() == 0) {
			state.setDead(true);
			return;
		}
		state.setCurHealth(Math.max(0, state.getCurHealth() - n)); 
	}
}
