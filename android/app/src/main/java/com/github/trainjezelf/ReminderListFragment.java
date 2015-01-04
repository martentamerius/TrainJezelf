package com.github.trainjezelf;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.github.trainjezelf.alarm.AlarmScheduler;
import com.github.trainjezelf.datastore.DataStore;
import com.github.trainjezelf.datastore.Reminder;
import com.github.trainjezelf.datastore.ReminderAdapter;
import com.shamanland.fab.FloatingActionButton;

import java.util.List;

/**
 * Reminder list fragment class.
 */
public class ReminderListFragment extends Fragment implements ReminderAdapter.IReminderViewHolderClickListener,
        View.OnClickListener, ReminderAdapter.IReminderViewHolderCardMenuListener {

    private static final float MINIMUM_CARD_WIDTH_DIP = 300.0f;

    /**
     * The floating action button
     */
    FloatingActionButton fab;

    /**
     * RecyclerView that handles display of the list
     */
    private RecyclerView recyclerView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reminder_list, container, false);

        // Get width of screen
        final DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        final float widthDp = dm.widthPixels / dm.density;
        final int nrofGridColumns = (int)(widthDp / MINIMUM_CARD_WIDTH_DIP);

        // Attach layout manager to recycler view
        recyclerView = (RecyclerView)view.findViewById(R.id.cardList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(nrofGridColumns,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // Populate adapter
        reminders = dataStore.getReminders();
        adapter = new ReminderAdapter(getActivity().getApplicationContext(), reminders);
        adapter.setOnClickListener(this);
        adapter.setCardMenuListener(this);
        recyclerView.setAdapter(adapter);
        registerForContextMenu(recyclerView);

        // Get handle to floating action button
        fab = (FloatingActionButton)view.findViewById(R.id.fab_new);
        fab.setOnClickListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Re-populate adapter, as the user might have added a notification (in the edit screen)
        reminders = dataStore.getReminders();
        adapter.replace(reminders);
    }

    /**
     * Sets the listener that should be notified of list item selection events.
     * @param listener the listener to notify.
     */
    public void setOnReminderSelectedListener(OnReminderSelectedListener listener) {
        mReminderSelectedListener = listener;
    }

    @Override
    public void onReminderClicked(int uniqueId) {
        if (mReminderSelectedListener != null) {
            mReminderSelectedListener.onReminderSelected(uniqueId);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == fab) {
            this.onReminderClicked(Reminder.NEW_REMINDER_UID);
        }
    }

    @Override
    public void onReminderDelete(int uniqueId) {
        removeReminder(uniqueId);
    }

    /**
     * Remove reminder from the list
     */
    private void removeReminder(int uniqueId) {
        AlarmScheduler.cancelScheduledReminder(getActivity().getApplicationContext(), uniqueId);
        reminders = dataStore.removeReminder(uniqueId);
        adapter.replace(reminders);
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
                int uniqueId = reminders.get(info.position).getUniqueId();
                removeReminder(uniqueId);
                return true;
        }
        return super.onContextItemSelected(item);
    }
}
