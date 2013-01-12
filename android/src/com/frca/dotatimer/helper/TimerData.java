package com.frca.dotatimer.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.Toast;

public class TimerData
{
    public static final String TAG_NICK = "nick";
    public static final String TAG_VALUE = "value";

    public static final String TAG_CHANNEL_NAME = "channel_name";
    public static final String TAG_CHANNEL_PASS = "channel_pass";
    public static final String TAG_CHANGED = "changed";

    public static final String TAG_TIMER = "timer";
    public static final String TAG_DELETE = "delete";

    public static final String TAG_USERS = "users";

    public static final String TAG_STATE = "state";
    public static final String TAG_REASON = "reason";

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

    public String channelName;
    public String channelPass;
    public Date changedAt;
    public DataPair timer;
    public DataPair delete;
    public UserList userList;

    private boolean filled;

    public class DataPair
    {
        public final Date date;
        public final String value;
        public final String nick;

        private DataPair(JSONObject json)
        {
            nick = JSONParser.getStringOrNull(json, TAG_NICK);
            value = JSONParser.getStringOrNull(json, TAG_VALUE);

            int intVal = JSONParser.getIntOrNil(json, TAG_VALUE);
            if (intVal == 0)
                date = null;
            else
            {
                long msVal = (long)intVal * 1000;
                date = new Date(msVal);
            }
        }
    }

    private class UserList extends ArrayList<UserData>
    {
        private static final long serialVersionUID = 1L;

        private UserList(JSONArray jsonArr)
        {
            JSONObject json;
            for (int i = 0; i < jsonArr.length(); ++i)
            {
                try {
                    json = jsonArr.getJSONObject(i);
                    add(new UserData(json));
                } catch (JSONException e) {
                    continue;
                }
            }
        }
    }

    private class UserData
    {
        public final String nick;
        public final int state;
        public final String reason;

        private UserData(JSONObject json)
        {
            nick = JSONParser.getStringOrNull(json, TAG_NICK);
            state = JSONParser.getIntOrNil(json, TAG_STATE);
            reason = JSONParser.getStringOrNull(json, TAG_REASON);
        }
    }

    private TimerData()
    {
        filled = false;
    }

    public static TimerData fromJSON(JSONObject jsonObj)
    {
        TimerData data = new TimerData();
        data.parseJSON(jsonObj);
        return data;
    }

    public static TimerData fromFile(Context context, String channel)
    {
        TimerData data = new TimerData();
        data.loadFromFile(context, channel+".json");
        return data;
    }

    public void reloadFromFile(Context context)
    {
        loadFromFile(context, channelName+".json");
    }

    public void loadFromFile(Context context, String fileName)
    {
        try
        {
            FileInputStream fis = context.openFileInput(fileName);
            StringBuffer fileContent = new StringBuffer("");

            byte[] buffer = new byte[1024];
            while (fis.read(buffer) != -1)
                fileContent.append(new String(buffer));

            String json = fileContent.toString();

            JSONObject jsonObj = new JSONObject(json);
            parseJSON(jsonObj);
        } catch (FileNotFoundException e) {
            Toast.makeText(context, "Nepodaøilo se najít soubor s daty", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, "Nepodaøilo se naèíst data", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            Toast.makeText(context, "Nepodaøilo se zpracovat data", Toast.LENGTH_LONG).show();
        }
    }

    public void parseJSON(JSONObject json)
    {
        if (filled && isUpdated(json))
            return;

        channelName = JSONParser.getStringOrNull(json, TAG_CHANNEL_NAME);
        channelPass = JSONParser.getStringOrNull(json, TAG_CHANNEL_PASS);

        int changeInt = JSONParser.getIntOrNil(json, TAG_CHANGED);
        if (changeInt == 0)
            changedAt = null;
        else
        {
            long changeMs = (long)changeInt * 1000;
            changedAt = new Date(changeMs);
        }

        NotificationDataHolder.initialize(changedAt);

        DataPair tempDataPair = new DataPair(JSONParser.getJSONOrNull(json, TAG_TIMER));
        boolean timerChanged = tempDataPair.date != null && !tempDataPair.date.equals(timer.date);
        timer = tempDataPair;
        if (timerChanged)
            NotificationDataHolder.set(NotificationDataHolder.TYPE_TIMER, getTimerString());

        tempDataPair = new DataPair(JSONParser.getJSONOrNull(json, TAG_DELETE));
        boolean deleteReasonChanged = Constants.isValid(tempDataPair.value) && !tempDataPair.value.equals(delete.value);
        delete = tempDataPair;
        if (deleteReasonChanged)
            NotificationDataHolder.set(NotificationDataHolder.TYPE_DELETE, delete.value);

        userList = new UserList(JSONParser.getJSONArrOrNull(json, TAG_USERS));

        filled = true;
    }

    private boolean isUpdated(JSONObject newJson)
    {
        int newChangeInt = JSONParser.getIntOrNil(newJson, TAG_CHANGED);
        if (newChangeInt == 0)
            return false;

        long currChangedMs = changedAt != null ? changedAt.getTime() : 0;
        return currChangedMs != (long)newChangeInt * 1000;
    }

    public String getTimerString()
    {
        if (timer.date == null)
            return "";

        return dateFormat.format(timer.date);
    }

    public String getRemainingString()
    {
        if (timer.date == null)
            return "";

        long diff = System.currentTimeMillis() - timer.date.getTime();
        return timePassed((int)(diff/1000));
    }

    private static String timePassed(int time)
    {
        boolean positive = time >= 0;
        if (!positive)
            time *= -1;

        String value = Constants.toHumanReadable(time);
        String prepand = positive ? "Zbývá" : "Uplynulo";
        return prepand + " " + value;
    }

    public boolean isDeleted()
    {
        return Constants.isValid(delete.value);
    }
}
