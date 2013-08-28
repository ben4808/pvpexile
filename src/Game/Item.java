package Game;

import java.io.Serializable;
import java.util.ArrayList;

import Game.Damage.DamageBonus;
import Game.Damage.DamageGraphic;
import Game.Effect.EffectType;
import Game.Effect.SingleEffect;
import Game.ItemEffect.ItemEffectType;


@SuppressWarnings("serial")
public class Item implements Serializable {
	public enum ItemType {
		ONE_HAND_WEAPON, TWO_HAND_WEAPON, PLATE, HELM, SHIELD,
		AMULET, POTION, MISSILE, NO_TYPE
	}
	
	private String name;
	private int id;
	private int graphic;
	private ItemType type;
	private int weight;
	private int quantity;
	private ArrayList<ItemEffect> effects;
	
	public Item(String n, int i, int g, ItemType t, int w, int q, ArrayList<ItemEffect> e) {
		name = n;
		id = i;
		graphic = g;
		type = t;
		weight = w;
		quantity = q;
		effects = e;
	}
	
	public Item (Item other) {
		this(other.getName(), other.getId(), other.getGraphic(), other.getType(), other.getWeight(), other.getQuantity(),
				other.getEffects());
	}
	
	public String getName() { return name; }
	public int getId() { return id; }
	public int getGraphic() { return graphic; }
	public ItemType getType() { return type; }
	public int getWeight() { return weight; }
	public int getTotalWeight() { return weight * quantity; }
	public int getQuantity() { return quantity;}
	public ArrayList<ItemEffect> getEffects() { return effects; }
	
	public boolean isUsable() { return type == ItemType.POTION || type == ItemType.MISSILE; }
	
	public void decrementQuantity() { quantity = Math.max(0, quantity - 1); }
	public void setEffects(ArrayList<ItemEffect> e) { effects = e; }
	
	public Effect effectOnMonster (Monster target) {
		Effect ret = new Effect(target.getState().getLocation());
		
		int cachedDamage = 0;
		for(ItemEffect effect : effects) {
			switch(effect.getType()) {
			case MELEE_DAMAGE:
				cachedDamage = effect.getAmount();
				break;
				
			case MELEE_BONUS:
				Monster active = Game.inst().getActiveMonster();
				int damage = Damage.calculateMissileDamage(active, target, new DamageBonus(cachedDamage, effect.getAmount()));
				ret.addSingleEffect(new SingleEffect(EffectType.DAMAGE, new Damage(DamageGraphic.MELEE, damage)));
				break;
				
			case POISON: ret.addSingleEffect(new SingleEffect(EffectType.POISON, effect.getAmount())); break;
			case CURSE: ret.addSingleEffect(new SingleEffect(EffectType.CURSE, effect.getAmount())); break;
			case SLOW: ret.addSingleEffect(new SingleEffect(EffectType.SLOW, effect.getAmount())); break;
			case HEAL: ret.addSingleEffect(new SingleEffect(EffectType.HEAL, effect.getAmount())); break;
			case ENERGY: ret.addSingleEffect(new SingleEffect(EffectType.ENERGY, effect.getAmount())); break;
			case CURE_POISON: ret.addSingleEffect(new SingleEffect(EffectType.CURE_POISON, effect.getAmount())); break;
			case BLESS: ret.addSingleEffect(new SingleEffect(EffectType.BLESS, effect.getAmount())); break;
			case HASTE: ret.addSingleEffect(new SingleEffect(EffectType.HASTE, effect.getAmount())); break;
			}
		}
		
		return ret;
	}
	
	public String getAbilityString() {
		String ret = "";
		int i = 0;
		for(ItemEffect effect : effects) {
			if(i > 0 && effect.getType() != ItemEffectType.MELEE_BONUS) ret += ", ";
			
			switch(effect.getType()) {
			case MELEE_DAMAGE: ret += "Dmg " + Integer.toString(effect.getAmount()); break;
			case MELEE_BONUS: ret += "+" + Integer.toString(effect.getAmount()); break;
			case POISON: ret += "Poison " + Integer.toString(effect.getAmount()); break;
			case CURSE: ret += "Curse " + Integer.toString(effect.getAmount()); break;
			case SLOW: ret += "Slow " + Integer.toString(effect.getAmount()); break;
			case HEAL: ret += "Heal " + Integer.toString(effect.getAmount()); break;
			case ENERGY: ret += "Energy +" + Integer.toString(effect.getAmount()); break;
			case CURE_POISON: ret += "Cure " + Integer.toString(effect.getAmount()); break;
			case BLESS: ret += "Bless " + Integer.toString(effect.getAmount()); break;
			case HASTE: ret += "Haste " + Integer.toString(effect.getAmount()); break;
			case ARMOR: ret += "Armor " + Integer.toString(effect.getAmount()); break;
			case RESIST_FIRE: ret += "Res. Fire " + Integer.toString(effect.getAmount()); break;
			case RESIST_MAGIC: ret += "Res. Mag " + Integer.toString(effect.getAmount()); break;
			case RESIST_COLD: ret += "Res. Cold " + Integer.toString(effect.getAmount()); break;
			}
			
			i++;
		}
		
		return ret;
	}
}
