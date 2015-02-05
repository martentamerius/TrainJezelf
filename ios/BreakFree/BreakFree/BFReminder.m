//
//  BFReminder.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminder.h"

#define kBFReminderUUID                     @"BFReminderUUID"
#define kBFReminderMessage                  @"BFReminderMessage"
#define kBFReminderFrequencyCount           @"BFReminderFrequencyCount"
#define kBFReminderFrequencyType            @"BFReminderFrequencyType"
#define kBFReminderFireDailyStartTime       @"BFReminderFireDailyStartTime"
#define kBFReminderFireDailyEndTime         @"BFReminderFireDailyEndTime"
#define kBFReminderShouldFireDuringWeekends @"BFReminderShouldFireDuringWeekends"
#define kBFReminderIsPaused                 @"BFReminderIsPaused"


@implementation BFReminder

- (instancetype)init
{
    if ((self = [super init])) {
        // At least generate a random UUID for this particular reminder (may be overwritten in -initWithCoder:)
        self.uuid = [NSUUID UUID];
        
        // Other defaults for when a new reminder is instantiated
        self.paused = NO;
        self.frequencyType = BFFrequencyDaily;
        self.frequencyCount = 1;
        self.message = nil;
        
        self.dailyPeriodStartComponents = [[NSDateComponents alloc] init];
        self.dailyPeriodStartComponents.hour = 8; // 8:00h
        self.dailyPeriodStartComponents.minute = 0;
        self.dailyPeriodStartComponents.nanosecond = 0;
        self.dailyPeriodEndComponents = [[NSDateComponents alloc] init];
        self.dailyPeriodEndComponents.hour = 19; // 19:00h
        self.dailyPeriodEndComponents.minute = 0;
        self.dailyPeriodEndComponents.nanosecond = 0;
        self.shouldFireDuringWeekends = NO;
    }
    return self;
}


#pragma mark - NSCoding protocol

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
        if ([aDecoder containsValueForKey:kBFReminderFireDailyStartTime])
            self.dailyPeriodStartComponents = [aDecoder decodeObjectForKey:kBFReminderFireDailyStartTime];
        if ([aDecoder containsValueForKey:kBFReminderFireDailyEndTime])
            self.dailyPeriodEndComponents = [aDecoder decodeObjectForKey:kBFReminderFireDailyEndTime];
        if ([aDecoder containsValueForKey:kBFReminderShouldFireDuringWeekends])
            self.shouldFireDuringWeekends = [aDecoder decodeBoolForKey:kBFReminderShouldFireDuringWeekends];
        
        if ([aDecoder containsValueForKey:kBFReminderIsPaused])
            self.paused = [aDecoder decodeBoolForKey:kBFReminderIsPaused];
    }
    return self;
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
    [aCoder encodeObject:self.uuid forKey:kBFReminderUUID];
    [aCoder encodeObject:self.message forKey:kBFReminderMessage];
    [aCoder encodeInteger:self.frequencyCount forKey:kBFReminderFrequencyCount];
    [aCoder encodeInteger:self.frequencyType forKey:kBFReminderFrequencyType];
    [aCoder encodeObject:self.dailyPeriodStartComponents forKey:kBFReminderFireDailyStartTime];
    [aCoder encodeObject:self.dailyPeriodEndComponents forKey:kBFReminderFireDailyEndTime];
    [aCoder encodeBool:self.shouldFireDuringWeekends forKey:kBFReminderShouldFireDuringWeekends];
    
    [aCoder encodeBool:self.isPaused forKey:kBFReminderIsPaused];
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

- (NSString *)dailyFirePeriodString
{
    // Assemble the string
    NSMutableString *periodString = [NSMutableString string];
    if (self.dailyPeriodStartComponents && self.dailyPeriodEndComponents) {
        NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
        numberFormatter.minimumIntegerDigits = 2;
        numberFormatter.maximumFractionDigits = 0;
        NSInteger startHour = (self.dailyPeriodStartComponents.hour != NSUndefinedDateComponent)?self.dailyPeriodStartComponents.hour:0;
        NSInteger startMinute = (self.dailyPeriodStartComponents.minute != NSUndefinedDateComponent)?self.dailyPeriodStartComponents.minute:0;
        
        [periodString appendFormat:@"%@:%@", @(startHour), [numberFormatter stringFromNumber:@(startMinute)]];
        
        NSInteger endHour = (self.dailyPeriodEndComponents.hour != NSUndefinedDateComponent)?self.dailyPeriodEndComponents.hour:0;
        NSInteger endMinute = (self.dailyPeriodEndComponents.minute != NSUndefinedDateComponent)?self.dailyPeriodEndComponents.minute:0;
        
        [periodString appendFormat:@" en %@:%@", @(endHour), [numberFormatter stringFromNumber:@(endMinute)]];
    }
    
    return [NSString stringWithString:periodString];
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
            // Reschedule local notifications for current reminder
            [self scheduleLocalNotificationsForCurrentReminder];
        }
    }
}

