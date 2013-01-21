package com.frca.dotatimer.helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.frca.dotatimer.MainActivity;

public class Preferences
{
    private final SharedPreferences pref;
    private SharedPreferences.Editor edit;

    private final Map<String, Boolean> changedKeys = new HashMap<String, Boolean>();

    public static Preferences preferences;

    public static Preferences getPreferences(Context context)
    {
        if (preferences == null)
            preferences = new Preferences(context);

        return preferences;
    }

    public Preferences(Context context)
    {
        pref = context.getSharedPreferences(Constants.PREF_OPTIONS, 0);
    }

    /********************/
    /*** READ METHODS ***/
    /********************/
    public String getString(String key)
    {
        return pref.getString(key, null);
    }

    public int getInt(String key)
    {
        return pref.getInt(key, 0);
    }

    /*********************/
    /*** WRITE METHODS ***/
    /*********************/
    /*
     * Returns
     * true if values changed
     * false if value remains same
     */
    public boolean putFromJSON(JSONObject json, String key)
    {
        prepareEdit();

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

            put(key, Integer.parseInt(value));
            return true;
        } catch(RuntimeException e) {
            String current = pref.getString(key, "");
            if (current.equals(value))
                return false;

            put(key, value);
            return true;
        }
    }

    public void commit()
    {
        commit(null);
    }

    private void commit(String key)
    {
        if (edit == null)
            return;

        edit.commit();
        edit = null;

        if (MainActivity.instance != null)
            MainActivity.instance.onPreferencesChanged(changedKeys);

        changedKeys.clear();
    }

    public void put(String key, String value)
    {
        prepareEdit();
        edit.putString(key, value);

        changedKeys.put(key, value != null);
    }

    public void put(String key, int value)
    {
        prepareEdit();
        edit.putInt(key, value);

        changedKeys.put(key, value != 0);
    }

    public void putAndCommit(String key, String value)
    {
        put(key, value);
        commit(key);
    }

    public void putAndCommit(String key, int value)
    {
        put(key, value);
        commit(key);
    }

    public void putAndCommitMultiple(Map<String, String> map)
    {
        Iterator<Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            try {
                int value = Integer.parseInt(entry.getValue());
                put(entry.getKey(), value);
            } catch(NumberFormatException e) {
                String value = entry.getValue();
                put(entry.getKey(), value);
            }
        }

        commit();
    }

    private void prepareEdit()
    {
        if (edit == null)
        {
            edit = pref.edit();
            changedKeys.clear();
        }

    }
    /**********************/
    /*** CUSTOM METHODS ***/
    /**********************/

    public String getNick()
    {
        return getString(TimerData.TAG_NICK);
    }

    public String getChannelName()
    {
        return getString(TimerData.TAG_CHANNEL_NAME);
    }

    public String getChannelPass()
    {
        return getString(TimerData.TAG_CHANNEL_PASS);
    }

    public boolean hasChannelSet()
    {
        return Constants.isValid(getChannelName()) && Constants.isValid(getChannelPass());
    }

}
