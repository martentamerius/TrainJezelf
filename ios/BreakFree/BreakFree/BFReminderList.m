//
//  BFReminderList.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderList.h"

#define kBFUserDefaultsCurrentReminderList     @"BFCurrentReminderList"


@interface BFReminderList ()
@property (nonatomic, strong) NSMutableArray *reminders;
@end


static BFReminderList *_reminderList;

@implementation BFReminderList


#pragma mark - Singleton

+ (BFReminderList *)sharedReminderList
{
    if (_reminderList)
        return _reminderList;
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _reminderList = [[BFReminderList alloc] init];
    });
    
    return _reminderList;
}


#pragma mark - Initialization

- (BFReminderList *)init
{
    self = [super init];
    if (self) {
        // Initialize an empty arrays
        self.reminders = [NSMutableArray array];
        
        // If available, load reminders directly from NSUserDefaults
        [self loadRemindersFromUserDefaults];
    }
    
    return self;
}


#pragma mark - List editing

- (NSUInteger)count
{
    return [self.reminders count];
}

- (void)addReminder:(BFReminder *)reminder
{
    if (reminder) {
        [self.reminders addObject:reminder];
        [self saveRemindersToUserDefaults];
    }
}

- (void)removeReminder:(BFReminder *)reminder
{
    if (reminder) {
        [self.reminders removeObject:reminder];
        [self saveRemindersToUserDefaults];
    }
}

- (BFReminder *)reminderAtIndex:(NSUInteger)index
{
    BFReminder *reminder;
    
    if ([self.reminders count]>index)
        reminder = [[self reminderList] objectAtIndex:index];

    return reminder;
}

- (BFReminder *)reminderWithUUID:(NSUUID *)uuid
{
    __block BFReminder *reminder;
    
    if ([self.reminders count]>0) {
        [self.reminders enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            BFReminder *currentReminder = (BFReminder *)obj;
            if ([currentReminder.uuid isEqual:uuid]) {
                reminder = currentReminder;
                *stop = YES;
            }
        }];
    }
    
    return reminder;
}

- (NSArray *)reminderList
{
    NSSortDescriptor *frequencyTypeSort = [[NSSortDescriptor alloc] initWithKey:@"frequencyType" ascending:YES];
    NSSortDescriptor *messageSort = [[NSSortDescriptor alloc] initWithKey:@"message" ascending:NO];
    return [self.reminders sortedArrayUsingDescriptors:@[ frequencyTypeSort, messageSort ]];
}


#pragma mark - Loading/Saving reminders

- (void)loadRemindersFromUserDefaults
{
    id remindersFromUserDefaults = [[NSUserDefaults standardUserDefaults] valueForKey:kBFUserDefaultsCurrentReminderList];
    
    if (remindersFromUserDefaults) {
        self.reminders = [NSMutableArray arrayWithArray:[NSKeyedUnarchiver unarchiveObjectWithData:remindersFromUserDefaults]];
    }
}

- (void)saveRemindersToUserDefaults
{
    // First, clean up any old garbage
    [self removeRemindersFromUserDefaults];
    
    // Then save the new reminder list
    if (self.reminders) {
        [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:self.reminders] forKey:kBFUserDefaultsCurrentReminderList];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}

- (void)removeRemindersFromUserDefaults
{
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:kBFUserDefaultsCurrentReminderList];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


#pragma mark - Local Notification scheduling

- (void)checkSchedulingOfLocalNotificationsForAllReminders
{
    // Iterate over all reminders in the list
    [self.reminderList enumerateObjectsUsingBlock:^(BFReminder *reminder, NSUInteger idx, BOOL *stop) {
        if ([reminder isPaused]) {
            // This reminder has been paused: remove all current local notifications for it
            [reminder removeAllLocalNotificationsForCurrentReminder];
        } else {
            // Check if the reminder needs some extra local notifications scheduling after the last fire date
            [reminder scheduleLocalNotificationsForCurrentReminder];
        }
    }];
}


@end
