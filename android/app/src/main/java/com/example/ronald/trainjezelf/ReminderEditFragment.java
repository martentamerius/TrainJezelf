package com.example.ronald.trainjezelf;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.ronald.trainjezelf.datastore.DataStore;
import com.example.ronald.trainjezelf.datastore.Reminder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReminderEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReminderEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ReminderEditFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int NROF_NUMBER_BUTTONS = 10;

    enum Period {
        HOUR,
        DAY,
        WEEK,
        MONTH
    }

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * The reminder
     */
    private Reminder reminder;

    /**
     * GUI controls
     */
    private EditText messageEditText;

    private Map<Integer, Button> numberButtons;
    private Map<Integer, Integer> numberIdToNumber;
    private Map<Integer, Integer> numberToNumberId;
    private int currentNumberButtonId = 0;
    private Map<Integer, Button> periodButtons;
    private Map<Integer, Integer> periodIdToIndex;
    private Map<Integer, Integer> indexToPeriodId;
    private int currentPeriodButtonId = 0;

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

        numberButtons = new HashMap<Integer, Button>(NROF_NUMBER_BUTTONS);
        numberIdToNumber = new HashMap<Integer, Integer>(NROF_NUMBER_BUTTONS);
        numberToNumberId = new HashMap<Integer, Integer>(NROF_NUMBER_BUTTONS);
        for (int i = 0; i < NROF_NUMBER_BUTTONS; i++) {
            int number = i + 1;
            String buttonId = "button" + number;
            int resourceId = getResources().getIdentifier(buttonId, "id", getActivity().getPackageName());
            Button button = (Button) getView().findViewById(resourceId);
            Log.d("ReminderEditFragment", buttonId + " lookup returned " + button);
            button.setOnClickListener(this);
            numberButtons.put(resourceId, button);
            numberIdToNumber.put(resourceId, number);
            numberToNumberId.put(number, resourceId);
        }

        periodButtons = new HashMap<Integer, Button>();
        periodIdToIndex = new HashMap<Integer, Integer>();
        indexToPeriodId = new HashMap<Integer, Integer>();
        int index = 0;
        for (Period period : Period.values()) {
            String buttonId = "button" + capitalizeFirstLetter(period.toString().toLowerCase());
            Log.d("ReminderEditFragment", buttonId);
            int resourceId = getResources().getIdentifier(buttonId, "id", getActivity().getPackageName());
            Button button = (Button) getView().findViewById(resourceId);
            button.setOnClickListener(this);
            periodButtons.put(resourceId, button);
            periodIdToIndex.put(resourceId, index);
            indexToPeriodId.put(index, resourceId);
            index++;
        }
    }

    public String capitalizeFirstLetter(String original){
        if(original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
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
        this.reminder = reminder;
        populateView();
    }

    /**
     * Loads reminder data into the UI.
     */
    private void populateView() {
        messageEditText.setText(reminder.getMessage());

        currentNumberButtonId = toggleButtonById(currentNumberButtonId,
                numberToNumberId.get(reminder.getNumberOfNotifiesPerPeriod()));
        currentPeriodButtonId = toggleButtonById(currentPeriodButtonId,
                indexToPeriodId.get(reminder.getPeriod().ordinal()));
    }

    public void saveReminder() {
        DataStore dataStore = DataStore.getInstance(getActivity());
        dataStore.remove(reminder);
        dataStore.add(getFromView());
    }

    private Reminder getFromView() {
         final String message = messageEditText.getText().toString();
         final int numberOfNotifiesPerPeriod = numberIdToNumber.get(currentNumberButtonId);
         final Reminder.Period period = Reminder.Period.values()[periodIdToIndex.get(currentPeriodButtonId)];
         return new Reminder(message, numberOfNotifiesPerPeriod, period);
     }

    public String getReminderText() {
        return messageEditText.getText().toString();
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (numberButtons.containsKey(viewId)) {
            currentNumberButtonId = toggleButtonById(currentNumberButtonId, viewId);
        } else if (periodButtons.containsKey(viewId)) {
            currentPeriodButtonId = toggleButtonById(currentPeriodButtonId, viewId);
        }
    }

    private int toggleButtonById(int currentButtonId, int newButtonId) {
        if (newButtonId == currentButtonId) {
            return currentButtonId;
        }
        // select new button
        Button button = (Button) getView().findViewById(newButtonId);
        button.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
        button.setTextColor(Color.WHITE);
        // deselect current button
        if (currentButtonId != 0) {
            button = (Button) getView().findViewById(currentButtonId);
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setTextColor(Color.BLACK);
        }
        return newButtonId;
    }
}
