//
//  BFAppDefines.h
//  BreakFree
//
//  Created by Marten Tamerius on 04-07-14.
//  Copyright (c) 2014 Tamerius & Bos. All rights reserved.
//

#ifndef BreakFree_BFAppDefines_h
#define BreakFree_BFAppDefines_h

// Local notification user info
#define kBFLocalNotificationReminderUUIDString          @"BFLocalNotificationReminderUUIDString"

// Storyboard segues
#define kBFSegueAutomaticLaunchViewToRemindersCV        @"BFTappedLaunchViewToRemindersCVSegue"
#define kBFSegueTappedLaunchViewToRemindersCV           @"BFAutomaticLaunchViewToRemindersCVSegue"

#define kBFSegueAddReminder                             @"BFAddReminderSegue"
#define kBFSegueReminderTapped                          @"BFReminderTappedSegue"
#define kBFSegueUnwindFromSaveReminderTapped            @"BFSaveReminderUnwindSegue"

// UITableViewCell identifiers
#define kBFReuseIDReminderCVCell                        @"BFReminderCVCell"
#define kBFReminderCVAccViewHeader                      @"BFReminderCVAccViewHeader"

#endif
