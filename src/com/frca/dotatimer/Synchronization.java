package com.frca.dotatimer;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.JSONParser;
import com.frca.dotatimer.helper.Preferences;

public class Synchronization extends AsyncTask<Void, Void, Void>
{
    private final Preferences preferences;
    private final Context context;
    private List<String> changed;

    public Synchronization(Context con)
    {
        preferences = new Preferences(con);
        context = con;
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {
        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(Constants.getServerJsonURL());
        if (json == null)
            return null;

        changed = new ArrayList<String>();
        for (String tag : Constants.RECEIVED_TAGS)
            if (preferences.putFromJSON(json, tag))
                changed.add(tag);

        preferences.commit();

        return null;
    }

    @Override
    protected void onPostExecute(Void arg)
    {
        if (!changed.isEmpty())
            notifyChange();

        if (MainActivity.instance != null)
            MainActivity.instance.syncComplete();
    }

    public void notifyChange()
    {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        String title;
        String text;

        if (changed.contains(Constants.TAG_DELETE_REASON))
        {
            title = "Timer vypnut";
            text = "Timer byl vypnut z dùvodu: '" + preferences.getDeleteReason() + "'";
        }
        else if (changed.contains(Constants.TAG_TIMER))
        {
            title = "Timer zmìnìn";
            text = "Timer byl nastaven na " + preferences.getTargetTimeString();
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

        //noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }
}
