//
//  BFReminder.h
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import <Foundation/Foundation.h>

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
@property (nonatomic, getter=isPaused) BOOL paused;

- (NSString *)frequencyTypeString;
- (void)setFrequencyTypeString:(NSString *)frequencyTypeString;

- (void)removeAllLocalNotificationsForCurrentReminder;
- (void)scheduleLocalNotificationsForCurrentReminder;

@end
