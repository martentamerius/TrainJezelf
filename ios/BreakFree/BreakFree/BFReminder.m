//
//  BFReminder.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminder.h"

#define kBFReminderUUID                         @"BFReminderUUID"
#define kBFReminderMessage                      @"BFReminderMessage"
#define kBFReminderFrequencyCount               @"BFReminderFrequencyCount"
#define kBFReminderFrequencyType                @"BFReminderFrequencyType"
#define kBFReminderLocalNotificationFireDate    @"BFReminderLocalNotificationFireDate"


@interface BFReminder ()
@property (nonatomic, strong) NSDate *localNotificationFireDate;
@end

@implementation BFReminder

- (instancetype)init
{
    self = [super init];
    if (self) {
        // At least generate a random UUID for this particular reminder (may be overwritten in -initWithCoder:)
        self.uuid = [NSUUID UUID];
    }
    
    return self;
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
    self = [super init];
    if (self) {
        if ([aDecoder containsValueForKey:kBFReminderUUID])
            self.uuid = [aDecoder decodeObjectForKey:kBFReminderUUID];
        if ([aDecoder containsValueForKey:kBFReminderMessage])
            self.message = [aDecoder decodeObjectForKey:kBFReminderMessage];
        if ([aDecoder containsValueForKey:kBFReminderFrequencyCount])
            self.frequencyCount = [aDecoder decodeIntegerForKey:kBFReminderFrequencyCount];
        if ([aDecoder containsValueForKey:kBFReminderFrequencyType])
            self.frequencyType = [aDecoder decodeIntegerForKey:kBFReminderFrequencyType];
        if ([aDecoder containsValueForKey:kBFReminderLocalNotificationFireDate])
            self.localNotificationFireDate = (NSDate *)[aDecoder decodeObjectForKey:kBFReminderLocalNotificationFireDate];
    }
    
    return self;
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
    [aCoder encodeObject:self.uuid forKey:kBFReminderUUID];
    [aCoder encodeObject:self.message forKey:kBFReminderMessage];
    [aCoder encodeInteger:self.frequencyCount forKey:kBFReminderFrequencyCount];
    [aCoder encodeInteger:self.frequencyType forKey:kBFReminderFrequencyType];
    [aCoder encodeObject:self.localNotificationFireDate forKey:kBFReminderLocalNotificationFireDate];
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

- (NSDate *)calculateNextLocalNotificationFireDate
{
    NSDate *fireDate;
    
    // Get the frequency type and determine the period
    NSCalendar *currentCalendar = [NSCalendar currentCalendar];
    NSDateComponents *period = [[NSDateComponents alloc] init];
    switch (self.frequencyType) {
        case BFFrequencyHourly: { period.hour = 1; break; }
        case BFFrequencyDaily: { period.day = 1; break; }
        case BFFrequencyWeekly: { period.week = 1; break; }
        case BFFrequencyMonthly: { period.month = 1; break; }
    }
    
    NSDate *now = [NSDate new];
    NSTimeInterval timeIntervalUntilNextPeriod = [now timeIntervalSinceDate:[currentCalendar dateByAddingComponents:period toDate:now options:0]];
    
    // Divide time interval between periods into equal parts
    // TODO: Substract non-working hours and introduce jitter
    NSTimeInterval nextReminderToSchedule = timeIntervalUntilNextPeriod / self.frequencyCount;
    
    fireDate = [now dateByAddingTimeInterval:nextReminderToSchedule];
    
    return fireDate;
}


#pragma mark - Debug

- (NSString *)debugDescription
{
    return [NSString stringWithFormat:@"UUID: %@ - Message: %@ - Scheduled: %@ - Frequency: %ld times per %@", self.uuid, self.message, (self.localNotificationFireDate)?self.localNotificationFireDate:@"NO", (long)self.frequencyCount, [self frequencyTypeString]];
}


@end
