package de.stevensolleder.simpledo.controller;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import de.stevensolleder.simpledo.model.SimpleDo;

public class TimeTimePickerDialog extends TimePickerDialog
{
    private Runnable backPressedRunnable;

    public TimeTimePickerDialog(Context context, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, listener, hourOfDay, minute, is24HourView);
    }

    public TimeTimePickerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
        super(context, themeResId, listener, hourOfDay, minute, is24HourView);
    }

    public void setOnBackPressed(Runnable backPressedRunnable)
    {
        this.backPressedRunnable=backPressedRunnable;
    }

    @Override
    public void onBackPressed()
    {
        backPressedRunnable.run();
    }
}
