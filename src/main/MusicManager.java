package main;

import java.io.File;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * High-performance software mixer with Lock-Free Queueing and SFX De-duplication.
 * Engineered to handle extreme sound effect spam without dropping framerates or glitching.
 */
public class MusicManager {

	public enum Track {
		BGM, BOSS, NONE
	}

	public enum SFX {
		LEVELUP("Audio/Effects/LevelUp.wav"), 
		COINPICKUP("Audio/Effects/CoinPickUp.wav"),
		DEATH("Audio/Effects/GameOver.wav"),
		OnEnemyHit("Audio/Effects/hitHurt.wav"), 
		OnPlayerHit("Audio/Effects/onPlayerHit.wav"),
		PickUp("Audio/Effects/power_up.wav");

		private final String path;

		SFX(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}

	private static class SoundInstance {
		byte[] data;
		float volume;
		int position = 0;
		boolean active = false;

		void setup(byte[] data, float volume) {
			this.data = data;
			this.volume = volume;
			this.position = 0;
			this.active = true;
		}
	}

	private static class MusicInstance {
		final byte[] data;
		int position = 0;

		MusicInstance(byte[] data) {
			this.data = data;
		}
	}

	// ── Config ─────────────────────────────────────────────────────────────
	private static final String BGM_PATH = "Audio/Music/bgm.wav";
	private static final String BOSS_PATH = "Audio/Music/boss.wav";
	
	private static final AudioFormat FORMAT = new AudioFormat(44100f, 16, 2, true, false);
	private static final int FADE_MS = 2000;
	
	private static final int MAX_SFX_VOICES = 10; 
	// Max number of the EXACT same SFX allowed to play in a single 11.6ms mixing frame
	private static final int MAX_DUPLICATES_PER_FRAME = 3; 

	private float masterVolume = 0.5f;
	private float sfxVolume = 1.0f;

	// ── State ──────────────────────────────────────────────────────────────
	private Track currentTrack = Track.NONE;
	private SourceDataLine line;
	private final Map<SFX, byte[]> sfxDataMap = new EnumMap<>(SFX.class);
	
	private final SoundInstance[] sfxPool = new SoundInstance[MAX_SFX_VOICES];
	
	// Lock-free queue to pass play requests from game thread to mixer thread instantly
	private final ConcurrentLinkedQueue<SFX> sfxQueue = new ConcurrentLinkedQueue<>();
	// Tracks duplicates inside a single mixing frame
	private final Map<SFX, Integer> frameDuplicateCounter = new EnumMap<>(SFX.class);
	
	private MusicInstance bgmNode;
	private MusicInstance bossNode;
	
	private float currentBgmVol = 0f;
	private float currentBossVol = 0f;
	private float targetBgmVol = 0f;
	private float targetBossVol = 0f;
	
	private volatile boolean running = true;

	// ── Init ───────────────────────────────────────────────────────────────

	public MusicManager() {
		for (int i = 0; i < MAX_SFX_VOICES; i++) {
			sfxPool[i] = new SoundInstance();
		}

		byte[] bgmBytes = loadAudioBytes(BGM_PATH);
		byte[] bossBytes = loadAudioBytes(BOSS_PATH);
		
		if (bgmBytes != null) bgmNode = new MusicInstance(bgmBytes);
		if (bossBytes != null) bossNode = new MusicInstance(bossBytes);

		for (SFX sfx : SFX.values()) {
			byte[] sfxBytes = loadAudioBytes(sfx.getPath());
			if (sfxBytes != null) {
				sfxDataMap.put(sfx, sfxBytes);
			}
		}

		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(FORMAT, 4096); 
			line.start();
		} catch (LineUnavailableException e) {
			System.err.println("[MusicManager] Critical error initialization failed!");
			return;
		}

		Thread mixerThread = new Thread(this::mixerLoop, "SoftwareAudioMixer");
		mixerThread.setDaemon(true);
		mixerThread.start();
	}

	/**
	 * Ultra-fast, lock-free submission. Calling this 10,000 times a frame
	 * will not stall your game loop at all.
	 */
	public void playSFX(SFX sfx) {
		if (sfx != null) {
			sfxQueue.add(sfx);
		}
	}

	private byte[] loadAudioBytes(String path) {
		File file = new File(path);
		if (!file.exists()) {
			System.err.println("[MusicManager] File not found: " + file.getAbsolutePath());
			return null;
		}
		try (AudioInputStream sourceStream = AudioSystem.getAudioInputStream(file)) {
			AudioFormat sourceFormat = sourceStream.getFormat();
			if (!sourceFormat.matches(FORMAT)) {
				try (AudioInputStream convertedStream = AudioSystem.getAudioInputStream(FORMAT, sourceStream)) {
					return convertedStream.readAllBytes();
				}
			} else {
				return sourceStream.readAllBytes();
			}
		} catch (Exception e) {
			System.err.println("[MusicManager] Failed to load: " + path);
			return null;
		}
	}

	// ── Software Mixer Engine Loop ──────────────────────────────────────────

