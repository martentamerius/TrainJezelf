//
//  BFReminderEditViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderEditViewController.h"
#import "BFReminderViewController.h"
#import "BFNavigationController.h"
#import "BFReminderList.h"
#import "BFFirePeriodViewController.h"
#import "BFReminder.h"

@interface BFReminderEditViewController () <UITextViewDelegate, UITextFieldDelegate, BFFirePeriodViewControllerDelegate, UIAlertViewDelegate>
@property (weak, nonatomic) IBOutlet UITextView *messageTextView;
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *frequencyCountButtons;
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *frequencyTypeButtons;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;
@property (weak, nonatomic) IBOutlet UIView *frequencyBackgroundView;
@property (weak, nonatomic) IBOutlet UITextField *dailyFirePeriodTextField;
@property (weak, nonatomic) IBOutlet UISwitch *weekendSwitch;
@end


@implementation BFReminderEditViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.frequencyBackgroundView.layer.cornerRadius = 4.0f;
    self.messageTextView.layer.cornerRadius = 4.0f;
    
    // Set values form BFReminder object
    if (self.reminder) {
        [self setMessageText:self.reminder.message];
        
        if ((self.reminder.frequencyCount>0) && (self.reminder.frequencyCount<([self.frequencyCountButtons count]+1))) {
            NSString *frequencyCount = [NSString stringWithFormat:@"%@", @(self.reminder.frequencyCount)];
            
            [self.frequencyCountButtons enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                UIButton *btn = (UIButton *)obj;
                if ([frequencyCount isEqualToString:btn.titleLabel.text]) {
                    [btn setSelected:YES];
                    *stop = YES;
                }
            }];
        }
        
        if (self.reminder.frequencyType<[self.frequencyTypeButtons count]) {
            NSString *frequencyType = [self.reminder frequencyTypeString];
            
            [self.frequencyTypeButtons enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                UIButton *btn = (UIButton *)obj;
                if ([frequencyType caseInsensitiveCompare:btn.titleLabel.text] == NSOrderedSame) {
                    [btn setSelected:YES];
                    *stop = YES;
                }
            }];
        }
        
        self.weekendSwitch.on = self.reminder.shouldFireDuringWeekends;
        self.dailyFirePeriodTextField.text = [self.reminder dailyFirePeriodString];
        [self.view setNeedsUpdateConstraints];
        
    } else {
        
        // Show placeholder message
        [self setMessageText:nil];
    }
}


#pragma mark - Reminder editing

- (void)setMessageText:(NSString *)message
{
    if (message) {
        self.messageTextView.text = self.reminder.message;
        self.messageTextView.textColor = [UIColor darkTextColor];
    } else {
        self.messageTextView.text = NSLocalizedString(@"Of what do you want to be reminded?", @"Reminder text placeholder message");
        self.messageTextView.textColor = [UIColor lightGrayColor];
    }
}

- (NSString *)messageText
{
    if ([self.messageTextView.text isEqualToString:NSLocalizedString(@"Of what do you want to be reminded?", @"Reminder text placeholder message")]) {
        return nil;
    } else {
        return [NSString stringWithString:self.messageTextView.text];
    }
}

- (IBAction)frequencyCountButtonTapped:(UIButton *)sender
{
    [self selectFrequencyCountButton:sender inOutletCollection:self.frequencyCountButtons];
}

- (IBAction)frequencyTypeButtonTapped:(UIButton *)sender
{    
    [self selectFrequencyCountButton:sender inOutletCollection:self.frequencyTypeButtons];
}

- (void)selectFrequencyCountButton:(UIButton *)newSelectedButton inOutletCollection:(NSArray *)outletCollection
{
    __block UIButton *oldSelectedButton;
    [outletCollection enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
        UIButton *btn = (UIButton *)obj;
        if (btn.isSelected) {
            oldSelectedButton = btn;
            *stop = YES;
        }
    }];
    
    if (oldSelectedButton) {
        [oldSelectedButton setSelected:NO];
    }
    [newSelectedButton setSelected:YES];
}

- (IBAction)weekendSwitchValueChanged:(UISwitch *)sender
{
    if (self.reminder) {
        self.reminder.shouldFireDuringWeekends = self.weekendSwitch.on;
    }
}


#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField
{
    if (textField != self.dailyFirePeriodTextField)
        return YES;
    
    // Only for the daily fire period textfield:
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        // iPads: show time picker in popover
        [self performSegueWithIdentifier:kBFSegueChooseDailyFirePeriodAsPopover sender:self];
    } else {
        // iPhones: show time picker as a modal sheet
        [self performSegueWithIdentifier:kBFSegueChooseDailyFirePeriodModally sender:self];
    }

    // Finally: don't let the daily fire textfield become first responder!
    return NO;
}


#pragma mark - BFFirePeriodViewControllerDelegate

- (void)firePeriodViewController:(BFFirePeriodViewController *)viewController didFinish:(BOOL)finish withStartDateComponents:(NSDateComponents *)startDateComps andEndDateComponents:(NSDateComponents *)endDateComps
{
    if (finish && self.reminder) {
        self.reminder.dailyPeriodStartComponents = startDateComps;
        self.reminder.dailyPeriodEndComponents = endDateComps;
        
        // Refresh daily fire time textfield
        self.dailyFirePeriodTextField.text = [self.reminder dailyFirePeriodString];
        [self.view setNeedsUpdateConstraints];
    }
}


