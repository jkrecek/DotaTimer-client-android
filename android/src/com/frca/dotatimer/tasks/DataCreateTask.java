package com.frca.dotatimer.tasks;

import org.apache.http.client.methods.HttpPost;

import android.app.ProgressDialog;
import android.content.Context;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.helper.ParameterMap;

public class DataCreateTask extends SynchronizationTask
{
    private final ProgressDialog dialog;

    public DataCreateTask(Context con, ParameterMap m)
    {
        super(con, m, HttpPost.class);
        dialog = new ProgressDialog(con);
    }

    @Override
    protected void onStart()
    {
        dialog.setMessage("Pøihlašování..");
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
        instance.onCreateTaskEnd(res);
    }

    @Override
    protected void setPostMessage()
    {
        postBody = map.toJSONString();
    }
}
