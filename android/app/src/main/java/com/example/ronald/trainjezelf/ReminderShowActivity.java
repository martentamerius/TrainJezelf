package com.example.ronald.trainjezelf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class ReminderShowActivity extends FragmentActivity
        implements ReminderShowFragment.OnFragmentInteractionListener {

    public static final String ARGUMENT_NOTIFICATION_TEXT = "notificationText";

    /**
     * The reminder index that we are showing
     */
    private String reminderText;

    /**
     * Reference to the display fragment
     */
    private ReminderShowFragment reminderShowFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_show);

        // Get handle to embedded fragment
        FragmentManager manager = getSupportFragmentManager();
        reminderShowFragment = (ReminderShowFragment) manager.findFragmentById(R.id.reminderShowFragment);

        // Fill activity with data to be edited
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            reminderText = extras.getString(ARGUMENT_NOTIFICATION_TEXT, "<er is iets mis gegaan>");
            refreshView();
        } else {
            Log.d("ReminderShowActivity", "no extras received");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }

    private void refreshView() {
        reminderShowFragment.displayReminder(reminderText);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Discard notification
//        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
//                .getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancel();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO?
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("ReminderShowActivity", "received new intent");
        setIntent(intent);
    }
}
