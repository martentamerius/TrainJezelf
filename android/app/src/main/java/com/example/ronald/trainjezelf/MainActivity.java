package com.example.ronald.trainjezelf;

import android.content.Intent;
import android.os.Bundle;
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
    boolean mIsDualPane = false;

    /**
     * The fragment where the reminder list is displayed
     */
    ReminderListFragment mReminderListFragment;

    /**
     * The fragment where the reminder details (null if absent)
     */
    ReminderDetailsFragment mReminderDetailsFragment;

    /**
     * Object for managing permanent storage of activity data
     */
    private DataStore dataStore = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        // find our fragments
        FragmentManager manager = getSupportFragmentManager();
        mReminderListFragment = (ReminderListFragment) manager.findFragmentById(R.id.headlines);
        mReminderDetailsFragment = (ReminderDetailsFragment) manager.findFragmentById(R.id.article);

        // Determine whether we are in single-pane or dual-pane mode by testing the visibility
        // of the reminder view.
        View reminderView = findViewById(R.id.article);
        mIsDualPane = reminderView != null && reminderView.getVisibility() == View.VISIBLE;

        // what to do when someone clicks in the list
        mReminderListFragment.setOnHeadlineSelectedListener(this);

        // Set up headlines fragment
        mReminderListFragment.setSelectable(mIsDualPane);
        restoreSelection(savedInstanceState);
    }

    /** Restore category/article selection from saved state. */
    void restoreSelection(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (mIsDualPane) {
                int reminderIndex = savedInstanceState.getInt("reminderIndex", 0);
                mReminderListFragment.setSelection(reminderIndex);
                onReminderSelected(reminderIndex);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
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
            mReminderListFragment.addReminder();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReminderSelected(int index) {
        if (mIsDualPane) {
            // display it on the article fragment
            mReminderDetailsFragment.displayReminder(mReminderListFragment.getReminder(index));
        } else {
            // use separate activity
            final Intent i = new Intent(MainActivity.this, ReminderEditActivity.class);
            i.putExtra("messageId", index);
            startActivity(i);
        }
    }
}
