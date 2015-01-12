package com.github.trainjezelf.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.trainjezelf.datastore.DataStore;
import com.github.trainjezelf.datastore.Reminder;
import com.github.trainjezelf.datastore.TimeRange;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;

import java.util.Random;

/**
 * Schedules alarms (notifications) on behalf of the app.
 */
public class AlarmScheduler {

    public static final String DEFAULT_ACTIVE_TIME_RANGE = "8:00-22:00";
    public static final int MINIMUM_ACTIVE_MINUTES = 60;

    private static final String LOG_TAG = "AlarmScheduler";

    private static boolean preferencesLoaded = false;
    private static int activeStartHour = 8;
    private static int activeStartMinute = 0;
    private static int activeEndHour = 22;
    private static int activeEndMinute = 0;

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

    private static void loadActiveIntervalFromPreferences(Context context) {
        if (!preferencesLoaded) {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String encoded = preferences.getString("active_period", DEFAULT_ACTIVE_TIME_RANGE);
            setActiveRangeFromEncoded(encoded);
            // Sanity check: we must at least have MINIMUM_ACTIVE_MINUTES in the specified time range
            final int beginMinutes = activeStartHour * 60 + activeStartMinute;
            final int endMinutes = activeEndHour * 60 + activeEndMinute;
            if (endMinutes - beginMinutes < MINIMUM_ACTIVE_MINUTES) {
                setActiveRangeFromEncoded(DEFAULT_ACTIVE_TIME_RANGE);
            }
            // State handling
            preferencesLoaded = true;
        }
        Log.d(LOG_TAG, String.format("%d:%d - %d:%d", activeStartHour, activeStartMinute,
                activeEndHour, activeEndMinute));
    }

    private static void setActiveRangeFromEncoded(String encoded) {
        final TimeRange range = new TimeRange(encoded);
        activeStartHour = range.getFromHour();
        activeStartMinute = range.getFromMinute();
        activeEndHour = range.getUntilHour();
        activeEndMinute = range.getUntilMinute();
    }

    public static void invalidatePreferencesLoaded() {
        preferencesLoaded = false;
    }

    private static Intent getNotificationIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        // Make the intent unique for this notification ID by specifying its type.
        // This makes sure that the Android OS does not discard the alarm because it thinks it is equal to another one.
        // (Refer for example to
        //     http://stackoverflow.com/questions/8469705/how-to-set-multiple-alarms-using-android-alarm-manager)
        intent.setType("ID=" + notificationId);
        return intent;
    }

    /**
     * Schedule alarm
     * @param context using context
     * @param atMillis at milliseconds UTC
     * @param notificationId unique Id of notification
     */
    private static void startAlert(Context context, long atMillis, int notificationId) {
        // Prevent notification overflow
        if (atMillis - System.currentTimeMillis() < MIN_SECONDS_UNTIL_NEXT_NOTIFICATION * 1000) {
            Log.d(LOG_TAG, String.format("millis too small (%d), adding %d", atMillis,
                    MIN_SECONDS_UNTIL_NEXT_NOTIFICATION * 1000));
            atMillis += MIN_SECONDS_UNTIL_NEXT_NOTIFICATION * 1000;
        }
        Intent intent = getNotificationIntent(context, notificationId);
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
        DateTime activeBegin = moment.withHourOfDay(activeStartHour).withMinuteOfHour(activeStartMinute)
                .withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime activeEnd = moment.withHourOfDay(activeEndHour).withMinuteOfHour(activeEndMinute)
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
                firstActiveTimeOfPeriod = now.withHourOfDay(activeStartHour).
                        withMinuteOfHour(activeStartMinute).
                        withSecondOfMinute(0).withMillisOfSecond(0);
                break;
            case WEEKLY:
                firstActiveTimeOfPeriod = now.withDayOfWeek(DateTimeConstants.SUNDAY).
                        withHourOfDay(activeStartHour).withMinuteOfHour(activeStartMinute).
                        withSecondOfMinute(0).withMillisOfSecond(0);
                // Not sure how JodaTime rounds the .withDayOfWeek() calculation. To be sure,
                // subtract one week if firstActiveTimeOfPeriod is later than now.
                if (slackedNow.isBefore(firstActiveTimeOfPeriod)) {
                    firstActiveTimeOfPeriod = firstActiveTimeOfPeriod.minusDays(DAYS_PER_WEEK);
                }
                break;
            case MONTHLY:
                firstActiveTimeOfPeriod = now.withDayOfMonth(1).
                        withHourOfDay(activeStartHour).withMinuteOfHour(activeStartMinute).
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

    public static long getMillisOfNextNotification(Context context, DateTime now, Reminder reminder) {

        loadActiveIntervalFromPreferences(context);

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
        final DataStore dataStore = DataStore.getInstance(context);
        final Reminder reminder = dataStore.get(notificationId);
        if (reminder == null) {
            // User may have deleted the reminder in the meanwhile
            Log.d(LOG_TAG, "ERROR, trying to schedule reminder that does not exist anymore");
            return;
        }
        final DateTime now = new DateTime();
        final long millisOfNextNotification = getMillisOfNextNotification(context, now, reminder);
        Log.d(LOG_TAG, String.format("scheduling next reminder for uid %d at %s", reminder.getUniqueId(),
                new DateTime(millisOfNextNotification).toString()));
        startAlert(context, millisOfNextNotification, notificationId);
    }

    /**
     * Cancel scheduled reminder
     * @param context app context
     * @param reminderUniqueId unique Id of the reminder
     */
    public static void cancelScheduledReminder(Context context, int reminderUniqueId) {
        final Intent intent = getNotificationIntent(context, reminderUniqueId);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderUniqueId, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent.cancel();
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private static boolean hasPendingAlarm(Context context, int reminderUniqueId) {
        final Intent intent = getNotificationIntent(context, reminderUniqueId);
        final boolean alarmUp = (PendingIntent.getBroadcast(context, reminderUniqueId, intent,
                PendingIntent.FLAG_NO_CREATE) != null);
        Log.d(LOG_TAG, String.format("Reminder %d has pending intent: %s", reminderUniqueId, alarmUp));
        return alarmUp;
    }

    /**
     * Re-schedule all reminders
     */
    public static void reScheduleAllReminders(Context context) {
        final DataStore dataStore = DataStore.getInstance(context);
        for (Reminder reminder : dataStore.getReminders()) {
            final int uniqueId = reminder.getUniqueId();
            if (hasPendingAlarm(context, uniqueId)) {
                scheduleNextReminder(context, reminder.getUniqueId());
            }
        }
    }
}
