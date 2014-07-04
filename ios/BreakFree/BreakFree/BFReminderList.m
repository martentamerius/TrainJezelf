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
        // Initialize an empty array
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
    [self.reminders addObject:reminder];
}

- (void)removeReminder:(BFReminder *)reminder
{
    [self.reminders removeObject:reminder];
}

- (void)removeReminderAtIndex:(NSUInteger)index
{
    if ([self.reminders count]>index) {
        [self.reminders removeObjectAtIndex:index];
    }
}

- (void)exchangeReminderAtIndex:(NSUInteger)firstIndex withReminderAtIndex:(NSUInteger)secondIndex
{
    if (([self.reminders count]>firstIndex) && ([self.reminders count]>secondIndex)) {
        [self.reminders exchangeObjectAtIndex:firstIndex withObjectAtIndex:secondIndex];
    }
}

- (BFReminder *)reminderAtIndex:(NSUInteger)index
{
    BFReminder *reminder;
    
    if ([self.reminders count]>index) {
        reminder = [self.reminders objectAtIndex:index];
    }
    
    return reminder;
}

- (NSArray *)reminderList
{
    return [NSArray arrayWithArray:self.reminders];
}


#pragma mark - Loading/Saving reminders

- (void)loadRemindersFromUserDefaults
{
    id remindersFromUserDefaults = [[NSUserDefaults standardUserDefaults] valueForKey:kBFUserDefaultsCurrentReminderList];
    
    if (remindersFromUserDefaults)
        self.reminders = [NSMutableArray arrayWithArray:[NSKeyedUnarchiver unarchiveObjectWithData:remindersFromUserDefaults]];
}

- (void)saveRemindersToUserDefaults
{
    [[NSUserDefaults standardUserDefaults] removeObjectForKey:kBFUserDefaultsCurrentReminderList];
    
    if (self.reminders) {
        [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:self.reminders] forKey:kBFUserDefaultsCurrentReminderList];
        
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}

@end
