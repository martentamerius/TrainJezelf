//
//  BFLaunchViewController.h
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import <UIKit/UIKit.h>

@class BFReminder;

@interface BFLaunchViewController : UIViewController
- (void)showReminder:(BFReminder *)reminder;
@end
