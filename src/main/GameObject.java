package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Open.Artifacts.WorldItem;
import Open.Artifacts.Common.TurboSocks;
import Open.Entities.Entity;
import Open.Entities.Exp;
import Open.Entities.Player;
import Open.Entities.Enemies.Enemy;
import Open.Entities.Enemies.EnemyWaves;
import Open.Entities.Interactible.BloodShrine;
import Open.Entities.Interactible.Chest;
import Open.Entities.Interactible.Interactible;
import Open.Entities.Interactible.Teleporter;
import Open.Entities.Interactible.TriShop;
import Open.Map.Background;
import Open.Map.Tree;
import Open.Upgrades.Upgrades;
import Open.Weapons.WeaponProjectile.WeaponEntity;
import States.BaseState;
import States.DeadState;
import States.DifficultyState;
import States.ItemState;
import States.MenuState;
import States.OpenState;
import States.PauseState;
import States.TutorialState;
import States.UpgradeState;
import States.WeaponSelectionState;
import main.enums.Difficulty;

public class GameObject {

	private MouseInput mouseHandler;

	private Camera cam;

	private int startButtonWidth;
	private int startButtonHeight;
	private GameButton startButton;

	private int exitControlButtonWidth;
	private int exitControlButtonHeight;
	private GameButton exitControlButton;

	private int controlButtonWidth;
	private int controlButtonHeight;
	private GameButton controlButton;

	private GameButton itemButton;

	private GameSwitchButton hitBoxToggleButton;

	private KeyboardInput keyH;

	private Player player;

	private BaseState state;
	private TutorialState stateTutorial;
	private DeadState stateDead;
	private MenuState stateMenu;
	private OpenState stateOpen;
	private UpgradeState stateUpgrade;
	private PauseState statePause;
	private BaseState weaponSelectState;
	private DifficultyState stateDifficulty;
	private ItemState stateItem;

	private ArrayList<WorldItem> groundItems;
	private ArrayList<Enemy> enemies;
	private ArrayList<Interactible> interactibles;
	private List<DamageText> damageTexts;
	private ArrayList<Exp> exp;
	private ArrayList<WeaponEntity> projectiles;
	private ArrayList<Tree> trees;

	private Difficulty difficulty = Difficulty.NORMAL;

	private Upgrades upgrades;
	private EnemyWaves waves;
	private Background map;
	private NotificationManager notificationManager;

	private PixelTransition pixelTransition = new PixelTransition();
	private BufferedImage lastFrame;

	private MusicManager musicManager;

	private int chestsOpened = 0;

	public GameObject(KeyboardInput keyH, MouseInput mouseHandler) {
		Assets.load();

		this.keyH = keyH;

		// Start music manager immediately so menu music plays
		musicManager = new MusicManager();
		musicManager.playBGM();

		stateTutorial = new TutorialState(this);
		stateDead = new DeadState(this);
		setStateMenu(new MenuState(this));
		stateOpen = new OpenState(this);
		stateUpgrade = new UpgradeState(this);
		weaponSelectState = new WeaponSelectionState(this);
		stateDifficulty = new DifficultyState(this);
		setStatePause(new PauseState(this));
		stateItem = new ItemState(this);

		setCam(new Camera(this));

		this.mouseHandler = mouseHandler;
		state = getStateMenu();

		startButtonWidth = 300;
		startButtonHeight = 100;

		controlButtonWidth = 300;
		controlButtonHeight = 100;

		exitControlButtonWidth = 300;
		exitControlButtonHeight = 100;

		setStartButton(new GameButton(AppPanel.WIDTH / 2 - startButtonWidth / 2,
				AppPanel.HEIGHT / 2 - startButtonHeight / 2, startButtonWidth, startButtonHeight, "START",
				this::showDifficulty, new Color(0, 60, 60), Color.BLACK));

		setControlButton(new GameButton(AppPanel.WIDTH / 2 - startButtonWidth / 2,
				AppPanel.HEIGHT / 2 - controlButtonWidth / 2 + 230 + controlButtonHeight / 2, controlButtonWidth,
				controlButtonHeight, "TUTORIAL", this::showTutorial, new Color(0, 60, 60), Color.BLACK));

		setExitControlButton(new GameButton(AppPanel.WIDTH / 2 - exitControlButtonWidth / 2,
				AppPanel.HEIGHT / 2 + exitControlButtonHeight / 2 + 50, exitControlButtonWidth, exitControlButtonHeight,
				"EXIT BACK", this::toMenu));

		setHitBoxToggleButton(new GameSwitchButton(AppPanel.WIDTH / 2 - 100, AppPanel.HEIGHT / 2 + 200, 200, 50,
				"HITBOX: ", false, new Color(60, 60, 60), Color.WHITE));

		setItemButton(new GameButton(AppPanel.WIDTH / 2 - 100, AppPanel.HEIGHT / 2 + 260, 200, 50, "ITEMS", () -> {
			state = stateItem;
		}, new Color(60, 60, 60), Color.WHITE));
	}

