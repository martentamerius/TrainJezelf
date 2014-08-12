package com.example.ronald.trainjezelf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.ronald.trainjezelf.datastore.DataStore;


/**
 * Main activity: displays a ListView with all reminders that the user has created.
 */
public class MainActivity extends FragmentActivity implements ReminderListFragment.OnReminderSelectedListener {
    /**
     * Whether or not we are in dual-pane mode
     */
    private boolean isDualPane = false;

    /**
     * The fragment where the reminder list is displayed
     */
    private ReminderListFragment reminderListFragment;

    /**
     * The fragment where the reminder details are displayed (null if absent)
     */
    private ReminderEditFragment reminderEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // find our fragments
        FragmentManager manager = getSupportFragmentManager();
        reminderListFragment = (ReminderListFragment) manager.findFragmentById(R.id.headlines);
        reminderEditFragment = (ReminderEditFragment) manager.findFragmentById(R.id.article);

        // Determine whether we are in single-pane or dual-pane mode by testing the visibility
        // of the reminder view.
        View reminderView = findViewById(R.id.article);
        isDualPane = reminderView != null && reminderView.getVisibility() == View.VISIBLE;

        // what to do when someone clicks in the list
        reminderListFragment.setOnReminderSelectedListener(this);

        // Set up headlines fragment
        reminderListFragment.setSelectable(isDualPane);
        restoreSelection(savedInstanceState);
    }

    /** Restore category/article selection from saved state. */
    void restoreSelection(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (isDualPane) {
                int reminderIndex = savedInstanceState.getInt("reminderIndex", 0);
                reminderListFragment.setSelection(reminderIndex);
                onReminderSelected(reminderIndex);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        restoreSelection(savedInstanceState);
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
            int numberOfReminders = reminderListFragment.addReminder();
            onReminderSelected(numberOfReminders - 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReminderSelected(int index) {
        if (isDualPane) {
            // display it on the article fragment
            reminderEditFragment.displayReminder(reminderListFragment.getReminder(index));
        } else {
            // use separate activity
            final Intent i = new Intent(MainActivity.this, ReminderEditActivity.class);
            i.putExtra(ReminderEditActivity.ARGUMENT_REMINDER_KEY, index);
            startActivity(i);
        }
    }

    /**
     * Save persistent app state.
     * According to the documentation, the right place for this is the onPause() method.
     * http://developer.android.com/training/basics/activity-lifecycle/pausing.html
     */
    @Override
    public void onPause() {
        super.onPause(); // Always call the superclass method first
        DataStore.getInstance(this).saveState();
    }
}
