package Open.Artifacts.Uncommon;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class Beer extends Artifact { // done
    public Beer(GameObject gameObj) {
        super(gameObj);
        this.name = "Beer";
        this.icon = Assets.UncommonBeerIcon;
        desc = "Decreases health by 10% but damage by 20%";
        setRarity(ArtifactRarity.UNCOMMON);
    }

    @Override
    public double getPercentHealth() {
    	return -(1.0 - (1.0 / (1.0 + (0.10 * count))));
    }
    @Override
    public double getPercentDamage() {
    	return 0.2 * count;
    }
}