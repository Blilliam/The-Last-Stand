package Open.Entities.Enemies;

import main.AppPanel;
import main.GameObject;
import main.enums.Difficulty;

import java.util.ArrayList;
import java.util.List;

public class EnemyWaves {

	private GameObject gameObj;
	private double credits;
	private double creditGainRate;
	private int tickCounter;

	private class SpawnCard {
		int id;
		int cost;
		int weight;
		int maxGroup;

		SpawnCard(int id, int cost, int weight, int maxGroup) {
			this.id = id;
			this.cost = cost;
			this.weight = weight;
			this.maxGroup = maxGroup;
		}
	}

	private List<SpawnCard> pool = new ArrayList<>();

	public EnemyWaves(GameObject gameObj) {
		this.gameObj = gameObj;
		this.credits = 15; // Start with more credits for a faster early game
		this.creditGainRate = 0.30;
		creditGainRate *= gameObj.getDifficulty().ordinal() * 0.6; // Bonus credits based on difficulty

		this.tickCounter = 0;

		// 2. REBALANCED POOL
		// Lowered costs for Zombies/Skeletons and boosted their weights
		pool.add(new SpawnCard(1, 8, 150, 10)); // Zombie: Now as cheap as bats, higher weight
		pool.add(new SpawnCard(2, 10, 100, 6)); // Skeleton: Much cheaper
		pool.add(new SpawnCard(4, 8, 60, 12)); // Bat: Kept cost, but lowered weight so they appear less

		// Elite Roster
		pool.add(new SpawnCard(3, 40, 25, 3)); // Mudman: Cheaper and slightly more common
		pool.add(new SpawnCard(5, 60, 12, 1)); // Glowing Bat: More affordable for the director
	}

	public double getDifficultyMult() {
		return (1.0 + (tickCounter / 1800.0) * 0.1) * gameObj.getMap().getStage() * gameObj.getDifficulty().ordinal(); // Faster
																														// scaling
																														// with
																														// stage
																														// and
		// difficulty
	}

	public void update() {
		if (gameObj.getState() != gameObj.getStateOpen())
			return;

		tickCounter++;
		credits += creditGainRate * gameObj.getMap().getStage();

		if (tickCounter % 600 == 0) {
			creditGainRate += 0.03 * gameObj.getMap().getStage() * Math.pow(gameObj.getDifficulty().ordinal() + 1, 2); // Faster ramp up
		}

		// Try to spawn as soon as we can afford any unit
		if (credits >= 8) {
			attemptSpawn();
		}
	}

	private void attemptSpawn() {
		List<SpawnCard> affordable = new ArrayList<>();
		for (SpawnCard sc : pool) {
			if (credits >= sc.cost)
				affordable.add(sc);
		}

		if (affordable.isEmpty())
			return;

		int totalWeight = 0;
		for (SpawnCard sc : affordable)
			totalWeight += sc.weight;
		int roll = (int) (Math.random() * totalWeight);
		int cursor = 0;
		SpawnCard selected = affordable.get(0);

		for (SpawnCard sc : affordable) {
			cursor += sc.weight;
			if (roll < cursor) {
				selected = sc;
				break;
			}
		}

		// 4. MORE AGGRESSIVE SPENDING
		// Director now spends up to 80% of budget at once (was 50%)
		double spendLimit = Math.max(selected.cost, credits * 0.8);
		int spawnedCount = 0;

		double spawnAngle = Math.random() * Math.PI * 2;
		double spawnDist = 600 + (Math.random() * 200);
		int centerX = (int) (gameObj.getPlayer().getX() + Math.cos(spawnAngle) * spawnDist);
		int centerY = (int) (gameObj.getPlayer().getY() + Math.sin(spawnAngle) * spawnDist);

		while (credits >= selected.cost && spendLimit >= selected.cost && spawnedCount < selected.maxGroup) {
			int jX = centerX + (int) ((Math.random() - 0.5) * 80);
			int jY = centerY + (int) ((Math.random() - 0.5) * 80);

			jX = Math.max(0, Math.min(jX, gameObj.getMap().WIDTH - 1));
			jY = Math.max(0, Math.min(jY, gameObj.getMap().HEIGHT - 1));

			gameObj.addEnemy(new Enemy(gameObj, jX, jY, selected.id, getDifficultyMult()));

			credits -= selected.cost;
			spendLimit -= selected.cost;
			spawnedCount++;
		}
	}

	public void setTickCounter(int ticks) {
		this.tickCounter = ticks;
	}

	public void setCredits(double credits) {
		this.credits = credits;
	}

	public void setCreditGainRate(double rate) {
		this.creditGainRate = rate;
	}
}