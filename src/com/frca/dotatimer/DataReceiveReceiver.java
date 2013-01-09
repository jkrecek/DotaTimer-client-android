package com.frca.dotatimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.frca.dotatimer.tasks.DataReceiveTask;


public class DataReceiveReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        new DataReceiveTask(context).execute();
    }
}
