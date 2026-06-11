package main;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Handles background music and boss music with smooth crossfade transitions.
 *
 * Expected audio files (place in project root "Audio/" folder): Audio/bgm.wav –
 * main gameplay loop Audio/boss.wav – boss fight loop
 *
 * Both clips are looped. Crossfade runs on a background thread so the game loop
 * is never stalled.
 */
public class MusicManager {

	public enum Track {
		BGM, BOSS, NONE
	}

	public enum SFX {
		LEVELUP("Audio/Effects/LevelUp.wav"), COINPICKUP("Audio/Effects/CoinPickUp.wav"),
		DEATH("Audio/Effects/GameOver.wav");

		private final String path;

		SFX(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}

	// ── Config ─────────────────────────────────────────────────────────────
	private static final String BGM_PATH = "Audio/Music/bgm.wav";
	private static final String BOSS_PATH = "Audio/Music/boss.wav";
	private float sfxVolume = 1.0f; // separate from masterVolume

	/** Crossfade duration in milliseconds. */
	private static final int FADE_MS = 2000;
	/** How often the fade thread ticks (ms). */
	private static final int FADE_TICK_MS = 50;
	/** Master volume 0.0 – 1.0. */
	private float masterVolume = 0.5f;

	// ── State ──────────────────────────────────────────────────────────────
	private Clip bgmClip;
	private Clip bossClip;
	private Track currentTrack = Track.NONE;
	private Thread fadeThread;
	private volatile boolean fadingIn = false;
	private volatile boolean fadingOut = false;

	// ── Init ───────────────────────────────────────────────────────────────

	private static final int POOL_SIZE = 4;
	private final Map<SFX, Clip[]> sfxPool = new EnumMap<>(SFX.class);
	private final Map<SFX, Integer> sfxPoolIndex = new EnumMap<>(SFX.class);

	public MusicManager() {
		bgmClip = loadClip(BGM_PATH);
		bossClip = loadClip(BOSS_PATH);
		for (SFX sfx : SFX.values()) {
			Clip[] pool = new Clip[POOL_SIZE];
			for (int i = 0; i < POOL_SIZE; i++) {
				pool[i] = loadClip(sfx.getPath());
			}
			sfxPool.put(sfx, pool);
			sfxPoolIndex.put(sfx, 0);
		}
	}

	public void playSFX(SFX sfx) {
		Clip[] pool = sfxPool.get(sfx);
		if (pool == null)
			return;

		int idx = sfxPoolIndex.get(sfx);
		Clip clip = pool[idx];
		sfxPoolIndex.put(sfx, (idx + 1) % POOL_SIZE);

		if (clip == null)
			return;
		if (clip.isRunning())
			clip.stop();
		clip.setFramePosition(0);
		FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		fc.setValue(fc.getMaximum()); // slam it to max
		clip.start();
	}

	private Clip loadClip(String path) {
		File file = new File(path);
		if (!file.exists()) {
			System.err.println("[MusicManager] File not found: " + file.getAbsolutePath());
			return null;
		}
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(file);
			Clip clip = AudioSystem.getClip();
			clip.open(ais);
			return clip;
		} catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
			System.err.println("[MusicManager] Failed to load: " + path);
			e.printStackTrace();
			return null;
		}
	}

	// ── Public API ─────────────────────────────────────────────────────────

	/**
	 * Start playing BGM immediately (or crossfade if something is already playing).
	 */

	public void playBGM() {
		if (currentTrack == Track.BGM)
			return;
		crossfade(bossClip, bgmClip);
		currentTrack = Track.BGM;
	}

	/** Crossfade to boss music. */
	public void playBossMusic() {
		if (currentTrack == Track.BOSS)
			return;
		crossfade(bgmClip, bossClip);
		currentTrack = Track.BOSS;
	}

	/** Stop all music (fades out). */
	public void stopAll() {
		if (currentTrack == Track.NONE)
			return;
		Clip active;
		if (currentTrack == Track.BGM) {
			active = bgmClip;
		} else {
			active = bossClip;
		}
		currentTrack = Track.NONE;
		fadeOutAndStop(active);
	}

	public void setMasterVolume(float v) {
		masterVolume = Math.max(0f, Math.min(1f, v));
	}

	// ── Crossfade logic ────────────────────────────────────────────────────

	/**
	 * Fades `outClip` to silence while fading `inClip` from silence to full. Runs
	 * on a daemon thread so it never blocks the game loop.
	 */
	private void crossfade(Clip outClip, Clip inClip) {
		// Stop any running fade
		if (fadeThread != null && fadeThread.isAlive()) {
			fadeThread.interrupt();
		}

		// Prepare the incoming clip
		if (inClip != null) {
			if (!inClip.isRunning()) {
				inClip.setFramePosition(0);
				inClip.loop(Clip.LOOP_CONTINUOUSLY);
			}
			setVolume(inClip, 0f);
		}

		fadeThread = new Thread(() -> {
			int steps = FADE_MS / FADE_TICK_MS;
			for (int i = 1; i <= steps; i++) {
				if (Thread.currentThread().isInterrupted())
					return;

				float progress = (float) i / steps;

				if (outClip != null && outClip.isRunning()) {
					setVolume(outClip, masterVolume * (1f - progress));
				}
				if (inClip != null && inClip.isRunning()) {
					setVolume(inClip, masterVolume * progress);
				}

				try {
					Thread.sleep(FADE_TICK_MS);
				} catch (InterruptedException e) {
					return;
				}
			}

			// Silence and stop the outgoing clip
			if (outClip != null) {
				setVolume(outClip, 0f);
				outClip.stop();
			}
		});
		fadeThread.setDaemon(true);
		fadeThread.start();
	}

	private void fadeOutAndStop(Clip clip) {
		if (clip == null)
			return;
		if (fadeThread != null && fadeThread.isAlive())
			fadeThread.interrupt();

		float startVol = getVolume(clip);
		fadeThread = new Thread(() -> {
			int steps = FADE_MS / FADE_TICK_MS;
			for (int i = 1; i <= steps; i++) {
				if (Thread.currentThread().isInterrupted())
					return;
				float progress = (float) i / steps;
				setVolume(clip, startVol * (1f - progress));
				try {
					Thread.sleep(FADE_TICK_MS);
				} catch (InterruptedException e) {
					return;
				}
			}
			clip.stop();
		});
		fadeThread.setDaemon(true);
		fadeThread.start();
	}

	// ── Volume helpers ─────────────────────────────────────────────────────

	/** Sets clip volume. vol: 0.0 (silent) → 1.0 (full). */
	private void setVolume(Clip clip, float vol) {
		if (clip == null)
			return;
		try {
			FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float min = fc.getMinimum();
			float max = fc.getMaximum();
			// Convert linear 0-1 to decibel range supported by this clip
			float db;
			if (vol <= 0.0001f) {
				db = min;
			} else {
				db = (float) (20.0 * Math.log10(vol));
			}
			fc.setValue(Math.max(min, Math.min(max, db)));
		} catch (IllegalArgumentException ignored) {
		}
	}

	private float getVolume(Clip clip) {
		if (clip == null)
			return 0f;
		try {
			FloatControl fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			// Convert dB back to linear
			return (float) Math.pow(10.0, fc.getValue() / 20.0);
		} catch (IllegalArgumentException e) {
			return masterVolume;
		}
	}

	public void setSFXVolume(float v) {
		sfxVolume = Math.max(0f, Math.min(1f, v));
	}
}