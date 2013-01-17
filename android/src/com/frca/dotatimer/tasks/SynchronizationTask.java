package com.frca.dotatimer.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.frca.dotatimer.MainActivity;
import com.frca.dotatimer.helper.Constants;
import com.frca.dotatimer.helper.JSONParser;
import com.frca.dotatimer.helper.NotificationDataHolder;
import com.frca.dotatimer.helper.ParameterMap;

public abstract class SynchronizationTask extends AsyncTask<Void, Void, String> {

    final private int timeout_ms = 15000; // 15 sec

    final protected Class<? extends HttpEntityEnclosingRequestBase> requestType;

    protected String url;
    protected String postBody;

    protected Context context;
    protected ParameterMap map;

    protected HttpClient client;
    protected HttpEntityEnclosingRequestBase request;

    protected HttpResponse response;
    protected InputStream result;
    protected String strResult;

    protected SynchronizationTask(Context con, ParameterMap m, Class<? extends HttpEntityEnclosingRequestBase> reqType)
    {
        context = con;
        map = m;
        requestType = reqType;
    }

    @Override
    protected String doInBackground(Void... maps)
    {
        prepareRequest(requestType);

        if (!doExecute())
            return "Connection problem";

        if (!loadResult())
            return "Loading data failed";

        String error = checkStatusError();
        if (error != null)
            return error;

        JSONParser jParser = new JSONParser();
        JSONObject json = jParser.getJSONFromInputStream(result);

        // recieved updated json
        JSONParser.saveJSONtoFile(context, json);

        NotificationDataHolder.unset();
        if (MainActivity.instance != null)
            MainActivity.instance.data.parseJSON(json);

        return "OK";
    }

    @Override
    protected final void onPreExecute()
    {
        onStart();
        if (MainActivity.instance != null)
            onStartActivity(MainActivity.instance);
    }


    @Override
    protected final void onPostExecute(String res)
    {
        onEnd(res);
        if (MainActivity.instance != null)
            onEndActivity(MainActivity.instance, res);
    }

    protected void onStart() {}
    protected void onEnd(String res) {}
    protected void onStartActivity(MainActivity instance) {}
    protected void onEndActivity(MainActivity instance, String res) {}

    protected void prepareRequest(Class<? extends HttpEntityEnclosingRequestBase> httpType)
    {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout_ms);
        HttpConnectionParams.setSoTimeout(httpParams, timeout_ms);
        client = new DefaultHttpClient(httpParams);

        setUpUrl();
        Log.d("url", url != null ? url : "null");
        setPostMessage();
        Log.d("post", postBody != null ? postBody : "null");

        try {
            request = httpType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        request.setURI(URI.create(url));
        if (postBody != null)
            request.setEntity(new ByteArrayEntity(postBody.getBytes(Charset.defaultCharset())));
    }

    protected boolean doExecute()
    {
        try {
            response = client.execute(request);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean loadResult()
    {
        try {
            result = response.getEntity().getContent();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected String checkStatusError()
    {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK ||
            statusCode == HttpStatus.SC_CREATED ||
            statusCode == HttpStatus.SC_ACCEPTED)
            return null;


        return String.valueOf(statusCode) + ": "+getStringResult();
    }

    protected void setUpFullUrl(String relativeUrl)
    {
        url = Constants.SERVER_ROUTE + '/' + relativeUrl;
    }

    protected void setUpUrl()
    {
        setUpFullUrl("api/data.json");
    }

    protected void setPostMessage()
    {

    }

    protected String getStringResult()
    {
        if (strResult == null)
        {
            if (result != null)
            {
                strResult = JSONParser.InputStreamToString(result);
                try {
                    result.close();
                } catch (IOException e) {
                    Log.e("IS Error", "Error while closing InputStream: " + e.toString());
                }
                result = null;
            }
            else
                Log.e("SynchroniztaionTask", "Result unexpectedly deleted");
        }


        return strResult;
    }

}
