//
//  RNSoundPlayer
//
//  Created by Johnson Su on 2018-07-10.
//

#import "RNSoundPlayer.h"

@interface RNSoundPlayer ()
@property (nonatomic, strong) NSMutableDictionary *players;
@property (nonatomic, strong) AVAudioPlayer *audioPlayer;
@property (nonatomic, strong) AVPlayer *player;
@end


@implementation RNSoundPlayer


static NSString *const EVENT_FINISHED_LOADING = @"FinishedLoading";
static NSString *const EVENT_FINISHED_LOADING_FILE = @"FinishedLoadingFile";
static NSString *const EVENT_FINISHED_LOADING_URL = @"FinishedLoadingURL";
static NSString *const EVENT_FINISHED_PLAYING = @"FinishedPlaying";


#pragma mark - public

- (instancetype)init
{
	if (self = [super init])
	{
		self.players = [NSMutableDictionary new];
		
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(itemDidFinishPlaying:) name:AVPlayerItemDidPlayToEndTimeNotification object:nil];
	}

	return self;
}

- (void)dealloc
{
	[self.players removeAllObjects];
	
	[[NSNotificationCenter defaultCenter] removeObserver:self name:AVPlayerItemDidPlayToEndTimeNotification object:nil];
}


#pragma mark - private


- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer *)player successfully:(BOOL)flag
{
	NSMutableDictionary *params = [NSMutableDictionary new];
	[params setObject:@(flag) forKey:@"success"];
	NSString *soundId = [self getIdWithPlayer:nil audioPlayer:player];
	if (soundId) {
		[params setObject:soundId forKey:@"id"];
	}
    [self sendEventWithName:EVENT_FINISHED_PLAYING body:[params copy]];
}

- (void)itemDidFinishPlaying:(NSNotification *)notification
{
	NSMutableDictionary *params = [NSMutableDictionary new];
	[params setObject:@(YES) forKey:@"success"];
//	NSString *soundId = [self getIdWithPlayer:nil audioPlayer:player];
//	if (soundId) {
//		[params setObject:soundId forKey:@"id"];
//	}
    [self sendEventWithName:EVENT_FINISHED_PLAYING body:[params copy]];
}

- (void)setPlayerData:(RNPlayerData*)data withId:(NSString*)id
{
	if (data) {
		[self.players setObject:data forKey:id];
	} else {
		[self.players removeObjectForKey:id];
	}
}

- (RNPlayerData *)getPlayerDataForId:(NSString*)id
{
	return [self.players objectForKey:id];
}

- (NSString*)getIdWithPlayer:(AVPlayer*)player audioPlayer:(AVAudioPlayer*)audioPlayer
{
	for (id key in self.players)
	{
		RNPlayerData *data = [self.players objectForKey:key];
		if (data.player == player || data.audioPlayer == audioPlayer) {
			return key;
		}
	}
	return nil;
}

- (RNPlayerData*)mountSoundFile:(NSString *)soundId name:(NSString *)name ofType:(NSString *)type numberofLoops:(NSInteger)numberofLoops
{
    NSString *soundFilePath;

	if ([name containsString:@"/assets/"]) {
		soundFilePath = [[NSBundle mainBundle] pathForResource:[name stringByReplacingOccurrencesOfString:@"/assets/" withString:@""] ofType:type];
	} else {
		soundFilePath = [[NSBundle mainBundle] pathForResource:name ofType:type];
	}

    if (soundFilePath == nil) {
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,NSUserDomainMask, YES);
        NSString *documentsDirectory = [paths objectAtIndex:0];
        soundFilePath = [NSString stringWithFormat:@"%@", [documentsDirectory stringByAppendingPathComponent:name]];
    }

    NSURL *soundFileURL = [NSURL fileURLWithPath:soundFilePath];
    AVAudioPlayer *audioPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:soundFileURL error:nil];
    if (audioPlayer != nil)
	{
        [audioPlayer setDelegate:self];
        [audioPlayer setNumberOfLoops:numberofLoops];
        [audioPlayer prepareToPlay];
		[self sendEventWithName:EVENT_FINISHED_LOADING body:@{@"id": soundId, @"success": [NSNumber numberWithBool:true]}];
        [self sendEventWithName:EVENT_FINISHED_LOADING_FILE body:@{@"id": soundId, @"success": [NSNumber numberWithBool:true], @"name": name, @"type": type}];
    }
	
	RNPlayerData *data = [RNPlayerData new];
	data.audioPlayer = audioPlayer;
	[self setPlayerData:data withId:soundId];
	
	return data;
}

- (RNPlayerData*)prepareUrl:(NSString*)id url:(NSString *)url
{
    NSURL *soundURL = [NSURL URLWithString:url];
    AVPlayer *avPlayer = [[AVPlayer alloc] initWithURL:soundURL];
//    [avPlayer prepareToPlay];
	
	RNPlayerData *data = [RNPlayerData new];
	data.player = avPlayer;
	[self setPlayerData:data withId:id];
    
	[self sendEventWithName:EVENT_FINISHED_LOADING body:@{@"id": id, @"success": [NSNumber numberWithBool:true]}];
    [self sendEventWithName:EVENT_FINISHED_LOADING_URL body: @{@"id": id, @"success": [NSNumber numberWithBool:true], @"url": url}];
	
	return data;
}


#pragma mark - react native

- (NSArray<NSString *> *)supportedEvents {
    return @[EVENT_FINISHED_PLAYING, EVENT_FINISHED_LOADING, EVENT_FINISHED_LOADING_URL, EVENT_FINISHED_LOADING_FILE];
}

