declare enum StreamType {
    RINGTONE = "RINGTONE",
    MEDIA = "MEDIA",
    VOICE_CALL = "VOICE_CALL"
}
declare const SoundPlayer: {
    playSoundFile: (id: string, name: string, numberOfLoops: number, streamType: StreamType, volume: number) => Promise<void>;
    playUrl: (id: string, url: string, streamType: StreamType, volume: number) => Promise<void>;
    loadSoundFile: (id: string, name: string, numberOfLoops: number, streamType: StreamType, volume: number) => Promise<void>;
    loadUrl: (id: string, url: string, streamType: StreamType, volume: number) => Promise<void>;
    onFinishedPlaying: (callback: (id: string, success: boolean) => any) => void;
    onFinishedLoading: (callback: (id: string, success: boolean) => any) => void;
    onFinishedLoadingURL: (callback: (id: string, success: boolean) => any) => void;
    pause: (id: string) => Promise<boolean>;
    resume: (id: string) => Promise<boolean>;
    stop: (id: string) => Promise<boolean>;
    stopAllSounds: () => Promise<void>;
    setVolume: (id: string, volume: number) => Promise<void>;
    seek: (id: string, seconds: number) => Promise<boolean>;
    getInfo: (id: string) => Promise<any>;
    unmount: () => void;
};
export { StreamType };
export default SoundPlayer;
