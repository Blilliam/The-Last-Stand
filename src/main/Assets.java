package main;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class Assets {

	// initializes all the images

	// Weapons
	public static BufferedImage aura;
	public static BufferedImage bone;
//	public static BufferedImage axe;
//	public static BufferedImage cross;
//	public static BufferedImage kingBible;
//	public static BufferedImage magicWand;
//	public static BufferedImage fireWand;

	public static BufferedImage WeaponBanana;
	public static BufferedImage ProjectileBanana;
	public static BufferedImage ProjectileBullet;
	public static BufferedImage ProjectileFireBall;
	
	public static BufferedImage AuraIcon;
	public static BufferedImage BananaIcon;
	public static BufferedImage FireStaffIcon;
	public static BufferedImage RevolverIcon;
	public static BufferedImage BoneIcon;
	public static BufferedImage SwordIcon;
	public static BufferedImage KatanaIcon;
	public static BufferedImage DexecutionerIcon;
	//artifacts
	public static BufferedImage CommonOatsIcon;
	public static BufferedImage CommonWatchIcon;
	public static BufferedImage CommonKeyIcon;
	public static BufferedImage CommonBatteryIcon;
	public static BufferedImage CommonCloverIcon;
	public static BufferedImage CommonMidasIcon;
	
	public static BufferedImage UncommonBackPackIcon;
	public static BufferedImage UncommonBeerIcon;
	public static BufferedImage UncommonDemonicBladeIcon;
	public static BufferedImage UncommonDemonicBloodIcon;
	public static BufferedImage UncommonEchoShardIcon;
	
	public static BufferedImage LegendaryMagenetIcon;

	
	public static BufferedImage GoldTomeIcon;
	public static BufferedImage HpTomeIcon;
	public static BufferedImage LuckTomeIcon;
	public static BufferedImage CritTomeIcon;
	public static BufferedImage ProjectileSpeedTomeIcon;
	public static BufferedImage ProjectileCountTomeIcon;
	public static BufferedImage SizeTomeIcon;
	public static BufferedImage XpTomeIcon;
	public static BufferedImage CooldownTomeIcon;
	public static BufferedImage DamageTomeIcon;
	public static BufferedImage CursedTomeIcon;
	

	// Player
	public static BufferedImage playerSheet;

	public static BufferedImage[] exp;
	
	public static BufferedImage[] impact;

	// Enemy GIF frames: walking and death
	public static BufferedImage[] zombieWalk;
	public static BufferedImage[] zombieDeath;

	public static BufferedImage[] skeletonWalk;
	public static BufferedImage[] skeletonDeath;

	public static BufferedImage[] mudmanWalk;
	public static BufferedImage[] mudmanDeath;

	public static BufferedImage[] batWalk;
	public static BufferedImage[] batDeath;

	public static BufferedImage[] glowingBatWalk;
	public static BufferedImage[] glowingBatDeath;

	public static void load() {
			
			CommonOatsIcon =loadImage("Images/Artifacts/Common/Item_Oats.png");
			CommonWatchIcon = loadImage("Images/Artifacts/Common/Item_Time_Bracelet.png");
			CommonCloverIcon = loadImage("Images/Artifacts/Common/Item_Clover.png");
			CommonBatteryIcon =loadImage("Images/Artifacts/Common/Item_Battery.png");
			CommonKeyIcon =loadImage("Images/Artifacts/Common/Item_Key.png");
			CommonMidasIcon =loadImage("Images/Artifacts/Common/Item_Golden_Glove.png");
			
			UncommonBackPackIcon =loadImage("Images/Artifacts/Uncommon/Item_Backpack.png");
			UncommonBeerIcon = loadImage("Images/Artifacts/Uncommon/Item_Beer.png");
			UncommonDemonicBladeIcon = loadImage("Images/Artifacts/Uncommon/Item_Demonic_Blade.png");
			UncommonDemonicBloodIcon =loadImage("Images/Artifacts/Uncommon/Item_Demonic_Blood.png");
			UncommonEchoShardIcon =loadImage("Images/Artifacts/Uncommon/Item_Echo_Shard.png");
			
			LegendaryMagenetIcon =loadImage("Images/Artifacts/Legendary/Item_Sucky_Magnet.png");
			
			
			GoldTomeIcon =loadImage("Images/Items/Tomes/Gold-Tome.png");
			HpTomeIcon =loadImage("Images/Items/Tomes/HP-Tome.png");;
			LuckTomeIcon =loadImage("Images/Items/Tomes/Luck-Tome.png");;
			CritTomeIcon =loadImage("Images/Items/Tomes/Precision-Tome.png");;
			ProjectileSpeedTomeIcon =loadImage("Images/Items/Tomes/Projectile-Tome.png");
			ProjectileCountTomeIcon =loadImage("Images/Items/Tomes/Quantity-Tome.png");
			SizeTomeIcon =loadImage("Images/Items/Tomes/Size-Tome.png");
			XpTomeIcon =loadImage("Images/Items/Tomes/XP-Tome.png");
			CooldownTomeIcon =loadImage("Images/Items/Tomes/Cooldown-Tome.png");
			DamageTomeIcon =loadImage("Images/Items/Tomes/Damage-Tome.png");
			CursedTomeIcon =loadImage("Images/Items/Tomes/Cursed_Tome_Logo.png");
			
			
			AuraIcon =loadImage("Images/Items/Aura_Icon.png");
			BananaIcon =loadImage("Images/Items/Banana_Icon.png");
			FireStaffIcon =loadImage("Images/Items/Fire_Staff.png");
			RevolverIcon =loadImage("Images/Items/Revolver_Icon.png");
			BoneIcon =loadImage("Images/Items/Bone_Icon.png");
			SwordIcon =loadImage("Images/Items/Sword_Icon.png");
			KatanaIcon =loadImage("Images/Items/Katana.png");
			DexecutionerIcon = loadImage("Images/Items/Dexecutioner.png");

			ProjectileBanana =loadImage("Images/Projectiles/Banana projectile.png");
			ProjectileBullet =loadImage("Images/Projectiles/projectileBullet.png");
			ProjectileFireBall =loadImage("Images/Projectiles/fireBallProjectile.png");
			bone =loadImage("Images/Projectiles/boneProjectile.png");
			
			exp = splitSpriteSheet(loadImage("Images/Coins/spr_coin_azu.png"), 16, 16);
			
			impact = splitSpriteSheet(loadImage("Images/Projectiles/Impact.png"), 48, 48);

			// Player
			
			playerSheet =loadImage("Images/Player/antonio_spritesheet.png");

			// Enemy GIFs
			zombieWalk = loadGifFrames("Images/Enemy/z.gif");
			zombieDeath = loadGifFrames("Images/Enemy/zDead.gif");

			skeletonWalk = loadGifFrames("Images/Enemy/s.gif");
			skeletonDeath = loadGifFrames("Images/Enemy/sDead.gif");

			mudmanWalk = loadGifFrames("Images/Enemy/m.gif");
			mudmanDeath = loadGifFrames("Images/Enemy/mDead.gif");

			batWalk = loadGifFrames("Images/Enemy/bat.gif");
			batDeath = loadGifFrames("Images/Enemy/batDead.gif");

			glowingBatWalk = loadGifFrames("Images/Enemy/glowBat.gif");
			glowingBatDeath = loadGifFrames("Images/Enemy/glowBatDead.gif");
	}
	
	private static BufferedImage loadImage(String path) {
	    try {
	        File file = new File(path);
	        if (!file.exists()) {
	            System.err.println("MISSING FILE: " + file.getAbsolutePath());
	            return null; 
	        }
	        return ImageIO.read(file);
	    } catch (IOException e) {
	        System.err.println("FAILED TO READ: " + path);
	        e.printStackTrace();
	        return null;
	    }
	}

	/**
	 * translates gif to buffered image array
	 * 
	 * @param string of the image path for getting file
	 * @return returns array of buffered images of the gif
	 */
	public static BufferedImage[] loadGifFrames(String path) {
	    File file = new File(path);
	    if (!file.exists()) {
	        System.err.println("GIF FILE NOT FOUND: " + file.getAbsolutePath());
	        return new BufferedImage[0];
	    }

	    try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {
	        var readers = ImageIO.getImageReadersByFormatName("gif");
	        if (!readers.hasNext()) return new BufferedImage[0];

	        var reader = readers.next();
	        reader.setInput(iis);

	        int frameCount = reader.getNumImages(true);
	        BufferedImage[] frames = new BufferedImage[frameCount];
	        
	        // Use a consistent canvas for the entire GIF
	        BufferedImage master = null;

	        for (int i = 0; i < frameCount; i++) {
	            BufferedImage frame = reader.read(i);
	            
	            if (master == null) {
	                master = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
	            }

	            // --- THE CRITICAL FIX ---
	            // Create a graphics object and set the composite to clear the previous area
	            Graphics2D g = master.createGraphics();
	            
	            // This clears the canvas so old frames don't "stack"
	            g.setComposite(AlphaComposite.Clear);
	            g.fillRect(0, 0, master.getWidth(), master.getHeight());
	            g.setComposite(AlphaComposite.SrcOver);
	            
	            // Draw the new frame
	            g.drawImage(frame, 0, 0, null);
	            g.dispose();

	            // Create a permanent copy for this specific animation frame
	            BufferedImage copy = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_INT_ARGB);
	            Graphics2D gCopy = copy.createGraphics();
	            gCopy.drawImage(master, 0, 0, null);
	            gCopy.dispose();
	            
	            frames[i] = copy;
	        }
	        
	        reader.dispose();
	        return frames;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return new BufferedImage[0];
	    }
	}

	public static BufferedImage[] splitSpriteSheet(BufferedImage sheet, int frameWidth, int frameHeight) {

	    int cols = sheet.getWidth() / frameWidth;

	    BufferedImage[] output = new BufferedImage[cols];

	    for (int x = 0; x < cols; x++) {

	        if (x * frameWidth + frameWidth > sheet.getWidth()) break;
	        if (frameHeight > sheet.getHeight()) break;

	        BufferedImage frame = sheet.getSubimage(
	            x * frameWidth,
	            0,
	            frameWidth,
	            frameHeight
	        );

	        output[x] = frame;
	    }

	    return output;
	}
}
