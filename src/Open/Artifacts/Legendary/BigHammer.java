package Open.Artifacts.Legendary;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class BigHammer extends Artifact {
	public BigHammer(GameObject gameObj) {
		super(gameObj);
		this.name = "Big Hammer";
		this.icon = Assets.LegendaryBigHammerIcon;
		desc = "+5% chance to \"Bonk\" an enemy when dealing damage\nDeals 20x damage\n(Linear scaling per stack)";
		setRarity(ArtifactRarity.LEGENDARY);
	}

	@Override
	public double getBonkChance() {
		// Linear: Base amount + (multiplier * count)
		// 5% per stack
		return 0.05 * count;
	}
}
