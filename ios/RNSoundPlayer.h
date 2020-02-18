//
//  RNSoundPlayer
//
//  Created by Johnson Su on 2018-07-10.
//

#import <React/RCTBridgeModule.h>
#import <AVFoundation/AVFoundation.h>
#import <React/RCTEventEmitter.h>

@interface RNSoundPlayer : RCTEventEmitter <RCTBridgeModule, AVAudioPlayerDelegate>

@end


#pragma mark - PlayerData

@interface RNPlayerData : NSObject
@property (nonatomic, strong) AVAudioPlayer *audioPlayer;
@property (nonatomic, strong) AVPlayer *player;
@end
