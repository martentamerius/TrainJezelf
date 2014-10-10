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

    private static final int ACTIVE_START_HOUR = 8;
    private static final int ACTIVE_START_MINUTE = 0;
    private static final int ACTIVE_END_HOUR = 22;
    private static final int ACTIVE_END_MINUTE = 0;

    private static final int MIN_SECONDS_UNTIL_NEXT_NOTIFICATION = 10;
    private static final int SCHEDULER_INACCURACY_SLACK_SECONDS = 10;

    private static final float JITTER_PERCENTAGE = 50.0f;

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

    private static Interval getActiveIntervalFor(DateTime dateTime) {
        DateTime activeBegin = dateTime.withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE)
                .withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime activeEnd = dateTime.withHourOfDay(ACTIVE_END_HOUR).withMinuteOfHour(ACTIVE_END_MINUTE)
                .withSecondOfMinute(0).withMillisOfSecond(0);
        return new Interval(activeBegin, activeEnd);
    }

    private static DateTime roundUpToNextMultipleOfSubPeriodMillis(DateTime reference, DateTime now, long millis) {
        long deltaMillis = Math.max(now.getMillis() - reference.getMillis(), 0L);
        long nextMultipleMillis = roundUp(deltaMillis, millis);
        return new DateTime(reference.getMillis() + nextMultipleMillis);
    }

    /**
     * Add milliseconds to date/time moment, skipping over inactive time if needed.
     * @param moment moment to add milliseconds to
     * @param millis milliseconds to add
     * @return resulting DateTime
     */
    private static DateTime addInactiveMillis(DateTime moment, int millis) {
        Interval activeInterval = getActiveIntervalFor(moment);
        final int activeMillis = (int)(activeInterval.getEndMillis() - activeInterval.getStartMillis());
        final int inactiveMillis = 24 * 60 * 60 * 1000 - activeMillis;
        while (millis > 0) {
            int millisToAdd = Math.min(activeMillis, millis);
            moment = moment.plusMillis(millisToAdd);
            millis = Math.max(0, millis - millisToAdd);
            activeInterval = getActiveIntervalFor(moment);
            if (!activeInterval.contains(moment)) {
                moment = moment.plusMillis(inactiveMillis);
            }
        }
        return moment;
    }

    public static long getMillisOfNextNotification(DateTime now, Reminder reminder) {
        Log.d(LOG_TAG, "now is: " + now);
        // Slackednow is slightly more in the future than 'now'.
        // The Android alarm manager may trigger an alarm just before the intended time we set,
        // and if we do nothing about this, the next notification will be scheduled at the same time,
        // causing a rapid burst of a couple of notifications.
        DateTime slackedNow = now.plusSeconds(SCHEDULER_INACCURACY_SLACK_SECONDS);

        // Calculate duration of active period
        Interval activeInterval = getActiveIntervalFor(now);
        int activeMillis = (int)(activeInterval.getEndMillis() - activeInterval.getStartMillis());

        DateTime startOfNextSubperiod = null;
        int subPeriodMillis = 0;
        switch (reminder.getPeriod()) {
            case HOURLY: {
                final int periodMillis = 60 * 60 * 1000;
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                startOfNextSubperiod = now.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                startOfNextSubperiod = roundUpToNextMultipleOfSubPeriodMillis(startOfNextSubperiod, slackedNow,
                        subPeriodMillis);
                DateTime endOfNextSubPeriod = startOfNextSubperiod.plusMillis(subPeriodMillis);
                Interval currentSubPeriodInterval = new Interval(startOfNextSubperiod, endOfNextSubPeriod);
                if (!activeInterval.contains(currentSubPeriodInterval)) {
                    // Shift sub period begin to next active period
                    startOfNextSubperiod = roundUpToNextMultipleOfSubPeriodMillis(
                            activeInterval.getStart().withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0),
                            activeInterval.getStart(), subPeriodMillis);
                    if (activeInterval.isBefore(now)) {
                        startOfNextSubperiod = startOfNextSubperiod.plusDays(1);
                    }
                }
            }
            break;
            case DAILY: {
                final int periodMillis;
                periodMillis = activeMillis;
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                for (int i = 0; i <= 1; i++) {
                    startOfNextSubperiod = roundUpToNextMultipleOfSubPeriodMillis(activeInterval.getStart().plusDays(i),
                            slackedNow, subPeriodMillis);
                    if (!activeInterval.isBefore(startOfNextSubperiod)) {
                        break;
                    }
                }
            }
            break;
            case WEEKLY: {
                final int periodMillis = activeMillis * 7;
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                DateTime firstActiveTimeOfPeriod = now.withDayOfWeek(DateTimeConstants.SUNDAY).
                        withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE).
                        withSecondOfMinute(0).withMillisOfSecond(0);
                // Not sure how JodaTime rounds the .withDayOfWeek() calculation. To be sure,
                // subtract one week if firstActiveTimeOfPeriod is later than now.
                if (slackedNow.isBefore(firstActiveTimeOfPeriod)) {
                    firstActiveTimeOfPeriod = firstActiveTimeOfPeriod.minusDays(7);
                }
                // Add sub period millis until start of next sub period is later than now.
                startOfNextSubperiod = firstActiveTimeOfPeriod;
                while (slackedNow.isAfter(startOfNextSubperiod)) {
                    startOfNextSubperiod = addInactiveMillis(startOfNextSubperiod, subPeriodMillis);
                }
            }
            break;
            case MONTHLY: {
                final int periodMillis = activeMillis * now.dayOfMonth().getMaximumValue();
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
                DateTime firstActiveTimeOfPeriod = now.withDayOfMonth(1).
                        withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE).
                        withSecondOfMinute(0).withMillisOfSecond(0);
                // Not sure how JodaTime rounds the .withDayOfWeek() calculation. To be sure,
                // subtract one month if firstActiveTimeOfPeriod is later than now.
                if (slackedNow.isBefore(firstActiveTimeOfPeriod)) {
                    firstActiveTimeOfPeriod = firstActiveTimeOfPeriod.minusMonths(1);
                }
                // Add sub period millis until start of next sub period is later than now.
                startOfNextSubperiod = firstActiveTimeOfPeriod;
                while (slackedNow.isAfter(startOfNextSubperiod)) {
                    startOfNextSubperiod = addInactiveMillis(startOfNextSubperiod, subPeriodMillis);
                }
            }
            break;
        }

        // Add random offset
        Log.d(LOG_TAG, "start of next sub period: " + startOfNextSubperiod);
        float randomFactor = new Random().nextFloat() / 100.0f * JITTER_PERCENTAGE;
        int randomOffsetMillis = (int) (randomFactor * subPeriodMillis);
        float shiftFactor = (100.0f - JITTER_PERCENTAGE) / 200.0f;
        int offsetMillis = (int) (subPeriodMillis * shiftFactor);
        startOfNextSubperiod = addInactiveMillis(startOfNextSubperiod, offsetMillis + randomOffsetMillis);

        return startOfNextSubperiod.getMillis();
    }

    public static void scheduleNextReminder(Context context, int notificationId) {
        InitializeJodaTimeIfNeeded(context);
        final Reminder reminder = DataStore.getInstance(context).get(notificationId);
        if (reminder == null) {
            // User may have deleted the reminder in the meanwhile
            Log.d(LOG_TAG, "ERROR, trying to schedule reminder that does not exist anymore");
            return;
        }
        final DateTime now = new DateTime();
        final long millisOfNextNotification = getMillisOfNextNotification(now, reminder);
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
