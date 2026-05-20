package Open.Artifacts.Common;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;

public class Battery extends Artifact {
    public Battery(GameObject gameObj) { //Implemented
        super(gameObj);
        this.name = "Battery";
        this.icon = Assets.CommonBatteryIcon;
        desc = "Increase attack speed by 15%";
    }

    @Override
    public double getPercentAttackSpeed() {
        return 0.15 * count;
    }
}