//
//  BFReminderEditViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderEditViewController.h"


@interface BFReminderEditViewController () <UITextViewDelegate>
@property (weak, nonatomic) IBOutlet UITextView *messageTextView;
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *frequencyCountButtons;
@property (strong, nonatomic) IBOutletCollection(UIButton) NSArray *frequencyTypeButtons;
@property (weak, nonatomic) IBOutlet UIImageView *imageView;
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
    
    // Set values form BFReminder object
    if (self.reminder) {
        self.messageTextView.text = self.reminder.message;
        
        if ((self.reminder.frequencyCount>0) && (self.reminder.frequencyCount<([self.frequencyCountButtons count]+1))) {
            NSString *frequencyCount = [NSString stringWithFormat:@"%ld", (long)self.reminder.frequencyCount];
            
            [self.frequencyCountButtons enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                UIButton *btn = (UIButton *)obj;
                if ([frequencyCount isEqualToString:btn.titleLabel.text]) {
                    [btn setSelected:YES];
                    *stop = YES;
                }
            }];
        }
        
        if ((self.reminder.frequencyType>0) && (self.reminder.frequencyType<[self.frequencyTypeButtons count])) {
            NSString *frequencyType = [self.reminder frequencyTypeString];
            
            [self.frequencyTypeButtons enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                UIButton *btn = (UIButton *)obj;
                if ([frequencyType caseInsensitiveCompare:btn.titleLabel.text] == NSOrderedSame) {
                    [btn setSelected:YES];
                    *stop = YES;
                }
            }];
        }
    }
}


#pragma mark - Reminder editing

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

- (void)selectButton:(UIButton *)newSelectedButton inOutletCollection:(NSArray *)outletColletion
{
    __block UIButton *oldSelectedButton;
    [outletColletion enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
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

- (void)textViewDidEndEditing:(UITextView *)textView
{
    self.reminder.message = textView.text;
}


#pragma mark - Navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([[segue identifier] isEqualToString:kBFSegueUnwindFromSaveReminderTapped]) {
        // Save user input
        if (self.reminder) {
            self.reminder.message = self.messageTextView.text;
            
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
        }
    }
}

@end
