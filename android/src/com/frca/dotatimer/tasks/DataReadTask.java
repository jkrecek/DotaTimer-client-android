package com.frca.dotatimer.tasks;

import org.apache.http.client.methods.HttpPost;

import android.content.Context;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.helper.NotificationDataHolder;
import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.Preferences;

public class DataReadTask extends SynchronizationTask
{
    public DataReadTask(Context con, ParameterMap m)
    {
        super(con, m, HttpPost.class);
    }

    @Override
    protected void onStartActivity(MainActivity instance)
    {
        instance.onReadTaskStart();
    }

    @Override
    protected void onEnd(String res)
    {
        NotificationDataHolder.show(context);
    }

    @Override
    protected void onEndActivity(MainActivity instance, String res)
    {
        instance.onReadTaskEnd(res);
    }

    @Override
    protected void setUpUrl() {
        String relativeUrl = "api/data/"+ Preferences.getPreferences(context).getChannelName() + ".json";
        String parameters = map.toGetString();
        setUpFullUrl(relativeUrl + "?" + parameters);
    }
}
