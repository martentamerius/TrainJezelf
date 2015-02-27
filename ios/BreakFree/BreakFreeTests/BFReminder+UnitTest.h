//
//  BFReminder+UnitTest.h
//  BreakFree
//
//  Created by Marten Tamerius on 14-02-15.
//  Copyright (c) 2015 Tamerius & Bos. All rights reserved.
//

#import "BFReminder.h"

@interface BFReminder ()

- (void)scheduleLocalNotificationWithFireDate:(NSDate *)date;
- (NSDate *)startOfPeriodCurrentPeriod;
- (NSDate *)startOfPeriodWithDate:(NSDate *)date;
- (NSTimeInterval)dailyPeriodDurationForDate:(NSDate *)date;
- (NSTimeInterval)periodDurationForStartDate:(NSDate *)periodStartDate;

- (BOOL)fireDateFallsInWeekend:(NSDate *)fireDate;
- (BOOL)fireDateFallsInOffHoursOrWeekends:(NSDate *)fireDate;

- (void)scheduleLocalNotificationsForCurrentReminderWithStartDate:(NSDate *)startDate;
@end
