package com.example.ronald.trainjezelf.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;

import com.example.ronald.trainjezelf.ReminderEditActivity;

/**
 * Created by ronald on 8-8-14.
 */
public class AlarmScheduler {

    /**
     * Schedule alarm
     * @param activity from activity
     * @param secondsFromNow seconds from now
     */
    public static void startAlert(Activity activity, int secondsFromNow, int reminderIndex, String notificationText) {
        Intent intent = new Intent(activity, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.ARGUMENT_REMINDER_KEY, reminderIndex);
        intent.putExtra(AlarmReceiver.ARGUMENT_NOTIFICATION_TEXT, notificationText);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity.getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Activity.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (secondsFromNow * 1000), pendingIntent);
    }
}
