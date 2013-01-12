package com.frca.dotatimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.tasks.DataReceiveTask;


public class DataReceiveReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("DataReceiveReceiver", "onReceive");

        new DataReceiveTask(context, Constants.getFirstTimerData(context)).execute();
    }
}
