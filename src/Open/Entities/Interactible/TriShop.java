package Open.Entities.Interactible;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import Open.Artifacts.Artifact;
import Open.Artifacts.WorldItem;
import Open.Entities.Entity;
import main.GameObject;
import main.enums.ArtifactRarity;
import main.enums.ChestState;

public class TriShop extends Interactible {

	// ── Layout ────────────────────────────────────────────────────────────────
	private static final int SLOT_W = 70;
	private static final int SLOT_H = 70;
	private static final int SLOT_GAP = 20;
	private static final int TOTAL_W = SLOT_W * 3 + SLOT_GAP * 2;
	private static final int TOTAL_H = 110; // shelf + sign
	private static final int SIGN_H = 28;
	private static final int SHELF_H = TOTAL_H - SIGN_H;

	// ── Shop data ─────────────────────────────────────────────────────────────
	private static final int ITEM_COUNT = 3;
	private final Artifact[] items = new Artifact[ITEM_COUNT];
	private final int[] prices = new int[ITEM_COUNT];
	private final boolean[] sold = new boolean[ITEM_COUNT];

	// ── Selection & Animation ─────────────────────────────────────────────────
	private double bobTimer = 0f;
	private int flashTimer = 0;
	private int flashSlot = -1;
	private int closestSlot = -1;

	// ── Price tiers ───────────────────────────────────────────────────────────
	private static final int[] PRICE_POOL = { 20, 25, 30, 50 };

	public TriShop(GameObject gameObj, int x, int y) {
		super(gameObj, x, y);

		width = TOTAL_W;
		height = TOTAL_H;

		// ── Built-in Hitbox Setup ────────────────────────────────────────────
		// We define the interaction zone directly inside the Entity 'hitBox' shape.
		// This expands the hitbox horizontally and vertically so checkCollision()
		// registers when the player stands near the terminal.
		int padX = 50;
		int padY = 60;
		setHitBox(new Rectangle2D.Double(x - TOTAL_W / 2.0 - padX, y - TOTAL_H - padY, TOTAL_W + (padX * 2),
				TOTAL_H + (padY * 2)));

		// Stock the shop
		for (int i = 0; i < ITEM_COUNT; i++) {
			items[i] = gameObj.getPlayer().getArtifactManager().getRandomArtifact();
			prices[i] = getPrice(items[i].getRarity());
			sold[i] = false;
		}
	}

	public int getPrice(ArtifactRarity rarity) {
		switch (rarity) {
		case COMMON:
			return PRICE_POOL[0];
		case UNCOMMON:
			return PRICE_POOL[1];
		case RARE:
			return PRICE_POOL[2];
		case LEGENDARY:
			return PRICE_POOL[3];
		default:
			throw new IllegalArgumentException("Unexpected value: " + rarity);
		}
	}

	// ═════════════════════════════════════════════════════════════════════════
	// UPDATE
	// ═════════════════════════════════════════════════════════════════════════
	@Override
	public void update() {
		bobTimer += 0.05f;
		if (flashTimer > 0)
			flashTimer--;

		if (getState() == ChestState.CLOSED) {
			// ── Built-in Collision Check ─────────────────────────────────────
			// Uses the engine's built-in narrow-phase Area intersection logic
			playerInRange = Entity.checkCollision(this, gameObj.getPlayer());

			if (playerInRange) {
				Shape playerHitBox = gameObj.getPlayer().getHitBox();

				if (playerHitBox != null) {
					// Extract the geometric center of the player's true hitbox
					double playerCenterX = playerHitBox.getBounds2D().getCenterX();
					double minDistance = Double.MAX_VALUE;
					int targetSlot = -1;

					// Evaluate which sub-slot center line is closest to the player's hitbox center
					for (int i = 0; i < ITEM_COUNT; i++) {
						if (sold[i])
							continue;

						double slotDist = Math.abs(slotWorldX(i) - playerCenterX);
						if (slotDist < minDistance) {
							minDistance = slotDist;
							targetSlot = i;
						}
					}
					closestSlot = targetSlot;
				}

				// Interaction verification
				if (gameObj.getKeyH().interact && closestSlot != -1) {
					tryBuySlot(closestSlot);
				}
			} else {
				closestSlot = -1;
			}
		} else {
			closestSlot = -1;
		}
	}

