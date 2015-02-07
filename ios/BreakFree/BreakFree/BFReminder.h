//
//  BFReminder.h
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

/*!
 * @typedef BFFrequencyType
 * @brief The reminder frequency type. (Hourly, daily, weekly or monthly.)
 * @constant BFFrequencyHourly  Every hour a user-chosen number of reminders.
 * @constant BFFrequencyDaily   Every day a user-chosen number of reminders.
 * @constant BFFrequencyWeekly  Every week a user-chosen number of reminders.
 * @constant BFFrequencyMonthly Every month a user-chosen number of reminders.
 */
typedef NS_ENUM(NSUInteger, BFFrequencyType) {
    BFFrequencyHourly,
    BFFrequencyDaily,
    BFFrequencyWeekly,
    BFFrequencyMonthly
};

@interface BFReminder : NSObject <NSCoding>
@property (nonatomic, strong) NSUUID *uuid;
@property (nonatomic, strong) NSString *message;
@property (nonatomic) NSInteger frequencyCount;
@property (nonatomic) BFFrequencyType frequencyType;

@property (nonatomic, strong) NSDateComponents *dailyPeriodStartComponents;
@property (nonatomic, strong) NSDateComponents *dailyPeriodEndComponents;
@property (nonatomic) BOOL shouldFireDuringWeekends;

@property (nonatomic, getter=isPaused) BOOL paused;

- (NSString *)frequencyTypeString;
- (void)setFrequencyTypeString:(NSString *)frequencyTypeString;

- (NSString *)dailyFirePeriodString;

- (void)removeAllLocalNotificationsForCurrentReminder;
- (void)scheduleLocalNotificationsForCurrentReminder;

@end
