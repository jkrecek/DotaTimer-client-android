package com.frca.dotatimer.helper;

import java.util.HashMap;

import android.content.Context;

import com.google.gson.Gson;

public class ParameterMap extends HashMap<String, String>
{
    private static final long serialVersionUID = 1L;

    public ParameterMap(Context context)
    {
        super();
        put(TimerData.TAG_NICK, Preferences.getPreferences(context).getNick());
        /*put(TimerData.TAG_CHANNEL_NAME, Constants.getPreferences(context).getChannelName());*/
        put(TimerData.TAG_CHANNEL_PASS, Preferences.getPreferences(context).getChannelPass());
    }

    public String toJSONString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toGetString()
    {
        String str = "";
        for (Entry<String, String> pair : entrySet())
            str += pair.getKey() + "=" + pair.getValue() + "&";

        return str.substring(0, str.length() - 1);
    }
}
