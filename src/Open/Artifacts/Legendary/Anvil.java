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
		desc = "Multiplies stat that gets leveled up when choosing a weapon";
		setRarity(ArtifactRarity.LEGENDARY);
	}
}
