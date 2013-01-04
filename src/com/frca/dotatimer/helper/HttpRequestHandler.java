package com.frca.dotatimer.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map.Entry;

import com.frca.dotatimer.MainActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class HttpRequestHandler extends  AsyncTask<ParameterMap, Void, String> {

    private ProgressDialog dialog;
    private Context context;
    
    public HttpRequestHandler(MainActivity activity)
    {
        dialog = new ProgressDialog(activity);
        context = activity;
    }

    protected void onPreExecute()
    {
       dialog.setMessage("Odesílání..");
       dialog.show();
    }

    protected String doInBackground(ParameterMap... maps)
    {
        String parameters = getParametersFromMap(maps[0]);

        Log.e("LAJNA", parameters);
        HttpURLConnection connection;
        OutputStreamWriter request = null;

        String response = null;            

        try {
            URL url = new URL(Constants.getServerHandlerURL());
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");    

            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(parameters);
            request.flush();
            request.close();            
            
            String line = "";               
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

            response = sb.toString();
            
            isr.close();
            reader.close();

        } catch(IOException e) {
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
            str += pair.getKey() +"=" + pair.getValue() + "&";
        
        return str.substring(0, str.length() - 1);        
    }

    protected void onPostExecute(String result)
    {
        dialog.dismiss();
        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
    }
}