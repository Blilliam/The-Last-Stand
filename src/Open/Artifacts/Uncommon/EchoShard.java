package Open.Artifacts.Uncommon;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class EchoShard extends Artifact {
    public EchoShard(GameObject gameObj) { // not done
        super(gameObj);
        this.name = "Experience Shard";
        this.icon = Assets.UncommonEchoShardIcon;
        desc = "Exp chance to drop extra duplicate +12%";
        setRarity(ArtifactRarity.UNCOMMON);
    }

    @Override
    public double getBonusExpDropChance() {
        return 0.12 * count;
    }
}