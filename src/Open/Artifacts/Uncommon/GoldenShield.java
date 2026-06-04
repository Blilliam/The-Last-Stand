package Open.Artifacts.Uncommon;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class GoldenShield extends Artifact {
	public GoldenShield(GameObject gameObj) {
		super(gameObj);
		this.name = "Golden Shield";
		this.icon = Assets.UncommonGoldenShieldIcon;
		desc = "Taking damage gives you money.\nVery cool.";
		setRarity(ArtifactRarity.UNCOMMON);
	}

	@Override
	public void onDamageTaken(int damageAmount) {
		int goldGained = damageAmount * count;
		gameObj.getPlayer().addGold(goldGained);
	}
}
