package Open.Artifacts.Legendary;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class Anvil extends Artifact {
	public Anvil(GameObject gameObj) {
		super(gameObj);
		this.name = "Anvil";
		this.icon = Assets.LegendaryAnvilIcon;
		desc = "+1 stat that gets leveled up when choosing a weapon\n(Max 3 different stats, then multiplies stat bonuses)";
		setRarity(ArtifactRarity.LEGENDARY);
	}

	@Override
	public int getStatBonusCount() {
		// Linear: each stack provides 1 additional stat selection slot
		// Max of 3 different stats per stack (then multiplies)
		return count;
	}
}
