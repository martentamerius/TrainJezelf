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


@interface BFReminderEditViewController () <UITextViewDelegate>
@property (weak, nonatomic) IBOutlet UITextView *messageTextView;
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *frequencyCountButtons;
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *frequencyTypeButtons;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;
@property (weak, nonatomic) IBOutlet UIView *frequencyBackgroundView;
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
    // Do any additional setup after loading the view.
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
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
        self.messageTextView.text = @"Waaraan wil je herinnerd worden?";
        self.messageTextView.textColor = [UIColor lightGrayColor];
    }
}

- (NSString *)messageText
{
    if ([self.messageTextView.text isEqualToString:@"Waaraan wil je herinnerd worden?"]) {
        return nil;
    } else {
        return [NSString stringWithString:self.messageTextView.text];
    }
}

- (IBAction)imageViewTapped:(UITapGestureRecognizer *)sender
{
    // TODO: Images!
}

- (IBAction)frequencyCountButtonTapped:(UIButton *)sender
{
    [self selectButton:sender inOutletCollection:self.frequencyCountButtons];
}

- (IBAction)frequencyTypeButtonTapped:(UIButton *)sender
{    
    [self selectButton:sender inOutletCollection:self.frequencyTypeButtons];
}

- (void)selectButton:(UIButton *)newSelectedButton inOutletCollection:(NSArray *)outletCollection
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


#pragma mark - UITextViewDelegate

- (BOOL)textViewShouldBeginEditing:(UITextView *)textView
{
    if ([self.messageTextView.text isEqualToString:@"Waaraan wil je herinnerd worden?"]) {
        self.messageTextView.text = @"";
        self.messageTextView.textColor = [UIColor darkTextColor];
    }
    return YES;
}


#pragma mark - Swipe down gesture or tap outside textview

- (IBAction)userWantsToDismissKeyboard:(UIGestureRecognizer *)sender
{
    // Dismiss keyboard, if present
    if ([self.messageTextView isFirstResponder]) {
        [self.messageTextView resignFirstResponder];
    }
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

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:kBFSegueReminderEditToReminder]) {
        BFReminder *reminder = ((BFNavigationController *)self.navigationController).receivedReminder;
        [[segue destinationViewController] showReminder:reminder];
    }
    if ([[segue identifier] isEqualToString:kBFSegueUnwindFromSaveReminderTapped]) {
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
                // Saving the changes to the user defaults will suffice
                [[BFReminderList sharedReminderList] saveRemindersToUserDefaults];
            } else {
                // The reminder has never been saved before; add the reminder to the reminder list!
                [[BFReminderList sharedReminderList] addReminder:self.reminder];
            }
            
            // Also check if any reminders need any local notification (re-)scheduling
            [[BFReminderList sharedReminderList] checkSchedulingOfLocalNotificationsForAllReminders];
        }
    }
}

@end
