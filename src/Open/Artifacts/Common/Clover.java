package Open.Artifacts.Common;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class Clover extends Artifact { // still not implemented
    public Clover(GameObject gameObj) {
        super(gameObj);
        this.name = "Clover";
        this.icon = Assets.CommonCloverIcon;
        desc = "Increases luck by 10%";
        setRarity(ArtifactRarity.COMMON);
    }

    @Override
    public double getPercentLuck() {
        return 0.10 * count;
    }
}