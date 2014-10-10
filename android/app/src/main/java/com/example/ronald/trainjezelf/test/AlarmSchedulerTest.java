package com.example.ronald.trainjezelf.test;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.example.ronald.trainjezelf.alarm.AlarmScheduler;
import com.example.ronald.trainjezelf.datastore.Reminder;

import org.joda.time.DateTime;

/**
 * Created by ronald on 2-10-14.
 */
public class AlarmSchedulerTest extends InstrumentationTestCase {

    public void testGetMillisOfNextReminder() {

        for (Reminder.Period period : Reminder.Period.values()) {
            period = Reminder.Period.WEEKLY;
            for (int nrPerPeriod = 10; nrPerPeriod <= 10; nrPerPeriod++) {
                DateTime now = new DateTime(1388534400000L); // 1-1-2014 00:00
                //DateTime now = new DateTime(1388604600000L); // 1-1-2014 19:30
                Reminder reminder = new Reminder("", nrPerPeriod, period, 0);

                for (int i = 0; i < nrPerPeriod * 8; i++) {
                    final long millisOfNextNotification = AlarmScheduler.getMillisOfNextNotification(now, reminder);
                    now = new DateTime(millisOfNextNotification);
                    Log.d("tag", now.toString());
                }
            }
        }
    }
}
