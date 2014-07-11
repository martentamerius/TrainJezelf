package com.example.ronald.trainjezelf;

import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;
import com.example.ronald.trainjezelf.datastore.ReminderAdapter;

import java.util.List;

/**
 * Created by Ronald on 8-7-2014.
 */
public class ReminderListFragment extends ListFragment implements OnItemClickListener {
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
     * The listener we are to notify when a list item is selected
     */
    OnReminderSelectedListener mReminderSelectedListener = null;

    /**
     * The datastore
     */
    DataStore dataStore = null;

    /**
     * Listener that will be notified of list item selections
     */
    public interface OnReminderSelectedListener {
        /**
         * Called when a given list item is selected.
         * @param index the index of the selected item.
         */
        public void onReminderSelected(int index);
    }

    /**
     * Default constructor required by framework.
     */
    public ReminderListFragment() {
        super();
    }

    @Override
    public void onStart() {
        super.onStart();
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listItems = getDataStore().loadReminders();
        adapter = new ReminderAdapter(getActivity(), android.R.layout.simple_list_item_1, listItems);
        setListAdapter(adapter);
    }

    /**
     * Sets the listener that should be notified of list item selection events.
     * @param listener the listener to notify.
     */
    public void setOnHeadlineSelectedListener(OnReminderSelectedListener listener) {
        mReminderSelectedListener = listener;
    }

    /**
     * Handles a click on a list item.
     *
     * This causes the configured listener to be notified that an item was selected.
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mReminderSelectedListener) {
            mReminderSelectedListener.onReminderSelected(position);
        }
    }

    /** Sets choice mode for the list
     *
     * @param selectable whether list is to be selectable.
     */
    public void setSelectable(boolean selectable) {
        if (selectable) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        } else {
            getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
    }

    /**
     * Add new reminder to the list
     */
    public void addReminder() {
        listItems.add(new Reminder("Reminder", 5, Reminder.Period.DAILY));
        adapter.notifyDataSetChanged();
    }

    /**
     * Remove reminder from the list
     */
    public void removeReminder(Reminder item) {
        listItems.remove(item);
        adapter.notifyDataSetChanged();
    }

    private DataStore getDataStore() {
        if (dataStore == null) {
            dataStore = new DataStore(getActivity());
        }
        return dataStore;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        getDataStore().saveReminders(this.listItems);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public Reminder getReminder(int index) {
        return listItems.get(index);
    }
}
