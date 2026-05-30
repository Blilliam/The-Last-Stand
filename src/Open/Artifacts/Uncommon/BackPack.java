package Open.Artifacts.Uncommon;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class BackPack extends Artifact {
    public BackPack(GameObject gameObj) { // done
        super(gameObj);
        this.name = "BackPack";
        this.icon = Assets.UncommonBackPackIcon;
        desc = "Increases projectile count for all weapons by 1";
        setRarity(ArtifactRarity.UNCOMMON);
    }

    @Override
    public int getBonusProjectiles() {
        return 1 * count;
    }
}