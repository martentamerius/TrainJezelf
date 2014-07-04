package com.example.ronald.trainjezelf;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;
import com.example.ronald.trainjezelf.datastore.ReminderAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity: displays a ListView with all reminders that the user has created.
 */
public class MainActivity extends Activity {
    /**
     * ListView containing the reminders
     */
    private ListView listView = null;

    /**
     * Items for the list
     */
    private List<Reminder> listItems = null;

    /**
     * Adapter that handles filling of the listview
     */
    private ReminderAdapter adapter = null;

    /**
     * Object for managing permanent storage of activity data
     */
    private DataStore dataStore = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create list for holding the resources
        if (dataStore == null) {
            dataStore = new DataStore(this);
        }
        listItems = dataStore.loadReminders();

        // create adapter
        adapter = new ReminderAdapter(this, android.R.layout.simple_list_item_1, listItems);

        // get reference to the listview resource
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        // what to do when someone clicks in the list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final Reminder item = (Reminder) parent.getItemAtPosition(position);
                listItems.remove(item);
                adapter.notifyDataSetChanged();
            }
        });
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
            addListViewItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        dataStore.saveReminders(this.listItems);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Add new reminder to the list
     */
    private void addListViewItem() {
        listItems.add(new Reminder("Reminder", 5, Reminder.Period.DAILY));
        adapter.notifyDataSetChanged();
    }
}
