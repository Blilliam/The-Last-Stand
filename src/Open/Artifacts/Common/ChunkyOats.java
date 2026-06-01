package Open.Artifacts.Common;

import java.awt.Graphics2D;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class ChunkyOats extends Artifact {

	public ChunkyOats(GameObject gameobj) { // implemented
		super(gameobj);
		this.name = "Chunky Oats";
		this.icon = Assets.CommonOatsIcon;
		desc = "Increase health by 25";
		setRarity(ArtifactRarity.COMMON);
	}

	@Override
	public int getFlatHealth() {
		return 25 * count;
	}

}