- (void)scheduleLocalNotificationWithFireDate:(NSDate *)date
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

- (NSDate *)startOfPeriodCurrentPeriod
{
    NSCalendar *currentCalendar = [NSCalendar autoupdatingCurrentCalendar];
    NSDate *startOfCurrentPeriod = [NSDate date];
    
    NSCalendarUnit allComponents = (NSCalendarUnitYear | NSCalendarUnitMonth | NSCalendarUnitWeekOfMonth |
                                    NSCalendarUnitWeekday | NSCalendarUnitHour | NSCalendarUnitMinute |
                                    NSCalendarUnitSecond | NSCalendarUnitNanosecond);
    NSDateComponents *startDateComps = [currentCalendar components:allComponents fromDate:startOfCurrentPeriod];
    
    if (self.frequencyType >= BFFrequencyHourly) {
        // Period starts at the beginning of the current hour
        startDateComps.nanosecond = 0;
        startDateComps.second = 0;
        startDateComps.minute = 0;
    }
    if (self.frequencyType >= BFFrequencyDaily) {
        // Period starts at the beginning of the daily period of the current day
        startDateComps.minute = self.dailyPeriodStartComponents.minute;
        startDateComps.hour = self.dailyPeriodStartComponents.hour;
    }
    if (self.frequencyType >= BFFrequencyWeekly) {
        // Period starts at the beginning of the current week (day = 1 means sunday)
        startDateComps.weekday = (self.shouldFireDuringWeekends)?1:2;
    }
    if (self.frequencyType == BFFrequencyMonthly) {
        // Period starts at the beginning of the current month
        startDateComps.day = 1;
        startDateComps.weekday = NSUndefinedDateComponent;
        startDateComps.weekOfMonth = NSUndefinedDateComponent;
    }
    
    // Reassemble the start date from the adjusted date components
    startOfCurrentPeriod = [currentCalendar dateFromComponents:startDateComps];
    
    if ((self.frequencyType == BFFrequencyMonthly) && (!self.shouldFireDuringWeekends)) {
        // Extra check for monthly reminders: see if the starting day falls in the weekend;
        // don't do that (lest we not lose a notification in the weekends...)
        NSDateComponents *nextDay = [[NSDateComponents alloc] init];
        nextDay.day = 1;
        while ([self fireDateFallsInWeekend:startOfCurrentPeriod]) {
            // Just add one extra day until it is monday again...
            startOfCurrentPeriod = [currentCalendar dateByAddingComponents:nextDay toDate:startOfCurrentPeriod options:0];
        }
    }

    return startOfCurrentPeriod;
}

- (NSTimeInterval)dailyPeriodDurationForDate:(NSDate *)date
{
    // Get the duration of a day by using the calendar API, excluding the off-hours
    NSCalendar *currentCalendar = [NSCalendar autoupdatingCurrentCalendar];
    NSCalendarUnit dateComps = (NSCalendarUnitYear | NSCalendarUnitMonth | NSCalendarUnitDay);
    
    // Adjust the time of the date for start and end
    NSDateComponents *periodStartComps = [currentCalendar components:dateComps fromDate:date];
    periodStartComps.hour = self.dailyPeriodStartComponents.hour;
    periodStartComps.minute = self.dailyPeriodStartComponents.minute;
    periodStartComps.nanosecond = 0;
    NSDateComponents *periodEndComps = [currentCalendar components:dateComps fromDate:date];
    periodEndComps.hour = self.dailyPeriodEndComponents.hour;
    periodEndComps.minute = self.dailyPeriodEndComponents.minute;
    periodEndComps.nanosecond = 0;
    
    // Assemble from start/end date components
    NSDate *periodStart = [currentCalendar dateFromComponents:periodStartComps];
    NSDate *periodEnd = [currentCalendar dateFromComponents:periodEndComps];
    
    return abs([periodEnd timeIntervalSinceDate:periodStart]);
}

