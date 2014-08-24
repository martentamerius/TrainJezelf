//
//  BFAppDelegate.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFAppDelegate.h"
#import "BFAppDefines.h"
#import "BFReminderList.h"
#import "BFLaunchViewController.h"

@implementation BFAppDelegate


#pragma mark - App startup

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    UILocalNotification *localNotification = [launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey];
    [self ingestLocalNotification:localNotification];
    
    return YES;
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
    [self ingestLocalNotification:notification];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


#pragma mark - App shutdown

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    
    if ([[BFReminderList sharedReminderList].reminderList count]>0) {
        [[BFReminderList sharedReminderList].reminderList enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            [self scheduleNextLocalNotificationForReminder:(BFReminder *)obj];
        }];
    }
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    
    [[BFReminderList sharedReminderList] saveRemindersToUserDefaults];
}


#pragma mark - Local notifications

- (void)scheduleNextLocalNotificationForReminder:(BFReminder *)reminder
{
    __block BOOL reminderIsAlreadyScheduled = NO;
    
    // Check if the reminder is already scheduled
    [[[UIApplication sharedApplication] scheduledLocalNotifications] enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        UILocalNotification *currentNotification = (UILocalNotification *)obj;
        NSString *uuidString =[currentNotification.userInfo objectForKey:kBFLocalNotificationReminderUUIDString];
        if (uuidString && ([uuidString isEqualToString:reminder.uuid.UUIDString])) {
            // Current notification is the same as the parameter...
            reminderIsAlreadyScheduled = YES;
            *stop = YES;
        }
    }];
    
    if (!reminderIsAlreadyScheduled) {
        // If not, create a new local notification
        UILocalNotification *localNotification = [[UILocalNotification alloc] init];
        if (localNotification == nil)
            return;
        
        localNotification.fireDate = [reminder calculateNextLocalNotificationFireDate];
        localNotification.timeZone = [NSTimeZone defaultTimeZone];
        localNotification.alertBody = [NSString stringWithString:reminder.message];
        localNotification.alertAction = NSLocalizedString(@"View Details", nil);
        
        localNotification.soundName = UILocalNotificationDefaultSoundName;
        localNotification.applicationIconBadgeNumber = 1;
        
        NSDictionary *infoDict = [NSDictionary dictionaryWithObject:reminder.uuid.UUIDString forKey:kBFLocalNotificationReminderUUIDString];
        localNotification.userInfo = infoDict;
        
        [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
    }
}

- (void)ingestLocalNotification:(UILocalNotification *)localNotification
{
    if (localNotification) {
        NSString *reminderUUIDString = [localNotification.userInfo objectForKey:kBFLocalNotificationReminderUUIDString];
        if (reminderUUIDString) {
            NSUUID *reminderUUID = [[NSUUID alloc] initWithUUIDString:reminderUUIDString];
            BFReminder *reminder = [[BFReminderList sharedReminderList] reminderWithUUID:reminderUUID];
            if (reminder) {
                UIStoryboard *storyBoard = [UIStoryboard storyboardWithName:@"Storyboard" bundle:nil];
                BFLaunchViewController *launchVC = [storyBoard instantiateInitialViewController];
                [launchVC showReminder:reminder];
                
                self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
                self.window.rootViewController = launchVC;
                [self.window makeKeyAndVisible];
            }
        }
        [UIApplication sharedApplication].applicationIconBadgeNumber = localNotification.applicationIconBadgeNumber-1;
    }
}

@end
