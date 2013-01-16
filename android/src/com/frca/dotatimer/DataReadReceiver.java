package com.frca.dotatimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.tasks.DataReadTask;


public class DataReadReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("DataReceiveReceiver", "onReceive");

        new DataReadTask(context, Constants.getFirstTimerData(context)).execute();
    }
}
