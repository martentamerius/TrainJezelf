package com.example.ronald.trainjezelf;

import android.app.Application;
import android.util.Log;

import com.example.ronald.trainjezelf.alarm.AlarmScheduler;

/**
 * Application wrapper class to handle execute-once actions.
 */
public class MyApplication extends Application {

    private boolean isFirstStart = true;

    public MyApplication() {
        // this method fires only once per application start.
        // getApplicationContext returns null here
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // this method fires once as well as constructor
        // but also application has context here

        if (isFirstStart) {
            Log.d("MyApplication", "re-scheduling all reminders");
            AlarmScheduler.reScheduleAllReminders(this);
            isFirstStart = false;
        }
    }
}
