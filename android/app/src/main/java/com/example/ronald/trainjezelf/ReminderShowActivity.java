package com.example.ronald.trainjezelf;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;

public class ReminderShowActivity extends FragmentActivity
        implements ReminderShowFragment.OnFragmentInteractionListener {

    public static String ARGUMENT_REMINDER_KEY = "reminderIndex";

    private DataStore dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_show);

        // Get handle to embedded fragment
        FragmentManager manager = getSupportFragmentManager();
        ReminderShowFragment reminderShowFragment = (ReminderShowFragment) manager.findFragmentById(R.id.reminderShowFragment);

        // Fill activity with data to be edited
        Intent intent = getIntent();
        int reminderIndex = intent.getExtras().getInt(ARGUMENT_REMINDER_KEY);
        Reminder reminder = DataStore.getInstance(this).get(reminderIndex);
        reminderShowFragment.displayReminder(reminder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reminder_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO?
    }
}
