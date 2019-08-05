declare enum StreamType {
    RINGTONE = "RINGTONE",
    MEDIA = "MEDIA"
}
declare const SoundPlayer: {
    playSoundFile: (name: string, type: string, numberOfLoops: number, streamType: StreamType) => Promise<void>;
    playUrl: (url: string, streamType: StreamType) => Promise<void>;
    loadSoundFile: (name: string, type: string, numberOfLoops: number, streamType: StreamType) => Promise<void>;
    loadUrl: (url: string, streamType: StreamType) => Promise<void>;
    onFinishedPlaying: (callback: (success: boolean) => any) => void;
    pause: () => Promise<boolean>;
    resume: () => Promise<boolean>;
    stop: () => Promise<boolean>;
    seek: (seconds: number) => Promise<boolean>;
    getInfo: () => Promise<any>;
    unmount: () => void;
};
export { StreamType };
export default SoundPlayer;
