package com.frca.dotatimer.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.frca.dotatimer.LoginActivity;
import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.R;
import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.InstanceHolder;
import com.frca.dotatimer.helper.NotificationDataHolder;
import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.Preferences;
import com.frca.dotatimer.helper.TimerData;
import com.frca.dotatimer.tasks.RequestTask.RequestType;
import com.frca.dotatimer.tasks.RequestTask.TaskEndListener;
import com.frca.dotatimer.tasks.RequestTask.TaskStartListener;

public class RequestManager {

    public static void requestTeamData(Context context) {
        // TODO params
        ParameterMap params = new ParameterMap(context);
        // params.add(TimerData.TAG_ACCOUNT, );
        RequestTask request = new RequestTask(context, params, RequestType.GET);

        // TODO addTeamId
        request.setUrl("data/1.json");

        request.setOnStartListener(new TaskStartListener() {
            @Override
            public void onStart(RequestTask request) {
                MainActivity activity = InstanceHolder.getMainInstance();
                if (activity != null) {
                    activity.toggleLayoutContent(false);
                    ((Button) activity.findViewById(R.id.button_refresh)).setEnabled(false);
                }
            }
        });

        request.setOnEndListener(new TaskEndListener() {
            @Override
            public void onEnd(RequestTask request) {
                NotificationDataHolder.show(request.getContext());
                MainActivity activity = InstanceHolder.getMainInstance();
                if (activity != null) {
                    Toast.makeText(request.getContext(), request.getError(), Toast.LENGTH_LONG).show();
                    activity.toggleLayoutContent(true);
                    ((Button) activity.findViewById(R.id.button_refresh)).setEnabled(true);
                    activity.onValuesChanged();
                }
            }
        });

        request.execute();
    }

    public static void requestTeamAuthenticate(Context context, final String teamName, final String teamPassword) {
        ParameterMap map = new ParameterMap(context);

        // override current pref values
        map.put(TimerData.TAG_CHANNEL_NAME, teamName);
        map.put(TimerData.TAG_CHANNEL_PASS, teamPassword);

        RequestTask request = new RequestTask(context, map, RequestType.POST);
        final ProgressDialog dialog = new ProgressDialog(context);

        request.setUrl("data.json");

        request.setOnStartListener(new TaskStartListener() {
            @Override
            public void onStart(RequestTask request) {
                dialog.setMessage("Pøihlašování..");
                dialog.show();
            }
        });

        request.setOnEndListener(new TaskEndListener() {
            @Override
            public void onEnd(RequestTask request) {
                dialog.dismiss();

                MainActivity activity = InstanceHolder.getMainInstance();
                if (activity != null) {
                    if (request.getError() != null) {
                        Toast.makeText(request.getContext(), "Error: " + request.getError(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Preferences preferences = Preferences.getPreferences(request.getContext());
                    if (TextUtils.isEmpty(teamName) && TextUtils.isEmpty(teamPassword)) {
                        preferences.put(TimerData.TAG_CHANNEL_NAME, teamName);
                        preferences.put(TimerData.TAG_CHANNEL_PASS, teamPassword);
                        preferences.commit();
                    } else {
                        Toast.makeText(request.getContext(), "Chyba, data nemohla být uložena", Toast.LENGTH_LONG).show();
                        Log.d("ActivityCreateTask", "Either channelName or channelPass is not supplied (name: '" + Constants.value(teamName) + "', pass: '" + Constants.value(teamPassword) + "')");
                    }
                }
            }
        });

        request.execute();
    }

    public static void requestTeamUpdate(Context context, ParameterMap map) {
        RequestTask request = new RequestTask(context, map, RequestType.PUT);
        final ProgressDialog dialog = new ProgressDialog(context);

        request.setUrl("data.json");

        request.setOnStartListener(new TaskStartListener() {
            @Override
            public void onStart(RequestTask request) {
                dialog.setMessage("Odesílání..");
                dialog.show();
            }
        });

        request.setOnEndListener(new TaskEndListener() {
            @Override
            public void onEnd(RequestTask request) {
                dialog.dismiss();

                MainActivity activity = InstanceHolder.getMainInstance();
                if (activity != null) {
                    Toast.makeText(request.getContext(), request.getError(), Toast.LENGTH_LONG).show();
                    activity.requestData();
                }
            }
        });

        request.execute();
    }

    public static void requestUserAuthenticate(final LoginActivity activity, final String account, final String displayName) {
        ParameterMap map = new ParameterMap(activity);

        map.put(TimerData.TAG_ACCOUNT, account);
        map.put(TimerData.TAG_DISPLAY_NAME, displayName);

        RequestTask request = new RequestTask(activity, map, RequestType.POST);

        request.setUrl("user.json");

        request.setOnEndListener(new TaskEndListener() {
            @Override
            public void onEnd(RequestTask request) {
                // TODO read data

                // TODO save something
                boolean success = true;
                if (success)
                    activity.finish();
                else {
                    // activity.showProgress(false);
                }
            }
        });

        request.execute();
    }
}
