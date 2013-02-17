package com.frca.dotatimer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.DisplayLayoutValues;
import com.frca.dotatimer.helper.InstanceHolder;
import com.frca.dotatimer.helper.Preferences;
import com.frca.dotatimer.helper.TimerData;
import com.frca.dotatimer.implementations.Dialog;
import com.frca.dotatimer.implementations.TimerDatePickerDialog;
import com.frca.dotatimer.tasks.RequestManager;

public class MainActivity extends FragmentActivity {
    private Timer timer = new Timer();
    private final Handler handler = new Handler();

    public TimerData data;

    public Preferences preferences;

    private final DisplayLayoutValues layoutVals = new DisplayLayoutValues();

    Calendar timeDatePicker;

    private static final String TEAM_IDENTIFIER = "team_identifier";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Debug.startMethodTracing("maine.trace");
        // or .detectAll() for all detectable problems
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // startActivity(new Intent(this, LoginActivity.class));

        ActionBar bar = getActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // bar.addTab(bar.newTab().setText(R.string.add_team).setTabListener(new ActionBarListener()));
        // bar.addTab(bar.newTab().setText("Frèové").setTabListener(new ActionBarListener()));
        List<String> menuItems = new ArrayList<String>();
        menuItems.add(getString(R.string.add_team));
        // TODO foreach teams
        menuItems.add("Frèové");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(bar.getThemedContext(), android.R.layout.simple_list_item_1, android.R.id.text1, menuItems);
        bar.setListNavigationCallbacks(adapter, new ActionBarListener());

        loadOptions();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(Constants.STATE_SELECTED_NAVIGATION_ITEM))
            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(Constants.STATE_SELECTED_NAVIGATION_ITEM));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
    }

    @Override
    protected void onDestroy() {
        Debug.stopMethodTracing();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        InstanceHolder.setActivityOnTop(this);

        if (data != null)
            scheduleCountdownUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();

        InstanceHolder.setActivityOnBackground(this);

        disableCountdownUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menu_change_account:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_change_display_name:
                // TODO
                return true;
            case R.id.menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*public void onPreferencesChanged(Map<String, Boolean> changedKeys) {
        if ((changedKeys.containsKey(TimerData.TAG_CHANNEL_NAME) && changedKeys.get(TimerData.TAG_CHANNEL_NAME)) || (changedKeys.containsKey(TimerData.TAG_CHANNEL_PASS) && changedKeys.get(TimerData.TAG_CHANNEL_PASS))) {
            // if (Constants.isValid(preferences.getNick()))
            // requestData();
            // else
            Dialog.showNick(this);
        }

        if (changedKeys.containsKey(TimerData.TAG_NICK) && changedKeys.get(TimerData.TAG_NICK)) {
            if (Constants.isValid(preferences.getChannelName()) && Constants.isValid(preferences.getChannelPass()))
                requestData();
            else
                Dialog.showJoin(this);
        }
    }*/

    public void onValuesChanged() {
        if (data == null)
            return;

        Button deleteButton = (Button) findViewById(R.id.button_delete);
        if (data.timer.date != null && !data.isDeleted()) {
            scheduleCountdownUpdate();
            deleteButton.setEnabled(true);
        } else {
            disableCountdownUpdate();
            deleteButton.setEnabled(false);

            layoutVals.fromNewData(data);
            layoutVals.setUpLayout(findViewById(R.id.layout_content), this);
        }
    }

    public void scheduleCountdownUpdate() {
        if (timer != null)
            disableCountdownUpdate();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                layoutVals.shortUpdater();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        layoutVals.shortUpdateLayout();
                    }
                });
            }
        }, 0, 1000);
    }

    public void disableCountdownUpdate() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void toggleLayoutContent(boolean show) {
        LinearLayout loading = (LinearLayout) findViewById(R.id.layout_loading);
        if (show)
            loading.setVisibility(View.GONE);
        else
            loading.setVisibility(View.VISIBLE);
    }

    private void loadOptions() {
        preferences = Preferences.getPreferences(this);

        if (!Constants.isValid(preferences.getString(Preferences.Key.USER_ACCOUNT)))
            startActivity(new Intent(this, LoginActivity.class));
        else {
            Toast.makeText(this, "Vítejte " + preferences.getString(Preferences.Key.USER_DISPLAY_NAME), Toast.LENGTH_LONG).show();

            // if (!preferences.hasChannelSet())
            // Dialog.showJoin(this);
            // else
            // onValuesChanged();
        }
    }

    public void callRefresh(View v) {
        requestData();
    }

    public void callChange(View v) {
        timeDatePicker = Calendar.getInstance();
        new DatePickerDialog(this, new TimerDatePickerDialog(this, timeDatePicker), timeDatePicker.get(Calendar.YEAR), timeDatePicker.get(Calendar.MONTH), timeDatePicker.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void callDelete(View v) {
        Dialog.showDelete(this);
    }

    public void requestData() {
        /*if (!Preferences.getPreferences(this).hasChannelSet())
            return;*/

        Intent intent = new Intent(this, DataReadReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);

        RequestManager.requestTeamData(this);

        int interval = preferences.getInt(Preferences.Key.SYNC_FREQUENCY) * 1000;

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, interval, sender);
    }

    public class ActionBarListener implements ActionBar.OnNavigationListener {

        @Override
        public boolean onNavigationItemSelected(int arg0, long arg1) {
            FragmentManager manager = MainActivity.this.getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            TeamTab fragment = new TeamTab();
            Bundle bundle = new Bundle();
            // bundle.putInt(key, value);
            fragment.setArguments(bundle);
            trans.replace(R.id.container, fragment);
            trans.commit();
            return true;
        }
    }

    public static class TeamTab extends Fragment {

        private String team_identifier;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            team_identifier = getArguments().getString(TEAM_IDENTIFIER);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, null);
        }
    }
}
