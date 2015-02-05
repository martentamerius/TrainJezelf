//
//  BFReminderCollectionViewController.m
//  BreakFree
//
//  Created by Marten Tamerius on 28-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderCollectionViewController.h"
#import "BFNavigationController.h"
#import "BFReminderViewController.h"
#import "BFReminderList.h"
#import "BFReminderEditViewController.h"
#import "BFReminderCollectionViewCell.h"
#import "BFAppDelegate.h"


@interface BFReminderCollectionViewController () <UICollectionViewDataSource, UICollectionViewDelegate, UICollectionViewDelegateFlowLayout, UIGestureRecognizerDelegate>
@property (nonatomic, strong) NSIndexPath *longPressedIndexPath;
@property (nonatomic, strong) UIBarButtonItem *playAllBarButton;
@property (nonatomic, strong) UIBarButtonItem *pauseAllBarButton;
@end

@implementation BFReminderCollectionViewController

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
    if ((self = [super initWithCoder:aDecoder])) {
        // Custom initialization
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(viewDidRotate:) name:UIDeviceOrientationDidChangeNotification object:nil];
        
        self.playAllBarButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemPlay target:self action:@selector(pauseOrPlayAllReminders:)];
        self.pauseAllBarButton = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemPause target:self action:@selector(pauseOrPlayAllReminders:)];
    }
    return self;
}

- (void)dealloc
{
    // Clean up notification center observing
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidRotate:(NSNotification *)notification
{
    // Recalculate layout when device was rotated
    [self.collectionViewLayout invalidateLayout];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self correctLeftNavigationBarItem];
}


#pragma mark - Pausing reminders

- (void)correctLeftNavigationBarItem
{
    // Set left barbutton item to pause (default) or play (if all reminders are paused)
    NSArray *reminderList = [[BFReminderList sharedReminderList] reminderList];
    __block BOOL showPausedButton = ([reminderList count]==0);
    [reminderList enumerateObjectsUsingBlock:^(BFReminder *reminder, NSUInteger idx, BOOL *stop) {
        if (![reminder isPaused]) {
            showPausedButton = YES;
            *stop = YES;
        }
    }];
    
    self.navigationItem.leftBarButtonItem = (showPausedButton)?self.pauseAllBarButton:self.playAllBarButton;
    self.navigationItem.leftBarButtonItem.enabled = ([reminderList count]>0);
}

- (IBAction)pauseOrPlayAllReminders:(UIBarButtonItem *)sender
{
    BOOL pauseAll = (sender == self.pauseAllBarButton);
    
    [[[BFReminderList sharedReminderList] reminderList] enumerateObjectsUsingBlock:^(BFReminder *reminder, NSUInteger idx, BOOL *stop) {
        reminder.paused = pauseAll;
    }];
    [[BFReminderList sharedReminderList] saveRemindersToUserDefaults];
    
    // Reload the currently visible items to show/hide pause image in background
    [self.collectionView reloadItemsAtIndexPaths:[self.collectionView indexPathsForVisibleItems]];
    
    if (self.longPressedIndexPath) {
        // Stop wiggling
        BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.longPressedIndexPath];
        if (cell) {
            [cell stopWiggling];
        }
        self.longPressedIndexPath = nil;
    }
    
    [self correctLeftNavigationBarItem];
}

- (IBAction)trashSelectedReminder:(UIGestureRecognizer *)sender
{
    NSIndexPath *indexPathToDelete;
    
    if (self.longPressedIndexPath) {
        // Set initial indexpath to delete to the wiggling cell and in any case: stop wiggling!
        indexPathToDelete = self.longPressedIndexPath;
        BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.longPressedIndexPath];
        [cell stopWiggling];
    }
    if ([sender isKindOfClass:[UISwipeGestureRecognizer class]]) {
        // If the gesture was a swipe on a different cell, get the swiped cell's indexpath
        CGPoint tapPoint = [sender locationInView:self.collectionView];
        indexPathToDelete = [self.collectionView indexPathForItemAtPoint:tapPoint];
    }
    
    if (indexPathToDelete) {
        BFReminder *reminder = [[BFReminderList sharedReminderList] reminderAtIndex:indexPathToDelete.row];
        if (reminder)
            [[BFReminderList sharedReminderList] removeReminder:reminder];
        
        [self.collectionView deleteItemsAtIndexPaths:@[ indexPathToDelete ]];
    }

    [self correctLeftNavigationBarItem];
}

