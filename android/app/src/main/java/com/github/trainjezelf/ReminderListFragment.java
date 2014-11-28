package com.github.trainjezelf;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.github.trainjezelf.alarm.AlarmScheduler;
import com.github.trainjezelf.datastore.DataStore;
import com.github.trainjezelf.datastore.Reminder;
import com.github.trainjezelf.datastore.ReminderAdapter;

import java.util.List;

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
     * The list of reminders
     */
    private List<Reminder> reminders = null;

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
    }

    @Override
    public void onStart() {
        super.onStart();
        reminders = dataStore.getReminders();
        adapter = new ReminderAdapter(getActivity(), android.R.layout.simple_list_item_1, reminders);
        setListAdapter(adapter);
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
            mReminderSelectedListener.onReminderSelected(reminders.get(position).getUniqueId());
        }
    }

    /**
     * Remove reminder from the list
     */
    private void removeReminder(int listIndex) {
        int uniqueId = reminders.get(listIndex).getUniqueId();
        AlarmScheduler.cancelScheduledReminder(getActivity().getApplicationContext(), uniqueId);
        reminders = dataStore.removeReminder(uniqueId);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        adapter.clear();
        adapter.addAll(reminders);
        adapter.notifyDataSetChanged();
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
