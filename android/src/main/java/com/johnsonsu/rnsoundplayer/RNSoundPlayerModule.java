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
	public void playSoundFile(final String id, String name, int numberOfLoops, String streamType, int volume, Promise promise) {
		try {
			final MediaPlayer player = pool.prepareSound(getReactApplicationContext(), id, name, StreamType.fromString(streamType), numberOfLoops, volume);
			player.setOnCompletionListener(new MediaPlayerPool.OnCompletionListener(pool, id) {
				@Override
				public void onComplete() {
					sendEventFinishedPlaying(id);
				}
			});
			player.setOnPreparedListener(new MediaPlayerPool.OnPreparedListener(true) {
				@Override
				public void onPrepared() {
					sendEventFinishedLoading(id);
				}
			});
			promise.resolve(null);
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	@ReactMethod
	public void loadSoundFile(final String id, String name, int numberOfLoops, String streamType, int volume, Promise promise) {
		try {
			MediaPlayer player = pool.prepareSound(getReactApplicationContext(), id, name, StreamType.fromString(streamType), numberOfLoops, volume);
			player.setOnCompletionListener(new MediaPlayerPool.OnCompletionListener(pool, id) {
				@Override
				public void onComplete() {
					sendEventFinishedPlaying(id);
				}
			});
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	@ReactMethod
	public void playUrl(final String id, final String url, String streamType, int volume, Promise promise) {
		try {
			final MediaPlayer player = pool.prepareSound(getReactApplicationContext(), id, url, StreamType.fromString(streamType), 0, volume);
			player.setOnCompletionListener(new MediaPlayerPool.OnCompletionListener(pool, id) {
				@Override
				public void onComplete() {
					sendEventFinishedPlaying(id);
				}
			});
			player.setOnPreparedListener(new MediaPlayerPool.OnPreparedListener(true) {
				@Override
				public void onPrepared() {
					sendEventFinishedLoadingUrl(id, url);
				}
			});
		} catch (Exception e) {
			promise.reject(e);
		}
	}

	@ReactMethod
	public void loadUrl(final String id, final String url, String streamType, int volume, Promise promise) {
		try {
			MediaPlayer player = pool.prepareSound(getReactApplicationContext(), id, url, StreamType.fromString(streamType), 0, volume);
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
	public void pause(final String id, Promise promise) {
		if (pool.pause(id)) {
			promise.resolve(true);
		} else {
			promise.resolve(false);
		}
	}

	@ReactMethod
	public void resume(final String id, Promise promise) {
		if (pool.resume(id)) {
			promise.resolve(true);
		} else {
			promise.resolve(false);
		}
	}

	@ReactMethod
	public void stop(final String id, Promise promise) {
		if (pool.stop(id)) {
			promise.resolve(true);
		} else {
			promise.resolve(false);
		}
	}

	@ReactMethod
	public void seek(final String id, float seconds, Promise promise) {
		if (pool.seek(id, seconds)) {
			promise.resolve(true);
		} else {
			promise.resolve(false);
		}
	}

	@ReactMethod
	public void setVolume(final String id, float volume) {
		pool.setVolume(id, volume);
	}

	@ReactMethod
	public void getInfo(final String id, Promise promise) {
		WritableMap map = Arguments.createMap();
		map.putDouble("currentTime", pool.getPosition(id));
		map.putDouble("duration", pool.getDuration(id));
		promise.resolve(map);
	}

	@ReactMethod
	public void stopAllSounds(Promise promise) {
		pool.stopAll();
		promise.resolve(null);
	}

	private void sendEventFinishedPlaying(final String id) {
		WritableMap params = Arguments.createMap();
		params.putBoolean("success", true);
		params.putString("id", id);
		getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FINISHED_PLAYING, params);
	}

	private void sendEventFinishedLoading(final String id) {
		WritableMap params = Arguments.createMap();
		params.putBoolean("success", true);
		params.putString("id", id);
		getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FINISHED_LOADING, params);
	}

	private void sendEventFinishedLoadingUrl(final String id, String url) {
		WritableMap params = Arguments.createMap();
		params.putBoolean("success", true);
		params.putString("url", url);
		params.putString("id", id);
		getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(EVENT_FINISHED_LOADING_URL, params);
	}

}
