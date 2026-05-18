package Open.Artifacts.Common;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;

public class Clover extends Artifact { // still not implemented
    public Clover(GameObject gameObj) {
        super(gameObj);
        this.name = "Clover";
        this.icon = Assets.CommonCloverIcon;
        desc = "Increases luck by 15%";
    }

    @Override
    public double getPercentLuck() {
        return 0.10 * count;
    }
}