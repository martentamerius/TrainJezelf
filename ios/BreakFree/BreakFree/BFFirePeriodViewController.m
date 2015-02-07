//
//  BFFirePeriodViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 27-01-15.
//  Copyright (c) 2015 Tamerius & Bos. All rights reserved.
//

#import "BFFirePeriodViewController.h"

@interface BFFirePeriodViewController () <UIPickerViewDataSource, UIPickerViewDelegate>
@property (weak, nonatomic) IBOutlet UIPickerView *dailyPeriodStartPicker;
@property (weak, nonatomic) IBOutlet UIPickerView *dailyPeriodEndPicker;

@property (nonatomic) NSDateComponents *startTimeComponents;
@property (nonatomic) NSDateComponents *endTimeComponents;
@end


@implementation BFFirePeriodViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    if (self.navigationController.parentViewController &&
        [self.navigationController.parentViewController isKindOfClass:[UIPopoverController class]]) {
        // Remove the cancel-button (left barbuttonitem) if this controller is presented by a popover controller
        self.navigationItem.leftBarButtonItem = nil;
    }
    
    // Set start and end timeintervals in respective date pickers
    if (self.dailyPeriodStartPicker) {
        if (self.startTimeComponents) {
            NSInteger startHour = (self.startTimeComponents.hour != NSUndefinedDateComponent)?self.startTimeComponents.hour:0;
            NSInteger startMinute = (self.startTimeComponents.minute != NSUndefinedDateComponent)?self.startTimeComponents.minute:0;
            [self.dailyPeriodStartPicker selectRow:startHour inComponent:0 animated:NO];
            [self.dailyPeriodStartPicker selectRow:floorf(startMinute / 5) inComponent:1 animated:NO];
            
        } else {
            
            // Default: 8:00h
            [self.dailyPeriodStartPicker selectRow:8 inComponent:0 animated:NO];
            [self.dailyPeriodStartPicker selectRow:0 inComponent:1 animated:NO];
        }
    }
    if (self.dailyPeriodEndPicker) {
        if (self.endTimeComponents) {
            NSInteger endHour = (self.endTimeComponents.hour != NSUndefinedDateComponent)?self.endTimeComponents.hour:0;
            NSInteger endMinute = (self.endTimeComponents.minute != NSUndefinedDateComponent)?self.endTimeComponents.minute:0;
            
            [self.dailyPeriodEndPicker selectRow:endHour inComponent:0 animated:NO];
            [self.dailyPeriodEndPicker selectRow:floorf(endMinute / 5) inComponent:1 animated:NO];
            
        } else {
            
            // Default: 19:00h
            [self.dailyPeriodStartPicker selectRow:19 inComponent:0 animated:NO];
            [self.dailyPeriodStartPicker selectRow:0 inComponent:1 animated:NO];
        }
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)initTimePickersWithStartDateComponents:(NSDateComponents *)startDateComps andEndDateComponents:(NSDateComponents *)endDateComps
{
    self.startTimeComponents = startDateComps;
    self.endTimeComponents = endDateComps;
}


#pragma mark - Button handling

- (IBAction)cancelTapped:(UIBarButtonItem *)sender
{
    if (self.delegate) {
        // Tell the delegate the controller was cancelled
        [self.delegate firePeriodViewController:self didFinish:NO withStartDateComponents:nil andEndDateComponents:nil];
    }
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)saveTapped:(UIBarButtonItem *)sender
{
    if (self.delegate) {
        // Convert selected time to a NSDateComponent
        NSDateComponents *startDateComps = [[NSDateComponents alloc] init];
        startDateComps.hour = [self.dailyPeriodStartPicker selectedRowInComponent:0];
        startDateComps.minute = 5 * [self.dailyPeriodStartPicker selectedRowInComponent:1];
        startDateComps.nanosecond = 0;
        
        NSDateComponents *endDateComps = [[NSDateComponents alloc] init];
        endDateComps.hour = [self.dailyPeriodEndPicker selectedRowInComponent:0];
        endDateComps.minute = 5 * [self.dailyPeriodEndPicker selectedRowInComponent:1];
        endDateComps.nanosecond = 0;
        
        // Tell the delegate about the end date
        [self.delegate firePeriodViewController:self didFinish:YES withStartDateComponents:startDateComps andEndDateComponents:endDateComps];
    }
    [self dismissViewControllerAnimated:YES completion:nil];
}


#pragma mark - UIPickerView DataSource

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
    return 2;
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
    NSInteger rowCount = (component == 0)?24:12;
    return rowCount;
}


#pragma mark - UIPickerView Delegate

- (NSAttributedString *)pickerView:(UIPickerView *)pickerView attributedTitleForRow:(NSInteger)row forComponent:(NSInteger)component
{
    // If the intended component if the minutes component: 0-59 mins with interval of 5 mins
    NSInteger numberInTitle = (component == 0)?row:(row * 5);
    NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
    numberFormatter.minimumIntegerDigits = 2;
    numberFormatter.maximumFractionDigits = 0;
    NSString *title;
    if (component == 0) {
        title = [NSString stringWithFormat:@"%@", @(numberInTitle)];
    } else {
        title = [numberFormatter stringFromNumber:@(numberInTitle)];
    }
    
    NSMutableParagraphStyle *style = [[NSMutableParagraphStyle alloc] init];
    style.alignment = NSTextAlignmentRight;
    style.headIndent = (component == 0)?0.0f:2.0f;
    UIFont *font = [UIFont preferredFontForTextStyle:UIFontTextStyleCaption1];
    NSDictionary *attrDict = @{ NSFontAttributeName: font, NSParagraphStyleAttributeName: style };
    
    NSAttributedString *attrTitle = [[NSAttributedString alloc] initWithString:title attributes:attrDict];
    
    return attrTitle;
}

- (CGFloat)pickerView:(UIPickerView *)pickerView widthForComponent:(NSInteger)component
{
    return 60.0f;
}

@end
