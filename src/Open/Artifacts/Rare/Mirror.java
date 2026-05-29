package Open.Artifacts.Rare;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class Mirror extends Artifact {
	public Mirror(GameObject gameObj) {
		super(gameObj);
		this.name = "Mirror";
		this.icon = Assets.RareMirrorIcon;
		desc = "+10% chance to reflect damage when hit\nIncreases invincibility frames\n(Linear scaling per stack)";
		setRarity(ArtifactRarity.RARE);
	}

	@Override
	public double getReflectionChance() {
		// Linear: Base amount + (multiplier * count)
		// 10% chance to reflect damage per stack
		return 0.10 * count;
	}

	@Override
	public double getBonusInvinsibilityFrames() {
		// Add some invincibility frames per stack (e.g., 10 frames per stack)
		return 10 * count;
	}
}
