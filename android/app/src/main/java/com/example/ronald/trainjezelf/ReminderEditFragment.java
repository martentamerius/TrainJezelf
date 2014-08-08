package com.example.ronald.trainjezelf;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReminderEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReminderEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ReminderEditFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * The reminder
     */
    private Reminder reminder;

    private EditText messageEditText;
    private EditText frequencyEditText;
    private Spinner spinner;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReminderEditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReminderEditFragment newInstance(String param1, String param2) {
        ReminderEditFragment fragment = new ReminderEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ReminderEditFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reminder_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get references to GUI elements
        messageEditText = (EditText) getView().findViewById(R.id.messageEditText);
        frequencyEditText = (EditText) getView().findViewById(R.id.frequencyEditText);
        spinner = (Spinner) getView().findViewById(R.id.spinner);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        // Populate spinner
        populateSpinner();
        this.reminder = reminder;
        populateView();
    }

    /**
     * Loads reminder data into the UI.
     */
    private void populateView() {
        messageEditText.setText(reminder.getMessage());
        frequencyEditText.setText(Integer.toString(reminder.getNumberOfNotifiesPerPeriod()));
        spinner.setSelection(reminder.getPeriod().ordinal());
    }

    private void populateSpinner() {
        List<String> list = new ArrayList<String>();
        for (Reminder.Period period: Reminder.Period.values()) {
            list.add(period.toString());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void saveReminder() {
        DataStore dataStore = DataStore.getInstance(getActivity());
        dataStore.remove(reminder);
        dataStore.add(getFromView());
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


    public String getReminderText() {
        return messageEditText.getText().toString();
    }
}
