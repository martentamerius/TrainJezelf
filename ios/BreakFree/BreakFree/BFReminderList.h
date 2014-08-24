//
//  BFReminderList.h
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BFReminder.h"


@interface BFReminderList : NSObject
+ (BFReminderList *)sharedReminderList;

- (NSUInteger)count;
- (NSUInteger)countForFrequencyType:(BFFrequencyType)frequencyType;

- (void)addReminder:(BFReminder *)reminder;
- (void)removeReminder:(BFReminder *)reminder;

- (BFReminder *)reminderAtIndex:(NSUInteger)index;
- (BFReminder *)reminderWithUUID:(NSUUID *)uuid;

- (NSArray *)reminderList;
- (NSArray *)remindersWithFrequencyType:(BFFrequencyType)frequencyType;

- (void)saveRemindersToUserDefaults;
- (void)removeRemindersFromUserDefaults;

@end
