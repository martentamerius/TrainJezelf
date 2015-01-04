package com.github.trainjezelf.datastore;

import android.content.Context;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.trainjezelf.R;

import java.util.List;

/**
 * Renders a view for one reminder list element.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    /**
     * Handle to application context
     */
    private Context context;

    /**
     * Own, private copy of the reminder list
     */
    private List<Reminder> reminderList;

    /**
     * Listener for clicks on one card
     */
    private IReminderViewHolderClickListener onClickListener = null;

    /**
     * Listener for card context menu actions
     */
    private IReminderViewHolderCardMenuListener cardMenuListener = null;

    public ReminderAdapter(Context context, List<Reminder> data) {
        this.context = context;
        this.reminderList = data;
    }

    public void setOnClickListener(IReminderViewHolderClickListener listener) {
        onClickListener = listener;
    }

    public void setCardMenuListener(IReminderViewHolderCardMenuListener listener) {
        cardMenuListener = listener;
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder viewHolder, int i) {
        Reminder reminder = reminderList.get(i);
        viewHolder.context = context;
        viewHolder.uniqueId = reminder.getUniqueId();
        viewHolder.message.setText(reminder.getMessage());
        viewHolder.frequency.setText(reminder.frequencyToString());
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_reminder_row, viewGroup, false);
        return new ReminderViewHolder(itemView, onClickListener, cardMenuListener);
    }

    public void replace(List<Reminder> reminders) {
        reminderList.clear();
        reminderList.addAll(reminders);
        notifyDataSetChanged();
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener, ActionMenuView.OnMenuItemClickListener {

        protected Context context;
        protected int uniqueId;
        protected TextView message;
        protected TextView frequency;

        private final IReminderViewHolderClickListener listener;
        private final IReminderViewHolderCardMenuListener menuListener;

        public ReminderViewHolder(View v, IReminderViewHolderClickListener listener,
                                  IReminderViewHolderCardMenuListener menuListener) {
            super(v);
            message = (TextView)v.findViewById(R.id.message);
            frequency = (TextView)v.findViewById(R.id.frequency);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            this.listener = listener;
            this.menuListener = menuListener;
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onReminderClicked(uniqueId);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final PopupMenu popup = new PopupMenu(context, v);
            final MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.card, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (menuListener != null) {
                        menuListener.onReminderDelete(uniqueId);
                    }
                    return false;
                }
            });
            popup.show();
            return true;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (menuListener != null) {
                final int id = item.getItemId();
                if (id == R.id.action_delete) {
                    menuListener.onReminderDelete(uniqueId);
                }
            }
            return false;
        }
    }

    public static interface IReminderViewHolderClickListener {
        public void onReminderClicked(int uniqueId);
    }

    public static interface IReminderViewHolderCardMenuListener {
        public void onReminderDelete(int uniqueId);
    }
}
