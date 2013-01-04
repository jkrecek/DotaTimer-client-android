package com.frca.dotatimer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.HttpRequestHandler;
import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.Preferences;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
    private Timer timer = new Timer();
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable()
    {
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
    
    public Preferences preferences;

    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        preferences = new Preferences(this);
        
        loadOptions();
        
        refreshStart();
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
    
    private void onValuesChanged()
    {
        Button deleteButton = (Button)findViewById(R.id.button_delete);
        if (preferences.getTimer() != 0 && !preferences.isDeleted())
        {
            planRefresh();
            deleteButton.setEnabled(true);
        }
        else
        {
            timer.cancel();
            deleteButton.setEnabled(false);
            refreshValues();
            refreshLayout();
        }
    }

    public void planRefresh()
    {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run()
            {
                refreshValues();
                handler.post(runnable);
            }
        }, 0, 1000);
        
    }
    
    public void refreshValues()
    {
        targetText = getTargetTimeString();
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
    
    private String getTargetTimeString()
    {
        long target = (long)preferences.getTimer()*1000;
        if (target == 0)
            return "";
        
        Date targetDate = new Date(target);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(targetDate);
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
                public void onClick(DialogInterface dialog, int id) {
                    String deleteReason = input.getText().toString().trim();
                    ParameterMap params = new ParameterMap();
                    params.put(Constants.TAG_DELETE_REASON, deleteReason);
                    params.put(Constants.TAG_DELETE_BY, preferences.getNick());
                    new HttpRequestHandler(MainActivity.this).execute(params);                    
                }
            })
            .setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
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
        refreshStart();
    }
    
    public void callChange(View v)
    {
        
    }
    
    public void callDelete(View v)
    {
        createDeleteDialog();
    }
    
    public void refreshStart()
    {
        ((Button)findViewById(R.id.button_refresh)).setEnabled(false);
        new Synchronization(this).execute();
    }
    
    public void refreshComplete()
    {
        ((Button)findViewById(R.id.button_refresh)).setEnabled(true);
        onValuesChanged();
    }

}
