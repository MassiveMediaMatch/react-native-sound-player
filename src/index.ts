import { NativeModules, NativeEventEmitter, EmitterSubscription } from 'react-native'

enum StreamType {
	RINGTONE = 'RINGTONE',
	MEDIA = 'MEDIA',
	VOICE_CALL = 'VOICE_CALL',
}

const { RNSoundPlayer } = NativeModules

const _soundPlayerEmitter: NativeEventEmitter = new NativeEventEmitter(RNSoundPlayer)
let _finishedPlayingListener: EmitterSubscription | null = null

const SoundPlayer = {
	playSoundFile: async (id: string, name: string, numberOfLoops: number, streamType: StreamType, volume: number): Promise<void> => {
		return RNSoundPlayer.playSoundFile(id, name, numberOfLoops, streamType, volume)
	},

	playUrl: async (id: string, url: string, streamType: StreamType, volume: number): Promise<void> => {
		return RNSoundPlayer.playUrl(id, url, streamType, volume)
	},

	loadSoundFile: async (id: string, name: string, numberOfLoops: number, streamType: StreamType, volume: number): Promise<void> => {
		return RNSoundPlayer.loadSoundFile(id, name, numberOfLoops, streamType, volume)
	},

	loadUrl: async (id: string, url: string, streamType: StreamType, volume: number): Promise<void> => {
		return RNSoundPlayer.loadUrl(id, url, streamType, volume)
	},

	onFinishedPlaying: (callback: (id: string, success: boolean) => any): void => {
		_finishedPlayingListener = _soundPlayerEmitter.addListener(
		  'FinishedPlaying',
		  callback,
		)
	},

	onFinishedLoading: (callback: (id: string, success: boolean) => any): void => {
		_finishedPlayingListener = _soundPlayerEmitter.addListener(
		  'FinishedLoading',
		  callback,
		)
	},

	onFinishedLoadingURL: (callback: (id: string, success: boolean) => any): void => {
		_finishedPlayingListener = _soundPlayerEmitter.addListener(
		  'FinishedLoadingURL',
		  callback,
		)
	},

	pause: async (id: string): Promise<boolean> => {
		return RNSoundPlayer.pause(id)
	},

	resume: async (id: string): Promise<boolean> => {
		return RNSoundPlayer.resume(id)
	},

	stop: async (id: string): Promise<boolean> => {
		return RNSoundPlayer.stop(id)
	},

	stopAllSounds: async (): Promise<void> => {
		return RNSoundPlayer.stopAllSounds()
	},

	setVolume: async (id: string, volume: number): Promise<void> => {
        return RNSoundPlayer.stopAllSounds()
    },

	seek: async (id: string, seconds: number): Promise<boolean> => {
		return RNSoundPlayer.seek(id, seconds)
	},

	getInfo: async (id: string): Promise<any> => RNSoundPlayer.getInfo(id),

	unmount: (): void => {
		_finishedPlayingListener && _finishedPlayingListener.remove()
	},
}

export { StreamType }
export default SoundPlayer
