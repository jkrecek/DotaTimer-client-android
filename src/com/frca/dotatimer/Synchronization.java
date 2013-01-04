package com.frca.dotatimer;

import org.json.JSONObject;

import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.JSONParser;
import com.frca.dotatimer.helper.Preferences;

import android.os.AsyncTask;

public class Synchronization extends AsyncTask<Void, Void, Void>
{
    private Preferences preferences;
    private MainActivity activity;
    
    public Synchronization(MainActivity act)
    {
        preferences = act.preferences;
        activity = act;
    }

    @Override
    protected Void doInBackground(Void... arg0)    
    {
        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(Constants.getServerJsonURL());
        if (json == null)
            return null;

        preferences.putFromJSON(json, Constants.TAG_TIMER);
        preferences.putFromJSON(json, Constants.TAG_SET_BY);
        preferences.putFromJSON(json, Constants.TAG_DELETE_REASON);
        preferences.putFromJSON(json, Constants.TAG_DELETE_BY);
        preferences.commit();
               
        return null;
    }

    protected void onPostExecute(Void arg)
    {
        activity.refreshComplete();
    }
}
