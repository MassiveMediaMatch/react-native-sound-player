package com.johnsonsu.rnsoundplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.SparseArray;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class MediaPlayerPool {
	private SparseArray<MediaPlayer> players;
	private ArrayList<Integer> loops;
	private ArrayList<Integer> maxLoops;

	public MediaPlayerPool() {
		players = new SparseArray<>(8);
		loops = new ArrayList<>(8);
		maxLoops = new ArrayList<>(8);
	}

	public boolean pause(final int id) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			try {
				player.pause();
				return true;
			} catch (IllegalStateException e) {
				return false;
			}
		}
		return false;
	}


	public boolean resume(final int id) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			try {
				player.start();
				return true;
			} catch (IllegalStateException e) {
				return false;
			}
		}
		return false;
	}


	public boolean stop(final int id) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			try {
				player.stop();
				return true;
			} catch (IllegalStateException e) {
				return false;
			}
		}
		return false;
	}


	public boolean seek(final int id, float seconds) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			try {
				player.seekTo((int) seconds * 1000);
				return true;
			} catch (IllegalStateException e) {
				return false;
			}
		}
		return false;
	}


	void setVolume(final int id, float volume) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			player.setVolume(volume, volume);
		}
	}

	public double getPosition(final int id) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			return player.getCurrentPosition() / 1000.0;
		}
		return 0;
	}

	public double getDuration(final int id) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			return player.getDuration() / 1000.0;
		}
		return 0;
	}

	public MediaPlayer loadSound(Context context, final int id, String uri, StreamType streamType, int numberOfLoops) throws IOException {
		// get instance
		MediaPlayer player;
		loops.add(id, 0);
		maxLoops.add(id, numberOfLoops);
		player = players.get(id);
		if (player == null) {
			player = new MediaPlayer();
		} else {
			player.reset();
		}
		// set data sources
		if (isValidInternetUrl(uri)) {
			player.setDataSource(context, Uri.parse(uri));
		} else {
			// check if it's a file, or a raw identifier
			if (uri.contains(".")) {
				AssetFileDescriptor afd = context.getAssets().openFd(uri);
				player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			} else {
				player.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + uri));
			}
		}
		this.applyStreamType(player, streamType);

		if (numberOfLoops < 0 || numberOfLoops > 1) {
			player.setLooping(true); // infinite or a number of loops
		}

		player.prepareAsync();

		return player;
	}

	private void applyStreamType(MediaPlayer player, StreamType streamType) {
		switch (streamType) {
			case RINGTONE:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build();
					player.setAudioAttributes(audioAttributes);
				} else {
					player.setAudioStreamType(AudioManager.STREAM_RING);
				}
				break;

			case MEDIA:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build();
					player.setAudioAttributes(audioAttributes);
				} else {
					player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				}
				break;
		}

	}

	private boolean isValidInternetUrl(String url) {
		try {
			URL obj = new URL(url);
			obj.toURI();
			return true;
		} catch (MalformedURLException e) {
			return false;
		} catch (URISyntaxException e) {
			return false;
		}
	}

	public static abstract class OnCompletionListener implements MediaPlayer.OnCompletionListener {
		private final MediaPlayerPool pool;
		private final int id;

		OnCompletionListener(MediaPlayerPool pool, int id) {
			this.pool = pool;
			this.id = id;
		}

		public abstract void onCompletion();

		@Override
		public void onCompletion(MediaPlayer mp) {
			// loop check
			int currentLoop = pool.loops.get(id);
			int maxLoop = pool.maxLoops.get(id);
			MediaPlayer pl = pool.players.get(id);

			currentLoop += 1;
			if (pl.isLooping() && currentLoop < maxLoop) {
				pl.setLooping(false);
			}
			onCompletion();
		}
	}
}
