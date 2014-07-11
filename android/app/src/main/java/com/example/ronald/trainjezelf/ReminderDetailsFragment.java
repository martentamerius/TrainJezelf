package com.example.ronald.trainjezelf;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.ronald.trainjezelf.datastore.Reminder;

/**
 * Fragment that displays a Reminder.
 */
public class ReminderDetailsFragment extends Fragment {
    /**
     * The reminder
     */
    private Reminder reminder;

    /**
     * Sets up the UI. It consists if a single WebView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.activity_reminder_edit, container, false);
    }

    /**
     * Displays a particular reminder.
     * @param reminder the reminder to display
     */
    public void displayReminder(Reminder reminder) {
        this.reminder = reminder;
        loadReminder();
    }

    /**
     * Loads reminder data into the UI.
     */
    private void loadReminder() {
        EditText messageEditText = (EditText) getView().findViewById(R.id.messageEditText);
        messageEditText.setText(reminder.getMessage());
    }
}