	public void update() {
		pixelTransition.update();
		state.upadate();
		MouseInput.update();
		if (notificationManager != null) {
			notificationManager.update();
		}
	}

	public void draw(Graphics2D g2) {
		state.draw(g2);
	}

	// Core initializer for a fresh game. Kept private and invoked by
	// startGameWithDifficulty.
	private void startGameCore() {
		chestsOpened = 0; // Reset chest counter for new game
		enemies = new ArrayList<>();
		groundItems = new ArrayList<>();
		exp = new ArrayList<>();
		damageTexts = new ArrayList<>();
		projectiles = new ArrayList<>();
		interactibles = new ArrayList<>();
		setTrees(new ArrayList<>(50));

		map = new Background(this);

		player = new Player(this);
		cam = new Camera(this);
		upgrades = new Upgrades(this);
		waves = new EnemyWaves(this);
		notificationManager = new NotificationManager();

		generateInteractibles();

		// Return to BGM when a new game starts (in case boss was playing)
		musicManager.playBGM();

		state = getWeaponSelectState();
	}

	/** Public entrypoint: set selected difficulty then start the game. */
	public void startGameWithDifficulty(String difficulty) {
		this.setDifficulty(difficulty);
		startGameCore();
	}

	/**
	 * TESTING METHOD: Setup a scenario simulating ~30 minutes of gameplay on stage
	 * 3 with the player positioned next to the teleporter, ready to fight the boss.
	 */
	public void setupTestingScenario() {
		// Start a normal game first
		startGameCore();

		// Immediately transition to stage 3 via pixel transition
		pixelTransition.start(lastFrame, () -> {
			// Clear all existing entities
			for (Entity e : enemies)
				e.setDead(true);
			for (Entity e : exp)
				e.setDead(true);
			for (Entity e : interactibles)
				e.setDead(true);
			for (Entity e : projectiles)
				e.setDead(true);
			for (Entity e : groundItems)
				e.setDead(true);

			// Set to stage 3
			map.setStage(2);
			map.nextMap();
			generateInteractibles();

			// Setup player for late-game scenario
			// 1. Set exp to 10,000 so many upgrades are available
			player.setCurrExp(1000000);
			player.setGold(2000);
			player.addHp(100); // Full HP

			// 2. Give player random artifacts (5-8 random ones)
			Random rand = new Random();
			int numArtifacts = 50; // 5-8 artifacts
			for (int i = 0; i < numArtifacts; i++) {
				player.getArtifactManager().addArtifact(player.getArtifactManager().getRandomArtifact());
			}
			player.getArtifactManager().addArtifact(new TurboSocks(this));
			player.getArtifactManager().addArtifact(new TurboSocks(this));

			// 3. Simulate 30 minutes of wave progression
			// 30 minutes = 1800 seconds ≈ 105,882 ticks at 17ms per tick
			int thirtyMinuteTicks = 10500;
			waves.setTickCounter(thirtyMinuteTicks);
			// Increase credit gain rate for late game
			waves.setCreditGainRate(2.5);
			waves.setCredits(500); // Give some initial credits for big spawns

			// 4. Position player near teleporter
			Teleporter tp = null;
			for (Interactible interactible : interactibles) {
				if (interactible instanceof Teleporter) {
					tp = (Teleporter) interactible;
					break;
				}
			}
			if (tp != null) {
				player.setX(tp.getX() - 150);
				player.setY(tp.getY());
			}
		});

		// Spawn some tough enemies to make it feel like late game
		// This will happen during the hold phase of the transition
		state = stateOpen;
	}

