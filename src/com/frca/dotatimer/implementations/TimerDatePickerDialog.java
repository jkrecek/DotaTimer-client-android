package com.frca.dotatimer.implementations;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

public class TimerDatePickerDialog implements DatePickerDialog.OnDateSetListener {
    boolean handled = false;

    private final Context context;
    private final Calendar timerPicker;

    public TimerDatePickerDialog(Context con, Calendar cal) {
        context = con;
        timerPicker = cal;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (handled)
            return;

        handled = true;

        timerPicker.set(Calendar.YEAR, year);
        timerPicker.set(Calendar.MONTH, monthOfYear);
        timerPicker.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        new TimePickerDialog(context, new TimerTimePickerDialog(context, timerPicker), timerPicker.get(Calendar.HOUR_OF_DAY),
            timerPicker.get(Calendar.MINUTE), true).show();
    }

}
