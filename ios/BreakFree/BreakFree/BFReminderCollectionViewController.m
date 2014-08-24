//
//  BFReminderCollectionViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 28-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderCollectionViewController.h"
#import "BFReminderList.h"
#import "BFReminderEditViewController.h"
#import "BFReminderCollectionViewCell.h"
#import "BFReminderCollectionViewAccessoryViewHeader.h"
#import "BFAppDelegate.h"


@interface BFReminderCollectionViewController () <UICollectionViewDataSource, UICollectionViewDelegate>
@end

@implementation BFReminderCollectionViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


#pragma mark - UICollectionViewDataSource

- (NSArray *)remindersForSection:(NSInteger)section
{
    NSArray *hourArray = [[BFReminderList sharedReminderList] remindersWithFrequencyType:BFFrequencyHourly];
    NSArray *dayArray = [[BFReminderList sharedReminderList] remindersWithFrequencyType:BFFrequencyDaily];
    NSArray *weekArray = [[BFReminderList sharedReminderList] remindersWithFrequencyType:BFFrequencyWeekly];
    NSArray *monthArray = [[BFReminderList sharedReminderList] remindersWithFrequencyType:BFFrequencyMonthly];
    
    NSMutableArray *listArray = [NSMutableArray array];
    if (hourArray && ([hourArray count]>0))
        [listArray addObject:hourArray];
    if (dayArray && ([dayArray count]>0))
        [listArray addObject:dayArray];
    if (weekArray && ([weekArray count]>0))
        [listArray addObject:weekArray];
    if (monthArray && ([monthArray count]>0))
        [listArray addObject:monthArray];
    
    return ([listArray count]>section)?[listArray objectAtIndex:section]:nil;
}

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    NSInteger sectionCount = 0;
    
    NSArray *hourArray = [[BFReminderList sharedReminderList] remindersWithFrequencyType:BFFrequencyHourly];
    NSArray *dayArray = [[BFReminderList sharedReminderList] remindersWithFrequencyType:BFFrequencyDaily];
    NSArray *weekArray = [[BFReminderList sharedReminderList] remindersWithFrequencyType:BFFrequencyWeekly];
    NSArray *monthArray = [[BFReminderList sharedReminderList] remindersWithFrequencyType:BFFrequencyMonthly];
    
    if (hourArray && ([hourArray count]>0))
        sectionCount++;
    if (dayArray && ([dayArray count]>0))
        sectionCount++;
    if (weekArray && ([weekArray count]>0))
        sectionCount++;
    if (monthArray && ([monthArray count]>0))
        sectionCount++;

    return MAX(sectionCount, 1);
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return [[self remindersForSection:section] count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[collectionView dequeueReusableCellWithReuseIdentifier:kBFReuseIDReminderCVCell forIndexPath:indexPath];
    
    // Configure the cell
    NSArray *reminderSectionArray = [self remindersForSection:indexPath.section];
    BFReminder *reminder = ([reminderSectionArray count]>indexPath.row)?[reminderSectionArray objectAtIndex:indexPath.row]:nil;
    
    if (reminder) {
        cell.messageLabel.text = reminder.message;
        cell.frequencyLabel.text = [NSString stringWithFormat:@"%ldx", (long)reminder.frequencyCount];
    }
    
    cell.layer.cornerRadius = 4.0f;
    
    return cell;
}

- (UICollectionReusableView *)collectionView:(UICollectionView *)collectionView viewForSupplementaryElementOfKind:(NSString *)kind atIndexPath:(NSIndexPath *)indexPath
{
    BFReminderCollectionViewAccessoryViewHeader *headerCell;
    
    if (indexPath && [kind isEqualToString:UICollectionElementKindSectionHeader]) {
        headerCell = (BFReminderCollectionViewAccessoryViewHeader *)[collectionView dequeueReusableSupplementaryViewOfKind:kind withReuseIdentifier:kBFReminderCVAccViewHeader forIndexPath:indexPath];
        
        NSArray *sectionReminderArray = [self remindersForSection:indexPath.section];
        if (sectionReminderArray && ([sectionReminderArray count]>0)) {
            BFReminder *firstReminder = (BFReminder *)[sectionReminderArray firstObject];
            
            NSString *headerTitle;
            switch (firstReminder.frequencyType) {
                case BFFrequencyHourly: headerTitle = @"Per uur"; break;
                case BFFrequencyDaily: headerTitle = @"Dagelijks"; break;
                case BFFrequencyWeekly: headerTitle = @"Wekelijks"; break;
                case BFFrequencyMonthly: headerTitle = @"Maandelijks"; break;
            }
            headerCell.headerTitleLabel.text = headerTitle;
        }
    }
    
    return headerCell;
}


#pragma mark - UICollectionViewDelegate


#pragma mark - Reminder Edit unwinding

- (IBAction)reminderEditFinished:(UIStoryboardSegue *)segue
{
    [self.collectionView reloadData];
    
    /*
    // Check to see if reminder needs scheduling
    BFReminderEditViewController *editVC = [segue sourceViewController];
    BFReminder *reminder = editVC.reminder;
    if ((!reminder.localNotificationFireDate) || ([reminder.localNotificationFireDate timeIntervalSinceNow]<0)) {
        BFAppDelegate *appDelegate = [[UIApplication sharedApplication] delegate];
        [appDelegate scheduleNextLocalNotificationForReminder:reminder];
    }*/
}


#pragma mark - Navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:kBFSegueAddReminder]) {
        // Create a new instance of the appropriate class, insert it into the array and pass it to editing view
        BFReminder *reminder = [[BFReminder alloc] init];
        reminder.message = @"Nieuwe reminder...";
        reminder.frequencyCount = 1;
        reminder.frequencyType = BFFrequencyDaily;
        
        [[BFReminderList sharedReminderList] addReminder:reminder];
        
        BFReminderEditViewController *editVC = (BFReminderEditViewController *)[segue destinationViewController];
        editVC.reminder = reminder;
        
    } else if ([[segue identifier] isEqualToString:kBFSegueReminderTapped]) {
        
        // Retrieve the reminder which has been tapped and pass it to the editing view
        NSIndexPath *tappedIndexPath = [[self.collectionView indexPathsForSelectedItems] firstObject];
        BFReminder *reminder = [[BFReminderList sharedReminderList] reminderAtIndex:tappedIndexPath.row];
        
        BFReminderEditViewController *editVC = (BFReminderEditViewController *)[segue destinationViewController];
        editVC.reminder = reminder;
    }
}


@end
