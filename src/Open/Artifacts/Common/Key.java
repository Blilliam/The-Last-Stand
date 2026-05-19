package Open.Artifacts.Common;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;

public class Key extends Artifact { // still no implemented
    public Key(GameObject gameObj) {
        super(gameObj);
        this.name = "Key";
        this.icon = Assets.CommonKeyIcon;
        desc = "Gives 10% chance to make interactibles free";
    }

    @Override
    public double getPercentFreeChest() {
        // multiplier = 0.10
        return 1.0 - (1.0 / (1.0 + (0.10 * count)));
    }
}