	private void generateInteractibles() {
		setInteractibles(new ArrayList<Interactible>());

		Random rand = new Random();

		int mapWidth = map.WIDTH;
		int mapHeight = map.HEIGHT;

		int centerX = mapWidth / 2;
		int centerY = mapHeight / 2;

		int tpRangeX = (int) (mapWidth * 0.2);
		int tpRangeY = (int) (mapHeight * 0.2);

		int tpX = centerX + (rand.nextInt(tpRangeX * 2) - tpRangeX);
		int tpY = centerY + (rand.nextInt(tpRangeY * 2) - tpRangeY);

		getInteractibles().add(new Teleporter(this, tpX, tpY));

		// Track used positions so interactibles don't spawn too close to each other
		ArrayList<Point> usedPositions = new ArrayList<>();
		usedPositions.add(new Point(tpX, tpY));

		int totalItems = 50;

		// Minimum distance between spawned interactibles (pixels). Scales with map
		// size.
		int minDistance = Math.max(80, Math.min(mapWidth, mapHeight) / 10);
		int minDistanceSq = minDistance * minDistance;

		for (int i = 0; i < totalItems; i++) {
			int spawnX = 0;
			int spawnY = 0;
			boolean placed = false;
			int attempts = 0;
			// Try several times to find a position that's not too close to existing ones
			while (!placed && attempts < 100) {
				spawnX = rand.nextInt(mapWidth);
				spawnY = rand.nextInt(mapHeight);
				boolean tooClose = false;
				for (Point p : usedPositions) {
					int dx = spawnX - p.x;
					int dy = spawnY - p.y;
					if (dx * dx + dy * dy < minDistanceSq) {
						tooClose = true;
						break;
					}
				}
				if (!tooClose) {
					placed = true;
					usedPositions.add(new Point(spawnX, spawnY));
				}
				attempts++;
			}

			// If we failed to find a spaced position after many attempts, just use last
			// candidate
			if (!placed) {
				usedPositions.add(new Point(spawnX, spawnY));
			}

			double r = rand.nextDouble();
			if (r < 0.2) { // 20% chance for a Blood Shrine
				getInteractibles().add(new BloodShrine(this, spawnX, spawnY));
			} else if (r < 0.7) { // next 40% chance for a Chest
				getInteractibles().add(new Chest(this, spawnX, spawnY));
			} else { // remaining 40% for a TriShop
				getInteractibles().add(new TriShop(this, spawnX, spawnY));
			}
		}
	}

	public boolean isOnScreen(int x, int y, int w, int h) {
		int camX = getCameraX();
		int camY = getCameraY();

		int objLeft = x;
		int objRight = x + w;
		int objTop = y;
		int objBottom = y + h;

		int screenLeft = camX;
		int screenRight = camX + AppPanel.WIDTH;
		int screenTop = camY;
		int screenBottom = camY + AppPanel.HEIGHT;

		if (objRight < screenLeft)
			return false;
		if (objLeft > screenRight)
			return false;
		if (objBottom < screenTop)
			return false;
		if (objTop > screenBottom)
			return false;

		return true;
	}

	public void nextMap() {
		pixelTransition.start(lastFrame, () -> {
			for (Entity e : enemies)
				e.setDead(true);
			for (Entity e : exp)
				e.setDead(true);
			for (Entity e : interactibles)
				e.setDead(true);
			for (Entity e : projectiles)
				e.setDead(true);
			for (Entity e : groundItems)
				e.setDead(true);

			map.nextMap();
			generateInteractibles();
		});
		// When moving to next map the boss is dead — resume BGM
		musicManager.playBGM();
	}

	// ── Music triggers ─────────────────────────────────────────────────────

	/** Called by Teleporter when a boss is spawned. */
	public void onBossSpawn() {
		musicManager.playBossMusic();
	}

	/** Called by boss classes when they die. */
	public void onBossDeath() {
		musicManager.playBGM();
	}

	// ── Misc helpers ───────────────────────────────────────────────────────

	public int getCameraX() {
		return (int) getCam().getPos().getX();
	}

	public int getCameraY() {
		return (int) getCam().getPos().getY();
	}

	private void showTutorial() {
		state = stateTutorial;
	}

	private void showDifficulty() {
		state = getStateDifficulty();
	}

	private void toMenu() {
		musicManager.playBGM();
		state = getStateMenu();
	}

	private void startUpgrades() {
		state = getStateUpgrade();
	}

	public PixelTransition getPixelTransition() {
		return pixelTransition;
	}

	public void setLastFrame(BufferedImage frame) {
		this.lastFrame = frame;
	}

	public MouseInput getMouseHandler() {
		return mouseHandler;
	}

	public void setMouseHandler(MouseInput mouseHandler) {
		this.mouseHandler = mouseHandler;
	}

	public KeyboardInput getKeyH() {
		return keyH;
	}

	public void setKeyH(KeyboardInput keyH) {
		this.keyH = keyH;
	}

	public BaseState getState() {
		return state;
	}

	public void setState(BaseState state) {
		this.state = state;
	}

	public Player getPlayer() {
		return player;
	}

