package com.frca.dotatimer.helper;

import android.app.Activity;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.SettingsActivity;

public class InstanceHolder {

    private static Activity instance = null;

    public static void setActivityOnTop(Activity activity) {
        instance = activity;
    }

    public static void setActivityOnBackground(Activity activity) {
        if (instance == activity)
            instance = null;
    }

    public static Activity getInstance() {
        return instance;
    }

    public static MainActivity getMainInstance() {
        if (instance instanceof MainActivity)
            return (MainActivity) instance;
        else
            return null;
    }

    public static SettingsActivity getSettingsInstance() {
        if (instance instanceof MainActivity)
            return (SettingsActivity) instance;
        else
            return null;
    }
}
