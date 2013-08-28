package Game;

import java.io.Serializable;


@SuppressWarnings("serial")
public class ItemEffect implements Serializable {
	public enum ItemEffectType {
		MELEE_DAMAGE, MELEE_BONUS, POISON, CURSE, SLOW, // weapons, missiles
		HEAL, ENERGY, CURE_POISON, // potions
		BLESS, HASTE, // potions, items
		ARMOR, RESIST_FIRE, RESIST_MAGIC, RESIST_COLD // items
	}

	private ItemEffectType type;
	private int amount;

	public ItemEffect(ItemEffectType t, int a) {
		type = t;
		amount = a;
	}

	public ItemEffectType getType() { return type; }
	public int getAmount() { return amount; }
}
