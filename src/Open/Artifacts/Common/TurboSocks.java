package Open.Artifacts.Common;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class TurboSocks extends Artifact { // still not implemented
    public TurboSocks(GameObject gameObj) {
        super(gameObj);
        this.name = "Turbo Socks";
        this.icon = Assets.CommonTurboSocksIcon;
        setDesc("Increase speed by 15%");
        setRarity(ArtifactRarity.COMMON);
    }

    // Custom method to be called by your gold logic
    @Override
    public double getBonusSpeed() {
        return 0.15 * getCount();
    }
}