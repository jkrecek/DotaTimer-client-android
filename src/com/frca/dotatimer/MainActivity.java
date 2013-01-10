package com.frca.dotatimer;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.Preferences;
import com.frca.dotatimer.tasks.DataReceiveTask;
import com.frca.dotatimer.tasks.DataSendTask;

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

    public static MainActivity instance;

    public Preferences preferences;

    Calendar timeDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = new Preferences(this);

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
                createChangeNickDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onValuesChanged()
    {
        Button deleteButton = (Button)findViewById(R.id.button_delete);
        if (preferences.getTimer() != 0 && !preferences.isDeleted())
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
        }
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
        RelativeLayout content = (RelativeLayout)findViewById(R.id.layout_content);
        RelativeLayout loading = (RelativeLayout)findViewById(R.id.layout_loading);
        if (show)
        {
            content.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
        }
        else
        {
            content.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }
    }

    public void refreshValues()
    {
        targetText = preferences.getTargetTimeString();
        isDeleted = preferences.isDeleted();

        if (preferences.isDeleted())
        {
            mainText = preferences.getDeleteReason();
            targetAuthor = preferences.getSetBy();
            mainAuthor = preferences.getDeleteBy();
        }
        else
        {
            mainText = getTargetRemaining();
            targetAuthor = null;
            mainAuthor = preferences.getSetBy();
        }

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
            createChangeNickDialog();
        else
            Toast.makeText(MainActivity.this, "Vítejte " + preferences.getNick(), Toast.LENGTH_LONG).show();

        onValuesChanged();
    }

    private void createChangeNickDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Nastavení nicku");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialogBuilder
            .setMessage("Nastavení tvojí pøezdívky")
            .setCancelable(false)
            .setView(input)
            .setPositiveButton("Potvrdit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String new_nick = input.getText().toString().trim();
                    if (new_nick != "")
                    {
                        preferences.putAndCommit(Constants.TAG_NICK, new_nick);
                        Toast.makeText(MainActivity.this, "Nick zmìnìn na: " + preferences.getNick(), Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(MainActivity.this, "Neplatný nick", Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,int id) {
                    if (preferences.getNick() == null)
                        finish();
                    else
                        dialog.cancel();
                }
            });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
        if (preferences.getNick() == null)
            dialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
    }

    private String getTargetRemaining()
    {
        long target = (long)preferences.getTimer()*1000;
        if (target == 0)
            return "";

        Time timeTarget = new Time();
        timeTarget.set(target);

        Time timeNow = new Time();
        timeNow.setToNow();
        long comp = timeTarget.toMillis(true)-timeNow.toMillis(true);
        return timePassed((int)(comp/1000));
    }

    private static String timePassed(int time)
    {
        boolean positive = time >= 0;
        if (!positive)
            time *= -1;

        String value = Constants.toHumanReadable(time);
        String prepand = positive ? "Zbývá" : "Uplynulo";
        return prepand + " " + value;
    }

    private void createDeleteDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Zrušení timeru");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        alertDialogBuilder
            .setMessage("Zadej dùvod pro zrušení souèasného timeru")
            .setCancelable(false)
            .setView(input)
            .setPositiveButton("Potvrdit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String deleteReason = input.getText().toString().trim();
                    if (!Constants.isValid(deleteReason))
                    {
                        Toast.makeText(MainActivity.this, "Musíš uvést dùvod", Toast.LENGTH_LONG).show();
                        return;
                    }

                    ParameterMap params = new ParameterMap();
                    params.put(Constants.TAG_DELETE_REASON, deleteReason);
                    params.put(Constants.TAG_DELETE_BY, preferences.getNick());
                    new DataSendTask(MainActivity.this).execute(params);
                }
            })
            .setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                }
            });

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
        if (preferences.getNick() == null)
            dialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
    }

    public void callRefresh(View v)
    {
        requestData();
    }

    public void callChange(View v)
    {
        timeDatePicker = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            boolean handled = false;
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
                if (handled)
                    return;

                handled = true;

                timeDatePicker.set(Calendar.YEAR, year);
                timeDatePicker.set(Calendar.MONTH, monthOfYear);
                timeDatePicker.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    boolean handled = false;
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute)
                    {
                        if (handled)
                            return;

                        handled = true;

                        timeDatePicker.set(Calendar.HOUR_OF_DAY, hour);
                        timeDatePicker.set(Calendar.MINUTE, minute);
                        timeDatePicker.set(Calendar.SECOND, 0);

                        int time = (int) (timeDatePicker.getTimeInMillis()/1000);
                        ParameterMap params = new ParameterMap();
                        params.put(Constants.TAG_TIMER, Integer.toString(time));
                        params.put(Constants.TAG_SET_BY, preferences.getNick());
                        new DataSendTask(MainActivity.this).execute(params);
                    }

                }, timeDatePicker.get(Calendar.HOUR_OF_DAY), timeDatePicker.get(Calendar.MINUTE), true).show();
            }
        }, timeDatePicker.get(Calendar.YEAR), timeDatePicker.get(Calendar.MONTH), timeDatePicker.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void callDelete(View v)
    {
        createDeleteDialog();
    }

    public void requestData()
    {
        Intent intent = new Intent(this, DataReceiveReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);

        new DataReceiveTask(this).execute();

        int interval = Constants.SYNC_INVERVAL * 60 * 1000;
        //int interval = 20 * 1000;
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, sender);
    }
}
