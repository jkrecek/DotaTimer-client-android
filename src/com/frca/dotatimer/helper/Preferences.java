package com.frca.dotatimer.helper;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    public enum Key {
        /*DISPLAY_NAME(0, "display_name"),
        PASSWORD(1, "password"),
        TEAM_NAME(11, "team_name"),
        TEAM_PASSWORD(12, "password"),*/
        SERVER_ADDRESS(0, "server_address"),
        SYNC_FREQUENCY(1, "sync_frequency"),
        USER_ACCOUNT(100, "user_account"),
        USER_AUTH_TOKEN(101, "user_auth_token"),
        USER_DISPLAY_NAME(102, "user_display_name");

        private final int keyId;
        private final String keyName;

        public final int getId() {
            return keyId;
        }

        public final String getName() {
            return keyName;
        }

        private Key(final int keyId, final String keyName) {
            this.keyId = keyId;
            this.keyName = keyName;
        }

        public static Key fromInt(int i) {
            switch (i) {
            case 0:
                return SERVER_ADDRESS;
            case 1:
                return SYNC_FREQUENCY;
                /*case 10:
                    return SERVER_ADDRESS;
                case 11:
                    return TEAM_NAME;
                case 12:
                    return TEAM_PASSWORD;
                case 20:
                    return SYNC_FREQUENCY;*/
            case 100:
                return USER_ACCOUNT;
            case 101:
                return USER_AUTH_TOKEN;
            case 102:
                return USER_DISPLAY_NAME;
            default:
                return null;
            }
        }
    }

    private final SharedPreferences pref;
    private SharedPreferences.Editor edit;

    public static Preferences preferences;

    public static Preferences getPreferences(Context context) {
        if (preferences == null)
            preferences = new Preferences(context);

        return preferences;
    }

    public Preferences(Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /********************/
    /*** READ METHODS ***/
    /********************/
    public String getString(String key) {
        return pref.getString(key, null);
    }

    public String getString(Key pk) {
        return getString(pk.getName());
    }

    public String getString(int resId) {
        return getString(Key.fromInt(resId));
    }

    public int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public int getInt(Key pk) {
        return getInt(pk.getName());
    }

    public int getInt(int resId) {
        return getInt(Key.fromInt(resId));
    }

    /*********************/
    /*** WRITE METHODS ***/
    /*********************/
    /*
     * Returns true if values changed false if value remains same
     */
    public boolean putFromJSON(JSONObject json, String key) {
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
        } catch (RuntimeException e) {
            String current = pref.getString(key, "");
            if (current.equals(value))
                return false;

            put(key, value);
            return true;
        }
    }

    public void commit() {
        if (edit == null)
            return;

        edit.commit();
        edit = null;
    }

    public void put(Object key, Object value) {
        String strKey = null;
        if (key instanceof String)
            strKey = (String) key;
        else if (key instanceof Integer)
            strKey = Key.fromInt(((Integer) key).intValue()).getName();
        else if (key instanceof Key)
            strKey = ((Key) key).getName();

        if (strKey == null)
            return;

        prepareEdit();

        if (value instanceof String)
            edit.putString(strKey, (String) value);
        else if (value instanceof Integer)
            edit.putInt(strKey, (Integer) value);
        else if (value instanceof Boolean)
            edit.putBoolean(strKey, (Boolean) value);
    }

    public void putAndCommit(Object key, Object value) {
        put(key, value);
        commit();
    }

    public void putAndCommitMultiple(Map<String, String> map) {
        Iterator<Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            try {
                int value = Integer.parseInt(entry.getValue());
                put(entry.getKey(), value);
            } catch (NumberFormatException e) {
                String value = entry.getValue();
                put(entry.getKey(), value);
            }
        }

        commit();
    }

    private void prepareEdit() {
        if (edit == null) {
            edit = pref.edit();
        }
    }
}
