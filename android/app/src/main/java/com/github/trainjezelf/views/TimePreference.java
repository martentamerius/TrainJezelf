package com.github.trainjezelf.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

import com.github.trainjezelf.R;
import com.github.trainjezelf.alarm.AlarmScheduler;
import com.github.trainjezelf.datastore.TimeRange;

/**
 * Time preference, allows the user to select a time range.
 */
public class TimePreference extends DialogPreference implements View.OnClickListener {

    private TimePicker picker_from = null;
    private TimePicker picker_until = null;
    private ViewSwitcher viewSwitcher = null;
    private View view;

    // Result
    private TimeRange range;
    private TimeRange previousRange;

    public TimePreference(Context ctxt) {
        this(ctxt, null);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);
        setPositiveButtonText(R.string.action_save);
        setNegativeButtonText(R.string.action_cancel);
    }

    private String encodePreference() {
        return range.encode();
    }

    private void decodePreference(String encoded) {
        range = new TimeRange(encoded);
    }

    @Override
    protected View onCreateDialogView() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        view = layoutInflater.inflate(R.layout.dialog_timespan, null);

        final View timePickerFromView = layoutInflater.inflate(R.layout.dialog_timespan_from, null);
        final View timePickerUntilView = layoutInflater.inflate(R.layout.dialog_timespan_until, null);
        viewSwitcher = (ViewSwitcher)view.findViewById(R.id.timespan_viewswitcher);
        viewSwitcher.addView(timePickerFromView, 0);
        viewSwitcher.addView(timePickerUntilView, 1);

        picker_from = (TimePicker)view.findViewById(R.id.time_from);
        picker_from.setIs24HourView(true);
        picker_until = (TimePicker)view.findViewById(R.id.time_until);
        picker_until.setIs24HourView(true);

        // Handle button clicks
        Button buttonFrom = (Button) view.findViewById(R.id.button_from);
        buttonFrom.setOnClickListener(this);
        buttonFrom.setEnabled(false);
        view.findViewById(R.id.button_until).setOnClickListener(this);

        return view;
    }

    @Override
    protected void onBindDialogView(@NonNull View v) {
        super.onBindDialogView(v);
        picker_from.setCurrentHour(range.getFromHour());
        picker_from.setCurrentMinute(range.getFromMinute());
        picker_until.setCurrentHour(range.getUntilHour());
        picker_until.setCurrentMinute(range.getUntilMinute());
        // Remember current range, we might have to revert after editing
        previousRange = new TimeRange(range);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            range.update(picker_from.getCurrentHour(), picker_from.getCurrentMinute(), picker_until.getCurrentHour(),
                    picker_until.getCurrentMinute());
            if (range.getUntilHour() * 60 + range.getUntilMinute() -
                    (range.getFromHour() * 60 + range.getFromMinute()) < AlarmScheduler.MINIMUM_ACTIVE_MINUTES) {
                range = new TimeRange(previousRange);
            }
            setSummary(getSummary());
            final String encoded = encodePreference();
            if (callChangeListener(encoded)) {
                persistString(encoded);
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            decodePreference(getPersistedString(AlarmScheduler.DEFAULT_ACTIVE_TIME_RANGE));
        } else {
            decodePreference((String)defaultValue);
        }
        setSummary(getSummary());
    }

    @Override
    public CharSequence getSummary() {
        return String.format("Van %02d:%02d tot %02d:%02d", range.getFromHour(), range.getFromMinute(),
                range.getUntilHour(), range.getUntilMinute());
    }

    // Handle button clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_from:
                v.setEnabled(false);
                view.findViewById(R.id.button_until).setEnabled(true);
                viewSwitcher.showPrevious();
                break;

            case R.id.button_until:
                v.setEnabled(false);
                view.findViewById(R.id.button_from).setEnabled(true);
                viewSwitcher.showNext();
                break;
        }
    }
}