	/** Purchases the targeted item slot and locks down the rest of the shop. */
	private void tryBuySlot(int idx) {
		if (idx == -1 || sold[idx])
			return;

		int gold = gameObj.getPlayer().getGold();
		double freeChance = gameObj.getPlayer().getArtifactManager().getPercentFreeChest();

		boolean free = Math.random() < freeChance;
		if (gold >= prices[idx] || free) {
			if (!free)
				gameObj.getPlayer().setGold(gold - prices[idx]);

			int slotWorldX = slotWorldX(idx);
			gameObj.getGroundItems().add(new WorldItem(gameObj, items[idx], slotWorldX, y - SHELF_H / 2));

			flashSlot = idx;
			flashTimer = 30;

			// Instantly transition remaining slots to SOLD and flip state to open
			for (int i = 0; i < ITEM_COUNT; i++) {
				sold[i] = true;
			}
			setState(ChestState.OPEN);
			closestSlot = -1;
		}
	}

	private int slotWorldX(int i) {
		int leftEdge = x - TOTAL_W / 2;
		return leftEdge + i * (SLOT_W + SLOT_GAP) + SLOT_W / 2;
	}

	// ═════════════════════════════════════════════════════════════════════════
	// DRAW
	// ═════════════════════════════════════════════════════════════════════════
	@Override
	public void draw(Graphics2D g) {
		int camX = gameObj.getCameraX();
		int camY = gameObj.getCameraY();

		int sx = x - TOTAL_W / 2 - camX;
		int sy = y - TOTAL_H - camY;

		drawSign(g, sx, sy);
		int shelfY = sy + SIGN_H;
		drawShelf(g, sx, shelfY);

		for (int i = 0; i < ITEM_COUNT; i++) {
			int slotX = sx + i * (SLOT_W + SLOT_GAP);
			int slotY = shelfY + (SHELF_H - SLOT_H) / 2;
			drawSlot(g, i, slotX, slotY);
		}

		if (playerInRange && hasAnyUnsold()) {
			drawPrompt(g, sx + TOTAL_W / 2, sy - 6);
		}
	}

	private void drawSign(Graphics2D g, int sx, int sy) {
		g.setColor(new Color(90, 55, 20));
		g.fillRect(sx, sy, TOTAL_W, SIGN_H);

		g.setColor(new Color(140, 90, 35));
		g.fillRect(sx, sy, TOTAL_W, 3);

		g.setColor(new Color(50, 28, 8));
		g.fillRect(sx, sy + SIGN_H - 3, TOTAL_W, 3);

		for (int rx : new int[] { sx + 6, sx + TOTAL_W - 10 }) {
			g.setColor(new Color(200, 160, 60));
			g.fillOval(rx, sy + SIGN_H / 2 - 3, 6, 6);
			g.setColor(new Color(120, 90, 20));
			g.drawOval(rx, sy + SIGN_H / 2 - 3, 6, 6);
		}

		g.setFont(new Font("Monospaced", Font.BOLD, 13));
		String title = "✦  T R I S H O P  ✦";
		FontMetrics fm = g.getFontMetrics();
		int tx = sx + (TOTAL_W - fm.stringWidth(title)) / 2;
		int ty = sy + SIGN_H / 2 + fm.getAscent() / 2 - 1;

		g.setColor(new Color(0, 0, 0, 120));
		g.drawString(title, tx + 1, ty + 1);
		g.setColor(new Color(255, 220, 80));
		g.drawString(title, tx, ty);
	}

	private void drawShelf(Graphics2D g, int sx, int shelfY) {
		g.setColor(new Color(55, 40, 20));
		g.fillRect(sx, shelfY, TOTAL_W, SHELF_H);

		g.setColor(new Color(70, 50, 25));
		for (int py = shelfY + 12; py < shelfY + SHELF_H; py += 20) {
			g.fillRect(sx, py, TOTAL_W, 3);
		}

		g.setColor(new Color(80, 55, 25));
		g.fillRect(sx, shelfY, 6, SHELF_H);
		g.fillRect(sx + TOTAL_W - 6, shelfY, 6, SHELF_H);

		g.setColor(new Color(120, 85, 40));
		g.fillRect(sx, shelfY, 2, SHELF_H);
		g.fillRect(sx + TOTAL_W - 2, shelfY, 2, SHELF_H);

		g.setColor(new Color(100, 70, 30));
		g.fillRect(sx, shelfY + SHELF_H - 8, TOTAL_W, 8);
		g.setColor(new Color(140, 100, 45));
		g.fillRect(sx, shelfY + SHELF_H - 8, TOTAL_W, 2);
	}

