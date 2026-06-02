package Open.Artifacts.Rare;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class DemonSoul extends Artifact {
	private int killCount = 0;
	private final int maxDamagePerStack = 100; // 100% cap per stack

	public DemonSoul(GameObject gameObj) {
		super(gameObj);
		this.name = "Demon Soul";
		this.icon = Assets.RareDemonSoulIcon;
		desc = "+1% damage when killing an enemy\n(Max +100% per stack)";
		setRarity(ArtifactRarity.RARE);
	}

	/**
	 * Called when the player kills an enemy
	 */
	public void onEnemyKilled() {
		killCount++;
	}

	@Override
	public double getPercentDamage() {
		// Each kill gives 1% damage, but capped at 100% per stack
		double damagePerKill = 0.01;
		double totalDamage = killCount * damagePerKill;
		
		// Cap at maxDamagePerStack * count
		double cap = maxDamagePerStack * 0.01 * count;
		
		// If we're treating it as linear stacking on TOP of the base
		// then each stack adds its own cap
		return Math.min(totalDamage * count, cap);
	}
}