#pragma mark - UITextViewDelegate

- (BOOL)textViewShouldBeginEditing:(UITextView *)textView
{
    if ([self.messageTextView.text isEqualToString:NSLocalizedString(@"Of what do you want to be reminded?", @"Reminder text placeholder message")]) {
        // Do some magic to remove the placeholder text
        self.messageTextView.text = @"";
        self.messageTextView.textColor = [UIColor darkTextColor];
    }
    return YES;
}

- (void)textViewDidEndEditing:(UITextView *)textView
{
    if ([self.messageTextView.text length]==0) {
        // If the user has removed all text, put in the placeholder text
        self.messageTextView.text = NSLocalizedString(@"Of what do you want to be reminded?", @"Reminder text placeholder message");
        self.messageTextView.textColor = [UIColor lightGrayColor];
    }
}


#pragma mark - Swipe down gesture or tap outside textview

- (IBAction)userWantsToDismissKeyboard:(UIGestureRecognizer *)sender
{
    if ([self.messageTextView isFirstResponder]) {
        // User tapped or swiped down on text view while it is first responder: dismiss keyboard
        [self.messageTextView resignFirstResponder];
    } else if ([sender isKindOfClass:[UITapGestureRecognizer class]]) {
        // If user tapped on inactive message text view: make it first responder again!
        [self.messageTextView becomeFirstResponder];
    }
}


#pragma mark - UIAlertView delegate

- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    // This alertview was shown because the user did not enter a message in the UITextView.
    // The user dismissed the alertview; now automatically let the UITextView become first responder.
    [self.messageTextView becomeFirstResponder];
}


#pragma mark - Shake gesture

- (BOOL)canBecomeFirstResponder
{
    // To support shake gesture
    return YES;
}

- (void)motionEnded:(UIEventSubtype)motion withEvent:(UIEvent *)event
{
    if (motion == UIEventSubtypeMotionShake) {
        [self performSegueWithIdentifier:kBFSegueReminderEditToReminderShake sender:self];
    }
}


#pragma mark - Navigation

- (BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender
{
    if (![identifier isEqualToString:kBFSegueUnwindFromSaveReminderTapped])
        return YES;
    
    // If the save button has been tapped, check if the reminder text message has been entered
    if (([self.messageTextView.text length]>0) &&
        (![self.messageTextView.text isEqualToString:NSLocalizedString(@"Of what do you want to be reminded?", @"Reminder text placeholder message")]))
        return YES;

    // If not: show an alert and cancel the segue transition
    NSString *alertTitle = NSLocalizedString(@"Warning", @"Empty message alertview title");
    NSString *alertMessage = NSLocalizedString(@"The message text is empty.", @"Empty message alertview message");
    NSString *alertButtonTitle = NSLocalizedString(@"OK", @"Empty message alertview button title");
    
    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:alertTitle message:alertMessage delegate:self
                                              cancelButtonTitle:alertButtonTitle otherButtonTitles:nil];
    [alertView show];
    
    return NO;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:kBFSegueReminderEditToReminder]) {
        BFReminder *reminder = ((BFNavigationController *)self.navigationController).receivedReminder;
        [[segue destinationViewController] showReminder:reminder];
    }
    if ([segue.identifier isEqualToString:kBFSegueUnwindFromSaveReminderTapped]) {
        // Save user input
        if (self.reminder && [self messageText]) {
            self.reminder.message = [self messageText];
            
            __block NSInteger frequencyCount = 0;
            [self.frequencyCountButtons enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                UIButton *btn = (UIButton *)obj;
                if (btn.isSelected) {
                    NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
                    frequencyCount = [[numberFormatter numberFromString:btn.titleLabel.text] integerValue];
                    *stop = YES;
                }
            }];
            self.reminder.frequencyCount = frequencyCount;
            
            __block NSString *frequencyTypeString;
            [self.frequencyTypeButtons enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                UIButton *btn = (UIButton *)obj;
                if (btn.isSelected) {
                    frequencyTypeString = btn.titleLabel.text;
                    *stop = YES;
                }
            }];
            if (frequencyTypeString)
                [self.reminder setFrequencyTypeString:frequencyTypeString];
            
            // Check if the reminder is already saved into the reminder list
            if ([[BFReminderList sharedReminderList] reminderWithUUID:self.reminder.uuid]) {
                // Save the changes to the user defaults
                [[BFReminderList sharedReminderList] saveRemindersToUserDefaults];
                // And adjust the scheduled local notifications
                [self.reminder scheduleLocalNotificationsForCurrentReminder];
            } else {
                // The reminder has never been saved before; add the reminder to the reminder list!
                [[BFReminderList sharedReminderList] addReminder:self.reminder];
            }
        }
    }
    if ([segue.identifier isEqualToString:kBFSegueChooseDailyFirePeriodAsPopover] || [[segue identifier] isEqualToString:kBFSegueChooseDailyFirePeriodModally]) {
        
        if (self.reminder) {
            // Set initial start and end time interval of the reminder in the BFFirePeriodViewController
            UINavigationController *destinationVC = segue.destinationViewController;
            BFFirePeriodViewController *firePeriodVC = (BFFirePeriodViewController *)[destinationVC topViewController];
            
            firePeriodVC.delegate = self;
            [firePeriodVC initTimePickersWithStartDateComponents:self.reminder.dailyPeriodStartComponents andEndDateComponents:self.reminder.dailyPeriodEndComponents];
        }
    }
}

@end
