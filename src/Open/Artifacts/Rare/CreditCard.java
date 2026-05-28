package Open.Artifacts.Rare;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class CreditCard extends Artifact {
	private int chestsOpened = 0;

	public CreditCard(GameObject gameObj) {
		super(gameObj);
		this.name = "Credit Card";
		this.icon = Assets.RareCreditCardIcon;
		desc = "Earn 2% luck when you open a chest\n(Linear scaling per stack)";
		setRarity(ArtifactRarity.RARE);
	}

	/**
	 * Called when the player opens a chest. Adds to the luck bonus.
	 */
	public void onChestOpened() {
		chestsOpened++;
	}

	@Override
	public double getPercentLuck() {
		// Linear: Base amount + (multiplier * count)
		// 2% luck per chest opened per stack
		return (0.02 * chestsOpened) * count;
	}
}
