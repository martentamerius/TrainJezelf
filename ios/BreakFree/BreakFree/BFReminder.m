//
//  BFReminder.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminder.h"

#define kBFReminderUUID                 @"BFReminderUUID"
#define kBFReminderMessage              @"BFReminderMessage"
#define kBFReminderFrequencyCount       @"BFReminderFrequencyCount"
#define kBFReminderFrequencyType        @"BFReminderFrequencyType"
#define kBFReminderIsPaused             @"BFReminderIsPaused"
#define kBFReminderLastFireDate         @"BFReminderLastFireDate"


@interface BFReminder ()
@property (nonatomic, strong) NSDate *lastFireDate;
@end

@implementation BFReminder

- (instancetype)init
{
    if ((self = [super init])) {
        // At least generate a random UUID for this particular reminder (may be overwritten in -initWithCoder:)
        self.uuid = [NSUUID UUID];
        self.paused = NO;
        self.frequencyType = BFFrequencyDaily;
        self.frequencyCount = 1;
        self.message = nil;
    }
    return self;
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
    if ((self = [super init])) {
        if ([aDecoder containsValueForKey:kBFReminderUUID])
            self.uuid = [aDecoder decodeObjectForKey:kBFReminderUUID];
        if ([aDecoder containsValueForKey:kBFReminderMessage])
            self.message = [aDecoder decodeObjectForKey:kBFReminderMessage];
        if ([aDecoder containsValueForKey:kBFReminderFrequencyCount])
            self.frequencyCount = [aDecoder decodeIntegerForKey:kBFReminderFrequencyCount];
        if ([aDecoder containsValueForKey:kBFReminderFrequencyType])
            self.frequencyType = [aDecoder decodeIntegerForKey:kBFReminderFrequencyType];
        if ([aDecoder containsValueForKey:kBFReminderIsPaused])
            self.paused = [aDecoder decodeBoolForKey:kBFReminderIsPaused];
        if ([aDecoder containsValueForKey:kBFReminderLastFireDate])
            self.lastFireDate = (NSDate *)[aDecoder decodeObjectForKey:kBFReminderLastFireDate];
    }
    return self;
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
    [aCoder encodeObject:self.uuid forKey:kBFReminderUUID];
    [aCoder encodeObject:self.message forKey:kBFReminderMessage];
    [aCoder encodeInteger:self.frequencyCount forKey:kBFReminderFrequencyCount];
    [aCoder encodeInteger:self.frequencyType forKey:kBFReminderFrequencyType];
    [aCoder encodeBool:self.isPaused forKey:kBFReminderIsPaused];
    [aCoder encodeObject:self.lastFireDate forKey:kBFReminderLastFireDate];
}


#pragma mark - Conversion routines

- (NSString *)frequencyTypeString
{
    NSString *frequencyTypeString = @"";
    switch (self.frequencyType) {
        case BFFrequencyHourly: frequencyTypeString = @"uur"; break;
        case BFFrequencyDaily: frequencyTypeString = @"dag"; break;
        case BFFrequencyWeekly: frequencyTypeString = @"week"; break;
        case BFFrequencyMonthly: frequencyTypeString = @"maand"; break;
    }
    return frequencyTypeString;
}

- (void)setFrequencyTypeString:(NSString *)frequencyTypeString
{
    if ([frequencyTypeString caseInsensitiveCompare:@"uur"] == NSOrderedSame) {
        self.frequencyType = BFFrequencyHourly;
    } else if ([frequencyTypeString caseInsensitiveCompare:@"dag"] == NSOrderedSame) {
        self.frequencyType = BFFrequencyDaily;
    } else if ([frequencyTypeString caseInsensitiveCompare:@"week"] == NSOrderedSame) {
        self.frequencyType = BFFrequencyWeekly;
    } else if ([frequencyTypeString caseInsensitiveCompare:@"maand"] == NSOrderedSame) {
        self.frequencyType = BFFrequencyMonthly;
    }
}


#pragma mark - Notification scheduling

- (void)setPaused:(BOOL)paused
{
    if (_paused != paused) {
        [self willChangeValueForKey:@"paused"];
        _paused = paused;
        [self didChangeValueForKey:@"paused"];
        
        if (paused) {
            // Remove all currently scheduled local notifications for current reminder
            [self removeAllLocalNotificationsForCurrentReminder];
        } else {
            // Reschedule new local notifications for current reminder
            [self scheduleLocalNotificationsForCurrentReminder];
        }
    }
}