- (NSTimeInterval)periodDurationForStartDate:(NSDate *)periodStartDate
{
    NSCalendar *currentCalendar = [NSCalendar autoupdatingCurrentCalendar];
    NSTimeInterval periodDuration = 0;
    
    switch (self.frequencyType) {
        case BFFrequencyHourly: {
            // Get the duration of an hour by using the calendar API (for the off-chance a leap second is introduced)
            NSDateComponents *frequencyType = [[NSDateComponents alloc] init];
            frequencyType.hour = 1;
            periodDuration = abs([[currentCalendar dateByAddingComponents:frequencyType toDate:periodStartDate options:0] timeIntervalSinceDate:periodStartDate]);
            
            break;
        }
            
        case BFFrequencyDaily: {
            // Get the duration of a day by using the calendar API, excluding the off-hours
            periodDuration = [self dailyPeriodDurationForDate:periodStartDate];
            break;
        }
            
        case BFFrequencyWeekly:
        case BFFrequencyMonthly: {
            
            // Get the duration of the current timeframe (week/month) by using the calendar API,
            // excluding the off-hours and, if applicable, the weekends
            NSDate *endOfCurrentPeriod;
            if (self.frequencyType == BFFrequencyWeekly) {
                
                // Check if the last day of the week should be a saturday or a friday
                NSCalendarUnit dateComps = (NSCalendarUnitYear | NSCalendarUnitMonth | NSCalendarUnitWeekOfMonth | NSCalendarUnitWeekday);
                NSDateComponents *periodEndComps = [currentCalendar components:dateComps fromDate:periodStartDate];
                periodEndComps.weekday = (self.shouldFireDuringWeekends)?6:5;
                endOfCurrentPeriod = [currentCalendar dateFromComponents:periodEndComps];
                
            } else {

                // BFFrequencyMonthly: add a month and then subtract a day to get to the end of the current month
                NSCalendarUnit dateComps = (NSCalendarUnitYear | NSCalendarUnitMonth | NSCalendarUnitDay);
                NSDateComponents *periodEndComps = [currentCalendar components:dateComps fromDate:periodStartDate];
                periodEndComps.month += 1;
                periodEndComps.day = 1;
                endOfCurrentPeriod = [currentCalendar dateFromComponents:periodEndComps];
                NSDateComponents *subtractOneDay = [[NSDateComponents alloc] init];
                subtractOneDay.day = -1;
                endOfCurrentPeriod = [currentCalendar dateByAddingComponents:subtractOneDay toDate:endOfCurrentPeriod options:0];
            }
            
            // Iterate over all days of the current timeframe to accumulate the total duration of the period
            NSDate *dayIterator = periodStartDate;
            while ([dayIterator timeIntervalSinceDate:endOfCurrentPeriod] < 0) {
                
                if (![self fireDateFallsInOffHoursOrWeekends:dayIterator]) {
                    // Add the duration of a day, excluding off-hours, for this iteration to the periodDuration
                    periodDuration += [self dailyPeriodDurationForDate:dayIterator];
                }
                
                // Is it the end of time already?!
                NSDateComponents *nextDay = [[NSDateComponents alloc] init];
                nextDay.day = 1;
                dayIterator = [currentCalendar dateByAddingComponents:nextDay toDate:dayIterator options:0];
            }
            
            break;
        }
    }
    
    return periodDuration;
}

- (BOOL)fireDateFallsInWeekend:(NSDate *)fireDate
{
    BOOL fallsInWeekend = NO;
    
    // Check if the fire date is in the weekend
    NSCalendar *currentCalendar = [NSCalendar autoupdatingCurrentCalendar];
    NSDateComponents *dayOfWeekComps = [currentCalendar components:NSCalendarUnitWeekday fromDate:fireDate];
    if ((dayOfWeekComps.weekday == 1) || (dayOfWeekComps.weekday == 6)) {
        // Sunday == 1 / Saturday == 6
        fallsInWeekend = YES;
    }
    
    return fallsInWeekend;
}

