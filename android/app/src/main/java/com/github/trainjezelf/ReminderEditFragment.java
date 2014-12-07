package com.github.trainjezelf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.trainjezelf.datastore.DataStore;
import com.github.trainjezelf.datastore.Reminder;
import com.github.trainjezelf.views.RadioGroupTable;


/**
 * Fragment for editing a reminder.
 */
public class ReminderEditFragment extends Fragment {

    /**
     * Unique ID of the reminder to edit.
     */
    private int uniqueId;

    /**
     * The reminder being edited.
     */
    private Reminder reminder;

    /**
     * GUI controls
     */
    private EditText messageEditText;
    private RadioGroupTable frequencyNumberGroup;
    private RadioGroup frequencyPeriodGroup;

    public ReminderEditFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reminder_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get references to GUI elements
        messageEditText = (EditText) getView().findViewById(R.id.messageEditText);
        frequencyNumberGroup = (RadioGroupTable) getView().findViewById(R.id.frequencyNumberPad);
        frequencyPeriodGroup = (RadioGroup) getView().findViewById(R.id.frequencyPeriodRadioGroup);
    }

    /**
     * Displays a particular reminder for editing.
     * @param uniqueId of the reminder to edit
     */
    public void displayReminder(int uniqueId) {
        this.uniqueId = uniqueId;

        if (uniqueId != Reminder.NEW_REMINDER_UID) {
            reminder = DataStore.getInstance(getActivity().getApplicationContext()).get(uniqueId);
            populateView();
        } else {
            populateViewWithDefaults();
        }

        // Hide keyboard when there is already some text
        if (!messageEditText.getText().toString().isEmpty()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    /**
     * Loads reminder data into the UI.
     */
    private void populateView() {
        messageEditText.setText(reminder.getMessage());
        messageEditText.setSelection(reminder.getMessage().length());
        frequencyNumberGroup.check(getResources().getIdentifier("button" + reminder.getNumberOfNotifiesPerPeriod(),
                "id", getActivity().getPackageName()));
        frequencyPeriodGroup.check(getResources().getIdentifier(reminder.getPeriod().getButtonId(), "id",
                getActivity().getPackageName()));
    }

    /**
     * Loads default reminder data into the UI.
     */
    private void populateViewWithDefaults() {
        messageEditText.setText(Reminder.DEFAULT_REMINDER_MESSAGE);
        messageEditText.setSelection(Reminder.DEFAULT_REMINDER_MESSAGE.length());
        frequencyNumberGroup.check(getResources().getIdentifier(
                "button" + Reminder.DEFAULT_NUMBER_OF_NOTIFIES_PER_PERIOD, "id", getActivity().getPackageName()));
        frequencyPeriodGroup.check(getResources().getIdentifier(Reminder.DEFAULT_PERIOD.getButtonId(), "id",
                getActivity().getPackageName()));
    }

    /**
     * Saves reminder as it is currently configured in the GUI.
     * @return unique ID of reminder
     */
    public int saveReminder() {
        DataStore dataStore = DataStore.getInstance(getActivity().getApplicationContext());
        reminder = dataStore.put(getFromView());
        return reminder.getUniqueId();
    }

    private Reminder getFromView() {
        final String message = messageEditText.getText().toString();

        // TODO bit hacky, but works
        final int selectedFrequencyNumberId = frequencyNumberGroup.getCheckedRadioButtonId();
        RadioButton button = (RadioButton)getView().findViewById(selectedFrequencyNumberId);
        int numberOfNotifiesPerPeriod = 1;
        if (button != null) {
            final String frequencyNumberText = button.getText().toString();
            try {
                numberOfNotifiesPerPeriod = Integer.parseInt(frequencyNumberText);
            } catch (NumberFormatException e) {
                // nothing to do
            }
        }

        final int buttonId = frequencyPeriodGroup.getCheckedRadioButtonId();
        button = (RadioButton)getView().findViewById(buttonId);
        Reminder.Period period = Reminder.Period.DAILY;
        if (button != null) {
            final String frequencyPeriodText = button.getText().toString();
            period = Reminder.Period.get(frequencyPeriodText);
        }

        // Reminder unique ID; either create a new one, or reuse UID of existing reminder
        int myUniqueId;
        if (uniqueId == Reminder.NEW_REMINDER_UID) {
            myUniqueId = DataStore.getInstance(getActivity().getApplicationContext()).getNextNotificationId();
        } else {
            myUniqueId = reminder.getUniqueId();
        }

        return new Reminder(message, numberOfNotifiesPerPeriod, period, myUniqueId);
     }
}
