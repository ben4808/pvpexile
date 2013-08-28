package Game;

import java.io.Serializable;
import java.util.Random;


@SuppressWarnings("serial")
public class Damage implements Serializable {
	public enum DamageGraphic {
		FIRE, MAGIC, POISON, MELEE, COLD, ANIM_FIRE, ANIM_MAGIC1, ANIM_MAGIC2;
		
		public boolean isAnimated() {
			return this == ANIM_FIRE || this == ANIM_MAGIC1 || this == ANIM_MAGIC2;
		}
	}
	
	public enum DamageType {
		MELEE, FIRE, ICE, MAGIC, POISON, TYPELESS;
	}
	
	public static class DamageBonus implements Serializable {
		public int damage;
		public int bonus;
		
		public DamageBonus(int d, int b) {
			damage = d;
			bonus = b;
		}
		
		public String toString() {
			return Integer.toString(damage) + "+" + Integer.toString(bonus);
		}
	}
	
	private DamageGraphic graphic;
	private int amount;
		
	public Damage(DamageGraphic g, int a) {
		graphic = g;
		amount = a;
	}
	
	public DamageGraphic getGraphic() { return graphic; }
	public int getAmount() { return amount; }
	
	public static int calculateMeleeDamage (Monster attacker, Monster target) {
		Random rand = new Random();
		DamageBonus damage = attacker.getMeleeDamage();
		double armorMultiplier = (100 - target.getArmor())/100.0;
		if(rand.nextInt(100) >= attacker.getCritChance()) {
			damage.damage *= 1.5;
			damage.bonus *= 2;
			return (int)Math.floor(rollDamage(damage) * armorMultiplier);
		}
		else if(rand.nextInt(100) >= attacker.getMissChance()) {
			return (int)Math.floor(rollDamage(damage) * armorMultiplier);
		}
		// missed
		return 0;
	}
	
	public static int calculateFieldDamage (Monster target, Field field) {
		DamageBonus damage = field.getDamage();
		
		switch(field) {
		case BLADES:
			return (int)Math.ceil(rollDamage(damage) * ((100 - target.getArmor())/100.0));
			
		case FIRE:
			return (int)Math.ceil(rollDamage(damage) * ((100 - target.getFireResist())/100.0));
			
		case ICE:
			return (int)Math.ceil(rollDamage(damage) * ((100 - target.getColdResist())/100.0));
			
		case FORCE:
			return (int)Math.ceil(rollDamage(damage) * ((100 - target.getMagicResist())/100.0));
		}
		
		return 0;
	}
	
	public static int calculatePoisonDamage (Monster target) {
		int maxDamage = target.getState().getPoison() * 5;
		return rollDamage(maxDamage / 2, maxDamage / 2);
	}
	
	public static int calculateMissileDamage (Monster thrower, Monster target, DamageBonus damage) {
		Random rand = new Random();
		if(rand.nextInt(100) > thrower.getMissChance())
			return (int)Math.floor(rollDamage(damage) * ((100 - target.getArmor())/100.0));
		return 0;
	}
	
	public static int calculateSpellDamage (Monster caster, Monster target, Spell spell) {
		DamageBonus damage = Spell.getBaseDamage(spell);
		damage.damage *= caster.getSpellMultiplier();
		damage.bonus *= caster.getSpellMultiplier();
		
		switch(Spell.getDamageType(spell)) {
		case FIRE:
			return (int)Math.ceil(rollDamage(damage) * ((100 - target.getFireResist())/100.0));
			
		case ICE:
			return (int)Math.ceil(rollDamage(damage) * ((100 - target.getColdResist())/100.0));
			
		case MAGIC:
			return (int)Math.ceil(rollDamage(damage) * ((100 - target.getMagicResist())/100.0));
			
		case TYPELESS:
			return (int)Math.ceil(rollDamage(damage));
		}
		
		return 0;
	}
	
	public static int rollDamage(int dmg, int bonus) { return rollDamage(new DamageBonus(dmg, bonus)); }
	
	public static int rollDamage (DamageBonus damage) {
		return new Random().nextInt(damage.damage+1) + damage.bonus;
	}
}
