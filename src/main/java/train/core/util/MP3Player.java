package train.core.util;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Plays an MP3 stream from the internet on its own thread (1.12 MP3Player with JLayer). The jukebox GUI asks for
 * .m3u / .pls radio links, which 1.12 handed to the decoder as they were: here a playlist is read first and its
 * first stream is played.
 */
public class MP3Player extends PlaybackListener implements Runnable {
	private final String streamURL;
	private volatile AdvancedPlayer player;
	private final Thread pThread;
	private volatile float volume = 0;
	private volatile boolean stopped = false;
	/** why it did not play, for the GUI; null while fine */
	public volatile String error;

	public MP3Player(String mp3url) {
		streamURL = mp3url;
		pThread = new Thread(this, "Traincraft jukebox");
		pThread.setDaemon(true);
		pThread.start();
	}

	@Override
	public void run() {
		try {
			String url = resolve(streamURL.trim(), 0);
			if (stopped) return;
			URLConnection c = URI.create(url).toURL().openConnection();
			c.setConnectTimeout(10000);
			c.setReadTimeout(20000);
			c.setRequestProperty("User-Agent", "Traincraft jukebox");
			player = new AdvancedPlayer(c.getInputStream());
			player.setVolume(volume);
			player.setPlayBackListener(this);
			if (stopped) {
				player.close();
				return;
			}
			player.play();
		} catch (Exception e) {
			error = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : ": " + e.getMessage());
		}
	}

	/** the stream a .m3u / .pls playlist points to, or the link itself */
	private static String resolve(String url, int depth) throws Exception {
		String lower = url.toLowerCase(Locale.ROOT);
		boolean playlist = lower.matches(".*\\.(m3u8?|pls)([?#].*)?$");
		if (!playlist || depth > 2) return url;
		URLConnection c = URI.create(url).toURL().openConnection();
		c.setConnectTimeout(10000);
		c.setReadTimeout(10000);
		c.setRequestProperty("User-Agent", "Traincraft jukebox");
		try (InputStream in = c.getInputStream();
			 BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String line;
			while ((line = r.readLine()) != null) {
				line = line.trim();
				//m3u: a plain line; pls: File1=...
				if (line.toLowerCase(Locale.ROOT).startsWith("file") && line.contains("=")) {
					line = line.substring(line.indexOf('=') + 1).trim();
				}
				if (line.startsWith("http://") || line.startsWith("https://")) {
					return resolve(line, depth + 1);
				}
			}
		}
		throw new IllegalArgumentException("no stream in the playlist");
	}

	public void stop() {
		stopped = true;
		AdvancedPlayer p = player;
		if (p != null) {
			p.close();
		}
	}

	@Override
	public void playbackStarted(PlaybackEvent evt) {}

	@Override
	public void playbackFinished(PlaybackEvent evt) {}

	public boolean isPlaying() {
		return pThread.isAlive();
	}

	/** 0..1, scaled by the game's record volume by the caller */
	public void setVolume(float f) {
		volume = f;
		AdvancedPlayer p = player;
		if (p != null) {
			p.setVolume(f);
		}
	}

	/** MP3 frames decoded so far */
	public int frames() {
		AdvancedPlayer p = player;
		return p == null ? 0 : p.frames;
	}

	public float getVolume() {
		return volume;
	}
}
