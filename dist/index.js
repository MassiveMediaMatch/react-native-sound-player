"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const react_native_1 = require("react-native");
var StreamType;
(function (StreamType) {
    StreamType["RINGTONE"] = "RINGTONE";
    StreamType["MEDIA"] = "MEDIA";
    StreamType["VOICE_CALL"] = "VOICE_CALL";
})(StreamType || (StreamType = {}));
exports.StreamType = StreamType;
const { RNSoundPlayer } = react_native_1.NativeModules;
const _soundPlayerEmitter = new react_native_1.NativeEventEmitter(RNSoundPlayer);
let _finishedPlayingListener = null;
const SoundPlayer = {
    playSoundFile: (id, name, numberOfLoops, streamType, volume) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.playSoundFile(id, name, numberOfLoops, streamType);
    }),
    playUrl: (id, url, streamType, volume) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.playUrl(id, url, streamType);
    }),
    loadSoundFile: (id, name, numberOfLoops, streamType, volume) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.loadSoundFile(id, name, numberOfLoops, streamType);
    }),
    loadUrl: (id, url, streamType, volume) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.loadUrl(id, url, streamType);
    }),
    onFinishedPlaying: (callback) => {
        _finishedPlayingListener = _soundPlayerEmitter.addListener('FinishedPlaying', callback);
    },
    onFinishedLoading: (callback) => {
        _finishedPlayingListener = _soundPlayerEmitter.addListener('FinishedLoading', callback);
    },
    onFinishedLoadingURL: (callback) => {
        _finishedPlayingListener = _soundPlayerEmitter.addListener('FinishedLoadingURL', callback);
    },
    pause: (id) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.pause(id);
    }),
    resume: (id) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.resume(id);
    }),
    stop: (id) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.stop(id);
    }),
    stopAllSounds: () => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.stopAllSounds();
    }),
    setVolume: (id, volume) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.stopAllSounds();
    }),
    seek: (id, seconds) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.seek(id, seconds);
    }),
    getInfo: (id) => __awaiter(this, void 0, void 0, function* () { return RNSoundPlayer.getInfo(id); }),
    unmount: () => {
        _finishedPlayingListener && _finishedPlayingListener.remove();
    },
};
exports.default = SoundPlayer;