- (void)scheduleNextLocalNotificationWithFireDate:(NSDate *)date
{
    if ((!self.isPaused) && self.message) {
        // Create a new local notification object
        UILocalNotification *localNotification = [[UILocalNotification alloc] init];
        if (localNotification == nil)
            return;
        
        // Construct the local notification content
        localNotification.fireDate = date;
        localNotification.alertBody = [NSString stringWithString:self.message];
        localNotification.alertAction = NSLocalizedString(@"View Details", nil);
        
        // TODO: Sound
        //localNotification.soundName = UILocalNotificationDefaultSoundName;
        localNotification.applicationIconBadgeNumber = [UIApplication sharedApplication].applicationIconBadgeNumber + 1;
        
        NSDictionary *infoDict = [NSDictionary dictionaryWithObject:self.uuid.UUIDString forKey:kBFLocalNotificationReminderUUIDString];
        localNotification.userInfo = infoDict;
        
        // Schedule the new local notification
        [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
        
        NSLog(@"Scheduled a local notification on %@ for %@", date, self);
    }
}

- (void)scheduleLocalNotificationsForCurrentReminder
{
    if (!self.isPaused) {
        NSCalendar *currentCalendar = [NSCalendar currentCalendar];
        NSDateComponents *period = [[NSDateComponents alloc] init];
        period.month = 1;
        NSDate *nextFireDate = (self.lastFireDate)?:[NSDate new];
        
        // Calculate the mean time interval between local notifications
        NSDateComponents *frequencyType = [[NSDateComponents alloc] init];
        switch (self.frequencyType) {
            case BFFrequencyHourly: frequencyType.hour = 1; break;
            case BFFrequencyDaily: frequencyType.day = 1; break;
            case BFFrequencyWeekly: frequencyType.week = 1; break;
            case BFFrequencyMonthly: frequencyType.month = 1; break;
        }
        NSTimeInterval meanTimeInterval = abs([[currentCalendar dateByAddingComponents:frequencyType toDate:nextFireDate options:0] timeIntervalSinceDate:nextFireDate]);
        meanTimeInterval /= self.frequencyCount;
        
        // Count the number of currently scheduled notifications for this reminder
        __block NSInteger currentlyScheduledNotifications = 0;
        NSArray *localNotifications = [[UIApplication sharedApplication] scheduledLocalNotifications];
        [localNotifications enumerateObjectsUsingBlock:^(UILocalNotification *notification, NSUInteger idx, BOOL *stop) {
            NSString *localNotificationReminderUUID = [notification.userInfo objectForKey:kBFLocalNotificationReminderUUIDString];
            if (localNotificationReminderUUID && [localNotificationReminderUUID isEqualToString:self.uuid.UUIDString]) {
                currentlyScheduledNotifications++;
            }
        }];
        
        // Calculate the first new fire date after the present date/time
        while ([nextFireDate timeIntervalSinceNow]<0) {
            nextFireDate = [nextFireDate dateByAddingTimeInterval:meanTimeInterval];
        }
        
        // Schedule max. 10 new local notifications for the current reminder
        // We assume the app will be called at least once before the last scheduled notification for background fetch...
        while (currentlyScheduledNotifications<10) {
            // Introduce random jitter within the (mean time interval / 4) plus and minus the current firedate.
            NSTimeInterval jitter =  (-1 * (meanTimeInterval / 4)) + arc4random_uniform(meanTimeInterval / 2);
            NSDate *jitteryDate = [nextFireDate dateByAddingTimeInterval:jitter];
            
            // Schedule a new local notification for the current fire date
            [self scheduleNextLocalNotificationWithFireDate:jitteryDate];
            
            // Add mean time interval to the current firedate for next local notification firedate
            // TODO: Bypass non-working hours
            nextFireDate = [nextFireDate dateByAddingTimeInterval:meanTimeInterval];
            currentlyScheduledNotifications++;
        }
        
        // Remember the last "nextFireDate" that was used for next time rescheduling is needed
        self.lastFireDate = nextFireDate;
    }
}

- (void)removeAllLocalNotificationsForCurrentReminder
{
    // Remove all currently scheduled local notifications from the application queue
    NSArray *scheduledLocalNotifications = [[UIApplication sharedApplication] scheduledLocalNotifications];
    [scheduledLocalNotifications enumerateObjectsUsingBlock:^(UILocalNotification *localNotification, NSUInteger idx, BOOL *stop) {
        if ([[localNotification.userInfo objectForKey:kBFLocalNotificationReminderUUIDString] isEqualToString:self.uuid.UUIDString]) {
            [[UIApplication sharedApplication] cancelLocalNotification:localNotification];
        }
    }];
    
    // Also nil out the last known "nextFireDate" for when rescheduling is needed
    self.lastFireDate = nil;
}


#pragma mark - Debug

- (NSString *)description
{
    return [NSString stringWithFormat:@"UUID: %@ - Msg: %@%@ - Frq: %@x per %@", self.uuid.UUIDString, [self.message substringToIndex:MIN(self.message.length, 8)], (self.isPaused)?@" - P":@"", @(self.frequencyCount), [self frequencyTypeString]];
}

- (NSString *)debugDescription
{
    return [NSString stringWithFormat:@"UUID: %@ - Message: %@ - Paused: %@ - Frequency: %@ times per %@", self.uuid.UUIDString, self.message, @(self.isPaused), @(self.frequencyCount), [self frequencyTypeString]];
}


@end
