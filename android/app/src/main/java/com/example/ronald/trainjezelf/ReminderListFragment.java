package com.example.ronald.trainjezelf;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;
import com.example.ronald.trainjezelf.datastore.ReminderAdapter;

/**
 * Reminder list fragment class.
 */
public class ReminderListFragment extends ListFragment implements OnItemClickListener {
    /**
     * Adapter that handles filling of the list view
     */
    private ReminderAdapter adapter = null;

    /**
     * The listener we are to notify when a list item is selected
     */
    private OnReminderSelectedListener mReminderSelectedListener = null;

    /**
     * The data store
     */
    private DataStore dataStore = null;

    /**
     * Listener that will be notified of list item selections
     */
    public interface OnReminderSelectedListener {
        public void onReminderSelected(int index);
    }

    /**
     * Default constructor required by framework.
     */
    public ReminderListFragment() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataStore = DataStore.getInstance(getActivity());
        adapter = new ReminderAdapter(getActivity(), android.R.layout.simple_list_item_1, dataStore.getReminders());
        setListAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        getListView().setOnItemClickListener(this);
        registerForContextMenu(getListView());
    }

    /**
     * Sets the listener that should be notified of list item selection events.
     * @param listener the listener to notify.
     */
    public void setOnReminderSelectedListener(OnReminderSelectedListener listener) {
        mReminderSelectedListener = listener;
    }

    /**
     * Handles a click on a list item.
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
    public int addReminder() {
        int newSize = dataStore.add(new Reminder("", 5, Reminder.Period.DAILY));
        adapter.notifyDataSetChanged();
        return newSize;
    }

    /**
     * Remove reminder from the list
     */
    private void removeReminder(int reminderId) {
        dataStore.remove(reminderId);
        adapter.notifyDataSetChanged();
    }

    /**
     * Get reminder from the list
     * @param index in the list
     * @return the reminder
     */
    public Reminder getReminder(int index) {
        return dataStore.get(index);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater mi = getActivity().getMenuInflater();
        mi.inflate(R.menu.listview_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                removeReminder(info.position);
                return true;
        }
        return super.onContextItemSelected(item);
    }
}
