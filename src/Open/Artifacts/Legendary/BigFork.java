package Open.Artifacts.Legendary;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class BigFork extends Artifact {
	public BigFork(GameObject gameObj) {
		super(gameObj);
		this.name = "Big Fork";
		this.icon = Assets.LegendaryBigForkIcon;
		desc = "+15% chance to ThickCrit (deals 4x damage)\n(Linear scaling per stack)";
		setRarity(ArtifactRarity.LEGENDARY);
	}

	@Override
	public double getThickCritChance() {
		// Linear: Base amount + (multiplier * count)
		// 15% per stack
		return 0.15 * count;
	}
}
