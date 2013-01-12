package com.frca.dotatimer.helper;

import java.util.HashMap;

import android.content.Context;

public class ParameterMap extends HashMap<String, String>
{
    private static final long serialVersionUID = 1L;

    public ParameterMap(Context context)
    {
        super();
        put(TimerData.TAG_NICK, Constants.getPreferences(context).getNick());
        put(TimerData.TAG_CHANNEL_NAME, Constants.getPreferences(context).getChannelName());
        put(TimerData.TAG_CHANNEL_PASS, Constants.getPreferences(context).getChannelPass());
    }

}
