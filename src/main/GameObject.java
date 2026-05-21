package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import Open.Artifacts.WorldItem;
import Open.Entities.Entity;
import Open.Entities.Exp;
import Open.Entities.Player;
import Open.Entities.Enemies.Enemy;
import Open.Entities.Enemies.EnemyWaves;
import Open.Entities.Interactible.Chest;
import Open.Entities.Interactible.Interactible;
import Open.Entities.Interactible.Teleporter;
import Open.Entities.Interactible.TriShop;
import Open.Map.Background;
import Open.Upgrades.Upgrades;
import Open.Weapons.WeaponProjectile.WeaponEntity;
import States.BaseState;
import States.ControlsState;
import States.DeadState;
import States.MenuState;
import States.OpenState;
import States.UpgradeState;
import States.WeaponSelectionState;

public class GameObject {

	private MouseInput mouseHandler;

	private int startButtonWidth;
	private int startButtonHeight;
	private GameButton startButton;

	private int exitControlButtonWidth;
	private int exitControlButtonHeight;
	private GameButton exitControlButton;

	private int controlButtonWidth;
	private int controlButtonHeight;
	private GameButton controlButton;

	private KeyboardInput keyH;

	private Player player;

	private BaseState state;
	private ControlsState stateControl;
	private DeadState stateDead;
	private MenuState stateMenu;
	private OpenState stateOpen;
	private UpgradeState stateUpgrade;
	private BaseState weaponSelectState;

	private ArrayList<WorldItem> groundItems;
	private ArrayList<Enemy> enemies;
	private ArrayList<Interactible> interactibles;
	private List<DamageText> damageTexts;
	private ArrayList<Exp> exp;
	private ArrayList<WeaponEntity> projectiles;

	private Upgrades upgrades;
	private EnemyWaves waves;
	private Background map;

	private PixelTransition pixelTransition = new PixelTransition();
	private BufferedImage lastFrame;

	public GameObject(KeyboardInput keyH, MouseInput mouseHandler) {
		Assets.load();

		this.keyH = keyH;

		stateControl = new ControlsState(this);
		stateDead = new DeadState(this);
		stateMenu = new MenuState(this);
		stateOpen = new OpenState(this);
		stateUpgrade = new UpgradeState(this);
		weaponSelectState = new WeaponSelectionState(this);

		this.mouseHandler = mouseHandler;
		state = stateMenu;

		startButtonWidth = 300;
		startButtonHeight = 100;

		controlButtonWidth = 300;
		controlButtonHeight = 100;

		exitControlButtonWidth = 300;
		exitControlButtonHeight = 100;

		setStartButton(new GameButton(AppPanel.WIDTH / 2 - startButtonWidth / 2,
				AppPanel.HEIGHT / 2 - startButtonHeight / 2, startButtonWidth, startButtonHeight, "START",
				this::startGame, new Color(0, 60, 60), Color.BLACK));

		setControlButton(new GameButton(AppPanel.WIDTH / 2 - startButtonWidth / 2,
				AppPanel.HEIGHT / 2 - controlButtonWidth / 2 + 230 + controlButtonHeight / 2, controlButtonWidth,
				controlButtonHeight, "CONTROLS", this::showControls, new Color(0, 60, 60), Color.BLACK));

		setExitControlButton(new GameButton(AppPanel.WIDTH / 2 - exitControlButtonWidth / 2,
				AppPanel.HEIGHT / 2 + exitControlButtonHeight / 2 + 50, exitControlButtonWidth, exitControlButtonHeight,
				"EXIT BACK", this::toMenu));
	}

	public void update() {
		pixelTransition.update();
		state.upadate();
		MouseInput.update();
	}

	public void draw(Graphics2D g2) {
		state.draw(g2);
	}

	private void startGame() {
		enemies = new ArrayList<Enemy>();
		groundItems = new ArrayList<WorldItem>();
		map = new Background(this);
		player = new Player(this);
		setUpgrades(new Upgrades(this));
		waves = new EnemyWaves(this);

		state = getWeaponSelectState();

		setExp(new ArrayList<Exp>());
		setInteractibles(new ArrayList<Interactible>());

		getInteractibles().add(new Chest(this, player.getX() + 200, player.getY()));
		getInteractibles().add(new Teleporter(this, player.getX() - 400, player.getY()));
		getInteractibles().add(new TriShop(this, player.getX(), player.getX() + 200));

		setDamageTexts(new ArrayList<>());
		setProjectiles(new ArrayList<WeaponEntity>());
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
		});
	}

	public int getCameraX() {
		return player.getX() - AppPanel.WIDTH / 2;
	}

	public int getCameraY() {
		return player.getY() - AppPanel.HEIGHT / 2;
	}

	private void showControls() {
		state = stateControl;
	}

	private void toMenu() {
		state = stateMenu;
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
}