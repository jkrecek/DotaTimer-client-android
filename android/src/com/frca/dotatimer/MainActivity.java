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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.Preferences;
import com.frca.dotatimer.helper.TimerData;
import com.frca.dotatimer.implementations.TimerDatePickerDialog;
import com.frca.dotatimer.tasks.DataReadTask;
import com.frca.dotatimer.tasks.DataUpdateTask;

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

    private TimerData data;
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
                createChangeNickDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onValuesChanged()
    {
        Button deleteButton = (Button)findViewById(R.id.button_delete);
        if (data.timer.date != null && !data.isDeleted())
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
        LinearLayout loading = (LinearLayout)findViewById(R.id.layout_loading);
        if (show)
            loading.setVisibility(View.GONE);
        else
            loading.setVisibility(View.VISIBLE);
    }

    public void refreshValues()
    {
        targetText = data.getTimerString();
        isDeleted = data.isDeleted();

        if (data.isDeleted())
        {
            targetAuthor = data.timer.nick;
            mainText = data.delete.value;
            mainAuthor = data.delete.nick;
        }
        else
        {
            mainText = data.getRemainingString();
            targetAuthor = null;
            mainAuthor = data.timer.nick;
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
                        preferences.putAndCommit(TimerData.TAG_NICK, new_nick);
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

                    ParameterMap params = new ParameterMap(MainActivity.this);
                    params.put(TimerData.TAG_DELETE, deleteReason);
                    new DataUpdateTask(MainActivity.this).execute(params);
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
        new DatePickerDialog(this, new TimerDatePickerDialog(this, timeDatePicker),
            timeDatePicker.get(Calendar.YEAR), timeDatePicker.get(Calendar.MONTH), timeDatePicker.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void callDelete(View v)
    {
        createDeleteDialog();
    }

    public void requestData()
    {
        Intent intent = new Intent(this, DataReadReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);

        new DataReadTask(this, data).execute();

        int interval = Constants.SYNC_INVERVAL * 60 * 1000;
        //int interval = 20 * 1000;
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, sender);
    }
}
