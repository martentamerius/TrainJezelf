package com.example.ronald.trainjezelf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;

import java.util.ArrayList;
import java.util.List;

public class ReminderEditActivity extends Activity {
    public static final String ARGUMENT_REMINDER_KEY = "reminderIndex";

    private Reminder reminder;

    private EditText messageEditText;
    private EditText frequencyEditText;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);

        // Get references to GUI elements
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        frequencyEditText = (EditText) findViewById(R.id.frequencyEditText);
        spinner = (Spinner) findViewById(R.id.spinner);

        // Populate spinner
        populateSpinner();

        // Fill activity with data to be edited
        Intent intent = getIntent();
        int reminderIndex = intent.getExtras().getInt(ARGUMENT_REMINDER_KEY);
        reminder = DataStore.getInstance(this).get(reminderIndex);
        populateView(reminder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reminder_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save) {
            DataStore dataStore = DataStore.getInstance(this);
            dataStore.remove(reminder);
            dataStore.add(getFromView());
            finish();
            return true;
        }
        if (id == R.id.action_cancel) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        for (Reminder.Period period: Reminder.Period.values()) {
            list.add(period.toString());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private void populateView(Reminder reminder) {
        messageEditText.setText(reminder.getMessage());
        frequencyEditText.setText(Integer.toString(reminder.getNumberOfNotifiesPerPeriod()));
        spinner.setSelection(reminder.getPeriod().ordinal());
    }

    private Reminder getFromView() {
        String message = messageEditText.getText().toString();
        int numberOfNotifiesPerPeriod;
        try {
            numberOfNotifiesPerPeriod = Integer.parseInt(frequencyEditText.getText().toString());
        } catch (NumberFormatException e) {
            numberOfNotifiesPerPeriod = 1;
        }
        Reminder.Period period = Reminder.Period.values()[spinner.getSelectedItemPosition()];
        return new Reminder(message, numberOfNotifiesPerPeriod, period);
    }
}
