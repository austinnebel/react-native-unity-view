//
//  RNUnityViewManager.m
//  RNUnityView
//
//  Created by xzper on 2018/2/23.
//  Copyright © 2018年 xzper. All rights reserved.
//

#import "RNUnityViewManager.h"
#import "RNUnityView.h"

@implementation RNUnityViewManager

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE(UnityView)
RCT_EXPORT_VIEW_PROPERTY(onMessage, RCTDirectEventBlock)

- (UIView *)view
{
    [self createUnity];
    self.currentView = [[RNUnityView alloc] init];
    if (self.isUnityReady) {
        [self.currentView setUnityView: [GetAppController() unityView]];
    }
    return self.currentView;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

+ (void)listenAppState
{
    for (NSString *name in @[UIApplicationDidBecomeActiveNotification,
                             UIApplicationDidEnterBackgroundNotification,
                             UIApplicationWillTerminateNotification,
                             UIApplicationWillResignActiveNotification,
                             UIApplicationWillEnterForegroundNotification,
                             UIApplicationDidReceiveMemoryWarningNotification]) {
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleAppStateDidChange:)
                                                     name:name
                                                   object:nil];
    }
}

+ (void)handleAppStateDidChange:(NSNotification *)notification
{
    UnityAppController* unityAppController = GetAppController();
    
    UIApplication* application = [UIApplication sharedApplication];
    
    if ([notification.name isEqualToString:UIApplicationWillResignActiveNotification]) {
        [unityAppController applicationWillResignActive:application];
    } else if ([notification.name isEqualToString:UIApplicationDidEnterBackgroundNotification]) {
        [unityAppController applicationDidEnterBackground:application];
    } else if ([notification.name isEqualToString:UIApplicationWillEnterForegroundNotification]) {
        [unityAppController applicationWillEnterForeground:application];
    } else if ([notification.name isEqualToString:UIApplicationDidBecomeActiveNotification]) {
        [unityAppController applicationDidBecomeActive:application];
    } else if ([notification.name isEqualToString:UIApplicationWillTerminateNotification]) {
        [unityAppController applicationWillTerminate:application];
    } else if ([notification.name isEqualToString:UIApplicationDidReceiveMemoryWarningNotification]) {
		[unityAppController applicationDidReceiveMemoryWarning:application];
	}
}

- (void)handleUnityReady {
    self.isUnityReady = YES;
    if (self.currentView) {
        [self.currentView setUnityView: [GetAppController() unityView]];
    }
}

- (void)createUnity {
    if (UnityIsInited()) {
        return;
    }
    UIApplication* application = [UIApplication sharedApplication];
    // Always keep RN window in top
    application.keyWindow.windowLevel = UIWindowLevelNormal + 1;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleUnityReady) name:@"UnityReady" object:nil];
    
    InitUnity();
    
    UnityAppController *controller = GetAppController();
    [controller application:application didFinishLaunchingWithOptions:self.bridge.launchOptions];
    [controller applicationDidBecomeActive:application];
    [RNUnityViewManager listenAppState];
}

- (void)setBridge:(RCTBridge *)bridge {
    _bridge = bridge;
}

RCT_EXPORT_METHOD(postMessage:(nonnull NSNumber *)reactTag gameObject:(NSString *)gameObject methodName:(NSString *)methodName message:(NSString *)message)
{
    UnityPostMessage(gameObject, methodName, message);
}

RCT_EXPORT_METHOD(pause:(nonnull NSNumber *)reactTag)
{
    UnityPauseCommand();
}

RCT_EXPORT_METHOD(resume:(nonnull NSNumber *)reactTag)
{
    UnityResumeCommand();
}

@end
