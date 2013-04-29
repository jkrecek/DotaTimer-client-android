package com.frca.dotatimer.helper;

import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.R;

public class NotificationDataHolder
{
    public static final int TYPE_TIMER = 1;
    public static final int TYPE_DELETE = 2;

    private static Date time;
    public static String value;
    public static int type;

    public static boolean isSet;

    public static void initialize(Date when)
    {
        time = when;
    }

    public static void set(int notiType, String val)
    {
        isSet = true;
        type = notiType;
        value = val;
    }

    public static void show(Context context)
    {
        if (!isSet)
            return;

        String title;
        String text;

        switch(type)
        {
            case TYPE_TIMER:
                title = "Upraven timer";
                text = "Timer byl nastaven na " + value;
                break;
            case TYPE_DELETE:
                title = "Vypnut timer";
                text = "Timer byl ukonèen z dùvodu: '" + value + "'";
                break;
            default:
                Log.d("NotificationDataHolder", "Unable to notify, inconsistent data");
                return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification noti = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setWhen(time.getTime())
                .getNotification();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noti);
    }

    public static void unset()
    {
        isSet = false;
        time = null;
        value = "";
        type = 0;
    }

}
