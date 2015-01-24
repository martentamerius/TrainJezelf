//
//  BFNavigationController.m
//  BreakFree
//
//  Created by Marten Tamerius on 23-09-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFNavigationController.h"
#import "BFReminderViewController.h"
#import "BFReminderEditViewController.h"
#import "BFReminderCollectionViewController.h"
#import "BFReminder.h"

@implementation BFNavigationController

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - Reminder from notification

- (void)applicationDidReceiveNotificationWithReminder:(BFReminder *)reminder
{
    self.receivedReminder = reminder;
    if ([self.topViewController isKindOfClass:[BFReminderCollectionViewController class]]) {
        [((BFReminderCollectionViewController *)self.topViewController) performSegueWithIdentifier:kBFSegueReminderListToReminder sender:self.topViewController];
    } else if ([self.topViewController isKindOfClass:[BFReminderEditViewController class]]){
        [((BFReminderEditViewController *)self.topViewController) performSegueWithIdentifier:kBFSegueReminderEditToReminder sender:self.topViewController];
    }
}

@end
