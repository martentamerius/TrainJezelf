package com.github.trainjezelf.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.trainjezelf.datastore.DataStore;

/**
 * Receives dismissal of notification by user
 */
public class DismissReceiver extends BroadcastReceiver {

    public static final String ARGUMENT_NOTIFICATION_ID = "notificationId";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final int notificationId = intent.getExtras().getInt(ARGUMENT_NOTIFICATION_ID);
        final DataStore dataStore = DataStore.getInstance(context);
        dataStore.clearNumberOfNotifications(notificationId);
    }

}
