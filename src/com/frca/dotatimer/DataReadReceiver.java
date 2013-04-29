package com.frca.dotatimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.frca.dotatimer.tasks.RequestManager;

public class DataReadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        RequestManager.requestTeamData(context);
    }
}
