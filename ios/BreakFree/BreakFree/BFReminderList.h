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

- (void)addReminder:(BFReminder *)reminder;
- (void)removeReminder:(BFReminder *)reminder;
- (void)removeReminderAtIndex:(NSUInteger)index;

- (void)exchangeReminderAtIndex:(NSUInteger)firstIndex withReminderAtIndex:(NSUInteger)secondIndex;

- (BFReminder *)reminderAtIndex:(NSUInteger)index;

- (NSArray *)reminderList;

- (void)saveRemindersToUserDefaults;
@end
