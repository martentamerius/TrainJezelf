package com.github.trainjezelf.test;

import android.content.Context;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.util.Log;

import com.github.trainjezelf.alarm.AlarmScheduler;
import com.github.trainjezelf.datastore.Reminder;
import com.github.trainjezelf.datastore.TimeRange;

import junit.framework.Assert;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

/**
 * Tests the alarm scheduler
 */
public class AlarmSchedulerTest extends AndroidTestCase {

    private static final int NUMBER_OF_TEST_PERIODS = 42;

    public void testGetNotificationIntent() {
        final Context context = getContext();
        final Intent intent1 = AlarmScheduler.getNotificationIntent(context, 1);
        final Intent intent2 = AlarmScheduler.getNotificationIntent(context, 2);
        Assert.assertFalse(intent1.filterEquals(intent2));
    }

    public void testGetMillisOfNextReminder() throws Exception {

        final Context context = getContext();
        final String LOG_TAG = getClass().getSimpleName();

        JodaTimeAndroid.init(context);

        final DateTime startTime = new DateTime(1388534400000L); // 1-1-2014 00:00
        //final DateTime startTime = new DateTime(1388604600000L); // 1-1-2014 19:30

        // Default active interval is 8.00 - 22.00
        final int startHour = 8;
        final int startMinute = 30;
        final int endHour = 17;
        final int endMinute = 30;
        TimeRange timeRange = new TimeRange("00:00-00:00");
        timeRange.update(startHour, startMinute, endHour, endMinute);
        AlarmScheduler.setActiveRangeFromEncoded(timeRange.encode());

        final int activeHours = ((endHour * 60 + endMinute) - (startHour * 60 + startMinute)) / 60;

        for (Reminder.Period period : Reminder.Period.values()) {

            Log.d(LOG_TAG, "period: " + period);

            // Calculate endTime
            DateTime endTime = startTime;
            switch (period) {
                case HOURLY:
                    endTime = startTime.plusHours(NUMBER_OF_TEST_PERIODS);
                    break;
                case DAILY:
                    endTime = startTime.plusDays(NUMBER_OF_TEST_PERIODS);
                    break;
                case WEEKLY:
                    endTime = startTime.plusWeeks(NUMBER_OF_TEST_PERIODS);
                    break;
                case MONTHLY:
                    endTime = startTime.plusMonths(NUMBER_OF_TEST_PERIODS);
                    break;
            }

            for (int nrPerPeriod = 1; nrPerPeriod <= 10; nrPerPeriod++) {
                DateTime now = new DateTime(startTime);
                switch (period) {
                    case WEEKLY:
                        now = now.withDayOfWeek(DateTimeConstants.MONDAY);
                        break;
                    case MONTHLY:
                        now = now.withDayOfMonth(1);
                }
                Log.i(LOG_TAG, "start: now is " + now);

                Reminder reminder = new Reminder("", nrPerPeriod, period, 0);
                reminder.setContext(context);

                Log.i(LOG_TAG, String.format("Testing period %s, number per period %d", period, nrPerPeriod));

                int previousValue = -1;
                int valueCount = 0;

                while (now.isBefore(endTime)) {
                    final long millisOfNextNotification = AlarmScheduler.getMillisOfNextNotification(now, reminder);
                    now = new DateTime(millisOfNextNotification);

                    Log.i(LOG_TAG, "now is " + now);

                    // Checks
                    int currentValue = -1;
                    switch (period) {
                        case HOURLY:
//                            currentValue = now.getHourOfDay();
//                            break;
                        case DAILY:
                            currentValue = now.getDayOfYear();
                            break;
                        case WEEKLY:
                            currentValue = now.getWeekOfWeekyear();
                            Log.i(LOG_TAG, "week number is " + currentValue);
                            break;
                        case MONTHLY:
                            currentValue = now.getMonthOfYear();
                            break;
                    }

                    if (previousValue == -1) {
                        valueCount = 1;
                        previousValue = currentValue;
                    } else if (currentValue == previousValue) {
                        valueCount++;
                    } else {
                        int expected = nrPerPeriod;
                        if (period == Reminder.Period.HOURLY) {
                            expected *= activeHours;
                        }
                        Log.i(LOG_TAG, String.format("Got %d notifications for period %s, nrPerPeriod %d",
                                valueCount, period, expected));
                        Assert.assertEquals(valueCount, expected);
//                        if (currentValue != previousValue + 1) {
//                            Assert.assertEquals(currentValue, previousValue + 1);
//                        }
                        valueCount = 1;
                        previousValue = currentValue;
                    }
                }
            }
        }
    }
}