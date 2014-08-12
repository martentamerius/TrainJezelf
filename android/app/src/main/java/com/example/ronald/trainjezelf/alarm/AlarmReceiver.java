package com.example.ronald.trainjezelf.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.ronald.trainjezelf.R;
import com.example.ronald.trainjezelf.ReminderShowActivity;

/**
 * Receives alarms from the app, even when the app is not running.
 * Created by ronald on 8-8-14.
 */
public class AlarmReceiver extends BroadcastReceiver {
    // TODO Factor out
    public static final String ARGUMENT_REMINDER_KEY = "reminderIndex";
    public static final String ARGUMENT_NOTIFICATION_TEXT = "notificationText";

    @Override
    public void onReceive(Context context, Intent intent) {
        final int reminderIndex = intent.getExtras().getInt(ARGUMENT_REMINDER_KEY);
        final String notificationText = intent.getExtras().getString(ARGUMENT_NOTIFICATION_TEXT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(notificationText);

        // Create intent for activity in our app
        Intent resultIntent = new Intent(context, ReminderShowActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Pass parameter to activity
        resultIntent.putExtra(ReminderShowActivity.ARGUMENT_REMINDER_KEY, reminderIndex);

        // Create the pending intent. Note that FLAG_UPDATE_CURRENT is essential for
        // passing intent extras to the activity.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // id allows you to update the notification later on.
        notificationManager.notify(1, mBuilder.build());
    }
}
