package com.example.ronald.trainjezelf;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;

public class ReminderShowActivity extends FragmentActivity
        implements ReminderShowFragment.OnFragmentInteractionListener {

    public static String ARGUMENT_REMINDER_KEY = "reminderIndex";

    /**
     * The reminder index that we are showing
     */
    private int reminderIndex;

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
            reminderIndex = extras.getInt(ARGUMENT_REMINDER_KEY);
            Log.d("ReminderShowActivity", "received reminder index " + reminderIndex);
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
        if (id == R.id.action_edit) {
            final Intent i = new Intent(ReminderShowActivity.this, ReminderEditActivity.class);
            i.putExtra(ReminderEditActivity.ARGUMENT_REMINDER_KEY, reminderIndex);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_delete) {
            DataStore.getInstance(this).remove(reminderIndex);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