RCT_EXPORT_METHOD(playUrl:(NSString *)soundId url:(NSString *)url streamType:(NSString *)streamType)
{
	RNPlayerData *playerData = [self getPlayerDataForId:soundId];
	if (!playerData) {
		playerData = [self prepareUrl:soundId url:url];
	}
	
	if (playerData.player) {
		[playerData.player play];
	}
}

RCT_EXPORT_METHOD(loadUrl:(NSString *)soundId url:(NSString *)url streamType:(NSString *)streamType)
{
	RNPlayerData *playerData = [self getPlayerDataForId:soundId];
	if (!playerData) {
		playerData = [self prepareUrl:soundId url:url];
	}
}

RCT_EXPORT_METHOD(playSoundFile:(NSString *)soundId name:(NSString *)name numberofLoops:(NSInteger)numberofLoops streamType:(NSString *)streamType)
{
	RNPlayerData *playerData = [self getPlayerDataForId:soundId];
	if (!playerData) {
		NSString *pathWithoutExt = [name stringByDeletingPathExtension];
		NSString *type = [name pathExtension];
		playerData = [self mountSoundFile:soundId name:pathWithoutExt ofType:type numberofLoops:numberofLoops];
	}
	
	if (playerData.audioPlayer) {
		[playerData.audioPlayer play];
	}
}

RCT_EXPORT_METHOD(loadSoundFile:(NSString *)soundId name:(NSString *)name numberofLoops:(NSInteger)numberofLoops streamType:(NSString *)streamType)
{
	RNPlayerData *playerData = [self getPlayerDataForId:soundId];
	if (!playerData) {
		NSString *pathWithoutExt = [name stringByDeletingPathExtension];
		NSString *type = [name pathExtension];
		playerData = [self mountSoundFile:soundId name:pathWithoutExt ofType:type numberofLoops:numberofLoops];
	}
}

RCT_EXPORT_METHOD(pause:(NSString*)soundId)
{
	RNPlayerData *data = [self getPlayerDataForId:soundId];
    if (data.audioPlayer != nil) {
        [data.audioPlayer pause];
    }
    if (data.player != nil) {
        [data.player pause];
    }
}

RCT_EXPORT_METHOD(resume:(NSString*)soundId)
{
	RNPlayerData *data = [self getPlayerDataForId:soundId];
    if (data.audioPlayer != nil) {
        [data.audioPlayer play];
    }
    if (data.player != nil) {
        [data.player play];
    }
}

RCT_EXPORT_METHOD(stop:(NSString*)soundId)
{
    RNPlayerData *data = [self getPlayerDataForId:soundId];
    if (data.audioPlayer != nil) {
        [data.audioPlayer stop];
    }
    if (data.player != nil) {
        [data.player pause];
    }
}

RCT_EXPORT_METHOD(stopAllSounds)
{
	for (id key in self.players) {
		RNPlayerData *data = [self.players objectForKey:key];
		if (data.audioPlayer != nil) {
			[data.audioPlayer stop];
		}
		if (data.player != nil) {
			[data.player pause];
		}
	}
}

RCT_EXPORT_METHOD(seek:(NSString*)soundId seconds:(float)seconds)
{
	RNPlayerData *data = [self getPlayerDataForId:soundId];
    if (data.audioPlayer != nil) {
        data.audioPlayer.currentTime = seconds;
    }
    if (data.player != nil) {
        [data.player seekToTime: CMTimeMakeWithSeconds(seconds, 1.0)];
    }
}

RCT_EXPORT_METHOD(setSpeaker:(BOOL) on)
{
    AVAudioSession *session = [AVAudioSession sharedInstance];
    if (on) {
        [session setCategory: AVAudioSessionCategoryPlayAndRecord error: nil];
        [session overrideOutputAudioPort:AVAudioSessionPortOverrideSpeaker error:nil];
    } else {
        [session setCategory: AVAudioSessionCategoryPlayback error: nil];
        [session overrideOutputAudioPort:AVAudioSessionPortOverrideNone error:nil];
    }
    [session setActive:true error:nil];
}

RCT_EXPORT_METHOD(setVolume:(NSString*)soundId volume:(float)volume)
{
	RNPlayerData *data = [self getPlayerDataForId:soundId];
    if (data.audioPlayer != nil) {
        [data.audioPlayer setVolume: volume];
    }
    if (data.player != nil) {
        [data.player setVolume: volume];
    }
}

RCT_REMAP_METHOD(getInfo,
				 getInfoWithId:(NSString*)soundId resolver:(RCTPromiseResolveBlock) resolve
                 rejecter:(RCTPromiseRejectBlock) reject)
{
	RNPlayerData *playerData = [self getPlayerDataForId:soundId];
    if (playerData.audioPlayer != nil)
	{
        NSDictionary *data = @{
                               @"currentTime": [NSNumber numberWithDouble:[playerData.audioPlayer currentTime]],
                               @"duration": [NSNumber numberWithDouble:[playerData.audioPlayer duration]]
                               };
        resolve(data);
    }
	
    if (playerData.player != nil)
	{
        CMTime currentTime = [[playerData.player currentItem] currentTime];
        CMTime duration = [[[playerData.player currentItem] asset] duration];
        NSDictionary *data = @{
                               @"currentTime": [NSNumber numberWithFloat:CMTimeGetSeconds(currentTime)],
                               @"duration": [NSNumber numberWithFloat:CMTimeGetSeconds(duration)]
                               };
        resolve(data);
    }
}


RCT_EXPORT_MODULE();

@end


#pragma mark - PlayerData

@implementation RNPlayerData

@end
