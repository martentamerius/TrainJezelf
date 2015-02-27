//
//  BreakFreeTests.m
//  BreakFreeTests
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "BFReminderList.h"
#import "BFReminder.h"
#import "BFReminder+UnitTest.h"

@interface BreakFreeTests : XCTestCase
@property (nonatomic, strong) BFReminderList *reminderList;
@property (nonatomic, strong) BFReminder *reminder;
@end


@implementation BreakFreeTests

- (void)setUp
{
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
    
}

- (void)tearDown
{
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testReminderDefaults
{
    // Init new reminder; check default values!
    BFReminder *reminder = [[BFReminder alloc] init];
    XCTAssertNotNil(reminder.uuid, @"Reminder UUID nil after init.");
    XCTAssertFalse([reminder isPaused], @"Reminder should not be paused after init.");
    
    // Check frequency string
    NSString *initFreqString = [NSString stringWithFormat:NSLocalizedString(@"day", @"Frequency type")];
    XCTAssertEqualObjects([reminder frequencyTypeString], initFreqString, @"Reminder should have frequency type %@ and not %@", initFreqString, [reminder frequencyTypeString]);
    
    // Check changing frequency string
    reminder.frequencyTypeString = NSLocalizedString(@"week", @"Frequency type");
    XCTAssertNotEqualObjects([reminder frequencyTypeString], initFreqString, @"Reminder should NOT have frequency type %@", initFreqString);
    
    // Check daily fire period string
    NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
    numberFormatter.minimumIntegerDigits = 2;
    numberFormatter.maximumFractionDigits = 0;
    NSString *dailyFirePeriod = [NSString stringWithFormat:NSLocalizedString(@"%@:%@ and %@:%@", @"Daily fire period string"), @(8), [numberFormatter stringFromNumber:@(0)], @(19), [numberFormatter stringFromNumber:@(0)]];
    XCTAssertEqualObjects([reminder dailyFirePeriodString], dailyFirePeriod, @"Reminder does not have correct daily fire period string.");
}

- (void)testReminderScheduling
{
    BFReminder *reminder = [[BFReminder alloc] init];
    
    // Test start of frequency period with some predefined dates
    NSCalendar *calendar = [NSCalendar autoupdatingCurrentCalendar];
    calendar.firstWeekday = BFDayOfWeek_Sunday;
    NSDateComponents *comps = [[NSDateComponents alloc] init];
    
    comps.day = 3;
    comps.month = 1;
    comps.year = 2015;
    comps.hour = reminder.dailyPeriodStartComponents.hour;
    comps.minute = reminder.dailyPeriodStartComponents.minute;
    NSDate *testStartDate = [calendar dateFromComponents:comps];
    
    // First day of month for testStartDate 03/01/2015 is 01/01/2015
    reminder.frequencyType = BFFrequencyMonthly;
    reminder.shouldFireDuringWeekends = NO;
    comps.day = 1;
    comps.month = 1;
    comps.year = 2015;
    NSDate *firstDayOfMonth = [calendar dateFromComponents:comps];
    
    NSDate *testStartOfPeriodDate = [reminder startOfPeriodWithDate:firstDayOfMonth];
    XCTAssertEqualObjects(testStartOfPeriodDate, firstDayOfMonth, @"Start of month for testdate is calculated incorrectly.");

    // First day of week, without weekends (!), for testStartDate 03/01/2015 (saturday) is 29/12/2014 (monday)
    reminder.frequencyType = BFFrequencyWeekly;
    reminder.shouldFireDuringWeekends = NO;
    comps.day = 29;
    comps.month = 12;
    comps.year = 2014;
    NSDate *firstDayOfWorkWeek = [calendar dateFromComponents:comps];
    
    testStartOfPeriodDate = [reminder startOfPeriodWithDate:testStartDate];
    XCTAssertEqualObjects(testStartOfPeriodDate, firstDayOfWorkWeek, @"Start of workweek for testdate is calculated incorrectly.");
    
    // First day of week, including weekends (!), for testStartDate 03/01/2015 (saturday) is 28/12/2014 (sunday)
    reminder.frequencyType = BFFrequencyWeekly;
    reminder.shouldFireDuringWeekends = YES;
    comps.day = 28;
    comps.month = 12;
    comps.year = 2014;
    NSDate *firstDayOfWeek = [calendar dateFromComponents:comps];
    
    testStartOfPeriodDate = [reminder startOfPeriodWithDate:testStartDate];
    XCTAssertEqualObjects(testStartOfPeriodDate, firstDayOfWeek, @"Start of week for testdate is calculated incorrectly.");
}


@end
