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
})(StreamType || (StreamType = {}));
exports.StreamType = StreamType;
const { RNSoundPlayer } = react_native_1.NativeModules;
const _soundPlayerEmitter = new react_native_1.NativeEventEmitter(RNSoundPlayer);
let _finishedPlayingListener = null;
const SoundPlayer = {
    playSoundFile: (name, type, streamType) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.playSoundFile(name, type, streamType);
    }),
    playUrl: (url, streamType) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.playUrl(url, streamType);
    }),
    loadSoundFile: (name, type, streamType) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.loadSoundFile(name, type, streamType);
    }),
    loadUrl: (url, streamType) => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.loadUrl(url, streamType);
    }),
    onFinishedPlaying: (callback) => {
        _finishedPlayingListener = _soundPlayerEmitter.addListener('FinishedPlaying', callback);
    },
    pause: () => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.pause();
    }),
    resume: () => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.resume();
    }),
    stop: () => __awaiter(this, void 0, void 0, function* () {
        return RNSoundPlayer.stop();
    }),
    getInfo: () => __awaiter(this, void 0, void 0, function* () { return RNSoundPlayer.getInfo(); }),
    unmount: () => {
        _finishedPlayingListener && _finishedPlayingListener.remove();
    }
};
exports.default = SoundPlayer;
