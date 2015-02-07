//
//  BFLaunchViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderViewController.h"
#import "BFReminder.h"

@interface BFReminderViewController ()
@property (weak, nonatomic) IBOutlet UIImageView *backgroundImage;
@property (weak, nonatomic) IBOutlet UILabel *citationLabel;
@property (nonatomic, strong) BFReminder *reminderToShow;
@property (atomic) NSDate *timeToDismiss;
@end

@implementation BFReminderViewController

static const NSArray *_quotes;

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
    if ((self = [super initWithCoder:aDecoder])) {
        // Initialize the quotes-array from the localized quotes-plist
        NSString *errorDesc = nil;
        NSPropertyListFormat format;
        NSString *plistPath = [[NSBundle mainBundle] pathForResource:@"Quotes" ofType:@"plist"];
        NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
        NSDictionary *plistDict = (NSDictionary *)[NSPropertyListSerialization propertyListFromData:plistXML
                                                   mutabilityOption:NSPropertyListImmutable
                                                   format:&format errorDescription:&errorDesc];
        if (!plistDict) {
            DLog(@"Error reading quotes plist with format %@ from %@. Error description: %@", @(format), plistPath, errorDesc);
        } else {
            NSArray *quoteArray = [plistDict objectForKey:@"Quotes"];
            _quotes = (quoteArray)?:[NSArray array];
        }
    }
    
    return self;
}

- (BOOL)prefersStatusBarHidden
{
    return YES;
}

- (BOOL)canBecomeFirstResponder
{
    // To support shake gesture
    return YES;
}

- (void)motionEnded:(UIEventSubtype)motion withEvent:(UIEvent *)event
{
    if (motion == UIEventSubtypeMotionShake) {
        [self showReminderMessage:nil];
    } 
}

- (void)showReminderMessage:(NSString *)message
{
    if (self.citationLabel) {
        NSUInteger randomQuoteIndex = arc4random_uniform((unsigned int)[_quotes count]);
        NSString *randomQuote = _quotes[randomQuoteIndex];
        
        // Show either the reminder message, or a random quote
        self.citationLabel.text = (message)?:randomQuote;
        [self.view setNeedsUpdateConstraints];
        
        // Show a (new) random image from asset catalog
        NSUInteger index = arc4random_uniform(kBFReminderImageCount);
        NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
        numberFormatter.minimumIntegerDigits = 3;
        numberFormatter.maximumFractionDigits = 0;
        NSMutableString *imageName = [NSMutableString stringWithFormat:@"%@_", (UIInterfaceOrientationIsLandscape(self.interfaceOrientation))?@"l":@"p"];
        [imageName appendString:[numberFormatter stringFromNumber:@(index)]];
        UIImage *randomImage = [UIImage imageNamed:imageName];
        if (randomImage)
            self.backgroundImage.image = randomImage;

        // Set dismiss date/time
        self.timeToDismiss = [NSDate dateWithTimeIntervalSinceNow:kBFReminderViewPeriod];
        
        // Dispatch the dismissal of the reminder view on the main queue after a set period of time
        [self scheduleDelayedDismissal];
    }
}

- (void)scheduleDelayedDismissal
{
    NSTimeInterval dispatchTimeInterval = [self.timeToDismiss timeIntervalSinceNow];
    
    __weak typeof(self) weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(dispatchTimeInterval * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if ([weakSelf.timeToDismiss timeIntervalSinceNow]<=1) {
            // Out of time!
            [weakSelf dismissReminderView:weakSelf];
        } else {
            // New random quotes or messages have been displayed; reschedule the delayed dismissal!
            [weakSelf scheduleDelayedDismissal];
        }
    });
}

- (void)showReminder:(BFReminder *)reminder
{
    // Actually displaying the reminder may happen later, when -viewWillAppear: is called...
    self.reminderToShow = reminder;
}

- (IBAction)dismissReminderView:(id)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}


#pragma mark - View lifecycle

- (void)viewDidRotate:(NSNotification *)notification
{
    // Refresh image (landscape/portrait)
    [self showReminderMessage:self.citationLabel.text];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    // Show the actual reminder message, or a random quote (when nil)
    [self showReminderMessage:(self.reminderToShow)?self.reminderToShow.message:nil];
    
    // Observe device rotations
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(viewDidRotate:) name:UIDeviceOrientationDidChangeNotification object:nil];
    
    // Hide the status bar until the fullscreen view is dismissed
    [[UIApplication sharedApplication] setStatusBarHidden:YES withAnimation:UIStatusBarAnimationSlide];
}

- (void)viewWillDisappear:(BOOL)animated
{
    // Show status bar after dismissing the fullscreen view
    [[UIApplication sharedApplication] setStatusBarHidden:NO withAnimation:UIStatusBarAnimationSlide
     ];
    
    // Clean up
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    [super viewWillDisappear:animated];
}

@end
