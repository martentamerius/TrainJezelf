//
//  BFReminderCollectionViewCell.m
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#import "BFReminderCollectionViewCell.h"

#define kWiggleBounceY 2.0f
#define kWiggleBounceDuration 0.2
#define kWiggleBounceDurationVariance 0.025

#define kWiggleRotateAngle 0.02f
#define kWiggleRotateDuration 0.1
#define kWiggleRotateDurationVariance 0.025


@interface BFReminderCollectionViewCell ()
@property (weak, nonatomic) IBOutlet UIImageView *btnTrashImageView;
@property (weak, nonatomic) IBOutlet UIImageView *btnPlayPauseImageView;
@property (nonatomic) BOOL isWiggling;
@end

@implementation BFReminderCollectionViewCell

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Reminders are actively scheduled by default
        self.isWiggling = NO;
    }
    return self;
}


#pragma mark - Wriggling animation

- (void)startWiggling
{
    self.btnPlayPauseImageView.image = (self.pauseBackgroundImageView.hidden)?[UIImage imageNamed:@"btn_pause"]:[UIImage imageNamed:@"btn_play"];
    self.btnPlayPauseImageView.hidden = NO;
    self.btnTrashImageView.hidden = NO;
    
    [UIView animateWithDuration:0.1 animations:^{
        // Wriggle
        [self.layer addAnimation:[self rotationAnimation] forKey:@"rotation"];
        [self.layer addAnimation:[self bounceAnimation] forKey:@"bounce"];
        // And perform a slight downscaling
        self.transform = CGAffineTransformMakeScale(0.96f, 0.96f);
    }];
    
    self.isWiggling = YES;
}

- (void)stopWiggling
{
    self.btnPlayPauseImageView.hidden = YES;
    self.btnTrashImageView.hidden = YES;
    
    [UIView animateWithDuration:0.15 animations:^{
        [self.layer removeAllAnimations];
        self.transform = CGAffineTransformIdentity;
    }];
    
    self.isWiggling = NO;
}

- (CAAnimation *)rotationAnimation
{
    CAKeyframeAnimation* animation = [CAKeyframeAnimation animationWithKeyPath:@"transform.rotation.z"];
    animation.values = @[@(-kWiggleRotateAngle), @(kWiggleRotateAngle)];
    
    animation.autoreverses = YES;
    animation.duration = [self randomizeInterval:kWiggleRotateDuration
                                    withVariance:kWiggleRotateDurationVariance];
    animation.repeatCount = HUGE_VALF;
    
    return animation;
}

- (CAAnimation *)bounceAnimation
{
    CAKeyframeAnimation* animation = [CAKeyframeAnimation animationWithKeyPath:@"transform.translation.y"];
    animation.values = @[@(kWiggleBounceY), @(0.0)];
    
    animation.autoreverses = YES;
    animation.duration = [self randomizeInterval:kWiggleBounceDuration
                                    withVariance:kWiggleBounceDurationVariance];
    animation.repeatCount = HUGE_VALF;
    
    return animation;
}

- (NSTimeInterval)randomizeInterval:(NSTimeInterval)interval withVariance:(double)variance
{
    double random = (arc4random_uniform(1000) - 500.0) / 500.0;
    return interval + variance * random;
}


@end
