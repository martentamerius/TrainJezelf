package com.github.trainjezelf.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.github.trainjezelf.R;
import com.github.trainjezelf.datastore.DataStore;
import com.github.trainjezelf.datastore.Reminder;

/**
 * Receives alarms from the app, even when the app is not running.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final int COLOR_PURPLE = 0xff9933cc;

    public static final String ARGUMENT_NOTIFICATION_ID = "notificationId";

    @Override
    public void onReceive(Context context, Intent intent) {
        final int notificationId = intent.getExtras().getInt(ARGUMENT_NOTIFICATION_ID);

        // Get notification contents from reminder in DataStore
        final DataStore dataStore = DataStore.getInstance(context);
        final Reminder reminder = dataStore.get(notificationId);
        final String notificationText = reminder.getMessage();
        final int notificationNumber = dataStore.getNextNumberOfNotifications(notificationId);

        // Create intent that will be used when the user dismisses the notification
        Intent deleteIntent = new Intent(context, DismissReceiver.class);
        deleteIntent.putExtra(DismissReceiver.ARGUMENT_NOTIFICATION_ID, notificationId);
        deleteIntent.setType("ID=" + notificationId);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, notificationId, deleteIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setTicker(notificationText)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(notificationText)
                        .setNumber(notificationNumber)
                        .setVibrate(new long[]{0, 350, 0})
                        .setLights(COLOR_PURPLE, 750, 2250)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDeleteIntent(deletePendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // ID allows us to update the notification later on.
        notificationManager.notify(notificationId, mBuilder.build());

        // Schedule the next notification; essentially, the notifications are daisy-chained.
        AlarmScheduler.scheduleNextReminder(context, notificationId);
    }
}
