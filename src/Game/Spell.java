package Game;

import java.awt.Point;
import java.util.ArrayList;

import Game.Damage.DamageBonus;
import Game.Damage.DamageGraphic;
import Game.Damage.DamageType;
import Game.Effect.EffectType;
import Game.Effect.SingleEffect;


public enum Spell {
	FLAME (SpellType.MAGE, "Flame", 10, 2, 7, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleDamageEffects(caster, p, this);
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	BLAST (SpellType.MAGE, "Blast", 20, 3, 7, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleDamageEffects(caster, p, this);
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	POISON (SpellType.MAGE, "Poison", 20, 4, 7, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleEffect(p, new SingleEffect(EffectType.POISON, 3));
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	WALL_OF_FORCE (SpellType.MAGE, "Wall of Force", 30, 5, 6, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			ArrayList<Effect> effects = new ArrayList<Effect>();
			GameMap map = Game.inst().getMap();
			for(Point point : wallPoints(p)) {
				if(!map.pointInBounds(point) || map.terrainAt(point).isSolid())
					continue;
				
				Effect effect = new Effect(point);
				int damage = 0;
				if(map.monsterAt(point) != null) {
					Monster target = map.monsterAt(point);
					damage = Damage.calculateFieldDamage(target, Field.FORCE);
				}
				effect.addSingleEffect(new SingleEffect(EffectType.CREATE_FIELD, damage, Field.FORCE));
				effects.add(effect);
			}
			return effects;
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return wallPoints(target);
		}
	},
	
	CONFLAGRATION (SpellType.MAGE, "Conflagration", 30, 5, 6, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			ArrayList<Effect> effects = new ArrayList<Effect>();
			GameMap map = Game.inst().getMap();
			for(Point point : radiusPoints(p, 2)) {
				if(!map.pointInBounds(point) || map.terrainAt(point).isSolid())
					continue;
				
				Effect effect = new Effect(point);
				int damage = 0;
				if(map.monsterAt(point) != null) {
					Monster target = map.monsterAt(point);
					damage = Damage.calculateFieldDamage(target, Field.FIRE);
				}
				effect.addSingleEffect(new SingleEffect(EffectType.CREATE_FIELD, damage, Field.FIRE));
				effects.add(effect);
			}
			return effects;
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return radiusPoints(target, 2);
		}
	},
	
	ICE_BOLT (SpellType.MAGE, "Ice Bolt", 40, 6, 7, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleDamageEffects(caster, p, this);
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	WOUND (SpellType.MAGE, "Wound", 40, 4, 7, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleDamageEffects(caster, p, this);
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	FIREBALL (SpellType.MAGE, "Fireball", 60, 10, 7, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			ArrayList<Effect> effects = new ArrayList<Effect>();
			for(Point point : radiusPoints(p, 1)) {
				if(Game.inst().getMap().monsterAt(point) != null) {
					Monster target = Game.inst().getMap().monsterAt(point);
					Effect effect = new Effect(point);
					int damage = Damage.calculateSpellDamage(caster, target, this);
					effect.addSingleEffect(new SingleEffect(EffectType.DAMAGE, new Damage(Spell.getDamageGraphic(this), damage)));
					effects.add(effect);
				}
			}
			return effects;
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return radiusPoints(target, 1);
		}
	},
	
	KILL (SpellType.MAGE, "Kill", 80, 15, 7, true) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleDamageEffects(caster, p, this);
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	HEAL (SpellType.PRIEST, "Heal", 10, 2, 8, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleEffect(p, new SingleEffect(EffectType.HEAL, 15));
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	MINOR_HASTE (SpellType.PRIEST, "Minor Haste", 20, 3, 8, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleEffect(p, new SingleEffect(EffectType.HASTE, 4));
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	MINOR_BLESS (SpellType.PRIEST, "Minor Bless", 20, 3, 8, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleEffect(p, new SingleEffect(EffectType.BLESS, 2));
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	CURE_POISON (SpellType.PRIEST, "Cure Poison", 20, 3, 8, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleEffect(p, new SingleEffect(EffectType.CURE_POISON, 2));
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	LIGHT_HEAL_ALL (SpellType.PRIEST, "Light Heal All", 30, 6, 0, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			ArrayList<Effect> effects = new ArrayList<Effect>();
			Effect effect = new Effect(p);
			effect.addSingleEffect(new SingleEffect(EffectType.HEAL_ALL, 10));
			effects.add(effect);
			return effects;
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return null;
		}
	},
	
	MAJOR_HASTE (SpellType.PRIEST, "Major Haste", 40, 5, 8, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleEffect(p, new SingleEffect(EffectType.HASTE, 10));
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	MAJOR_BLESS (SpellType.PRIEST, "Major Bless", 40, 5, 8, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			return simpleEffect(p, new SingleEffect(EffectType.BLESS, 6));
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	CLEANSE (SpellType.PRIEST, "Cleanse", 70, 10, 8, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			ArrayList<Effect> effects = new ArrayList<Effect>();
			if(Game.inst().getMap().monsterAt(p) != null) {
				Monster target = Game.inst().getMap().monsterAt(p);
				Effect effect = new Effect(p);
				effect.addSingleEffect(new SingleEffect(EffectType.HEAL, target.getMaxHealth()));
				effect.addSingleEffect(new SingleEffect(EffectType.CURE_POISON, 10));
				effects.add(effect);
			}
			return effects;
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	},
	
	BATTLE_BOOST (SpellType.PRIEST, "Battle Boost", 70, 10, 8, false) {
		public ArrayList<Effect> getEffects (Monster caster, Point p) {
			ArrayList<Effect> effects = new ArrayList<Effect>();
			if(Game.inst().getMap().monsterAt(p) != null) {
				Effect effect = new Effect(p);
				effect.addSingleEffect(new SingleEffect(EffectType.HASTE, 8));
				effect.addSingleEffect(new SingleEffect(EffectType.BLESS, 5));
				effects.add(effect);
			}
			return effects;
		}
		
		public ArrayList<Point> getEffectPoints (Point target) {
			return simplePoint(target);
		}
	};
	
	private static ArrayList<Effect> simpleDamageEffects (Monster caster, Point p, Spell spell) {
		ArrayList<Effect> effects = new ArrayList<Effect>();
		if(Game.inst().getMap().monsterAt(p) != null) {
			Monster target = Game.inst().getMap().monsterAt(p);
			Effect effect = new Effect(p);
			int damage = Damage.calculateSpellDamage(caster, target, spell);
			effect.addSingleEffect(new SingleEffect(EffectType.DAMAGE, new Damage(Spell.getDamageGraphic(spell), damage)));
			effects.add(effect);
		}
		return effects;
	}
	
	public static ArrayList<Effect> simpleEffect (Point p, SingleEffect e) {
		ArrayList<Effect> effects = new ArrayList<Effect>();
		if(Game.inst().getMap().monsterAt(p) != null) {
			Effect effect = new Effect(p);
			effect.addSingleEffect(e);
			effects.add(effect);
		}
		return effects;
	}
	
	private static ArrayList<Point> simplePoint (Point p) {
		ArrayList<Point> ret = new ArrayList<Point>();
		ret.add(p);
		return ret;
	}
	
	public enum SpellType { MAGE, PRIEST };
	
	private SpellType type;
	private String name;
	private int level;
	private int spellPoints;
	private int range;
	private boolean needVision;
	
	private Spell (SpellType t, String n, int l, int s, int r, boolean need) {
		type = t;
		name = n;
		level = l;
		spellPoints = s;
		range = r;
		needVision = need;
	}
	
	public SpellType getSpellType() { return type; }
	public String getName() { return name; }
	public int getLevel() { return level; }
	public int getSpellPoints() { return spellPoints; }
	public int getRange() { return range; }
	public boolean needsVision() { return needVision; }
	
	public abstract ArrayList<Effect> getEffects (Monster caster, Point p);
	public abstract ArrayList<Point> getEffectPoints (Point target);
	
	public static ArrayList<Point> radiusPoints(Point p, int radius) {
		ArrayList<Point> points = new ArrayList<Point>();
		if(radius >= 0) {
			points.add(new Point(p.x, p.y));
		}
		if(radius >= 1) {
			points.add(new Point(p.x, p.y - 1));
			points.add(new Point(p.x + 1, p.y - 1));
			points.add(new Point(p.x + 1, p.y));
			points.add(new Point(p.x + 1, p.y + 1));
			points.add(new Point(p.x, p.y + 1));
			points.add(new Point(p.x - 1, p.y + 1));
			points.add(new Point(p.x - 1, p.y));
			points.add(new Point(p.x - 1, p.y - 1));
		}
		if(radius >= 2) {
			points.add(new Point(p.x - 1, p.y - 2));
			points.add(new Point(p.x, p.y - 2));
			points.add(new Point(p.x + 1, p.y - 2));
			points.add(new Point(p.x + 2, p.y - 1));
			points.add(new Point(p.x + 2, p.y));
			points.add(new Point(p.x + 2, p.y + 1));
			points.add(new Point(p.x - 1, p.y + 2));
			points.add(new Point(p.x, p.y + 2));
			points.add(new Point(p.x + 1, p.y + 2));
			points.add(new Point(p.x - 2, p.y - 1));
			points.add(new Point(p.x - 2, p.y));
			points.add(new Point(p.x - 2, p.y + 1));
		}
		return points;
	}
	
	public static ArrayList<Point> wallPoints (Point p) {
		ArrayList<Point> points = new ArrayList<Point>();
		for(int x = p.x - 4; x <= p.x + 4; x++) {
			points.add(new Point(x, p.y - 1));
			points.add(new Point(x, p.y));
		}
		return points;
	}
	
	public static DamageBonus getBaseDamage(Spell spell) {
		switch(spell) {
		case FLAME: return new DamageBonus(8, 5);
		case BLAST: return new DamageBonus(12, 8);
		case ICE_BOLT: return new DamageBonus(15, 15);
		case WOUND: return new DamageBonus(12, 6);
		case FIREBALL: return new DamageBonus(16, 9);
		case KILL: return new DamageBonus(40, 35);
		}
		return null;
	}
	
	public static DamageType getDamageType(Spell spell) {
		switch(spell) {
		case FLAME: return DamageType.FIRE;
		case BLAST: return DamageType.MAGIC;
		case ICE_BOLT: return DamageType.ICE;
		case WOUND: return DamageType.TYPELESS;
		case FIREBALL: return DamageType.FIRE;
		case KILL: return DamageType.MAGIC;
		}
		return null;
	}
	
	public static DamageGraphic getDamageGraphic(Spell spell) {
		switch(spell) {
		case FLAME: return DamageGraphic.ANIM_FIRE;
		case BLAST: return DamageGraphic.ANIM_MAGIC1;
		case ICE_BOLT: return DamageGraphic.COLD;
		case WOUND: return DamageGraphic.ANIM_MAGIC2;
		case FIREBALL: return DamageGraphic.ANIM_FIRE;
		case KILL: return DamageGraphic.ANIM_MAGIC2;
		}
		return null;
	}
}
