//
//  BFReminderEditViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderEditViewController.h"

@interface BFReminderEditViewController () <UITextViewDelegate, UIActionSheetDelegate>
@property (weak, nonatomic) IBOutlet UITextView *messageTextView;
@property (weak, nonatomic) IBOutlet UILabel *frequencyCountLabel;
@property (weak, nonatomic) IBOutlet UILabel *frequencyTypeLabel;
@property (nonatomic) NSInteger selectedFrequencyType;

@property (weak, nonatomic) IBOutlet UIStepper *frequencyCountStepper;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;

@property (weak, nonatomic) IBOutlet UIButton *saveButton;
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
    // Set values form BFReminder object
    if (self.reminder) {
        self.messageTextView.text = self.reminder.message;
        self.frequencyCountLabel.text = [NSString stringWithFormat:@"%d", self.reminder.frequencyCount];
        self.frequencyCountStepper.value = self.reminder.frequencyCount;
        self.frequencyCountStepper.stepValue = 1.0;
        self.frequencyTypeLabel.text = [self.reminder frequencyTypeString];
        self.selectedFrequencyType = self.reminder.frequencyType;
    }
}


#pragma mark - Reminder editing

- (IBAction)frequencyCountStepperValueChanged:(UIStepper *)sender
{
    if (sender.value>1.0) {
        self.reminder.frequencyCount = sender.value;
        self.frequencyCountLabel.text = [NSString stringWithFormat:@"%d", self.reminder.frequencyCount];
    } else {
        sender.value = 1.0;
    }
}

- (IBAction)frequencyTypeLabelTapped:(UITapGestureRecognizer *)sender
{
    UIActionSheet *actionSheet = [[UIActionSheet alloc] initWithTitle:@"Per ..." delegate:self cancelButtonTitle:nil destructiveButtonTitle:nil otherButtonTitles:@"uur", @"dag", @"maand", nil];
    
    [actionSheet showFromRect:self.saveButton.frame inView:self.view animated:YES];
}

- (IBAction)imageViewTapped:(UITapGestureRecognizer *)sender
{
    
}

- (IBAction)saveButtonTapped:(UIButton *)sender
{
    // Save user input
    if (self.reminder) {
        self.reminder.message = self.messageTextView.text;
        self.reminder.frequencyCount = self.frequencyCountStepper.value;
        self.reminder.frequencyType = self.selectedFrequencyType;
        
        // Tell delegate that the reminder has been saved
        if (self.delegate)
            [self.delegate reminderSaveButtonTappedInEditViewController:self];
    }
    
    // And perform unwinding segue
    UIStoryboardSegue *unwindSegue = [self segueForUnwindingToViewController:self.parentViewController fromViewController:self identifier:kBTSegueReminderSaveTapped];
    [unwindSegue perform];
}


#pragma mark - UITextViewDelegate

- (void)textViewDidEndEditing:(UITextView *)textView
{
    self.reminder.message = textView.text;
}


#pragma mark - UIActionSheetDelegate

- (void)actionSheet:(UIActionSheet *)actionSheet didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    self.selectedFrequencyType = buttonIndex;
    self.frequencyTypeLabel.text = [self.reminder frequencyTypeString];
}


#pragma mark - Navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:kBTSegueReminderSaveTapped]) {
        
    }
}

@end