	public void addDamageText(double x, double y, double damage, boolean isCrit) {
		getDamageTexts().add(new DamageText(x, y, damage, isCrit));
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public ArrayList<Enemy> getEnemies() {
		return enemies;
	}

	public void addEnemy(Enemy e) {
		enemies.add(e);
	}

	public void setEnemies(ArrayList<Enemy> enemies) {
		this.enemies = enemies;
	}

	public EnemyWaves getWaves() {
		return waves;
	}

	public void setWaves(EnemyWaves waves) {
		this.waves = waves;
	}

	public Background getMap() {
		return map;
	}

	public void addExp(int value, int x, int y) {
		getExp().add(new Exp(this, value, x, y));
	}

	public void addProjectiles(WeaponEntity w) {
		getProjectiles().add(w);
	}

	public ArrayList<WeaponEntity> getProjectiles() {
		return projectiles;
	}

	public void setProjectiles(ArrayList<WeaponEntity> projectiles) {
		this.projectiles = projectiles;
	}

	public ArrayList<Exp> getExp() {
		return exp;
	}

	public void setExp(ArrayList<Exp> exp) {
		this.exp = exp;
	}

	public GameButton getExitControlButton() {
		return exitControlButton;
	}

	public void setExitControlButton(GameButton exitControlButton) {
		this.exitControlButton = exitControlButton;
	}

	public GameButton getStartButton() {
		return startButton;
	}

	public void setStartButton(GameButton startButton) {
		this.startButton = startButton;
	}

	public GameButton getControlButton() {
		return controlButton;
	}

	public void setControlButton(GameButton controlButton) {
		this.controlButton = controlButton;
	}

	public Upgrades getUpgrades() {
		return upgrades;
	}

	public void setUpgrades(Upgrades upgrades) {
		this.upgrades = upgrades;
	}

	public OpenState getStateOpen() {
		return stateOpen;
	}

	public void setStateOpen(OpenState stateOpen) {
		this.stateOpen = stateOpen;
	}

	public DeadState getStateDead() {
		return stateDead;
	}

	public void setStateDead(DeadState stateDead) {
		this.stateDead = stateDead;
	}

	public UpgradeState getStateUpgrade() {
		return stateUpgrade;
	}

	public void setStateUpgrade(UpgradeState stateUpgrade) {
		this.stateUpgrade = stateUpgrade;
	}

	public BaseState getWeaponSelectState() {
		return weaponSelectState;
	}

	public ArrayList<WorldItem> getGroundItems() {
		return groundItems;
	}

	public void setGroundItems(ArrayList<WorldItem> groundItems) {
		this.groundItems = groundItems;
	}

	public List<DamageText> getDamageTexts() {
		return damageTexts;
	}

	public void setDamageTexts(List<DamageText> damageTexts) {
		this.damageTexts = damageTexts;
	}

	public ArrayList<Interactible> getInteractibles() {
		return interactibles;
	}

	public void setInteractibles(ArrayList<Interactible> interactibles) {
		this.interactibles = interactibles;
	}

	public PauseState getStatePause() {
		return statePause;
	}

	public void setStatePause(PauseState statePause) {
		this.statePause = statePause;
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public void notifyArtifactPickup(Open.Artifacts.Artifact artifact) {
		if (notificationManager != null) {
			notificationManager.addNotification(artifact);
		}
	}

	public Camera getCam() {
		return cam;
	}

	public void setCam(Camera cam) {
		this.cam = cam;
	}

	public MenuState getStateMenu() {
		return stateMenu;
	}

	public void setStateMenu(MenuState stateMenu) {
		this.stateMenu = stateMenu;
	}

	public DifficultyState getStateDifficulty() {
		return stateDifficulty;
	}

	public void setStateDifficulty(DifficultyState stateDifficulty) {
		this.stateDifficulty = stateDifficulty;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = Difficulty.valueOf(difficulty);
	}

	public Difficulty getDifficultyEnum() {
		return difficulty;
	}

	public ArrayList<Tree> getTrees() {
		return trees;
	}

	public void setTrees(ArrayList<Tree> trees) {
		this.trees = trees;
	}

	public MusicManager getMusicManager() {
		return musicManager;
	}

	public int getChestsOpened() {
		return chestsOpened;
	}

	public void incrementChestsOpened() {
		this.chestsOpened++;
	}

	public GameSwitchButton getHitBoxToggleButton() {
		return hitBoxToggleButton;
	}

	public void setHitBoxToggleButton(GameSwitchButton hitBoxToggleButton) {
		this.hitBoxToggleButton = hitBoxToggleButton;
	}

	public GameButton getItemButton() {
		return itemButton;
	}

	public void setItemButton(GameButton itemButton) {
		this.itemButton = itemButton;
	}

}