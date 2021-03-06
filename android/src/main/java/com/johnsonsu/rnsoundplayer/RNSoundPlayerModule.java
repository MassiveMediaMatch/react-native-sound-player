package com.johnsonsu.rnsoundplayer;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import java.io.File;

import java.io.IOException;
import javax.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.content.res.AssetFileDescriptor;

enum StreamType {
  RINGTONE("RINGTONE"),
  MEDIA("MEDIA");

   private String streamType;

   StreamType(String streamType) {
    this.streamType = streamType;
  }

   public static StreamType fromString(String text) {
    for (StreamType b : StreamType.values()) {
      if (b.streamType.equalsIgnoreCase(text)) {
        return b;
      }
    }
    return null;
  }
}

public class RNSoundPlayerModule extends ReactContextBaseJavaModule {

  public final static String EVENT_FINISHED_PLAYING = "FinishedPlaying";
  public final static String EVENT_FINISHED_LOADING = "FinishedLoading";
  public final static String EVENT_FINISHED_LOADING_FILE = "FinishedLoadingFile";
  public final static String EVENT_FINISHED_LOADING_URL = "FinishedLoadingURL";

  private final ReactApplicationContext reactContext;
  private MediaPlayer mediaPlayer;
  private int numberOfPlays;

  public RNSoundPlayerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNSoundPlayer";
  }

  @ReactMethod
  public void playSoundFile(String name, String type, int numberOfLoops, String streamType, Promise promise) {
    mountSoundFile(name, type, numberOfLoops, streamType, true, promise);
  }

  @ReactMethod
  public void loadSoundFile(String name, String type, int numberOfLoops, String streamType, Promise promise) {
    mountSoundFile(name, type, numberOfLoops, streamType, false, promise);
  }

  @ReactMethod
  public void playUrl(String url, String streamType, Promise promise) {
    prepareUrl(url, streamType, true, promise);
  }

  @ReactMethod
  public void loadUrl(String url, String streamType, Promise promise) {
    prepareUrl(url, streamType, false, promise);
  }

  @ReactMethod
  public void pause(Promise promise) {
    if (this.mediaPlayer != null) {
      try {
        this.mediaPlayer.pause();
        promise.resolve(true);
      } catch (IllegalStateException e) {
        promise.reject(e);
      }
    } else {
      promise.resolve(false);
    }
  }

  @ReactMethod
  public void resume(Promise promise) {
    if (this.mediaPlayer != null) {
      try {
        this.mediaPlayer.start();
        promise.resolve(true);
      } catch (IllegalStateException e) {
        promise.reject(e);
      }
    } else {
      promise.resolve(false);
    }
  }

  @ReactMethod
  public void stop(Promise promise) {
    if (this.mediaPlayer != null){
      try {
        this.mediaPlayer.stop();
        promise.resolve(true);
      } catch (IllegalStateException e) {
        promise.reject(e);
      }
    } else {
      promise.resolve(false);
    }
  }

  @ReactMethod
  public void seek(float seconds, Promise promise) {
    if (this.mediaPlayer != null){
      try {
        this.mediaPlayer.seekTo((int)seconds * 1000);
        promise.resolve(true);
      } catch (IllegalStateException e) {
        promise.reject(e);
      }
    } else {
      promise.resolve(false);
    }
  }

  @ReactMethod
  public void setVolume(float volume) throws IOException {
    if (this.mediaPlayer != null) {
      this.mediaPlayer.setVolume(volume, volume);
    }
  }

  @ReactMethod
  public void getInfo(
      Promise promise) {
    WritableMap map = Arguments.createMap();
    map.putDouble("currentTime", this.mediaPlayer.getCurrentPosition() / 1000.0);
    map.putDouble("duration", this.mediaPlayer.getDuration() / 1000.0);
    promise.resolve(map);
  }

  private void sendEvent(ReactApplicationContext reactContext,
                       String eventName,
                       @Nullable WritableMap params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  private void mountSoundFile(String name, String type, final int numberOfLoops, String streamType, final boolean playFile, Promise promise) {
    try {
      this.numberOfPlays = 0;
      
      if (this.mediaPlayer == null) {
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(
          new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
              WritableMap params = Arguments.createMap();
              params.putBoolean("success", true);
              sendEvent(getReactApplicationContext(), EVENT_FINISHED_PLAYING, params);

              numberOfPlays += 1;
              if (mediaPlayer.isLooping() && numberOfPlays < numberOfLoops) {
                mediaPlayer.setLooping(false);
              }
            }
        });
      } else {
        this.mediaPlayer.reset();
      }

      this.applyStreamType(StreamType.fromString(streamType));
      if (name.contains("/assets/")) {
        String fileName = name.replace("/assets/", "") + '.' + type;
        AssetFileDescriptor afd = getReactApplicationContext().getAssets().openFd(fileName);
        this.mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
      } else {
        Uri uri = Uri.parse("android.resource://" + getReactApplicationContext().getPackageName() + "/raw/" + name);
        this.mediaPlayer.setDataSource(getCurrentActivity(), uri);
      }

      if (numberOfLoops < 0) {
        this.mediaPlayer.setLooping(true); // infinite
      } else if (numberOfLoops > 1) {
        this.mediaPlayer.setLooping(true);
      }

      this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
          if (playFile) {
            RNSoundPlayerModule.this.mediaPlayer.start();
          }
        }
      });
      this.mediaPlayer.prepareAsync();

      WritableMap params = Arguments.createMap();
      params.putBoolean("success", true);
      sendEvent(getReactApplicationContext(), EVENT_FINISHED_LOADING, params);
      WritableMap onFinishedLoadingFileParams = Arguments.createMap();
      onFinishedLoadingFileParams.putBoolean("success", true);
      onFinishedLoadingFileParams.putString("name", name);
      onFinishedLoadingFileParams.putString("type", type);
      sendEvent(getReactApplicationContext(), EVENT_FINISHED_LOADING_FILE, onFinishedLoadingFileParams);
      promise.resolve(null);
    } catch (IOException e) {
      promise.reject(e);
    } catch (IllegalArgumentException e) {
      promise.reject(e);
    } catch (SecurityException e) {
      promise.reject(e);
    } catch (IllegalStateException e) {
      promise.reject(e);
    } catch (NullPointerException e) {
      promise.reject(e);
    }
  }

  private Uri getUriFromFile(String name, String type) {
    String folder = getReactApplicationContext().getFilesDir().getAbsolutePath();
    String file = name + "." + type;

    // http://blog.weston-fl.com/android-mediaplayer-prepare-throws-status0x1-error1-2147483648
    // this helps avoid a common error state when mounting the file
    File ref = new File(folder + "/" + file);

    if (ref.exists()) {
      ref.setReadable(true, false);
    }

    return Uri.parse("file://" + folder + "/" + file);
  }

  private void prepareUrl(String url, String streamType, final boolean playFile, Promise promise) {
    try {
      if (this.mediaPlayer == null) {
        Uri uri = Uri.parse(url);
        this.mediaPlayer = MediaPlayer.create(getCurrentActivity(), uri);
        this.mediaPlayer.setOnCompletionListener(
          new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
              WritableMap params = Arguments.createMap();
              params.putBoolean("success", true);
              sendEvent(getReactApplicationContext(), EVENT_FINISHED_PLAYING, params);
            }
        });
      } else {
        Uri uri = Uri.parse(url);
        this.mediaPlayer.reset();
        this.mediaPlayer.setDataSource(getCurrentActivity(), uri);
        this.applyStreamType(StreamType.fromString(streamType));
      }

      this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
          if (playFile) {
            RNSoundPlayerModule.this.mediaPlayer.start();
          }
        }
      });
      this.mediaPlayer.prepareAsync();

      WritableMap params = Arguments.createMap();
      params.putBoolean("success", true);
      sendEvent(getReactApplicationContext(), EVENT_FINISHED_LOADING, params);
      WritableMap onFinshedLoadingURLParams = Arguments.createMap();
      onFinshedLoadingURLParams.putBoolean("success", true);
      onFinshedLoadingURLParams.putString("url", url);
      sendEvent(getReactApplicationContext(), EVENT_FINISHED_LOADING_URL, onFinshedLoadingURLParams);
      promise.resolve(null);
    } catch (IOException e) {
      promise.reject(e);
    } catch (IllegalArgumentException e) {
      promise.reject(e);
    } catch (SecurityException e) {
      promise.reject(e);
    } catch (IllegalStateException e) {
      promise.reject(e);
    } catch (NullPointerException e) {
      promise.reject(e);
    }
  }

  private void applyStreamType (StreamType streamType) {
     switch (streamType) {
      case RINGTONE:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build();
          this.mediaPlayer.setAudioAttributes(audioAttributes);
        } else {
          this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        }
        break;

       case MEDIA:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build();
          this.mediaPlayer.setAudioAttributes(audioAttributes);
        } else {
          this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        break;
    }

   }
}
