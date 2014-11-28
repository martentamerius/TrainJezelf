package com.github.trainjezelf.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.trainjezelf.datastore.DataStore;
import com.github.trainjezelf.datastore.Reminder;

/**
 * Class that receives the boot intent and starts the relevant process.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Schedule all reminders
            for (Reminder reminder : DataStore.getInstance(context).getReminders()) {
                AlarmScheduler.scheduleNextReminder(context, reminder.getUniqueId());
            }
        }
    }

}
