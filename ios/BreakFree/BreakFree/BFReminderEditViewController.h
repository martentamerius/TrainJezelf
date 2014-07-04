//
//  BFReminderEditViewController.h
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BFReminder.h"

@class BFReminderEditViewController;

@protocol BFReminderEditViewControllerDelegate <NSObject>
- (void)reminderSaveButtonTappedInEditViewController:(BFReminderEditViewController *)viewController;
@end


@interface BFReminderEditViewController : UIViewController
@property (nonatomic, strong) id <BFReminderEditViewControllerDelegate> delegate;
@property (nonatomic, strong) BFReminder *reminder;
@end
