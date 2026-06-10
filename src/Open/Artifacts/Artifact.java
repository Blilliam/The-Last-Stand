package Open.Artifacts;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Open.Entities.Entity;
import main.GameObject;
import main.enums.ArtifactRarity;

public abstract class Artifact {
	private int count;
	protected String name;
	protected BufferedImage icon;
	protected GameObject gameObj;
	private String desc;
	private ArtifactRarity rarity;

	public Artifact(GameObject gameObj) {
		setCount(1);
		this.gameObj = gameObj;
	}
	public ArtifactRarity getRarity() {
		return rarity;
	}
	
	public String getDescription() {
		return getDesc();
	}
	public String getName() {
		return name + " (" + rarity.toString() + ")";
	}
	
	public BufferedImage getIcon() {
		return icon;
	}

	public void addCount() {
		setCount(getCount() + 1);
	}

	public double getPercentDamage() {
		return 0;
	}

	public int getFlatDamage() {
		return 0;
	}

	public int getBonusProjectiles() {
		return 0;
	}

	public double getPercentHealth() {
		return 0;
	}

	public int getFlatHealth() {
		return 0;
	}

	public void onCritEffect() {
		return;
	}
	public void onHitEffect() {
		return;
	}

	public double getPercentAttackSpeed() {
		return 0;
	}

	public double getPercentLuck() {
		return 0;
	}

	public double getPercentFreeChest() {
		return 0;
	}

	public double getPercentBonusExp() {
		return 0;
	}

	public double getBonusInvinsibilityFrames() {
		return 0;
	}
	public double getBonusExpDropChance() {
		return 0;
	}
	
	public void update() {
		return;
	}

	public double getPercentBonusGold() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getChestLuckBonus() {
		return 0;
	}

	public double getReflectionChance() {
		return 0;
	}

	public double getDamagePerKillPercent() {
		return 0;
	}

	public double getThickCritChance() {
		return 0;
	}

	public double getBonkChance() {
		return 0;
	}

	public int getStatBonusCount() {
		return 0;
	}

	public double getMagnetCooldownMultiplier() {
		return 0;
	}

	public void onDamageTaken(int damageAmount) {
		return;
	}

	public void onKill(int goldReward) {
		return;
	}

	public double getHealthScalingBonus() {
		return 0;
	}

	public void setRarity(ArtifactRarity rarity) {
		this.rarity = rarity;
	}
	public double getBonusSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

}
