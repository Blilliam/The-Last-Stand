package Open.Artifacts.Uncommon;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;

public class EchoShard extends Artifact {
    public EchoShard(GameObject gameObj) { // not done
        super(gameObj);
        this.name = "Echo Shard";
        this.icon = Assets.UncommonEchoShardIcon;
        desc = "Increases chance for enemeis to drop double exp by 15%";
    }

    @Override
    public double getBonusExpDropChance() {
        return 0.15 * count;
    }
}