package com.frca.dotatimer;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.Dialog;
import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.Preferences;
import com.frca.dotatimer.helper.TimerData;
import com.frca.dotatimer.implementations.TimerDatePickerDialog;
import com.frca.dotatimer.tasks.DataReadTask;

public class MainActivity extends Activity
{
    private Timer timer = new Timer();
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            refreshLayout();
        }
    };

    private String targetText;
    private String targetAuthor;
    private String mainText;
    private String mainAuthor;
    private boolean isDeleted;

    public TimerData data;

    public static MainActivity instance;

    public Preferences preferences;

    Calendar timeDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = Constants.getPreferences(this);

        loadOptions();

        requestData();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        instance = this;

        scheduleCountdownUpdate();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        instance = null;

        disableCountdownUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_change_nick:
                Dialog.showNick(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onValuesChanged()
    {
        return;
        /*
        Button deleteButton = (Button)findViewById(R.id.button_delete);
        if (getData().timer.date != null && !getData().isDeleted())
        {
            scheduleCountdownUpdate();
            deleteButton.setEnabled(true);
        }
        else
        {
            disableCountdownUpdate();
            deleteButton.setEnabled(false);

            refreshValues();
            refreshLayout();
        }*/
    }

    public void scheduleCountdownUpdate()
    {
        if (timer != null)
            disableCountdownUpdate();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run()
            {
                refreshValues();
                handler.post(runnable);
            }
        }, 0, 1000);
    }

    public void disableCountdownUpdate()
    {
        timer.cancel();
        timer = null;
    }

    public void toggleLayoutContent(boolean show)
    {
        LinearLayout loading = (LinearLayout)findViewById(R.id.layout_loading);
        if (show)
            loading.setVisibility(View.GONE);
        else
            loading.setVisibility(View.VISIBLE);
    }

    public void refreshValues()
    {
        return;/*
        targetText = getData().getTimerString();
        isDeleted = getData().isDeleted();

        if (data.isDeleted())
        {
            targetAuthor = getData().timer.nick;
            mainText = getData().delete.value;
            mainAuthor = getData().delete.nick;
        }
        else
        {
            mainText = getData().getRemainingString();
            targetAuthor = null;
            mainAuthor = getData().timer.nick;
        }*/
    }

    public void refreshLayout()
    {
        TextView target = (TextView)findViewById(R.id.text_target);
        target.setText(targetText);
        if (isDeleted)
            target.setPaintFlags(target.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        TextView remaining = (TextView)findViewById(R.id.text_main);
        remaining.setText(mainText);

        TextView target_author = (TextView)findViewById(R.id.text_target_author);
        if (targetAuthor != null)
        {
            target_author.setVisibility(View.VISIBLE);
            target_author.setText("by "+targetAuthor);
        }
        else
            target_author.setVisibility(View.GONE);

        TextView main_author = (TextView)findViewById(R.id.text_main_author);
        if (mainAuthor != null)
        {
            main_author.setVisibility(View.VISIBLE);
            main_author.setText("by "+mainAuthor);
        }
        else
            main_author.setVisibility(View.GONE);
    }

    private void loadOptions()
    {
        if (preferences.getNick() == null)
            Dialog.showNick(this);
        else
            Toast.makeText(MainActivity.this, "Vítejte " + preferences.getNick(), Toast.LENGTH_LONG).show();

        Dialog.showJoin(this);

        //onValuesChanged();
    }

    public void callRefresh(View v)
    {
        requestData();
    }

    public void callChange(View v)
    {
        timeDatePicker = Calendar.getInstance();
        new DatePickerDialog(this, new TimerDatePickerDialog(this, timeDatePicker),
            timeDatePicker.get(Calendar.YEAR), timeDatePicker.get(Calendar.MONTH), timeDatePicker.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void callDelete(View v)
    {
        //Dialog.showDelete(this);
        Dialog.showJoin(this);
    }

    public void requestData()
    {
        Intent intent = new Intent(this, DataReadReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);

        new DataReadTask(this, new ParameterMap(this)).execute();

        int interval = Constants.SYNC_INVERVAL * 60 * 1000;
        //int interval = 20 * 1000;
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, sender);
    }

    public void onReadTaskStart()
    {
        toggleLayoutContent(false);
        ((Button)findViewById(R.id.button_refresh)).setEnabled(false);
    }

    public void onReadTaskEnd(String result)
    {
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        toggleLayoutContent(true);
        ((Button)findViewById(R.id.button_refresh)).setEnabled(true);
        onValuesChanged();
    }

    public void onCreateTaskEnd(String result)
    {
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    public void onUpdateTaskEnd(String result)
    {
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        requestData();
    }

    private TimerData getData()
    {
        if (data == null)
            data = Constants.getFirstTimerData(this);

        return data;
    }
}