- (IBAction)playOrPauseSelectedReminder:(UIGestureRecognizer *)sender
{
    if (self.longPressedIndexPath) {
        BFReminder *reminder = [[BFReminderList sharedReminderList] reminderAtIndex:self.longPressedIndexPath.row];
        BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.longPressedIndexPath];
        
        if (cell && reminder) {
            reminder.paused = (![reminder isPaused]);            
            [[BFReminderList sharedReminderList] saveRemindersToUserDefaults];
            
            // Show/hide the pause background image
            cell.pauseBackgroundImageView.hidden = (![reminder isPaused]);
        }
        
        self.longPressedIndexPath = nil;
        [cell stopWiggling];
    }
    
    [self correctLeftNavigationBarItem];
}

- (IBAction)longPressActivated:(UILongPressGestureRecognizer *)sender
{
    if (self.longPressedIndexPath) {
        // Stop wiggling of currently selected cell
        BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.longPressedIndexPath];
        if (cell) {
            [cell stopWiggling];
        }
    }
    
    // Get the indexpath of the long pressed cell
    CGPoint tapPoint = [sender locationInView:self.collectionView];
    self.longPressedIndexPath = [self.collectionView indexPathForItemAtPoint:tapPoint];
    
    if (self.longPressedIndexPath) {
        // Show wriggling animation
        BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.longPressedIndexPath];
        if (cell) {
            [cell startWiggling];
        }
    }
}

- (IBAction)handleUserTap:(UITapGestureRecognizer *)sender
{
    CGPoint tapPoint = [sender locationInView:self.collectionView];
    NSIndexPath *tappedIndexPath = [self.collectionView indexPathForItemAtPoint:tapPoint];
    
    if (tappedIndexPath && (!self.longPressedIndexPath)) {
        
        // No cell is wiggling; just set the cell selected state and perform segue to edit the tapped reminder
        [self.collectionView selectItemAtIndexPath:tappedIndexPath animated:NO scrollPosition:UICollectionViewScrollPositionNone];
        [self performSegueWithIdentifier:kBFSegueReminderTapped sender:self];
        
    } else if (tappedIndexPath && ([tappedIndexPath isEqual:self.longPressedIndexPath])) {
        
        // The tapped reminder is equal to the currently wiggling reminder
        BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.longPressedIndexPath];
        if (cell) {
            // Check the touch inside the cell
            CGPoint touchPointInCell = [cell convertPoint:tapPoint fromView:self.collectionView];
            if (CGRectContainsPoint(cell.bounds, touchPointInCell)) {
                
                if (touchPointInCell.x<(cell.bounds.size.width / 3.0f)) {
                    // Trash button tapped
                    [self trashSelectedReminder:sender];
                    
                } else if (touchPointInCell.x>(2*(cell.bounds.size.width / 3.0f))) {
                    // Play/pause button tapped
                    [self playOrPauseSelectedReminder:sender];
                }
                
                // Stop wiggling anyway
                [cell stopWiggling];
                self.longPressedIndexPath = nil;
            }
        }
        
    } else {
        
        // Just stop wiggling...
        BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.longPressedIndexPath];
        if (cell)
            [cell stopWiggling];
    }
}


#pragma mark - UICollectionViewDataSource

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView
{
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    return [[BFReminderList sharedReminderList].reminderList count];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[collectionView dequeueReusableCellWithReuseIdentifier:kBFReuseIDReminderCVCell forIndexPath:indexPath];
    
    // Configure the content
    BFReminder *reminder;
    if ([[BFReminderList sharedReminderList].reminderList count]>indexPath.row)
        reminder = [[BFReminderList sharedReminderList].reminderList objectAtIndex:indexPath.row];
    
    if (reminder) {
        cell.messageLabel.text = reminder.message;
        cell.pauseBackgroundImageView.hidden = (![reminder isPaused]);
        
        NSString *freqText = [NSString stringWithFormat:@"%@x per %@", @(reminder.frequencyCount), [reminder frequencyTypeString]];
        
        // Create italic body style font for frequency string
        UIFont *font = [UIFont preferredFontForTextStyle:UIFontTextStyleFootnote];
        UIFontDescriptor *fontDesc = [[font fontDescriptor] fontDescriptorWithSymbolicTraits:UIFontDescriptorTraitItalic];
        UIFont *italicsFont = [UIFont fontWithDescriptor:fontDesc size:font.pointSize];
        NSAttributedString *attrFreqText = [[NSAttributedString alloc] initWithString:freqText
                                                                           attributes:@{ NSFontAttributeName: italicsFont }];
        cell.frequencyLabel.attributedText = attrFreqText;
        
        [cell setNeedsLayout];
    }

    // Configure the cell
    cell.layer.cornerRadius = 4.0f;
    
    return cell;
}


