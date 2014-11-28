package com.github.trainjezelf;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.github.trainjezelf.alarm.AlarmScheduler;

public class ReminderEditActivity extends FragmentActivity {
    
    public static final String ARGUMENT_REMINDER_UNIQUE_ID = "reminderUniqueId";

    /**
     * Reference to the edit fragment
     */
    private ReminderEditFragment reminderEditFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);
        setupActionBar();

        // Get handle to editing fragment
        FragmentManager manager = getSupportFragmentManager();
        reminderEditFragment = (ReminderEditFragment) manager.findFragmentById(R.id.reminderEditFragment);

        // Fill activity with data to be edited
        Intent intent = getIntent();
        int reminderUniqueId = intent.getExtras().getInt(ARGUMENT_REMINDER_UNIQUE_ID);
        reminderEditFragment.displayReminder(reminderUniqueId);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
        final int id = item.getItemId();
        if (id == R.id.action_save) {
            int uniqueId = reminderEditFragment.saveReminder();
            AlarmScheduler.scheduleNextReminder(this, uniqueId);
            setResult(RESULT_OK, getIntent());
            finish();
            return true;
        }
        if (id == R.id.action_cancel || id == android.R.id.home) {
            setResult(RESULT_CANCELED, getIntent());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
