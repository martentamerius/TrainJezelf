package com.github.trainjezelf.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;

/**
 * Creates a table of radio buttons.
 */
public class RadioGroupTable extends TableLayout implements View.OnClickListener {

    private RadioButton activeRadioButton;
    private SparseArray<RadioButton> buttons = new SparseArray<>();

    /**
     * @param context the context
     */
    public RadioGroupTable(Context context) {
        super(context);
    }

    /**
     * @param context the context
     * @param attrs the attributes
     */
    public RadioGroupTable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onClick(View v) {
        final RadioButton rb = (RadioButton) v;
        if (activeRadioButton != null) {
            activeRadioButton.setChecked(false);
        }
        rb.setChecked(true);
        activeRadioButton = rb;
    }

    /* (non-Javadoc)
     * @see android.widget.TableLayout#addView(android.view.View, int, android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void addView(@NonNull View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        setChildrenOnClickListener((TableRow)child);
        addRadioButton(child);
    }

    /* (non-Javadoc)
     * @see android.widget.TableLayout#addView(android.view.View, android.view.ViewGroup.LayoutParams)
     */
    @Override
    public void addView(@NonNull View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        setChildrenOnClickListener((TableRow)child);
        addRadioButton(child);
    }

    private void addRadioButton(View child) {
        if (child instanceof TableRow) {
            TableRow tableRow = (TableRow)child;
            for (int index = 0; index < tableRow.getChildCount(); index++) {
                View rowChild = tableRow.getChildAt(index);
                if (rowChild instanceof RadioButton) {
                    RadioButton button = (RadioButton)rowChild;
                    this.buttons.put(button.getId(), button);
                }
            }
        }
    }

    private void setChildrenOnClickListener(TableRow tr) {
        final int c = tr.getChildCount();
        for (int i=0; i < c; i++) {
            final View v = tr.getChildAt(i);
            if ( v instanceof RadioButton ) {
                v.setOnClickListener(this);
            }
        }
    }

    public int getCheckedRadioButtonId() {
        if (activeRadioButton != null) {
            return activeRadioButton.getId();
        }
        return -1;
    }

    public void check(int buttonId) {
        this.onClick(this.buttons.get(buttonId));
    }
}
