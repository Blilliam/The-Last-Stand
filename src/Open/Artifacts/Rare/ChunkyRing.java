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
		desc = "Every 100 health give +20% damage\n(Linear scaling per stack)";
		setRarity(ArtifactRarity.RARE);
	}

	@Override
	public double getHealthScalingBonus() {
		// Linear: Base amount + (multiplier * count)
		// For every 100 health, give +20% damage
		double playerHealth = gameObj.getPlayer().getCurrHp();
		double damagePercentPerHealth = 0.20 / 100.0; // 0.002 per HP
		return (playerHealth * damagePercentPerHealth) * count;
	}
}
