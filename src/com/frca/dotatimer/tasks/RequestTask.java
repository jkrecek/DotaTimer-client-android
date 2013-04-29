package com.frca.dotatimer.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.frca.dotatimer.helper.JSONParser;
import com.frca.dotatimer.helper.ParameterMap;
import com.frca.dotatimer.helper.Preferences;

public class RequestTask extends AsyncTask<Void, Void, Void> {

    final private int timeout_ms = 15000; // 15
                                          // sec

    protected final RequestType requestType;
    final protected Class<? extends HttpEntityEnclosingRequestBase> requestClass;

    protected String url;
    // protected String params;

    protected String finalUrl;
    protected String finalPost;

    protected Context context;
    protected ParameterMap parameters;

    protected HttpClient client;
    protected HttpEntityEnclosingRequestBase request;

    protected HttpResponse response;
    protected InputStream ISResult;
    protected String strResult;
    protected JSONObject jsonResult;

    protected String executeResult;
    protected String error;

    private static boolean in_progress;

    enum RequestType {
        GET, POST, PUT
    }

    public RequestTask(Context con, ParameterMap m, RequestType type) {
        context = con;
        parameters = m;

        requestType = type;
        if (type == RequestType.PUT)
            requestClass = HttpPut.class;
        else
            requestClass = HttpPost.class;
    }

    protected void prepareRequest(Class<? extends HttpEntityEnclosingRequestBase> httpType) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout_ms);
        HttpConnectionParams.setSoTimeout(httpParams, timeout_ms);
        client = new DefaultHttpClient(httpParams);

        try {
            request = httpType.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        setRequestParameters();
        request.setURI(URI.create(finalUrl));
        if (finalPost != null)
            request.setEntity(new ByteArrayEntity(finalPost.getBytes(Charset.defaultCharset())));
    }

    @Override
    protected Void doInBackground(Void... maps) {
        prepareRequest(requestClass);

        if (!doExecute()) {
            error = "Connection problem";
            return null;
        }

        if (!loadResult()) {
            error = "Loading data failed";
            return null;
        }

        error = checkStatusError();
        if (error != null)
            return null;

        JSONParser jParser = new JSONParser();
        jsonResult = jParser.getJSONFromInputStream(ISResult);

        // DataHolder.saveJSON(context, jsonResult);

        return null;
    }

    @Override
    protected final void onPreExecute() {
        if (startListener != null)
            startListener.onStart(this);

        in_progress = true;
    }

    @Override
    protected final void onPostExecute(Void v) {
        in_progress = false;

        if (endListener != null)
            endListener.onEnd(this);
    }

    private TaskStartListener startListener = null;
    private TaskEndListener endListener = null;

    public abstract interface TaskStartListener {
        abstract void onStart(RequestTask t);
    }

    public abstract interface TaskEndListener {
        abstract void onEnd(RequestTask t);
    }

    public void setOnStartListener(TaskStartListener listener) {
        startListener = listener;
    }

    public void setOnEndListener(TaskEndListener listener) {
        endListener = listener;
    }

    protected boolean doExecute() {
        try {
            response = client.execute(request);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean loadResult() {
        try {
            ISResult = response.getEntity().getContent();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected String checkStatusError() {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_ACCEPTED)
            return null;

        return String.valueOf(statusCode) + ": " + getStringResult();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private void setRequestParameters() {
        String hostAddress = Preferences.getPreferences(context).getString(Preferences.Key.SERVER_ADDRESS);
        String tempUrl = hostAddress + "/api/" + url;
        if (requestType == RequestType.GET) {
            finalUrl = tempUrl + "?" + parameters.toGetString();
        } else if (requestType == RequestType.POST || requestType == RequestType.PUT) {
            finalUrl = tempUrl;
            finalPost = parameters.toJSONString();
        }

    }

    protected String getStringResult() {
        if (strResult == null) {
            if (ISResult != null) {
                strResult = JSONParser.InputStreamToString(ISResult);
                try {
                    ISResult.close();
                } catch (IOException e) {
                    Log.e("IS Error", "Error while closing InputStream: " + e.toString());
                }
                // ISResult = null;
            } else
                Log.e("SynchronizationTask", "Result unexpectedly deleted");
        }

        return strResult;
    }

    public Context getContext() {
        return context;
    }

    public String getError() {
        return error;
    }

    public ParameterMap getParameterMap() {
        return parameters;
    }

    public static boolean isTaskInProgress() {
        return in_progress;

    }
}
