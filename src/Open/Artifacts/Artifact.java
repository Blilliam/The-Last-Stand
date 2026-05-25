package Open.Artifacts;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Open.Entities.Entity;
import main.GameObject;
import main.enums.ArtifactRarity;

public abstract class Artifact {
	protected int count;
	protected String name;
	protected BufferedImage icon;
	protected GameObject gameObj;
	protected String desc;
	private ArtifactRarity rarity;

	public Artifact(GameObject gameObj) {
		count = 1;
		this.gameObj = gameObj;
	}
	public ArtifactRarity getRarity() {
		return rarity;
	}
	
	public String getDescription() {
		return desc;
	}
	public String getName() {
		return name;
	}
	
	public BufferedImage getIcon() {
		return icon;
	}

	public void addCount() {
		count++;
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

	public void setRarity(ArtifactRarity rarity) {
		this.rarity = rarity;
	}

}