- (BOOL)fireDateFallsInOffHoursOrWeekends:(NSDate *)fireDate
{
    BOOL fallsInOffHoursOrWeekends = NO;
    
    // Check if the original firedate happens to be during daily off-hours
    NSCalendar *currentCalendar = [NSCalendar autoupdatingCurrentCalendar];
    NSDateComponents *fireDateComps = [currentCalendar components:(NSCalendarUnitHour | NSCalendarUnitMinute | NSCalendarUnitSecond) fromDate:fireDate];
    NSTimeInterval fireTimeInterval = (fireDateComps.hour * 3600) + (fireDateComps.minute * 60) + fireDateComps.second;
    
    NSTimeInterval dailyStartTime = (self.dailyPeriodStartComponents.hour * 3600) + (self.dailyPeriodStartComponents.minute * 60);
    NSTimeInterval dailyEndTime = (self.dailyPeriodEndComponents.hour * 3600) + (self.dailyPeriodEndComponents.minute * 60);
    
    if ((fireTimeInterval < dailyStartTime) || (fireTimeInterval > dailyEndTime)) {
        // Fire date falls within the off-hours range
        fallsInOffHoursOrWeekends = YES;
    } else if (!self.shouldFireDuringWeekends) {
        // Check if the original fire date falls during the weekend
        fallsInOffHoursOrWeekends &= [self fireDateFallsInWeekend:fireDate];
    }
    
    return fallsInOffHoursOrWeekends;
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
    
    NSLog(@"All notifications removed for %@", self);
}

- (void)scheduleLocalNotificationsForCurrentReminder
{
    // 1. Remove currently scheduled notifications
    [self removeAllLocalNotificationsForCurrentReminder];
    
    if (!self.isPaused) {
        // 2. Get the start of the current period
        NSDate *startOfCurrentPeriod = [self startOfPeriodCurrentPeriod];
        // and the duration of the period according to the frequency type
        NSTimeInterval dailyPeriodDuration = [self periodDurationForStartDate:startOfCurrentPeriod];
        
        if (dailyPeriodDuration > 0) {
            // 3. Calculate the mean time interval for the current period
            NSTimeInterval meanInterval = dailyPeriodDuration / self.frequencyCount;
            // and determine the next fire date after the present date/time by repeatedly adding the mean time interval
            NSDate *nextFireDate = startOfCurrentPeriod;
            while ([nextFireDate timeIntervalSinceNow]<-1) {
                nextFireDate = [nextFireDate dateByAddingTimeInterval:meanInterval];
            }

            // Calculate the max number of local notifications to schedule, include in calculations the off-hours and
            // weekends, although these may not actually get scheduled. (Prepare notifications for at least 3 days)
            NSInteger maxNotificationCount = self.frequencyCount;
            switch (self.frequencyType) {
                case BFFrequencyHourly: { maxNotificationCount *= (3 * 24); break; }
                case BFFrequencyDaily: { maxNotificationCount *= ((3 * 24 * 3600) / dailyPeriodDuration); break; }
                default: break;
            }
            
            // 4. Schedule new local notifications for the current reminder and assume the app will be called for
            // background fetch at least once before the last scheduled notification has passed...
            NSInteger counter = 0;
            while (counter<maxNotificationCount) {
                
                // Don't schedule if the next firedate is during daily off-hours or in weekends (if appropriate)
                if (![self fireDateFallsInOffHoursOrWeekends:nextFireDate]) {
                    
                    // 5. Introduce random jitter within the range of +/- (mean time interval / 4) of the current firedate
                    NSTimeInterval jitter =  (-1 * (meanInterval / 4)) + arc4random_uniform(meanInterval / 2);
                    NSDate *jitteryDate = [nextFireDate dateByAddingTimeInterval:jitter];
                    
                    // Check, and recheck! Maybe this time, including the jitter, the date falls in daily off-hours or weekend.
                    if (![self fireDateFallsInOffHoursOrWeekends:jitteryDate]) {
                        
                        // 6. All is well; schedule a new local notification with the "jittery" fire date
                        [self scheduleLocalNotificationWithFireDate:jitteryDate];
                    }
                }
                
                // 7. Add mean time interval to the current fire date for next local notification
                nextFireDate = [nextFireDate dateByAddingTimeInterval:meanInterval];
                
                // Get ready for the next iteration!
                counter++;
            }
        }
    }
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
