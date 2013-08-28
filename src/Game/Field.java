package Game;

import Game.Damage.DamageBonus;
import Game.Damage.DamageGraphic;
import Game.Damage.DamageType;


public enum Field {
	FORCE ("Force Field", 8, DamageType.MAGIC, DamageGraphic.MAGIC, 7, 8, 12),
	FIRE ("Flame Field", 9, DamageType.FIRE, DamageGraphic.FIRE, 7, 5, 20),
	ICE ("Ice Field", 12, DamageType.ICE, DamageGraphic.COLD, 8, 8, 12),
	BLADES ("Blade Field", 13, DamageType.MELEE, DamageGraphic.MELEE, 10, 10, 8);
	
	private String name;
	private int graphic;
	private DamageType damageType;
	private DamageGraphic damageGraphic;
	DamageBonus damage;
	private int disappearChance;
	
	private Field (String n, int g, DamageType dt, DamageGraphic dg, int dmg, int b, int dc) {
		name = n;
		graphic = g;
		damageType = dt;
		damageGraphic = dg;
		damage = new DamageBonus(dmg, b);
		disappearChance = dc;
	}
	
	public String getName() { return name; }
	public int getGraphic() { return graphic; }
	public DamageType getDamageType() { return damageType; }
	public DamageGraphic getDamageGraphic() { return damageGraphic; }
	public DamageBonus getDamage() { return damage; }
	public int getDisappearChance() { return disappearChance; }
}
