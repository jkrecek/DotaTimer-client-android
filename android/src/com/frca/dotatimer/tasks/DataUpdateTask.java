package com.frca.dotatimer.tasks;

import org.apache.http.client.methods.HttpPut;

import android.app.ProgressDialog;
import android.content.Context;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.helper.ParameterMap;

public class DataUpdateTask extends SynchronizationTask
{
    private final ProgressDialog dialog;

    public DataUpdateTask(Context con, ParameterMap m)
    {
        super(con, m, HttpPut.class);
        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onStart()
    {
        dialog.setMessage("Odesílání..");
        dialog.show();
    }

    @Override
    protected void onEnd(String result)
    {
        dialog.dismiss();
    }

    @Override
    protected void onEndActivity(MainActivity instance, String res)
    {
        instance.onUpdateTaskEnd(res);
    }

    @Override
    protected void setPostMessage()
    {
        postBody = map.toJSONString();
    }
}