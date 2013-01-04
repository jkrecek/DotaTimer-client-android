package com.frca.dotatimer.helper;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;

public class Preferences
{
    private SharedPreferences pref;
    private SharedPreferences.Editor edit;
    
    public Preferences(Activity activity)
    {
        pref = activity.getSharedPreferences(Constants.PREF_OPTIONS, 0);
    }
    
    public String getString(String key)
    {
        return pref.getString(key, null);
    }
    
    public int getInt(String key)
    {
        return pref.getInt(key, 0);
    }
    
    public void putFromJSON(JSONObject json, String key)
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
            edit.putInt(key, Integer.parseInt(value));
        } catch(NumberFormatException e) {
            edit.putString(key, value);
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
