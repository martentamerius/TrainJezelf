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

import java.util.Random;

/**
 * Schedules alarms (notifications) on behalf of the app.
 * Created by ronald on 8-8-14.
 */
public class AlarmScheduler {
    private static final String LOG_TAG = "AlarmScheduler";

    private static final int ACTIVE_START_HOUR = 9;
    private static final int ACTIVE_START_MINUTE = 0;
    private static final int ACTIVE_END_HOUR = 20;
    private static final int ACTIVE_END_MINUTE = 0;

    private static final int MIN_SECONDS_UNTIL_NEXT_NOTIFICATION = 10;
    private static final int SCHEDULER_INACCURACY_SLACK_SECONDS = 10;

    private static final int JITTER_PERCENTAGE = 25;

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
     * @param atMillis at milliseconds UTC
     * @param notificationText text of notification
     * @param notificationId unique Id of notification
     */
    private static void startAlert(Context context, long atMillis, String notificationText, int notificationId) {
        // Prevent notification overflow
        if (atMillis - System.currentTimeMillis() < MIN_SECONDS_UNTIL_NEXT_NOTIFICATION * 1000) {
            Log.d(LOG_TAG, String.format("millis too small (%d), adding %d", atMillis,
                    MIN_SECONDS_UNTIL_NEXT_NOTIFICATION * 1000));
            atMillis += MIN_SECONDS_UNTIL_NEXT_NOTIFICATION * 1000;
        }
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.ARGUMENT_NOTIFICATION_TEXT, notificationText);
        intent.putExtra(AlarmReceiver.ARGUMENT_NOTIFICATION_ID, notificationId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent);
    }

    private static long roundUp(long num, long multiple) {
        return (num + multiple - 1) / multiple * multiple;
    }

    private static long getMillisOfNextNotification(Reminder reminder) {
        DateTime now = new DateTime();
        Log.d(LOG_TAG, "now is: " + now);
        // Slackednow is slightly more in the future than 'now'.
        // The Android alarm manager may trigger an alarm just before the intended time we set,
        // and if we do nothing about this, the next notification will be scheduled at the same time,
        // causing a rapid burst of a couple of notifications.
        DateTime slackedNow = now.plusSeconds(SCHEDULER_INACCURACY_SLACK_SECONDS);

        // Calculate duration of active period
        DateTime activeBegin = now.withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE)
                .withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime activeEnd = now.withHourOfDay(ACTIVE_END_HOUR).withMinuteOfHour(ACTIVE_END_MINUTE)
                .withSecondOfMinute(0).withMillisOfSecond(0);
        Interval activeInterval = new Interval(activeBegin, activeEnd);
        long activeMillis = activeInterval.getEndMillis() - activeInterval.getStartMillis();
        long inactiveMillis = 24 * 60 * 60 * 1000 - activeMillis;

        DateTime startOfNextSubperiod = null;
        int subPeriodMillis = 0;
        switch (reminder.getPeriod()) {
            case HOURLY: {
                final int periodMillis = 60 * 60 * 1000;
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                long nextSubPeriodMillis = roundUp(slackedNow.getMillis(), subPeriodMillis);
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
                final int periodMillis = (int)activeMillis;
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                startOfNextSubperiod = activeBegin;
                if (activeInterval.isBefore(now)) {
                    Log.d(LOG_TAG, "in inactive period, increasing start of next subperiod by 24h");
                    startOfNextSubperiod = startOfNextSubperiod.plusDays(1);
                }
                while (slackedNow.isAfter(startOfNextSubperiod)) {
                    Log.d(LOG_TAG, slackedNow + " is before " + startOfNextSubperiod + ", adding sub period millis");
                    startOfNextSubperiod = startOfNextSubperiod.plusMillis(subPeriodMillis);
                }
            }
            break;
            case WEEKLY: {
                final int periodMillis = (int) activeMillis * 7;
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                DateTime firstActiveTimeOfPeriod = now.withDayOfWeek(DateTimeConstants.MONDAY).
                        withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE).
                        withSecondOfMinute(0).withMillisOfSecond(0);
                final int nrofDays = (int) (now.getMillis() - firstActiveTimeOfPeriod.getMillis()) /
                        (24 * 60 * 60 * 1000);
                startOfNextSubperiod = firstActiveTimeOfPeriod.plusMillis(nrofDays * (int)inactiveMillis);
                while (slackedNow.isAfter(startOfNextSubperiod)) {
                    startOfNextSubperiod = startOfNextSubperiod.plusMillis(subPeriodMillis);
                }
            }
            break;
            case MONTHLY: {
                final int periodMillis = (int) activeMillis * now.dayOfMonth().getMaximumValue();
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                DateTime firstActiveTimeOfPeriod = now.withDayOfMonth(1).
                        withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE).withSecondOfMinute(0);
                final int nrofDays = (int) (now.getMillis() - firstActiveTimeOfPeriod.getMillis()) /
                        (24 * 60 * 60 * 1000);
                startOfNextSubperiod = firstActiveTimeOfPeriod.plusMillis(nrofDays * (int)inactiveMillis);
                while (slackedNow.isAfter(startOfNextSubperiod)) {
                    startOfNextSubperiod = startOfNextSubperiod.plusMillis(subPeriodMillis);
                }
            }
            break;
        }

        Log.d(LOG_TAG, "start of next sub period: " + startOfNextSubperiod);
        int randomOffsetMillis = (int)(new Random().nextFloat() /  100.0f * JITTER_PERCENTAGE * subPeriodMillis);
        Log.d(LOG_TAG, "random offset millis is: " + randomOffsetMillis);
        return startOfNextSubperiod.getMillis() + randomOffsetMillis;
    }

    public static void scheduleNextReminder(Context context, int notificationId) {
        InitializeJodaTimeIfNeeded(context);
        Reminder reminder = DataStore.getInstance(context).get(notificationId);
        if (reminder == null) {
            // User may have deleted the reminder in the meanwhile
            Log.d(LOG_TAG, "ERROR, trying to schedule reminder that does not exist anymore");
            return;
        }
        long millisOfNextNotification = getMillisOfNextNotification(reminder);
        startAlert(context, millisOfNextNotification, reminder.getMessage(), notificationId);
    }

    public static void cancelScheduledReminder(Context context, int reminderUniqueId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderUniqueId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent.cancel();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