#pragma mark - UICollectionViewDelegate



#pragma mark - Reminder Edit unwinding

- (IBAction)reminderEditFinished:(UIStoryboardSegue *)segue
{
    // Collection view should reload its data and refresh UI
    [self.collectionViewLayout invalidateLayout];
    [self.collectionView reloadData];
    
    // Quite possibly the pause-button should be dis-/enabled or so
    [self correctLeftNavigationBarItem];
}


#pragma mark - UICollectionViewDelegateFlowLayout

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    CGSize cellBounds = CGSizeZero;
    if (UIDeviceOrientationIsLandscape([[UIDevice currentDevice] orientation])) {
        // Orientation is landscape: use the max of the frame's sizes
        cellBounds.width = (MAX(self.collectionView.frame.size.width, self.collectionView.frame.size.height) / 2.0f) - 16.0f;
    } else {
        // The orientation is portrait: the width is the smallest of the frame's sizes
        cellBounds.width = MIN(self.collectionView.frame.size.width, self.collectionView.frame.size.height) - 16.0f;
    }
    BFReminder *reminder;
    if ([[BFReminderList sharedReminderList].reminderList count]>indexPath.row)
        reminder = [[BFReminderList sharedReminderList].reminderList objectAtIndex:indexPath.row];
    
    if (reminder) {
        // Calculate height from the actual reminder message
        CGRect messageSize = [reminder.message boundingRectWithSize:CGSizeMake(cellBounds.width - 16.0f, 80.0f) options:NSStringDrawingUsesLineFragmentOrigin attributes:@{ NSFontAttributeName : [UIFont preferredFontForTextStyle:UIFontTextStyleBody] } context:nil];
        cellBounds.height = ceilf(messageSize.size.height) + 32.0f;
        
    } else {
        // Assume the reminder message has one line
        cellBounds.height = 50.0f;
    }

    return cellBounds;
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
        [self performSegueWithIdentifier:kBFSegueReminderListToReminderShake sender:self];
    }
}


#pragma mark - Navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if (self.longPressedIndexPath) {
        // Stop wiggling anyway
        BFReminderCollectionViewCell *cell = (BFReminderCollectionViewCell *)[self.collectionView cellForItemAtIndexPath:self.longPressedIndexPath];
        if (cell) {
            [cell stopWiggling];
        }
        self.longPressedIndexPath = nil;
    }
    
    if ([segue.identifier isEqualToString:kBFSegueReminderListToReminder]) {
        BFReminder *reminder = ((BFNavigationController *)self.navigationController).receivedReminder;
        [[segue destinationViewController] showReminder:reminder];
    }
    if ([[segue identifier] isEqualToString:kBFSegueAddReminder]) {
        // Create a new instance of a reminder and pass it to editing view
        // Note: do not insert it into the reminder list yet; only upon save tapped
        BFReminder *reminder = [[BFReminder alloc] init];
        
        BFReminderEditViewController *editVC = (BFReminderEditViewController *)[segue destinationViewController];
        editVC.reminder = reminder;
        
    } else if ([[segue identifier] isEqualToString:kBFSegueReminderTapped]) {
        
        // Retrieve the reminder which has been tapped and pass it to the editing view
        NSIndexPath *tappedIndexPath = [[self.collectionView indexPathsForSelectedItems] firstObject];
        BFReminder *reminder = [[BFReminderList sharedReminderList] reminderAtIndex:tappedIndexPath.row];
        
        BFReminderEditViewController *editVC = (BFReminderEditViewController *)[segue destinationViewController];
        editVC.reminder = reminder;
    }
}

@end
