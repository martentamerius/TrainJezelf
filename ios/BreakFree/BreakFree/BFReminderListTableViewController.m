//
//  BFReminderListTableViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderListTableViewController.h"
#import "BFReminderList.h"
#import "BFReminderListTableViewCell.h"
#import "BFReminderEditViewController.h"


@interface BFReminderListTableViewController () <BFReminderEditViewControllerDelegate>
@end


@implementation BFReminderListTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialisation
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    //self.navigationItem.rightBarButtonItem = self.editButtonItem;
//    
//    // Register tableviewcell identifiers before dequeueing.
//    [self.tableView registerClass:[BFReminderListTableViewCell class] forCellReuseIdentifier:kBFReminderListTableViewCell];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [[BFReminderList sharedReminderList] count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    BFReminderListTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:kBFReminderListTableViewCell forIndexPath:indexPath];

    // Configure the cell
    BFReminder *reminder = [[BFReminderList sharedReminderList] reminderAtIndex:indexPath.row];
    
    cell.messageLabel.text = reminder.message;
    cell.frequencyLabel.text = [NSString stringWithFormat:@"%dx %@", reminder.frequencyCount, [reminder frequencyTypeString]];
    
    return cell;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source and from table view
        [[BFReminderList sharedReminderList] removeReminderAtIndex:indexPath.row];
        
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }
}

- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
    // Swap reminders in list
    [[BFReminderList sharedReminderList] exchangeReminderAtIndex:fromIndexPath.row withReminderAtIndex:toIndexPath.row];
    
    // Reload rows at specified indexpaths
    [self.tableView reloadRowsAtIndexPaths:@[ fromIndexPath, toIndexPath ] withRowAnimation:UITableViewRowAnimationAutomatic];
}

- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return YES;
}


#pragma mark - BFReminderEditViewControllerDelegate

- (void)reminderSaveButtonTappedInEditViewController:(BFReminderEditViewController *)viewController
{
    [self.tableView reloadData];
}


#pragma mark - Navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:kBTSegueAddReminder]) {
        // Create a new instance of the appropriate class, insert it into the array and pass it to editing view
        BFReminder *reminder = [[BFReminder alloc] init];
        reminder.message = @"Nieuwe reminder...";
        reminder.frequencyCount = 1;
        reminder.frequencyType = BFFrequencyDaily;
        
        [[BFReminderList sharedReminderList] addReminder:reminder];
        
        BFReminderEditViewController *editVC = (BFReminderEditViewController *)[segue destinationViewController];
        editVC.reminder = reminder;
        editVC.delegate = self;
        
    } else if ([[segue identifier] isEqualToString:kBTSegueReminderTapped]) {
        // Retrieve the reminder which has been tapped and pass it to the editing view
        NSIndexPath *tappedIndexPath = [self.tableView indexPathForSelectedRow];
        BFReminder *reminder = [[BFReminderList sharedReminderList] reminderAtIndex:tappedIndexPath.row];
        
        BFReminderEditViewController *editVC = (BFReminderEditViewController *)[segue destinationViewController];
        editVC.reminder = reminder;
        editVC.delegate = self;
    }
}

@end
