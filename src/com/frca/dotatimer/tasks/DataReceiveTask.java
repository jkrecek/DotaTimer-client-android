package com.frca.dotatimer.tasks;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.R;
import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.JSONParser;
import com.frca.dotatimer.helper.NotificationDataHolder;
import com.frca.dotatimer.helper.TimerData;

public class DataReceiveTask extends AsyncTask<Void, Void, Void>
{
    private final Context context;
    private final TimerData data;

    public DataReceiveTask(Context con, TimerData originData)
    {
        context = con;
        data = originData;
    }

    @Override
    protected void onPreExecute()
    {
        Log.d("DataReceive", "Start");

        if (MainActivity.instance != null)
        {
            MainActivity.instance.toggleLayoutContent(false);
            ((Button)MainActivity.instance.findViewById(R.id.button_refresh)).setEnabled(false);
        }
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {
        long startMs = System.currentTimeMillis();
        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromUrl(Constants.getServerJsonURL());

        Log.d("DataReceiveTime", "JSON received in ms " + String.valueOf(System.currentTimeMillis() - startMs));

        saveJSONtoFile(json);

        Log.d("DataReceiveTime", "JSON saved into file in ms " + String.valueOf(System.currentTimeMillis() - startMs));

        NotificationDataHolder.unset();
        data.parseJSON(json);

        Log.d("DataReceiveTime", "Handling json data in ms " + String.valueOf(System.currentTimeMillis() - startMs));

        return null;
    }

    @Override
    protected void onPostExecute(Void arg)
    {
        Log.d("DataReceive", "Complete");

        NotificationDataHolder.show(context);

        if (MainActivity.instance != null)
        {
            MainActivity.instance.toggleLayoutContent(true);
            ((Button)MainActivity.instance.findViewById(R.id.button_refresh)).setEnabled(true);
            MainActivity.instance.onValuesChanged();
        }
    }

    private void saveJSONtoFile(JSONObject json)
    {
        String channelName = JSONParser.getStringOrNull(json, TimerData.TAG_CHANNEL_NAME);

        try {
            FileOutputStream fos = context.openFileOutput(channelName+".json", Context.MODE_PRIVATE);
            fos.write(json.toString().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("DataReceiveTask", "Cannot write, file was not found");
        } catch (IOException e) {
            Log.d("DataReceiveTask", "Error while writing into file");
        }
    }
}
