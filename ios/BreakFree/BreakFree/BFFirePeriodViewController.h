//
//  BFFirePeriodViewController.h
//  BreakFree
//
//  Created by Marten Tamerius on 27-01-15.
//  Copyright (c) 2015 Tamerius & Bos. All rights reserved.
//

@class BFFirePeriodViewController;


@protocol BFFirePeriodViewControllerDelegate <NSObject>
- (void)firePeriodViewController:(BFFirePeriodViewController *)viewController
                       didFinish:(BOOL)finish
         withStartDateComponents:(NSDateComponents *)startDateComps
            andEndDateComponents:(NSDateComponents *)endDateComps;
@end

@interface BFFirePeriodViewController : UIViewController
@property (nonatomic, weak) id<BFFirePeriodViewControllerDelegate> delegate;
- (void)initTimePickersWithStartDateComponents:(NSDateComponents *)startDateComps
                          andEndDateComponents:(NSDateComponents *)endDateComps;
@end
