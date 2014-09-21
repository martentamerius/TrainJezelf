package com.example.ronald.trainjezelf;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;
import com.example.ronald.trainjezelf.views.RadioGroupTable;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReminderEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ReminderEditFragment extends Fragment {

    /**
     * The reminder to edit
     */
    private Reminder reminder;

    /**
     * GUI controls
     */
    private EditText messageEditText;
    private RadioGroupTable frequencyNumberGroup;
    private RadioGroup frequencyPeriodGroup;

    /**
     * Fragment callback
     */
    private OnFragmentInteractionListener mListener;

    public ReminderEditFragment() {
        // Required empty constructor
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * Displays a particular reminder.
     * @param reminder the reminder to display
     */
    public void displayReminder(Reminder reminder) {
        this.reminder = reminder;
        populateView();
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
     * Saves reminder as it is currently configured in the GUI.
     */
    public Reminder saveReminder() {
        DataStore dataStore = DataStore.getInstance(getActivity());
        reminder = dataStore.put(getFromView());
        return reminder;
    }

    private Reminder getFromView() {
        final String message = messageEditText.getText().toString();

        // TODO bit hacky
        final int selectedFrequencyNumberId = frequencyNumberGroup.getCheckedRadioButtonId();
        RadioButton button = (RadioButton)getView().findViewById(selectedFrequencyNumberId);
        final String frequencyNumberText = button.getText().toString();
        int numberOfNotifiesPerPeriod = 1;
        try {
            numberOfNotifiesPerPeriod = Integer.parseInt(frequencyNumberText);
        } catch (NumberFormatException e) {
            // nothing to do
        }

        final int buttonId = frequencyPeriodGroup.getCheckedRadioButtonId();
        button = (RadioButton)getView().findViewById(buttonId);
        final String frequencyPeriodText = button.getText().toString();
        final Reminder.Period period = Reminder.Period.get(frequencyPeriodText);

        // reuse unique Id
        return new Reminder(message, numberOfNotifiesPerPeriod, period, reminder.getUniqueId());
     }
}
