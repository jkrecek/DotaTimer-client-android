package com.frca.dotatimer.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Constants
{
    public static final String TAG_NICK = "nick";
    public static final String TAG_TIMER = "timer";
    public static final String TAG_SET_BY = "set_by";
    public static final String TAG_DELETE_REASON = "delete_reason";
    public static final String TAG_DELETE_BY = "delete_by";
    public static final String TAG_PASS = "pass";

    public static final String[] RECEIVED_TAGS = {
        Constants.TAG_TIMER,
        Constants.TAG_SET_BY,
        Constants.TAG_DELETE_REASON,
        Constants.TAG_DELETE_BY
    };

    public static final Map<String, String> RECEIVED_VALUES_PAIRS;

    static
    {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(Constants.TAG_TIMER, Constants.TAG_SET_BY);
        aMap.put(Constants.TAG_DELETE_REASON, Constants.TAG_DELETE_BY);
        RECEIVED_VALUES_PAIRS = Collections.unmodifiableMap(aMap);
    }

    public static final String HASH_PASS = "UcMsc3kYdXHi5KvhI6MRTfMxPOLfB8";

    public static final String PREF_OPTIONS = "options";

    private static final String SERVER_ROUTE = "http://dotatimer.himym.cz";
    private static final String SERVER_HANDLER = "handler.php";
    private static final String SERVER_JSON = "dotatimer.json";

    public static final int SYNC_INVERVAL = 5; // in minutes

    public static String getServerHandlerURL()
    {
        return SERVER_ROUTE + "/" + SERVER_HANDLER;
    }

    public static String getServerJsonURL()
    {
        return SERVER_ROUTE + "/" + SERVER_JSON;
    }

    public static String toHumanReadable(int time)
    {
        int days    = (int) Math.floor(time / 86400);
        int hours   = (int) Math.floor((time - (days * 86400))/ 3600);
        int minutes = (int) Math.floor((time - (days * 86400) - (hours * 3600)) / 60);
        int seconds = (int) Math.floor(time - (days * 86400) - (hours * 3600) - (minutes*60));


        String Days = Integer.toString(days);
        String Hours = Integer.toString(hours);
        String Minutes = Integer.toString(minutes);
        String Seconds = Integer.toString(seconds);

        if (days == 0)
            Days = "";
        else if (days == 1)
            Days += " den ";
        else if (days < 5)
            Days += " dny ";
        else
            Days += " dn� ";

        if (hours == 0)
            Hours = "";
        else if (hours == 1)
            Hours += " hodina ";
        else if (hours < 5)
            Hours += " hodiny ";
        else
            Hours += " hodin ";

        if (minutes == 0)
            Minutes = "";
        else if (minutes == 1)
            Minutes += " minuta ";
        else if (minutes < 5)
            Minutes += " minuty ";
        else
            Minutes += " minut ";

        if (seconds == 0)
            Seconds = "";
        else if (seconds == 1)
            Seconds += " vte�ina";
        else if (seconds < 5)
            Seconds += " vte�iny";
        else
            Seconds += " vte�in";

        return Days + Hours + Minutes + Seconds;
    }

    public static boolean isValid(String str)
    {
        return str != null && !str.equals("");
    }

    public static boolean isValueTag(String tag)
    {
        if (Constants.isValid(tag))
            for (String _tag : RECEIVED_VALUES_PAIRS.keySet())
                if (tag.equals(_tag))
                    return true;

        return false;
    }

    public static String getAuthorTag(String tag)
    {
        if (!Constants.isValid(tag))
            return null;

        for (Entry<String, String> entry : RECEIVED_VALUES_PAIRS.entrySet())
            if (tag.equals(entry.getKey()))
                return entry.getValue();

        return null;
    }
}