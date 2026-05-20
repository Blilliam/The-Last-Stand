package Open.Artifacts.Common;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;

public class Watch extends Artifact { // done
    public Watch(GameObject gameObj) {
        super(gameObj);
        this.name = "Watch";
        this.icon = Assets.CommonWatchIcon;
        desc = "Increase exp gain by 15%";
    }
    @Override
    public double getPercentBonusExp() {
        return 0.15 * count;
    }
}