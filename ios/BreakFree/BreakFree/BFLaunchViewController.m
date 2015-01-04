//
//  BFLaunchViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFLaunchViewController.h"
#import "BFReminder.h"

@interface BFLaunchViewController ()
@property (weak, nonatomic) IBOutlet UILabel *citationLabel;
@property (nonatomic, strong) BFReminder *reminderToShow;
@end

@implementation BFLaunchViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)showReminder:(BFReminder *)reminder
{
    // Actually displaying the reminder may happen later, when -viewWillAppear: is called...
    self.reminderToShow = reminder;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Start a timer for automatic push to reminders list
    __weak BFLaunchViewController *weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [weakSelf performSegueWithIdentifier:kBFSegueAutomaticLaunchViewToRemindersCV sender:self];
    });
}

- (void)viewWillAppear:(BOOL)animated
{
    if (self.reminderToShow) {
        self.citationLabel.text = self.reminderToShow.message;
    }
}

@end