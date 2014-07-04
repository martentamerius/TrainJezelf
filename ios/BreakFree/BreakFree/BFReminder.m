//
//  BFReminder.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminder.h"

#define kBFReminderMessage          @"BFReminderMessage"
#define kBFReminderFrequencyCount   @"BFReminderFrequencyCount"
#define kBFReminderFrequencyType    @"BFReminderFrequencyType"


@implementation BFReminder

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
    self = [super init];
    if (self) {
        if ([aDecoder containsValueForKey:kBFReminderMessage])
            self.message = [aDecoder decodeObjectForKey:kBFReminderMessage];
        if ([aDecoder containsValueForKey:kBFReminderFrequencyCount])
            self.frequencyCount = [aDecoder decodeIntegerForKey:kBFReminderFrequencyCount];
        if ([aDecoder containsValueForKey:kBFReminderFrequencyType])
            self.frequencyType = [aDecoder decodeIntegerForKey:kBFReminderFrequencyType];
    }
    
    return self;
}

- (void)encodeWithCoder:(NSCoder *)aCoder
{
    [aCoder encodeObject:self.message forKey:kBFReminderMessage];
    [aCoder encodeInteger:self.frequencyCount forKey:kBFReminderFrequencyCount];
    [aCoder encodeInteger:self.frequencyType forKey:kBFReminderFrequencyType];
}


#pragma mark - Conversion routines

- (NSString *)frequencyTypeString
{
    NSString *frequencyTypeString = @"";
    switch (self.frequencyType) {
        case BFFrequencyHourly: frequencyTypeString = @"per uur"; break;
        case BFFrequencyDaily: frequencyTypeString = @"per dag"; break;
        case BFFrequencyMonthly: frequencyTypeString = @"per maand"; break;
    }
    return frequencyTypeString;
}
@end
