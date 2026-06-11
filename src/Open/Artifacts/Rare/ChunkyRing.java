package Open.Artifacts.Rare;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class ChunkyRing extends Artifact {
	public ChunkyRing(GameObject gameObj) {
		super(gameObj);
		this.name = "Chunky Ring";
		this.icon = Assets.RareChunkyRingIcon;
		setDesc("Every 1 max health give +0.2% damage\n(Linear scaling per stack)");
		setRarity(ArtifactRarity.RARE);
	}

	@Override
	public double getHealthScalingBonus() {
		// Linear: Base amount + (multiplier * count)
		// For every 100 health, give +20% damage
		double playerHealth = gameObj.getPlayer().getMaxHp();
		double damagePercentPerHealth = 0.20 / 100.0; // 0.002 per HP
		return (playerHealth * damagePercentPerHealth) * getCount();
	}
}
