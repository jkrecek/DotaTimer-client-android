package com.frca.dotatimer.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences
{
    private final SharedPreferences pref;
    private SharedPreferences.Editor edit;

    public Preferences(Context context)
    {
        pref = context.getSharedPreferences(Constants.PREF_OPTIONS, 0);
    }

    public String getString(String key)
    {
        return pref.getString(key, null);
    }

    public int getInt(String key)
    {
        return pref.getInt(key, 0);
    }

    /*
     * Returns
     * true if values changed
     * false if value remains same
     */
    public boolean putFromJSON(JSONObject json, String key)
    {
        if (edit == null)
            edit = pref.edit();

        String value;
        try {
            value = json.getString(key);
        } catch (JSONException e) {
            value = null;
        }

        try {
            int current = pref.getInt(key, 0);
            int received = Integer.parseInt(value);
            if (current == received)
                return false;

            edit.putInt(key, Integer.parseInt(value));
            return true;
        } catch(RuntimeException e) {
            String current = pref.getString(key, null);
            if (current.equals(value))
                return false;

            edit.putString(key, value);
            return true;
        }
    }

    public void commit()
    {
        if (edit == null)
            return;

        edit.commit();
        edit = null;
    }

    public void putAndCommit(String key, String value)
    {
        pref.edit().putString(key, value).commit();
    }

    public void putAndCommit(String key, int value)
    {
        pref.edit().putInt(key, value).commit();
    }

    /**********************/
    /*** CUSTOM METHODS ***/
    /**********************/

    public String getNick()
    {
        return getString(Constants.TAG_NICK);
    }

    public int getTimer()
    {
        return getInt(Constants.TAG_TIMER);
    }

    public String getTargetTimeString()
    {
        long miliseconds = (long)getTimer()*1000;
        if (miliseconds == 0)
            return "";

        Date targetDate = new Date(miliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(targetDate);
    }

    public String getSetBy()
    {
        return getString(Constants.TAG_SET_BY);
    }

    public String getDeleteReason()
    {
        return getString(Constants.TAG_DELETE_REASON);
    }

    public String getDeleteBy()
    {
        return getString(Constants.TAG_DELETE_BY);
    }

    public boolean isDeleted()
    {
        return Constants.isValid(getDeleteReason());
    }
}
