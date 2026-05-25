package Open.Artifacts;

import java.util.Timer;

import Open.Entities.Exp;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class Magnet extends Artifact {
	int cooldown;
	int count;

	public Magnet(GameObject gameobj) { // done
		super(gameobj);
		this.name = "Watch";
		this.icon = Assets.LegendaryMagenetIcon;
		cooldown = 200;
		count = 0;
		double sec = cooldown/60;
		desc = "Every " + sec + " seconds, absorb all exp on the ground";
		setRarity(ArtifactRarity.LEGENDARY);
	}

	@Override
	public void update() {
		count++;
		if (count >= cooldown) {
			count = 0;
			for (Exp e : gameObj.getExp()) {
				e.setCollected(true);
			}

		}
	}

}
