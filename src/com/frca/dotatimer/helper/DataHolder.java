package com.frca.dotatimer.helper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class DataHolder {

    public static Map<String, TimerData> datas = new HashMap<String, TimerData>();

    public static TimerData getTimerData(Context context, String channelName) {
        if (datas.containsKey(channelName))
            return datas.get(channelName);

        TimerData newData = TimerData.fromFile(context, channelName);
        datas.put(channelName, newData);
        return newData;
    }

    public static TimerData saveJSON(Context context, JSONObject json) {
        saveJSONtoFile(context, json);

        TimerData timer;

        String channelName;
        try {
            channelName = json.getString(TimerData.TAG_CHANNEL_NAME);
        } catch (JSONException e) {
            Log.d("JSON saving", "Incoming JSON has invalid data.");
            return null;
        }

        if (datas.containsKey(channelName)) {
            timer = datas.get(channelName);
            timer.parseJSON(json);

        } else {
            timer = TimerData.fromJSON(json);
            datas.put(channelName, timer);
        }

        return timer;
    }

    public static TimerData getFirstTimerData(Context context) {
        return null;
        // return getTimerData(context, Preferences.getPreferences(context).getChannelName());
    }

    public static void saveJSONtoFile(Context context, JSONObject json) {
        String channelName = JSONParser.getStringOrNull(json, TimerData.TAG_CHANNEL_NAME);

        try {
            FileOutputStream fos = context.openFileOutput(channelName + ".json", Context.MODE_PRIVATE);
            String jsonString = json.toString();
            jsonString += "\n";
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("DataReceiveTask", "Cannot write, file was not found");
        } catch (IOException e) {
            Log.d("DataReceiveTask", "Error while writing into file");
        }
    }
}
