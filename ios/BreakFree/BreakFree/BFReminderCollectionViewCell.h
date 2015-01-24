//
//  BFReminderCollectionViewCell.h
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface BFReminderCollectionViewCell : UICollectionViewCell
@property (weak, nonatomic) IBOutlet UILabel *messageLabel;
@property (weak, nonatomic) IBOutlet UILabel *frequencyLabel;
@property (weak, nonatomic) IBOutlet UIImageView *thumbnailImageView;
@property (weak, nonatomic) IBOutlet UIImageView *pauseBackgroundImageView;

- (void)startWiggling;
- (void)stopWiggling;
@end
