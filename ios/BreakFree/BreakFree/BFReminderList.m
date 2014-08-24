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


#pragma mark - Initialisation

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

- (NSUInteger)countForFrequencyType:(BFFrequencyType)frequencyType
{
    return [[self remindersWithFrequencyType:frequencyType] count];
}

- (void)addReminder:(BFReminder *)reminder
{
    if (reminder)
        [self.reminders addObject:reminder];
}

- (void)removeReminder:(BFReminder *)reminder
{
    if (reminder)
        [self.reminders removeObject:reminder];
}

- (BFReminder *)reminderAtIndex:(NSUInteger)index
{
    BFReminder *reminder;
    
    if ([self.reminders count]>index)
        reminder = [self.reminders objectAtIndex:index];
    
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
    return [NSArray arrayWithArray:self.reminders];
}

- (NSArray *)remindersWithFrequencyType:(BFFrequencyType)frequencyType
{
    __block NSMutableArray *filteredReminders = [NSMutableArray array];
    
    [self.reminders enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        BFReminder *currentReminder = (BFReminder *)obj;
        if (currentReminder.frequencyType == frequencyType)
            [filteredReminders addObject:currentReminder];
    }];
    
    return [NSArray arrayWithArray:filteredReminders];
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
    [self removeRemindersFromUserDefaults];
    
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

@end
