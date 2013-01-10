package com.frca.dotatimer.tasks;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.R;
import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.JSONParser;
import com.frca.dotatimer.helper.Preferences;

public class DataReceiveTask extends AsyncTask<Void, Void, Void>
{
    private final Preferences preferences;
    private final Context context;
    private final List<String> changed = new ArrayList<String>();

    public DataReceiveTask(Context con)
    {
        preferences = new Preferences(con);
        context = con;
    }

    @Override
    protected void onPreExecute()
    {
        Log.d("DataReceive", "Start");

        if (MainActivity.instance != null)
        {
            MainActivity.instance.toggleLayoutContent(false);
            ((Button)MainActivity.instance.findViewById(R.id.button_refresh)).setEnabled(false);
        }
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {
        long startMs = System.currentTimeMillis();
        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(Constants.getServerJsonURL());

        Log.d("DataReceiveTime", "JSON received in ms " + String.valueOf(System.currentTimeMillis() - startMs));

        if (json == null)
            return null;

        for (String tag : Constants.RECEIVED_TAGS)
        {
            if (preferences.putFromJSON(json, tag))
            {
                String authorTag = Constants.getAuthorTag(tag);
                if (authorTag != null && !authorTag.equals(preferences.getNick()))
                    changed.add(tag);
            }
        }

        preferences.commit();

        Log.d("DataReceiveTime", "Handling json data in ms " + String.valueOf(System.currentTimeMillis() - startMs));

        return null;
    }

    @Override
    protected void onPostExecute(Void arg)
    {
        if (!changed.isEmpty())
            notifyChange();

        Log.d("DataReceive", "Complete");

        if (MainActivity.instance != null)
        {
            MainActivity.instance.toggleLayoutContent(true);
            ((Button)MainActivity.instance.findViewById(R.id.button_refresh)).setEnabled(true);
            MainActivity.instance.onValuesChanged();
        }
    }

    private void notifyChange()
    {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String title;
        String text;

        if (changed.contains(Constants.TAG_TIMER))
        {
            title = "Timer zmìnìn";
            text = "Timer byl nastaven na " + preferences.getTargetTimeString();
        }
        else if (changed.contains(Constants.TAG_DELETE_REASON))
        {
            title = "Timer vypnut";
            text = "Timer byl vypnut z dùvodu: '" + preferences.getDeleteReason() + "'";
        }
        else
            return;

        Notification noti = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .getNotification();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noti);
    }
}