	private void drawSlot(Graphics2D g, int idx, int slotX, int slotY) {
		boolean isSold = sold[idx];
		boolean isHighlighted = (idx == closestSlot) && playerInRange && !isSold;

		Color slotBg = isSold ? new Color(30, 25, 15, 180)
				: (isHighlighted ? new Color(45, 35, 15, 240) : new Color(20, 15, 8, 220));
		g.setColor(slotBg);
		g.fillRoundRect(slotX, slotY, SLOT_W, SLOT_H, 6, 6);

		if (flashTimer > 0 && flashSlot == idx) {
			float a = flashTimer / 30f;
			g.setColor(new Color(1f, 0.9f, 0.2f, a * 0.6f));
			g.fillRoundRect(slotX, slotY, SLOT_W, SLOT_H, 6, 6);
		}

		// ── Selection Highlighting border adjustment ─────────────────────────
		if (isHighlighted) {
			g.setColor(new Color(255, 215, 50));
			g.setStroke(new BasicStroke(3f));
		} else {
			g.setColor(isSold ? new Color(60, 50, 30) : new Color(140, 105, 40));
			g.setStroke(new BasicStroke(2f));
		}
		g.drawRoundRect(slotX, slotY, SLOT_W, SLOT_H, 6, 6);
		g.setStroke(new BasicStroke(1f));

		if (isSold) {
			g.setFont(new Font("Monospaced", Font.BOLD, 11));
			g.setColor(new Color(100, 80, 40, 180));
			String soldText = "SOLD";
			FontMetrics fm = g.getFontMetrics();
			g.drawString(soldText, slotX + (SLOT_W - fm.stringWidth(soldText)) / 2,
					slotY + SLOT_H / 2 + fm.getAscent() / 2 - 2);
			return;
		}

		Artifact art = items[idx];
		if (art != null && art.getIcon() != null) {
			int iconSize = 40;
			int bob = (int) (Math.sin(bobTimer + idx * 1.1) * 3);
			int ix = slotX + (SLOT_W - iconSize) / 2;
			int iy = slotY + 6 + bob;

			int glowAlpha = isHighlighted ? 80 : 40;
			g.setColor(new Color(255, 220, 80, glowAlpha));
			g.fillOval(ix - 4, iy - 2, iconSize + 8, iconSize + 8);

			g.drawImage(art.getIcon(), ix, iy, iconSize, iconSize, null);
		}

		drawPriceTag(g, idx, slotX, slotY);
	}

	private void drawPriceTag(Graphics2D g, int idx, int slotX, int slotY) {
		int gold = gameObj.getPlayer().getGold();
		boolean canAfford = gold >= prices[idx];

		String priceStr = "$" + prices[idx];
		g.setFont(new Font("Monospaced", Font.BOLD, 11));
		FontMetrics fm = g.getFontMetrics();
		int tw = fm.stringWidth(priceStr);
		int tagW = tw + 10;
		int tagH = 16;
		int tagX = slotX + (SLOT_W - tagW) / 2;
		int tagY = slotY + SLOT_H - tagH - 3;

		Color tagColor = canAfford ? new Color(180, 140, 0) : new Color(100, 30, 30);
		g.setColor(tagColor);
		g.fillRoundRect(tagX, tagY, tagW, tagH, 4, 4);

		g.setColor(canAfford ? new Color(255, 215, 50) : new Color(160, 60, 60));
		g.setStroke(new BasicStroke(1.5f));
		g.drawRoundRect(tagX, tagY, tagW, tagH, 4, 4);
		g.setStroke(new BasicStroke(1f));

		g.setColor(Color.WHITE);
		g.drawString(priceStr, tagX + (tagW - tw) / 2, tagY + tagH - 3);
	}

	private void drawPrompt(Graphics2D g, int centerX, int bottomY) {
		String msg = "[E] BUY";
		g.setFont(new Font("Monospaced", Font.BOLD, 13));
		FontMetrics fm = g.getFontMetrics();
		int mw = fm.stringWidth(msg);
		int mh = fm.getHeight();
		int pad = 7;

		int bx = centerX - mw / 2 - pad;
		int by = bottomY - mh - pad * 2;
		int bw = mw + pad * 2;
		int bh = mh + pad;

		g.setColor(new Color(0, 0, 0, 160));
		g.fillRoundRect(bx + 2, by + 2, bw, bh, 5, 5);

		g.setColor(new Color(30, 20, 8, 230));
		g.fillRoundRect(bx, by, bw, bh, 5, 5);

		g.setColor(new Color(200, 160, 40));
		g.setStroke(new BasicStroke(2f));
		g.drawRoundRect(bx, by, bw, bh, 5, 5);
		g.setStroke(new BasicStroke(1f));

		g.setColor(new Color(255, 220, 80));
		g.drawString(msg, bx + pad, by + mh - 2);
	}

	@Override
	public void open() {
		if (closestSlot != -1) {
			tryBuySlot(closestSlot);
		}
	}

	private boolean hasAnyUnsold() {
		for (boolean s : sold)
			if (!s)
				return true;
		return false;
	}
}