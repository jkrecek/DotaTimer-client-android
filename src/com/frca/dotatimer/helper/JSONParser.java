package com.frca.dotatimer.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    // constructor
    public JSONParser() {

    }

    public JSONObject getJSONFromUrl(String url) {

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }

    /***********************/
    /*** PARSING HELPERS ***/
    /***********************/
    public static String getStringOrNull(JSONObject json, String key)
    {
        try {
            return json.getString(key);
        } catch (JSONException e) {
            Log.d("JSONParser", "Unable to parse string from value '"+key+"'");
            return null;
        }
    }

    public static int getIntOrNil(JSONObject json, String key)
    {
        try {
            return json.getInt(key);
        } catch (JSONException e) {
            Log.d("JSONParser", "Unable to parse int from value '"+key+"'");
            return 0;
        }
    }

    public static JSONObject getJSONOrNull(JSONObject json, String key)
    {
        try {
            return json.getJSONObject(key);
        } catch (JSONException e) {
            Log.d("JSONParser", "Unable to parse JSON from value '"+key+"'");
            return null;
        }
    }

    public static JSONArray getJSONArrOrNull(JSONObject json, String key)
    {
        try {
            return json.getJSONArray(key);
        } catch (JSONException e) {
            Log.d("JSONParser", "Unable to parse JSONArray from value '"+key+"'");
            return null;
        }
    }
}