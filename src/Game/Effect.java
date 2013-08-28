package Game;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;


@SuppressWarnings("serial")
public class Effect implements Serializable {
	public enum EffectType {
		DAMAGE, CREATE_FIELD, POISON, CURSE, SLOW, HEAL, HEAL_ALL, ENERGY, CURE_POISON, BLESS, HASTE
	}
	
	public static class SingleEffect implements Serializable {
		private EffectType type;
		private int amount;
		private Object extra;
		
		public SingleEffect(EffectType t, int a) {
			type = t;
			amount = a;
			extra = null;
		}
		
		public SingleEffect(EffectType t, int a, Object o) {
			type = t;
			amount = a;
			extra = o;
		}
		
		public SingleEffect(EffectType t, Object o) {
			type = t;
			amount = 0;
			extra = o;
		}
		
		public EffectType getType() { return type; }
		public int getAmount() { return amount; }
		public Object getExtra() { return extra; }
	}
	
	private Point point;
	private ArrayList<SingleEffect> effects;
	
	private Damage damage;
	private ArrayList<String> statusLines;
	
	public Effect (Point p) {
		point = p;
		effects = new ArrayList<SingleEffect>();
		damage = null;
		statusLines = null;
	}
	
	public Point getPoint() { return point; }
	public ArrayList<SingleEffect> getEffects() { return effects; }
	public Damage getDamage() { return damage; }
	public ArrayList<String> getStatusLines() { return statusLines; }
	
	public void addSingleEffect(SingleEffect s) { effects.add(s); }
	
	public void apply () {
		Game game = Game.inst();
		Monster monster = game.getMap().monsterAt(point);
		
		damage = null;
		statusLines = new ArrayList<String>();
		for(SingleEffect effect : effects) {
			switch(effect.getType()) {
			case DAMAGE: 
				damage = (Damage)effect.getExtra();
				monster.deductHealth(damage.getAmount());
				statusLines.add(monster.getName() + (damage.getAmount() > 0 ? " takes " + damage.getAmount() + "." : " undamaged."));
				break;
				
			case CREATE_FIELD: 
				Field field = (Field)effect.getExtra();
				game.getMap().setField(point, field);
				if(monster != null) {
					int amount = effect.getAmount();
					damage = new Damage(field.getDamageGraphic(), amount);
					monster.deductHealth(amount);
					statusLines.add(monster.getName() + (amount > 0 ? " takes " + amount + "." : " undamaged."));
				}
				break;
			
			case POISON: 
				monster.getState().changePoison(effect.getAmount()); 
				statusLines.add(monster.getName() + " poisoned " + effect.getAmount() + ".");
				break;
				
			case CURSE: 
				monster.getState().changeBlessed(-effect.getAmount()); 
				statusLines.add(monster.getName() + " cursed " + effect.getAmount() + ".");
				break;
				
			case SLOW: 
				monster.getState().changeSpeed(-effect.getAmount()); 
				statusLines.add(monster.getName() + " slowed " + effect.getAmount() + ".");
				break;
				
			case HEAL: 
				monster.getState().changeCurHealth(effect.getAmount()); 
				statusLines.add(monster.getName() + " healed " + effect.getAmount() + ".");
				break;
				
			case HEAL_ALL: 
				Player player = game.getActivePlayer();
				for (Monster m : player.getMonsters()) {
					m.getState().changeCurHealth(effect.getAmount()); 
				}
				statusLines.add("Party healed " + effect.getAmount() + ".");
				break;
				
			case ENERGY: 
				monster.getState().changeSpellPoints(effect.getAmount()); 
				statusLines.add(monster.getName() + " energized " + effect.getAmount() + ".");
				break;
				
			case CURE_POISON: 
				monster.getState().changePoison(-effect.getAmount()); 
				statusLines.add(monster.getName() + " cured " + effect.getAmount() + ".");
				break;
				
			case BLESS: 
				monster.getState().changeBlessed(effect.getAmount()); 
				statusLines.add(monster.getName() + " blessed " + effect.getAmount() + ".");
				break;
				
			case HASTE: 
				monster.getState().changeSpeed(effect.getAmount()); 
				statusLines.add(monster.getName() + " hasted " + effect.getAmount() + ".");
				break;
			}
		}
	}
}
