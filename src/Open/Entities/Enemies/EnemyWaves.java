package Open.Entities.Enemies;

import java.util.ArrayList;
import java.util.List;

import main.GameObject;
import main.enums.Difficulty;
import main.enums.DifficultyConfigs;
import main.enums.DifficultyConfig;

public class EnemyWaves {

	// ── Difficulty preset ────────────────────────────────────────────────

	// ── State ─────────────────────────────────────────────────────────────
	private final GameObject gameObj;
	private DifficultyConfig config;
	private double credits;
	private double creditGainRate;
	private int tickCounter;

	// ── Spawn card ────────────────────────────────────────────────────────
	private static class SpawnCard {
		int id;
		int cost;
		int weight;
		int maxGroup;
		String cluster;

		SpawnCard(int id, int cost, int weight, int maxGroup) {
			this.id = id;
			this.cost = cost;
			this.weight = weight;
			this.maxGroup = maxGroup;
			this.cluster = null;
		}

		SpawnCard cluster(String name) {
			this.cluster = name;
			return this;
		}
	}

	private final List<SpawnCard> pool = new ArrayList<>();

	// ── Constructor ───────────────────────────────────────────────────────
	public EnemyWaves(GameObject gameObj) {
		this(gameObj, Difficulty.NORMAL);
	}

	public EnemyWaves(GameObject gameObj, Difficulty difficulty) {
		this.gameObj = gameObj;
		this.config = toConfig(difficulty);
		this.credits = 100;
		this.creditGainRate = 0.30 * config.creditGainMult;
		this.tickCounter = 0;
		buildPool();
	}

	private static DifficultyConfig toConfig(Difficulty d) {
		return switch (d) {
		case EASY -> DifficultyConfigs.EASY;
		case HARD -> DifficultyConfigs.HARD;
		default -> DifficultyConfigs.NORMAL;
		};
	}

	/** Swap difficulty mid-game if needed (e.g. from a settings menu). */
	public void setDifficulty(Difficulty d) {
		config = toConfig(d);
		creditGainRate = 0.30 * config.creditGainMult;
	}

	// ── Pool ──────────────────────────────────────────────────────────────
	private void buildPool() {
		// Basic
		pool.add(new SpawnCard(1, 8, 140, 10));
		pool.add(new SpawnCard(2, 10, 100, 6));
		pool.add(new SpawnCard(4, 8, 50, 12));
		// Clusters
		pool.add(new SpawnCard(-1, 30, 80, 1).cluster("bat_swarm"));
		pool.add(new SpawnCard(-1, 40, 50, 1).cluster("zombie_mob"));
		pool.add(new SpawnCard(-1, 50, 30, 1).cluster("skeleton_ring"));
		pool.add(new SpawnCard(-1, 60, 20, 1).cluster("mixed_rush"));
		// Elites
		pool.add(new SpawnCard(3, 40, 25, 3));
		pool.add(new SpawnCard(5, 60, 12, 1));
		// Mini-boss packs
		pool.add(new SpawnCard(-1, 120, 8, 1).cluster("mini_mudman_pack"));
		pool.add(new SpawnCard(-1, 200, 4, 1).cluster("glowbat_storm"));
		// Roaming bosses
		pool.add(new SpawnCard(-1, 400, 3, 1).cluster("roaming_lich"));
		pool.add(new SpawnCard(-1, 600, 2, 1).cluster("roaming_fire"));
		pool.add(new SpawnCard(-1, 900, 1, 1).cluster("roaming_void"));
	}

	// ── Difficulty mult ───────────────────────────────────────────────────
	public double getDifficultyMult() {
		double timeMult = 1.0 + (tickCounter / 1800.0) * 0.1;
		return timeMult * gameObj.getMap().getStage() * config.enemyStatMult;
	}

	// ── Update ────────────────────────────────────────────────────────────
	public void update() {
		if (gameObj.getState() != gameObj.getStateOpen())
			return;

		tickCounter++;
		credits += creditGainRate * gameObj.getMap().getStage();

		if (tickCounter % 600 == 0)
			creditGainRate += config.rampMult * gameObj.getMap().getStage();

		if (credits >= 8)
			attemptSpawn();
	}

