import { NativeModules, NativeEventEmitter, EmitterSubscription } from 'react-native'

enum StreamType {
	RINGTONE = 'RINGTONE',
	MEDIA = 'MEDIA',
}

const { RNSoundPlayer } = NativeModules

const _soundPlayerEmitter: NativeEventEmitter = new NativeEventEmitter(RNSoundPlayer)
let _finishedPlayingListener: EmitterSubscription | null = null

const SoundPlayer = {
	playSoundFile: async (name: string, type: string, numberOfLoops: number, streamType: StreamType): Promise<void> => {
		return RNSoundPlayer.playSoundFile(name, type, numberOfLoops, streamType)
	},
	
	playUrl: async (url: string, streamType: StreamType): Promise<void> => {
		return RNSoundPlayer.playUrl(url, streamType)
	},

	loadSoundFile: async (name: string, type: string, numberOfLoops: number, streamType: StreamType): Promise<void> => {
		return RNSoundPlayer.loadSoundFile(name, type, numberOfLoops, streamType)
	},
	
	loadUrl: async (url: string, streamType: StreamType): Promise<void> => {
		return RNSoundPlayer.loadUrl(url, streamType)
	},
	
	onFinishedPlaying: (callback: (success: boolean) => any) => {
		_finishedPlayingListener = _soundPlayerEmitter.addListener(
		  'FinishedPlaying',
		  callback
		)
	},
	
	pause: async (): Promise<boolean> => {
		return RNSoundPlayer.pause()
	},

	resume: async (): Promise<boolean> => {
		return RNSoundPlayer.resume()
	},

	stop: async (): Promise<boolean> => {
		return RNSoundPlayer.stop()
	},

	getInfo: async () => RNSoundPlayer.getInfo(),

	unmount: () => {
		_finishedPlayingListener && _finishedPlayingListener.remove()
	}
}

export { StreamType }
export default SoundPlayer