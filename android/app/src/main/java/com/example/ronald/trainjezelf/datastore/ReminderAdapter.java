package com.example.ronald.trainjezelf.datastore;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.ronald.trainjezelf.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Ronald on 4-7-2014.
 */
public class ReminderAdapter extends ArrayAdapter<Reminder> {
    private final Context context;
    private final List<Reminder> data;

    public ReminderAdapter(Context context, int layoutResourceId, List<Reminder> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Reminder reminder = data.get(position);

        // Get a row view
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.listview_reminder_row, parent, false);

        // Populate row view
        final TextView messageView = (TextView) rowView.findViewById(R.id.message);
        messageView.setText(reminder.getMessage());
        final TextView frequencyView = (TextView) rowView.findViewById(R.id.frequency);
        frequencyView.setText(reminder.frequencyToString());
        // TODO: icon

        return rowView;
    }
}
