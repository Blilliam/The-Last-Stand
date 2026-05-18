package main;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import Open.Artifacts.WorldItem;
import Open.Entities.Exp;
import Open.Entities.Player;
import Open.Entities.Enemies.Enemy;
import Open.Entities.Enemies.EnemyWaves;
import Open.Entities.Interactible.Chest;
import Open.Map.Background;
import Open.Upgrades.Upgrades;
import Open.Weapons.WeaponProjectile.WeaponEntity;
import States.BaseState;
import States.ControlsState;
import States.DeadState;
import States.MenuState;
import States.OpenState;
import States.UpgradeState;

public class GameObject {
	// declaring everything

	private MouseInput mouseHandler;

	// buttons for game start/settings stuff
	private int startButtonWidth;
	private int startButtonHeight;
	private GameButton startButton;

	private int exitControlButtonWidth;
	private int exitControlButtonHeight;
	private GameButton exitControlButton;

	private int controlButtonWidth;
	private int controlButtonHeight;
	private GameButton controlButton;

	// keyboard inputs
	private KeyboardInput keyH;

	// player
	private Player player;

	private BaseState state;
	private ControlsState stateControl;
	private DeadState stateDead;
	private MenuState stateMenu;
	private OpenState stateOpen;
	private UpgradeState stateUpgrade;

	private ArrayList<WorldItem> groundItems;

	// all enemies
	private ArrayList<Enemy> enemies;

	// chests
	private ArrayList<Chest> chests;
	
	private List<DamageText> damageTexts;

	// exp
	private ArrayList<Exp> exp;

	private ArrayList<WeaponEntity> projectiles;

	// upgrades
	private Upgrades upgrades;

	// manages enemy generation
	private EnemyWaves waves;

	// the map
	private Background map;

	public GameObject(KeyboardInput keyH, MouseInput mouseHandler) {
		Assets.load(); // loads all the images

		this.keyH = keyH;

		stateControl = new ControlsState(this);
		stateDead = new DeadState(this);
		stateMenu = new MenuState(this);
		stateOpen = new OpenState(this);
		stateUpgrade = new UpgradeState(this);

		this.mouseHandler = mouseHandler;
		state = stateMenu; // sets teh state to the meneu

		// actually initializes the buttons
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
		state.upadate();
		MouseInput.update();
	}

	public void draw(Graphics2D g2) {
		state.draw(g2);
	}

	private void startGame() { // creates new everything
		enemies = new ArrayList<Enemy>();
		groundItems = new ArrayList<WorldItem>();
		map = new Background(this);
		player = new Player(this);
		setUpgrades(new Upgrades(this));
		waves = new EnemyWaves(this);
		state = getStateOpen();
		setExp(new ArrayList<Exp>());
		setChests(new ArrayList<Chest>());
		getChests().add(new Chest(this, player.getX() + 200, player.getY()));
		setDamageTexts(new ArrayList<>());
		setProjectiles(new ArrayList<WeaponEntity>());
		// projectiles.add(new WeaponEntity(this, Assets.ProjectileBanana, player.getX()
		// + 200, player.getY(), 0, 0, 0));
	}

	public boolean isOnScreen(int x, int y, int w, int h) {

		int camX = getCameraX();
		int camY = getCameraY();

		// Object bounds in world space
		int objLeft = x;
		int objRight = x + w;
		int objTop = y;
		int objBottom = y + h;

		// Screen bounds in world space
		int screenLeft = camX;
		int screenRight = camX + AppPanel.WIDTH;
		int screenTop = camY;
		int screenBottom = camY + AppPanel.HEIGHT;

		// Check if completely outside screen
		if (objRight < screenLeft)
			return false; // left of screen
		if (objLeft > screenRight)
			return false; // right of screen
		if (objBottom < screenTop)
			return false; // above screen
		if (objTop > screenBottom)
			return false; // below screen

		return true; // otherwise it's visible
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

	public ArrayList<Chest> getChests() {
		return chests;
	}

	public void setChests(ArrayList<Chest> chests) {
		this.chests = chests;
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
}
