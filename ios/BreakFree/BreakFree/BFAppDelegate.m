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
#import "BFNavigationController.h"

@implementation BFAppDelegate

#pragma mark - App startup

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // Set minimum background fetch interval
    [[UIApplication sharedApplication] setMinimumBackgroundFetchInterval:UIApplicationBackgroundFetchIntervalMinimum];
    
    // iOS 8: Register the app for alert notifications.
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(registerUserNotificationSettings:)]) {
        UIUserNotificationType types = UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert;
        UIUserNotificationSettings *mySettings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings:mySettings];
    }

    // Check if the app was started up because of a local notification
    UILocalNotification *localNotification = [launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey];
    if (localNotification)
        [self ingestLocalNotification:localNotification];
    
    // Check if any reminders need local notification scheduling
    [[BFReminderList sharedReminderList] checkSchedulingOfLocalNotificationsForAllReminders];
    
    return YES;
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
    // Handle the local notification
    [self ingestLocalNotification:notification];
    
    // Then check if any reminders need local notification scheduling
    [[BFReminderList sharedReminderList] checkSchedulingOfLocalNotificationsForAllReminders];
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    
    // Reset app icon badge number when the app is active
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
}


#pragma mark - App shutdown

- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
    
    // Start by saving the current reminder to the user defaults
    [[BFReminderList sharedReminderList] saveRemindersToUserDefaults];
    
    // Then check if any reminders need local notification scheduling
    [[BFReminderList sharedReminderList] checkSchedulingOfLocalNotificationsForAllReminders];
}


#pragma mark - Background fetch

- (void)application:(UIApplication *)application performFetchWithCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
    // Check the list of reminders whether any new local notifications need scheduling
    NSLog(@"Checking scheduling of all reminders!");
    [[BFReminderList sharedReminderList] checkSchedulingOfLocalNotificationsForAllReminders];
    
    // Don't forget to run the completion handler!
    completionHandler(UIBackgroundFetchResultNewData);
}


#pragma mark - Receiving local notifications

- (void)ingestLocalNotification:(UILocalNotification *)localNotification
{
    if (localNotification) {
        // Retrieve the actual reminder using the UUID string in the UserInfo dictionary
        NSString *reminderUUIDString = [localNotification.userInfo objectForKey:kBFLocalNotificationReminderUUIDString];
        if (reminderUUIDString) {
            NSUUID *reminderUUID = [[NSUUID alloc] initWithUUIDString:reminderUUIDString];
            BFReminder *reminder = [[BFReminderList sharedReminderList] reminderWithUUID:reminderUUID];
            if (reminder) {
                // Show the reminder to the user...
                BFNavigationController *initialVC = (BFNavigationController *)self.window.rootViewController;
                [initialVC applicationDidReceiveNotificationWithReminder:reminder];
            }
        }
    }
}

@end
