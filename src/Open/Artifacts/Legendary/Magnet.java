package Open.Artifacts.Legendary;

import java.util.Timer;

import Open.Artifacts.Artifact;
import Open.Entities.Exp;
import main.Assets;
import main.GameObject;
import main.enums.ArtifactRarity;

public class Magnet extends Artifact {
	private int tickCounter;
	private final int baseCooldown = 1200; // 20 seconds at 60fps

	public Magnet(GameObject gameobj) { // done
		super(gameobj);
		this.name = "Magnet";
		this.icon = Assets.LegendaryMagenetIcon;
		tickCounter = 0;
		double baseSec = baseCooldown / 60.0;
		desc = "Every " + baseSec + " seconds, absorb all exp on the ground\n(Cooldown decreases with each stack - hyperbolic)";
		setRarity(ArtifactRarity.LEGENDARY);
	}

	@Override
	public double getMagnetCooldownMultiplier() {
		// Hyperbolic: 1 - (1/(1+(multiplier*count))
		// This gives the reduction factor
		return 1.0 - (1.0 / (1.0 + (0.10 * this.count)));
	}

	@Override
	public void update() {
		tickCounter++;
		
		// Calculate actual cooldown with hyperbolic reduction
		double reductionFactor = getMagnetCooldownMultiplier();
		int effectiveCooldown = (int) (baseCooldown * (1.0 - reductionFactor));
		
		if (tickCounter >= effectiveCooldown) {
			tickCounter = 0;
			for (Exp e : gameObj.getExp()) {
				e.setCollected(true);
			}
		}
		
	}

}
