package Open.Artifacts.Uncommon;

import Open.Artifacts.Artifact;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class Kevin extends Artifact {
	// Kevin fires every 60 ticks (1 second), tracked locally
	private int tickCounter = 0;
	private boolean damageThisTick = false;

	public Kevin(GameObject gameObj) {
		super(gameObj);
		this.name = "Kevin";
		this.icon = Assets.UncommonKevinIcon; // reuse closest thematic icon
		desc = "Kevin doesn't like you.\n+25% chance per stack to hit you for 1 damage\nevery second you damage an enemy.\n(Above 100%: guaranteed 1 dmg + chance for extra)";
		setRarity(ArtifactRarity.UNCOMMON);
	}

	@Override
	public void onHitEffect() {
		double totalChance = 0.25 * count;
		int guaranteed = (int) totalChance;
		double partial = totalChance - guaranteed;

		int dmg = guaranteed;
		if (Math.random() < partial)
			dmg++;

		if (dmg > 0) {
			// Bypass invincibility frames — Kevin is relentless
			double current = gameObj.getPlayer().getCurrHp();
			gameObj.getPlayer().setCurrHp((int) Math.max(1, current - dmg));
			gameObj.getPlayer().getArtifactManager().onDamageTaken(dmg);
		}
	}
}