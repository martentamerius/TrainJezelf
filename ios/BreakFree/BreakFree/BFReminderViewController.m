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
@property (atomic) NSUInteger countdown;
@end

@implementation BFReminderViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
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
    const NSArray *quotes = @[ @"Wees jezelf, er zijn al anderen genoeg.",
                               @"Laat zien wie je bent, los van verwachtingen, los van goedkeuring.",
                               @"Wie met zijn hoofd in de wolken loopt en met zijn voeten op de grond, is een waarlijk groot mens.",
                               @"Als je met beide benen op de grond staat, kom je geen stap verder.",
                               @"Wie met beide benen op de grond blijft staan, komt niet ver.",
                               @"Je kleiner voordoen dan je bent, bewijst de wereld geen dienst.",
                               @"Waar je ook staat, je staat altijd wel op iemands tenen.",
                               @"Iemand met een nieuw idee is een vreemde vogel, tot het idee blijkt te werken...",
                               @"Wie angst heeft voor de toekomst, heeft die toekomst al half bedorven.",
                               @"Jezelf afmeten naar je geringste daad is de kracht van de oceaan bepalen naar de luchtigheid van haar schuim.",
                               @"Wees niet bang een stukje van jezelf te geven; het groeit allemaal weer aan.",
                               @"Pas wanneer je ten einde raad je angst om de hals durft te vallen, zal hij je nooit meer naar de keel vliegen.",
                               @"Beken kleur. Wees wie je bent. Dan stralen je ogen.",
                               @"Zij die geen angst kennen, kennen geen moed. Want werkelijke moed is het overwinnen van de angst.",
                               @"Pluk de dag, en laat nog wat hangen voor morgen.",
                               @"Als je waagt, groeit je moed. Als je aarzelt, groeit je vrees.",
                               @"Angst voor morgen komt een dag te vroeg.",
                               @"Wie als kind is gekreukeld, moet als volwassene leren strijken.",
                               @"Je bent het resultaat van alle vroegere afbeeldingen die je voor jezelf geschilderd hebt. En je kunt altijd nieuwe schilderen.",
                               @"Ik ben geen mislukkeling. Ik heb alleen 10.000 manieren gevonden die niet werken.",
                               @"Ik kom op het leven af en het leven op mij, dat botst soms een beetje.",
                               @"Het is niet de berg die wij overwinnen maar onszelf.",
                               @"Niks moet, Alles kan." ];
    
    if (self.citationLabel) {
        NSUInteger randomQuoteIndex = arc4random_uniform((unsigned int)[quotes count]);
        NSString *randomQuote = quotes[randomQuoteIndex];
        
        // Show either the reminder message, or a random quote
        self.citationLabel.text = (message)?:randomQuote;
        
        // Increase the quote countdown timer
        self.countdown++;
        // Dispatch the dismissal of the reminder view on the main queue after a set period of time
        [self scheduleDelayedDismissal];
    }
}

- (void)scheduleDelayedDismissal
{
    __weak typeof(self) weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(kBFReminderViewPeriod * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (weakSelf.countdown == 0) {
            // Out of time!
            [weakSelf dismissReminderView:weakSelf];
        } else {
            // New random quotes or messages have been displayed; reschedule the dismissal!
            weakSelf.countdown--;
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

- (void)viewWillAppear:(BOOL)animated
{
    // Show a random image from asset catalog
    NSUInteger index = arc4random_uniform(kBFReminderImageCount);
    NSString *imageName = [NSString stringWithFormat:@"%03u", index];
    UIImage *randomImage = [UIImage imageNamed:imageName];
    if (randomImage)
        self.backgroundImage.image = randomImage;
    
    // Show the actual reminder message, or a random quote (when nil)
    [self showReminderMessage:(self.reminderToShow)?self.reminderToShow.message:nil];
    
    [super viewWillAppear:animated];
}

@end