	// ── Spawn ─────────────────────────────────────────────────────────────
	private void attemptSpawn() {
		List<SpawnCard> affordable = new ArrayList<>();
		for (SpawnCard sc : pool)
			if (credits >= sc.cost)
				affordable.add(sc);
		if (affordable.isEmpty())
			return;

		int totalWeight = 0;
		for (SpawnCard sc : affordable)
			totalWeight += sc.weight;
		int roll = (int) (Math.random() * totalWeight), cursor = 0;
		SpawnCard selected = affordable.get(0);
		for (SpawnCard sc : affordable) {
			cursor += sc.weight;
			if (roll < cursor) {
				selected = sc;
				break;
			}
		}

		credits -= selected.cost;

		double angle = Math.random() * Math.PI * 2;
		double dist = 600 + Math.random() * 200;
		int cx = clamp((int) (gameObj.getPlayer().getX() + Math.cos(angle) * dist), 0, gameObj.getMap().WIDTH - 1);
		int cy = clamp((int) (gameObj.getPlayer().getY() + Math.sin(angle) * dist), 0, gameObj.getMap().HEIGHT - 1);

		if (selected.cluster != null) {
			spawnCluster(selected.cluster, cx, cy);
		} else {
			double spendLimit = Math.max(selected.cost, credits * (config.spendPct / 100.0));
			int spawned = 0;
			while (credits >= selected.cost && spendLimit >= selected.cost && spawned < selected.maxGroup) {
				gameObj.addEnemy(new Enemy(gameObj, clamp(cx + jitter(80), 0, gameObj.getMap().WIDTH - 1),
						clamp(cy + jitter(80), 0, gameObj.getMap().HEIGHT - 1), selected.id, getDifficultyMult()));
				credits -= selected.cost;
				spendLimit -= selected.cost;
				spawned++;
			}
		}
	}

	// ── Cluster definitions ───────────────────────────────────────────────
	private void spawnCluster(String name, int cx, int cy) {
		double diff = getDifficultyMult();
		double bossDiff = diff * config.bossStatMult;

		switch (name) {
		case "bat_swarm": {
			int count = 15 + (int) (Math.random() * 6);
			for (int i = 0; i < count; i++) {
				double a = (2 * Math.PI / count) * i + Math.random() * 0.3;
				int r = 60 + (int) (Math.random() * 60);
				spawnAt(4, cx + (int) (Math.cos(a) * r), cy + (int) (Math.sin(a) * r), diff);
			}
			break;
		}
		case "zombie_mob": {
			for (int i = 0; i < 12; i++)
				spawnAt(1, cx + jitter(50), cy + jitter(50), diff);
			break;
		}
		case "skeleton_ring": {
			for (int i = 0; i < 8; i++) {
				double a = (2 * Math.PI / 8) * i;
				spawnAt(2, cx + (int) (Math.cos(a) * 80), cy + (int) (Math.sin(a) * 80), diff);
			}
			break;
		}
		case "mixed_rush": {
			for (int i = 0; i < 4; i++)
				spawnAt(1, cx + jitter(100), cy + jitter(100), diff);
			for (int i = 0; i < 4; i++)
				spawnAt(2, cx + jitter(100), cy + jitter(100), diff);
			for (int i = 0; i < 4; i++)
				spawnAt(4, cx + jitter(150), cy + jitter(150), diff);
			break;
		}
		case "mini_mudman_pack": {
			for (int i = 0; i < 3; i++)
				spawnAt(3, cx + jitter(60), cy + jitter(60), diff * 1.5);
			break;
		}
		case "glowbat_storm": {
			for (int i = 0; i < 8; i++) {
				double a = (2 * Math.PI / 8) * i;
				int r = 40 + (int) (Math.random() * 40);
				spawnAt(5, cx + (int) (Math.cos(a) * r), cy + (int) (Math.sin(a) * r), diff);
			}
			break;
		}
		case "roaming_lich": {
			gameObj.addEnemy(new AbyssalGod(gameObj, null, cx, cy, bossDiff));
			for (int i = 0; i < 4; i++) {
				double a = (2 * Math.PI / 4) * i;
				spawnAt(5, cx + (int) (Math.cos(a) * 150), cy + (int) (Math.sin(a) * 150), diff);
			}
			break;
		}
		case "roaming_fire": {
			gameObj.addEnemy(new FireDemonBoss(gameObj, null, cx, cy, bossDiff));
			for (int i = 0; i < 6; i++) {
				double a = (2 * Math.PI / 6) * i;
				spawnAt(4, cx + (int) (Math.cos(a) * 120), cy + (int) (Math.sin(a) * 120), diff * 1.2);
			}
			break;
		}
		case "roaming_void": {
			gameObj.addEnemy(new VoidLichBoss(gameObj, null, cx, cy, bossDiff));
			for (int i = 0; i < 8; i++) {
				double a = (2 * Math.PI / 8) * i;
				spawnAt(5, cx + (int) (Math.cos(a) * 180), cy + (int) (Math.sin(a) * 180), diff * 1.3);
			}
			break;
		}
		}
	}

	// ── Helpers ───────────────────────────────────────────────────────────
	private void spawnAt(int type, int x, int y, double diff) {
		gameObj.addEnemy(new Enemy(gameObj, clamp(x, 0, gameObj.getMap().WIDTH - 1),
				clamp(y, 0, gameObj.getMap().HEIGHT - 1), type, diff));
	}

	private int jitter(int range) {
		return (int) ((Math.random() - 0.5) * range * 2);
	}

	private int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}