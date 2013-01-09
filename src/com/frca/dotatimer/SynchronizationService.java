package com.frca.dotatimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class SynchronizationService extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        new Synchronization(context).execute();
    }
}
