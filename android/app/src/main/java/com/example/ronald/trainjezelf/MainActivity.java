package com.example.ronald.trainjezelf;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ronald.trainjezelf.datastore.Reminder;


/**
 * Main activity: displays a fragment that lists all reminders that the user has created.
 */
public class MainActivity extends FragmentActivity implements ReminderListFragment.OnReminderSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // Load preference defaults, if they have no value yet
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        // Set fragment callback for when someone clicks in the list
        FragmentManager manager = getSupportFragmentManager();
        ReminderListFragment reminderListFragment = (ReminderListFragment) manager.findFragmentById(R.id.headlines);
        reminderListFragment.setOnReminderSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
            onReminderSelected(Reminder.NEW_REMINDER_UID);
            return true;
        } else if (id == R.id.action_preferences) {
            final Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_about) {
            final Intent i = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReminderSelected(int uniqueId) {
        // Edit reminder in a new activity
        final Intent intent = new Intent(MainActivity.this, ReminderEditActivity.class);
        intent.putExtra(ReminderEditActivity.ARGUMENT_REMINDER_UNIQUE_ID, uniqueId);
        startActivity(intent);
    }
}
