package com.johnsonsu.rnsoundplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

public class MediaPlayerPool {
	private static final String TAG = MediaPlayerPool.class.getSimpleName();

	private HashMap<String, MediaPlayer> players;
	private HashMap<String, Integer> loops;
	private HashMap<String, Integer> maxLoops;

	public MediaPlayerPool() {
		players = new HashMap<String, MediaPlayer>(8);
		loops = new HashMap<String, Integer>(8);
		maxLoops = new HashMap<String, Integer>(8);
	}

	public boolean pause(final String id) {
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


	public boolean resume(final String id) {
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


	public boolean stop(final String id) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			try {
				player.stop();
				player.release();
				players.remove(id);
				return true;
			} catch (IllegalStateException e) {
				return false;
			}
		}
		return false;
	}


	public boolean seek(final String id, float seconds) {
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


	void setVolume(final String id, float volume) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			player.setVolume(volume, volume);
		}
	}

	public double getPosition(final String id) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			return player.getCurrentPosition() / 1000.0;
		}
		return 0;
	}

	public double getDuration(final String id) {
		MediaPlayer player = players.get(id);
		if (player != null) {
			return player.getDuration() / 1000.0;
		}
		return 0;
	}

	public void stopAll() {
		for (MediaPlayer player : players.values()) {
			player.stop();
			player.release();
		}
		players.clear();
	}

	public MediaPlayer prepareSound(Context context, final String id, String uri, StreamType streamType, int numberOfLoops, float volume) throws IOException {
		// get instance
		MediaPlayer player;
		loops.put(id, 0);
		maxLoops.put(id, numberOfLoops);
		player = players.get(id);
		if (player == null) {
			Log.v(TAG, id + "=new player (" + players.size() + ")");
			player = new MediaPlayer();
			players.put(id, player);
		} else {
			Log.v(TAG, id + "=reset player (" + players.size() + ")");
			player.reset();
		}
		// set data sources
		if (isValidInternetUrl(uri)) {
			Log.v(TAG, id + "=set internet data source");
			player.setDataSource(uri);
		} else {
			// check if it's a file, or a raw identifier
			Log.v(TAG, id + "=set file descriptor");
			if (uri.contains(".")) {
				AssetFileDescriptor afd = context.getAssets().openFd(uri);
				player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			} else {
				player.setDataSource(context, Uri.parse("android.resource://" + context.getPackageName() + "/raw/" + uri));
			}
		}
		player.setVolume(volume, volume);
		this.applyStreamType(player, streamType);
		player.prepare();
		return player;
	}

	private void applyStreamType(MediaPlayer player, StreamType streamType) {
		switch (streamType) {
			case RINGTONE:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setLegacyStreamType(AudioManager.STREAM_RING).build();
					player.setAudioAttributes(audioAttributes);
				} else {
					player.setAudioStreamType(AudioManager.STREAM_RING);
				}
				break;
			case MEDIA:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setLegacyStreamType(AudioManager.STREAM_MUSIC).build();
					player.setAudioAttributes(audioAttributes);
				} else {
					player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				}
				break;
			case VOICE_CALL:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).setLegacyStreamType(AudioManager.STREAM_VOICE_CALL).build();
					player.setAudioAttributes(audioAttributes);
				} else {
					player.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
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

	public static abstract class OnPreparedListener implements MediaPlayer.OnPreparedListener {

		private final boolean playWhenPrepared;

		public OnPreparedListener(boolean playWhenPrepared) {
			this.playWhenPrepared = playWhenPrepared;
		}

		public abstract void onPrepared();

		@Override
		public void onPrepared(MediaPlayer mp) {
			if (playWhenPrepared) {
				Log.v(TAG,  "=play when prepared");
				mp.start();
			}
			onPrepared();
		}
	}

	public static abstract class OnCompletionListener implements MediaPlayer.OnCompletionListener {
		private final MediaPlayerPool pool;
		private final String id;

		OnCompletionListener(MediaPlayerPool pool, String id) {
			this.pool = pool;
			this.id = id;
		}

		public abstract void onComplete();

		@Override
		public void onCompletion(MediaPlayer mp) {
			// loop check
			int currentLoop = pool.loops.containsKey(id) ? pool.loops.get(id) : 0;
			int maxLoop = pool.maxLoops.containsKey(id) ? pool.maxLoops.get(id) : 0;

			Log.v(TAG, id + "=loop (" + currentLoop + ") of max (" + maxLoop + ")");
			if (maxLoop < 0 || maxLoop > 1) {
				if (maxLoop < 0 || currentLoop < maxLoop) {
					try {
						mp.seekTo(0);
						mp.start();
					} catch (IllegalStateException e) {
						mp.release();
						pool.players.remove(id);
					}
				} else {
					Log.v(TAG, id + "=released");
					mp.release();
					pool.players.remove(id);
				}
			} else {
				Log.v(TAG, id + "=released");
				mp.release();
				pool.players.remove(id);
			}
			pool.loops.put(id, ++currentLoop);
			onComplete();
		}
	}
}
