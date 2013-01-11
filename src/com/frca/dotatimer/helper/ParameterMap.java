package com.frca.dotatimer.helper;

import java.util.HashMap;

import android.content.Context;

public class ParameterMap extends HashMap<String, String>
{
    private static final long serialVersionUID = 1L;

    public ParameterMap(Context context)
    {
        super();
        put(Constants.TAG_PASS, Constants.HASH_PASS);
        put(Constants.TAG_NICK, Constants.getPreferences(context).getNick());
    }

}
