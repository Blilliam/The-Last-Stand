package Open.Artifacts.Uncommon;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class BloodyDagger extends Artifact {
    public BloodyDagger(GameObject gameObj) {
        super(gameObj);
        this.name = "Bloody Dagger";
        this.icon = Assets.UncommonDemonicBladeIcon;
        desc = "+25% chance to heal 1 HP on critical hit per stack\n(Above 100%: guaranteed heal + extra chance for +1 more)";
        setRarity(ArtifactRarity.UNCOMMON);
    }

    // Called by ArtifactManager.onCritEffect() after a crit lands.
    // Linear: 0.25 * count chance to heal 1 HP.
    // If chance >= 1.0, guaranteed heal + remainder as bonus chance for a 2nd HP.
    @Override
    public void onCritEffect() {
        double totalChance = 0.25 * count;
        int guaranteedHeals = (int) totalChance;           // floor: how many guaranteed
        double partialChance = totalChance - guaranteedHeals; // remainder

        double currentHp  = gameObj.getPlayer().getCurrHp();
        double maxHp      = gameObj.getPlayer().getMaxHp();

        // Apply guaranteed heals
        double heal = guaranteedHeals;

        // Roll for the partial chance
        if (Math.random() < partialChance) {
            heal += 1;
        }

        if (heal > 0) {
            gameObj.getPlayer().setCurrHp((int) Math.min(maxHp, currentHp + heal));
        }
    }
}