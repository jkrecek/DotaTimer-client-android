package com.frca.dotatimer.implementations;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.TimerData;
import com.frca.dotatimer.tasks.DataUpdateTask;

public class TimerTimePickerDialog implements TimePickerDialog.OnTimeSetListener
{
    boolean handled = false;

    private final Context context;
    private final Calendar timerPicker;

    public TimerTimePickerDialog(Context con, Calendar cal)
    {
        context = con;
        timerPicker = cal;
    }

    @Override
    public void onTimeSet(TimePicker view, int hour, int minute)
    {
        if (handled)
            return;

        handled = true;

        timerPicker.set(Calendar.HOUR_OF_DAY, hour);
        timerPicker.set(Calendar.MINUTE, minute);
        timerPicker.set(Calendar.SECOND, 0);

        int time = (int) (timerPicker.getTimeInMillis()/1000);
        ParameterMap params = new ParameterMap(context);
        params.put(TimerData.TAG_TIMER, Integer.toString(time));
        new DataUpdateTask(context, params).execute();
    }
}
