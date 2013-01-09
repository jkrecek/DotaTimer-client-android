package com.frca.dotatimer.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map.Entry;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.frca.dotatimer.MainActivity;

public class HttpRequestHandler extends AsyncTask<ParameterMap, Void, String>
{

    private final ProgressDialog dialog;
    private final MainActivity activity;

    public HttpRequestHandler(MainActivity act)
    {
        dialog = new ProgressDialog(act);
        activity = act;
    }

    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Odesílání..");
        dialog.show();
    }

    @Override
    protected String doInBackground(ParameterMap... maps)
    {
        String parameters = getParametersFromMap(maps[0]);

        String lineResponse = "";

        try
        {
            URL url = new URL(Constants.getServerHandlerURL());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");

            OutputStreamWriter request = new OutputStreamWriter(connection.getOutputStream());
            request.write(parameters);
            request.flush();
            request.close();

            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            lineResponse = reader.readLine();

            isr.close();
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lineResponse.equals("OK"))
            return "Data úspìšnì odeslána";
        else
            return "Nastal problém, data se neodeslala";
    }

    private String getParametersFromMap(ParameterMap map)
    {
        String str = "";
        for (Entry<String, String> pair : map.entrySet())
            str += pair.getKey() + "=" + pair.getValue() + "&";

        return str.substring(0, str.length() - 1);
    }

    @Override
    protected void onPostExecute(String result)
    {
        dialog.dismiss();
        Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        activity.syncPlan();
    }
}