	private void mixerLoop() {
		byte[] mixerBuffer = new byte[2048];
		int framesToMix = mixerBuffer.length / 4; 
		int[] mixBufferInt = new int[framesToMix * 2];

		float fadeStep = (float) framesToMix / (FORMAT.getSampleRate() * (FADE_MS / 1000f));

		while (running) {
			Arrays.fill(mixBufferInt, 0);
			frameDuplicateCounter.clear();

			// 1. Process the incoming lock-free queue and assign to voices
			SFX incomingSfx;
			while ((incomingSfx = sfxQueue.poll()) != null) {
				// De-duplication check: limit identical sounds in this 11.6ms window
				int count = frameDuplicateCounter.getOrDefault(incomingSfx, 0);
				if (count >= MAX_DUPLICATES_PER_FRAME) {
					continue; // Discard excessive duplicate spam
				}
				frameDuplicateCounter.put(incomingSfx, count + 1);

				byte[] data = sfxDataMap.get(incomingSfx);
				if (data == null) continue;

				float volume = sfxVolume;
				if (incomingSfx == SFX.OnEnemyHit || incomingSfx == SFX.OnPlayerHit) {
					volume *= 0.1f; 
				}

				// Find available voice slot or drop if completely full
				// (Dropping is cleaner than stealing when dealing with massive spam)
				for (SoundInstance si : sfxPool) {
					if (!si.active) {
						si.setup(data, volume);
						break;
					}
				}
			}

			// 2. Handle Music Crossfades
			if (currentBgmVol < targetBgmVol) currentBgmVol = Math.min(targetBgmVol, currentBgmVol + fadeStep);
			else if (currentBgmVol > targetBgmVol) currentBgmVol = Math.max(targetBgmVol, currentBgmVol - fadeStep);

			if (currentBossVol < targetBossVol) currentBossVol = Math.min(targetBossVol, currentBossVol + fadeStep);
			else if (currentBossVol > targetBossVol) currentBossVol = Math.max(targetBossVol, currentBossVol - fadeStep);

			if (bgmNode != null && currentBgmVol > 0.001f) {
				mixMusic(bgmNode, currentBgmVol * masterVolume, mixBufferInt);
			}
			if (bossNode != null && currentBossVol > 0.001f) {
				mixMusic(bossNode, currentBossVol * masterVolume, mixBufferInt);
			}

			// 3. Mix Active Channels
			int activeCount = 0;
			for (SoundInstance si : sfxPool) {
				if (!si.active) continue;
				activeCount++;
				
				byte[] data = si.data;
				float vol = si.volume;

				for (int i = 0; i < mixBufferInt.length; i += 2) {
					if (si.position + 3 >= data.length) {
						si.active = false;
						break;
					}

					int left = (data[si.position + 1] << 8) | (data[si.position] & 0xFF);
					int right = (data[si.position + 3] << 8) | (data[si.position + 2] & 0xFF);

					mixBufferInt[i] += (int) (left * vol);
					mixBufferInt[i + 1] += (int) (right * vol);

					si.position += 4;
				}
			}

			// 4. Dynamic Headroom Compression
			float headroomScaler = 1.0f;
			if (activeCount > 2) {
				headroomScaler = 1.8f / (float) Math.sqrt(activeCount);
			}

			// 5. Mathematical Clamping and Formatting
			for (int i = 0; i < mixBufferInt.length; i += 2) {
				int left = (int) (mixBufferInt[i] * headroomScaler);
				int right = (int) (mixBufferInt[i + 1] * headroomScaler);

				if (left > 32767) left = 32767;
				else if (left < -32768) left = -32768;

				if (right > 32767) right = 32767;
				else if (right < -32768) right = -32768;

				int byteIdx = i * 2;
				mixerBuffer[byteIdx] = (byte) (left & 0xFF);
				mixerBuffer[byteIdx + 1] = (byte) ((left >> 8) & 0xFF);
				mixerBuffer[byteIdx + 2] = (byte) (right & 0xFF);
				mixerBuffer[byteIdx + 3] = (byte) ((right >> 8) & 0xFF);
			}

			line.write(mixerBuffer, 0, mixerBuffer.length);
		}
		line.close();
	}

	private void mixMusic(MusicInstance node, float vol, int[] mixBufferInt) {
		byte[] data = node.data;
		for (int i = 0; i < mixBufferInt.length; i += 2) {
			if (node.position + 3 >= data.length) {
				node.position = 0;
			}

			int left = (data[node.position + 1] << 8) | (data[node.position] & 0xFF);
			int right = (data[node.position + 3] << 8) | (data[node.position + 2] & 0xFF);

			mixBufferInt[i] += (int) (left * vol);
			mixBufferInt[i + 1] += (int) (right * vol);

			node.position += 4;
		}
	}

	// ── Public API ─────────────────────────────────────────────────────────

	public void playBGM() {
		currentTrack = Track.BGM;
		targetBgmVol = 1.0f;
		targetBossVol = 0.0f;
	}

	public void playBossMusic() {
		currentTrack = Track.BOSS;
		targetBgmVol = 0.0f;
		targetBossVol = 1.0f;
	}

	public void stopAll() {
		currentTrack = Track.NONE;
		targetBgmVol = 0.0f;
		targetBossVol = 0.0f;
	}

	public void setMasterVolume(float v) {
		masterVolume = Math.max(0f, Math.min(1f, v));
	}

	public void setSFXVolume(float v) {
		sfxVolume = Math.max(0f, Math.min(1f, v));
	}
	
	public void shutdown() {
		this.running = false;
	}
}
