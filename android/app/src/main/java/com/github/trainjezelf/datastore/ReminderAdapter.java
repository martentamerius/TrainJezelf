package com.github.trainjezelf.datastore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.trainjezelf.R;

import java.util.List;

/**
 * Renders a view for one reminder list element.
 */
public class ReminderAdapter extends ArrayAdapter<Reminder> {
    private final Context context;
    private List<Reminder> data;

    public ReminderAdapter(Context context, int layoutResourceId, List<Reminder> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
    }

    // Cache view items for list scrolling performance
    // This prevents expensive calls to findViewById for each list item.
    static class ViewHolder {
        TextView message;
        TextView frequency;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder viewHolder;
        if (rowView == null) {
            final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.listview_reminder_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.message = (TextView) rowView.findViewById(R.id.message);
            viewHolder.frequency = (TextView) rowView.findViewById(R.id.frequency);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }
        final Reminder reminder = data.get(position);
        viewHolder.message.setText(reminder.getMessage());
        viewHolder.frequency.setText(reminder.frequencyToString());
        return rowView;
    }
}
