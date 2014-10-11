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

    private static final int MILLISECONDS_PER_HOUR = 60 * 60 * 1000;
    private static final int MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR;
    private static final int DAYS_PER_WEEK = 7;

    private static final float JITTER_PERCENTAGE = 80.0f;

    private static boolean jodaTimeIsInitialized = false;

    /**
     * Initialize JodaTime library if needed
     * @param context app context
     */
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

    /**
     * Get active interval for given moment
     * @param moment to get active interval for
     * @return resulting DateTime
     */
    private static Interval getActiveIntervalFor(DateTime moment) {
        DateTime activeBegin = moment.withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE)
                .withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime activeEnd = moment.withHourOfDay(ACTIVE_END_HOUR).withMinuteOfHour(ACTIVE_END_MINUTE)
                .withSecondOfMinute(0).withMillisOfSecond(0);
        return new Interval(activeBegin, activeEnd);
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
        final int inactiveMillis = MILLISECONDS_PER_DAY - activeMillis;
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

    /**
     * Get millisecond duration of one reminder period
     * @param now current date/time
     * @param reminder the reminder
     * @return millisecond duration of one reminder period
     */
    private static int getSubPeriodMillis(DateTime now, Reminder reminder) {

        // Calculate duration of active period
        Interval activeInterval = getActiveIntervalFor(now);
        int activeMillis = (int)(activeInterval.getEndMillis() - activeInterval.getStartMillis());

        int subPeriodMillis = 0;
        switch (reminder.getPeriod()) {
            case HOURLY: {
                // Just re-state the hourly reminder as a daily reminder and use the daily algorithm for scheduling.
                final int periodMillis;
                periodMillis = activeMillis;
                float hourlyToDailyFactor = (float)activeMillis / (float)(MILLISECONDS_PER_HOUR);
                int equivalentRemindersPerDay = (int)(hourlyToDailyFactor * reminder.getNumberOfNotifiesPerPeriod());
                subPeriodMillis = periodMillis / equivalentRemindersPerDay;
            }
            break;
            case DAILY: {
                final int periodMillis;
                periodMillis = activeMillis;
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
            }
            break;
            case WEEKLY: {
                final int activePeriodMillis = activeMillis * DAYS_PER_WEEK;
                subPeriodMillis = activePeriodMillis / reminder.getNumberOfNotifiesPerPeriod();
            }
            break;
            case MONTHLY: {
                final int periodMillis = activeMillis * now.dayOfMonth().getMaximumValue();
                subPeriodMillis = periodMillis / reminder.getNumberOfNotifiesPerPeriod();
            }
            break;
        }

        return subPeriodMillis;
    }

    /**
     * Get first active moment of period
     * @param now current date/time
     * @param reminder the reminder
     * @return first active moment of period
     */
    private static DateTime getFirstActiveMomentOfPeriod(DateTime now, Reminder reminder) {
        DateTime slackedNow = now.plusSeconds(SCHEDULER_INACCURACY_SLACK_SECONDS);
        DateTime firstActiveTimeOfPeriod = null;
        switch (reminder.getPeriod()) {
            case HOURLY:
            case DAILY:
                firstActiveTimeOfPeriod = now.withHourOfDay(ACTIVE_START_HOUR).
                        withMinuteOfHour(ACTIVE_START_MINUTE).
                        withSecondOfMinute(0).withMillisOfSecond(0);
                break;
            case WEEKLY:
                firstActiveTimeOfPeriod = now.withDayOfWeek(DateTimeConstants.SUNDAY).
                        withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE).
                        withSecondOfMinute(0).withMillisOfSecond(0);
                // Not sure how JodaTime rounds the .withDayOfWeek() calculation. To be sure,
                // subtract one week if firstActiveTimeOfPeriod is later than now.
                if (slackedNow.isBefore(firstActiveTimeOfPeriod)) {
                    firstActiveTimeOfPeriod = firstActiveTimeOfPeriod.minusDays(DAYS_PER_WEEK);
                }
                break;
            case MONTHLY:
                firstActiveTimeOfPeriod = now.withDayOfMonth(1).
                        withHourOfDay(ACTIVE_START_HOUR).withMinuteOfHour(ACTIVE_START_MINUTE).
                        withSecondOfMinute(0).withMillisOfSecond(0);
                // Not sure how JodaTime rounds the .withDayOfWeek() calculation. To be sure,
                // subtract one month if firstActiveTimeOfPeriod is later than now.
                if (slackedNow.isBefore(firstActiveTimeOfPeriod)) {
                    firstActiveTimeOfPeriod = firstActiveTimeOfPeriod.minusMonths(1);
                }
                break;
        }

        return firstActiveTimeOfPeriod;
    }

    public static long getMillisOfNextNotification(DateTime now, Reminder reminder) {

        // Slackednow is slightly more in the future than 'now'.
        // The Android alarm manager may trigger an alarm just before the intended time we set,
        // and if we do nothing about this, the next notification will be scheduled at the same time,
        // causing a rapid burst of a couple of notifications.
        DateTime slackedNow = now.plusSeconds(SCHEDULER_INACCURACY_SLACK_SECONDS);

        // Calculate first active moment of the current period
        DateTime firstActiveTimeOfPeriod = getFirstActiveMomentOfPeriod(now, reminder);

        // Calculate milliseconds of a sub period
        int subPeriodMillis = getSubPeriodMillis(now, reminder);

        // Add sub period millis until start of next sub period is later than now.
        DateTime startOfNextSubperiod = firstActiveTimeOfPeriod;
        while (slackedNow.isAfter(startOfNextSubperiod)) {
            startOfNextSubperiod = addInactiveMillis(startOfNextSubperiod, subPeriodMillis);
        }

        // Add random offset
        float randomFactor = new Random().nextFloat() / 100.0f * JITTER_PERCENTAGE;
        int randomOffsetMillis = (int) (randomFactor * subPeriodMillis);
        float shiftFactor = (100.0f - JITTER_PERCENTAGE) / 200.0f;
        int offsetMillis = (int) (subPeriodMillis * shiftFactor);
        startOfNextSubperiod = addInactiveMillis(startOfNextSubperiod, offsetMillis + randomOffsetMillis);

        return startOfNextSubperiod.getMillis();
    }

    /**
     * Schedules next reminder for given notification Id
     * @param context app context
     * @param notificationId the notification Id
     */
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

    /**
     * Cancel scheduled reminder
     * @param context app context
     * @param reminderUniqueId unique Id of the reminder
     */
    public static void cancelScheduledReminder(Context context, int reminderUniqueId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderUniqueId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent.cancel();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
