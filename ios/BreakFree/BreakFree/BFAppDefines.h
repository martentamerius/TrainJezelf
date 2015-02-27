//
//  BFAppDefines.h
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#ifndef BreakFree_BFAppDefines_h
#define BreakFree_BFAppDefines_h

// Time in seconds the reminder view should be modally displayed
#define kBFReminderViewPeriod                           7

// Number of (sequentially numbered) images to show in reminder view
#define kBFReminderImageCount                           6

// Local notification user info
#define kBFLocalNotificationReminderUUIDString          @"BFLocalNotificationReminderUUIDString"

// Storyboard segues
#define kBFSegueAddReminder                             @"BFAddReminderSegue"
#define kBFSegueReminderTapped                          @"BFReminderTappedSegue"
#define kBFSegueUnwindFromSaveReminderTapped            @"BFSaveReminderUnwindSegue"

#define kBFSegueReminderListToReminderShake             @"BFReminderListToReminderShakeSegue"
#define kBFSegueReminderListToReminder                  @"BFReminderListToReminderSegue"
#define kBFSegueUnwindFromReminderToReminderList        @"BFUnwindFromReminderToReminderListSegue"

#define kBFSegueChooseDailyFirePeriodAsPopover          @"BFSegueChooseDailyFirePeriodAsPopover"
#define kBFSegueChooseDailyFirePeriodModally            @"BFSegueChooseDailyFirePeriodModally"

#define kBFSegueReminderEditToReminderShake             @"BFReminderEditToReminderShakeSegue"
#define kBFSegueReminderEditToReminder                  @"BFReminderEditToReminderSegue"
#define kBFSegueUnwindFromReminderToReminderEdit        @"BFUnwindFromReminderToReminderEditSegue"

// UITableViewCell identifiers
#define kBFReuseIDReminderCVCell                        @"BFReminderCVCell"
#define kBFReminderCVAccViewHeader                      @"BFReminderCVAccViewHeader"

#endif
