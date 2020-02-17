import { NativeModules, NativeEventEmitter, EmitterSubscription } from 'react-native'

enum StreamType {
	RINGTONE = 'RINGTONE',
	MEDIA = 'MEDIA',
}

const { RNSoundPlayer } = NativeModules

const _soundPlayerEmitter: NativeEventEmitter = new NativeEventEmitter(RNSoundPlayer)
let _finishedPlayingListener: EmitterSubscription | null = null

const SoundPlayer = {
	playSoundFile: async (id: number, name: string, numberOfLoops: number, streamType: StreamType): Promise<void> => {
		return RNSoundPlayer.playSoundFile(id, name, numberOfLoops, streamType)
	},

	playUrl: async (id: number, url: string, streamType: StreamType): Promise<void> => {
		return RNSoundPlayer.playUrl(id, url, streamType)
	},

	loadSoundFile: async (id: number, name: string, type: string, numberOfLoops: number, streamType: StreamType): Promise<void> => {
		return RNSoundPlayer.loadSoundFile(id, name, type, numberOfLoops, streamType)
	},

	loadUrl: async (id: number, url: string, streamType: StreamType): Promise<void> => {
		return RNSoundPlayer.loadUrl(id, url, streamType)
	},

	onFinishedPlaying: (callback: (success: boolean) => any) => {
		_finishedPlayingListener = _soundPlayerEmitter.addListener(
			'FinishedPlaying',
			callback
		)
	},

	onFinishedLoading: (callback: (success: boolean) => any) => {
		_finishedPlayingListener = _soundPlayerEmitter.addListener(
			'FinishedLoading',
			callback
		)
	},

	onFinishedLoadingURL: (callback: (success: boolean) => any) => {
		_finishedPlayingListener = _soundPlayerEmitter.addListener(
			'FinishedLoadingURL',
			callback
		)
	},

	pause: async (id: number): Promise<boolean> => {
		return RNSoundPlayer.pause(id)
	},

	resume: async (id: number): Promise<boolean> => {
		return RNSoundPlayer.resume(id)
	},

	stop: async (id: number): Promise<boolean> => {
		return RNSoundPlayer.stop(id)
	},

	seek: async (id: number, seconds: number): Promise<boolean> => {
		return RNSoundPlayer.seek(id, seconds)
	},

	getInfo: async (id: number) => RNSoundPlayer.getInfo(id),

	unmount: () => {
		_finishedPlayingListener && _finishedPlayingListener.remove()
	}
}

export { StreamType }
export default SoundPlayer