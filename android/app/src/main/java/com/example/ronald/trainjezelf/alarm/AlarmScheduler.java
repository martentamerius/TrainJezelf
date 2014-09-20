package com.example.ronald.trainjezelf.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;

/**
 * Schedules alarms (notifications) on behalf of the app.
 * Created by ronald on 8-8-14.
 */
public class AlarmScheduler {
    private static final String LOG_TAG = "AlarmScheduler";

    private static final int ACTIVE_START_HOUR = 7;
    private static final int ACTIVE_START_MINUTE = 0;
    private static final int ACTIVE_END_HOUR = 22;
    private static final int ACTIVE_END_MINUTE = 0;

    private static int intentId = 0;

    private static boolean jodaTimeIsInitialized = false;

    private static void InitializeJodaTimeIfNeeded(Context context) {
        if (!jodaTimeIsInitialized) {
            JodaTimeAndroid.init(context);
            jodaTimeIsInitialized = true;
        }
    }

    /**
     * Schedule alarm
     * @param context using context
     * @param secondsFromNow seconds from now
     */
    private static void startAlert(Context context, int secondsFromNow, String notificationText, int notificationId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.ARGUMENT_NOTIFICATION_TEXT, notificationText);
        intent.putExtra(AlarmReceiver.ARGUMENT_NOTIFICATION_ID, (notificationId << 16) | intentId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, intentId++, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (secondsFromNow * 1000), pendingIntent);
    }

    private static long roundUp(long num, long multiple) {
        return (num + multiple - 1) / multiple * multiple;
    }

    private static int getSecondsUntilNextNotification(Reminder reminder) {
        DateTime now = new DateTime();

        // Calculate duration of active period
        DateTime activeBegin = now.withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE);
        DateTime activeEnd = now.withHourOfDay(ACTIVE_END_HOUR).withMinuteOfHour(ACTIVE_END_MINUTE);
        Interval activeInterval = new Interval(activeBegin, activeEnd);
        long activeMillis = activeInterval.getEndMillis() - activeInterval.getStartMillis();
        long inactiveMillis = 24 * 60 * 60 * 1000 - activeMillis;

        DateTime startOfNextSubperiod = null;
        switch (reminder.getPeriod()) {
            case HOURLY: {
                final int periodMillis = 60 * 60 * 1000;
                final int subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                long nextSubPeriodMillis = roundUp(now.getMillis(), subPeriodMillis);
                startOfNextSubperiod = new DateTime(nextSubPeriodMillis);
                DateTime endOfNextSubPeriod = startOfNextSubperiod.plusMillis(subPeriodMillis);
                Interval currentSubPeriodInterval = new Interval(startOfNextSubperiod, endOfNextSubPeriod);
                if (!activeInterval.contains(currentSubPeriodInterval)) {
                    // Shift sub period begin to next active period
                    nextSubPeriodMillis = roundUp(activeBegin.getMillis(), subPeriodMillis);
                    startOfNextSubperiod = new DateTime(nextSubPeriodMillis);
                    if (activeInterval.isBefore(now)) {
                        startOfNextSubperiod = startOfNextSubperiod.plusDays(1);
                    }
                }
            }
            break;
            case DAILY: {
                // TODO: debug
                final int periodMillis = (int) activeMillis;
                final int subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                long nextSubPeriodMillis = roundUp(now.getMillis(), subPeriodMillis);
                startOfNextSubperiod = new DateTime(nextSubPeriodMillis);
                if (!activeInterval.contains(startOfNextSubperiod)) {
                    // Shift sub period start to begin of next active period
                    startOfNextSubperiod = activeBegin;
                    if (activeInterval.isBefore(now)) {
                        startOfNextSubperiod = startOfNextSubperiod.plusDays(1);
                    }
                }
            }
            break;
            case WEEKLY: {
                final int periodMillis = (int) activeMillis * 7;
                final int subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                DateTime firstActiveTimeOfPeriod = now.withDayOfWeek(DateTimeConstants.MONDAY).
                        withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE).withSecondOfMinute(0);
                final int nrofDays = (int) (now.getMillis() - firstActiveTimeOfPeriod.getMillis()) /
                        (24 * 60 * 60 * 1000);
                startOfNextSubperiod = firstActiveTimeOfPeriod.plusMillis(nrofDays * (int)inactiveMillis);
                while (now.isAfter(startOfNextSubperiod)) {
                    startOfNextSubperiod = startOfNextSubperiod.plusMillis(subPeriodMillis);
                }
            }
            break;
            case MONTHLY: {
                final int periodMillis = (int) activeMillis * now.dayOfMonth().getMaximumValue();
                final int subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                DateTime firstActiveTimeOfPeriod = now.withDayOfMonth(1).
                        withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE).withSecondOfMinute(0);
                final int nrofDays = (int) (now.getMillis() - firstActiveTimeOfPeriod.getMillis()) /
                        (24 * 60 * 60 * 1000);
                startOfNextSubperiod = firstActiveTimeOfPeriod.plusMillis(nrofDays * (int)inactiveMillis);
                while (now.isAfter(startOfNextSubperiod)) {
                    startOfNextSubperiod = startOfNextSubperiod.plusMillis(subPeriodMillis);
                }
            }
            break;
        }

        Log.d(LOG_TAG, "start of next sub period: " + startOfNextSubperiod);

        // TODO add random jitter
        return (int)(startOfNextSubperiod.getMillis() - now.getMillis()) / 1000;
    }

    public static void scheduleNextReminder(Context context, int notificationId) {
        InitializeJodaTimeIfNeeded(context);
        Reminder reminder = DataStore.getInstance(context).get(notificationId);
        int secondsUntilNextNotification = getSecondsUntilNextNotification(reminder);
        startAlert(context, secondsUntilNextNotification, reminder.getMessage(), notificationId);
    }
}
