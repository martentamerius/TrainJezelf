//
//  BFNavigationController.h
//  BreakFree
//
//  Created by Marten Tamerius on 23-09-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import <UIKit/UIKit.h>

@class BFReminder;

@interface BFNavigationController : UINavigationController
@property (nonatomic, strong) BFReminder *receivedReminder;

- (void)applicationDidReceiveNotificationWithReminder:(BFReminder *)reminder;
@end
