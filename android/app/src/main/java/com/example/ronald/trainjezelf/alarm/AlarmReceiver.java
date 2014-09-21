package com.example.ronald.trainjezelf.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ronald.trainjezelf.R;
import com.example.ronald.trainjezelf.ReminderShowActivity;

/**
 * Receives alarms from the app, even when the app is not running.
 * Created by ronald on 8-8-14.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "AlarmReceiver";

    // TODO Factor out
    public static final String ARGUMENT_NOTIFICATION_TEXT = "notificationText";
    public static final String ARGUMENT_NOTIFICATION_ID = "notificationId";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String notificationText = intent.getExtras().getString(ARGUMENT_NOTIFICATION_TEXT);
        final int notificationId = intent.getExtras().getInt(ARGUMENT_NOTIFICATION_ID);

        Log.d(LOG_TAG, "Notification ID: " + notificationId);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setTicker(notificationText)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(notificationText)
                        //.setNumber(intentId) // TODO set correct number
                        .setAutoCancel(true)
                        .setVibrate(new long[] {0, 350, 0})
                        .setLights(Color.GREEN, 750, 2250) // TODO set purple
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        // Create intent for activity in our app
        Intent resultIntent = new Intent(context, ReminderShowActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Pass parameter to activity
        resultIntent.putExtra(ReminderShowActivity.ARGUMENT_NOTIFICATION_TEXT, notificationText);

        // Create the pending intent. Note that FLAG_UPDATE_CURRENT is essential for
        // passing intent extras to the activity.
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // id allows you to update the notification later on.
        notificationManager.notify(notificationId, mBuilder.build());

        // Schedule new one
        AlarmScheduler.scheduleNextReminder(context, notificationId);
    }
}
