package com.example.ronald.trainjezelf;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ronald.trainjezelf.alarm.AlarmScheduler;
import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;

public class ReminderEditActivity extends FragmentActivity
        implements ReminderEditFragment.OnFragmentInteractionListener {
    
    public static final String ARGUMENT_REMINDER_UNIQUE_ID = "reminderUniqueId";

    /**
     * The reminder index that we are showing
     */
    private int reminderUniqueId;

    /**
     * Reference to the display fragment
     */
    private ReminderEditFragment reminderEditFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Get handle to embedded fragment
        FragmentManager manager = getSupportFragmentManager();
        reminderEditFragment = (ReminderEditFragment) manager.findFragmentById(R.id.reminderEditFragment);

        // Fill activity with data to be edited
        Intent intent = getIntent();
        reminderUniqueId = intent.getExtras().getInt(ARGUMENT_REMINDER_UNIQUE_ID);
        refreshView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reminderEditFragment.saveReminder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshView();
    }

    private void refreshView() {
        Reminder reminder = DataStore.getInstance(this).get(reminderUniqueId);
        reminderEditFragment.displayReminder(reminder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reminder_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save) {
            Reminder reminder = reminderEditFragment.saveReminder();
            AlarmScheduler.scheduleNextReminder(this, reminder.getUniqueId());
            finish();
            return true;
        }
        if (id == R.id.action_cancel || id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO?
    }
}
