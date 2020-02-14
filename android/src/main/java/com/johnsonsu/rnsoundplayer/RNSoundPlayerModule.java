package com.johnsonsu.rnsoundplayer;

import android.media.MediaPlayer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNSoundPlayerModule extends ReactContextBaseJavaModule {

	public final static String EVENT_FINISHED_PLAYING = "FinishedPlaying";
	public final static String EVENT_FINISHED_LOADING = "FinishedLoading";
	public final static String EVENT_FINISHED_LOADING_URL = "FinishedLoadingURL";

	private final MediaPlayerPool pool;

	public RNSoundPlayerModule(ReactApplicationContext reactContext) {
		super(reactContext);
		this.pool = new MediaPlayerPool();
	}

	@Override
	public String getName() {
		return "RNSoundPlayer";
	}

	@ReactMethod
	public void playSoundFile(final int id, String name, int numberOfLoops, String streamType, Promise promise) {
		try {
			final MediaPlayer player = pool.loadSound(getReactApplicationContext(), id, name, StreamType.fromString(streamType), numberOfLoops);
			player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					player.start();
					sendEventFinishedLoading(id);
				}
			});
			player.setOnCompletionListener(new MediaPlayerPool.OnCompletionListener(pool, id) {
				@Override
				public void onCompletion() {
					sendEventFinishedPlaying(id);
				}
			});
			promise.resolve(null);
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	@ReactMethod
	public void loadSoundFile(final int id, String name, int numberOfLoops, String streamType, Promise promise) {
		try {
			MediaPlayer player = pool.loadSound(getReactApplicationContext(), id, name, StreamType.fromString(streamType), numberOfLoops);
			player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					sendEventFinishedLoading(id);
				}
			});
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	@ReactMethod
	public void playUrl(final int id, final String url, String streamType, Promise promise) {
		try {
			final MediaPlayer player = pool.loadSound(getReactApplicationContext(), id, url, StreamType.fromString(streamType), 0);
			player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					player.start();
					sendEventFinishedLoadingUrl(id, url);
				}
			});
			player.setOnCompletionListener(new MediaPlayerPool.OnCompletionListener(pool, id) {
				@Override
				public void onCompletion() {
					sendEventFinishedPlaying(id);
				}
			});
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	@ReactMethod
	public void loadUrl(final int id, final String url, String streamType, Promise promise) {
		try {
			MediaPlayer player = pool.loadSound(getReactApplicationContext(), id, url, StreamType.fromString(streamType), 0);
			player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					sendEventFinishedLoadingUrl(id, url);
				}
			});
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	@ReactMethod
	public void pause(int id, Promise promise) {
		if (pool.pause(id)) {
			promise.resolve(true);
		} else {
			promise.resolve(false);
		}
	}

	@ReactMethod
	public void resume(int id, Promise promise) {
		if (pool.resume(id)) {
			promise.resolve(true);
		} else {
			promise.resolve(false);
		}
	}

	@ReactMethod
	public void stop(int id, Promise promise) {
		if (pool.stop(id)) {
			promise.resolve(true);
		} else {
			promise.resolve(false);
		}
	}

	@ReactMethod
	public void seek(int id, float seconds, Promise promise) {
		if (pool.seek(id, seconds)) {
			promise.resolve(true);
		} else {
			promise.resolve(false);
		}
	}

	@ReactMethod
	public void setVolume(int id, float volume) {
		pool.setVolume(id, volume);
	}

	@ReactMethod
	public void getInfo(int id, Promise promise) {
		WritableMap map = Arguments.createMap();
		map.putDouble("currentTime", pool.getPosition(id));
		map.putDouble("duration", pool.getDuration(id));
		promise.resolve(map);
	}

	private void sendEventFinishedPlaying(int id) {
		WritableMap params = Arguments.createMap();
		params.putBoolean("success", true);
		params.putInt("id", id);
		getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FINISHED_PLAYING, params);
	}

	private void sendEventFinishedLoading(int id) {
		WritableMap params = Arguments.createMap();
		params.putBoolean("success", true);
		params.putInt("id", id);
		getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FINISHED_LOADING, params);
	}

	private void sendEventFinishedLoadingUrl(int id, String url) {
		WritableMap params = Arguments.createMap();
		params.putBoolean("success", true);
		params.putString("url", url);
		params.putInt("id", id);
		getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FINISHED_LOADING_URL, params);
	}

}
