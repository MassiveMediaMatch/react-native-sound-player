declare enum StreamType {
    RINGTONE = "RINGTONE",
    MEDIA = "MEDIA"
}
declare const SoundPlayer: {
    playSoundFile: (id: number, name: string, numberOfLoops: number, streamType: StreamType) => Promise<void>;
    playUrl: (id: number, url: string, streamType: StreamType) => Promise<void>;
    loadSoundFile: (id: number, name: string, type: string, numberOfLoops: number, streamType: StreamType) => Promise<void>;
    loadUrl: (id: number, url: string, streamType: StreamType) => Promise<void>;
    onFinishedPlaying: (callback: (success: boolean) => any) => void;
    onFinishedLoading: (callback: (success: boolean) => any) => void;
    onFinishedLoadingURL: (callback: (success: boolean) => any) => void;
    pause: (id: number) => Promise<boolean>;
    resume: (id: number) => Promise<boolean>;
    stop: (id: number) => Promise<boolean>;
    seek: (id: number, seconds: number) => Promise<boolean>;
    getInfo: (id: number) => Promise<any>;
    unmount: () => void;
};
export { StreamType };
export default SoundPlayer;
