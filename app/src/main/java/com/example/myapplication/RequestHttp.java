package com.example.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class RequestHttp extends AsyncTask<LatLng, String, String> {
    private StringBuilder urlBuilder;
    private URL url;
    private HttpURLConnection conn;
    private MainActivity mMainActivity;


    public RequestHttp(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }


    @Override
    protected String doInBackground(LatLng... latLngs) {

        String strCoord = String.valueOf(latLngs[0].longitude) + "," + String.valueOf(latLngs[0].latitude);
        StringBuilder strBuilder = new StringBuilder();

        urlBuilder = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=" + strCoord + "&sourcecrs=epsg:4326&output=json&orders=addr");
        try {
            url = new URL(urlBuilder.toString());

            Log.d("myLog", "URL?" + urlBuilder);

            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "API-KEY-ID_HERE");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", "API-KEY_HERE");

            BufferedReader buffReader;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                buffReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                buffReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            String line;
            while ((line = buffReader.readLine()) != null) {
                strBuilder.append(line);
            }
            buffReader.close();
            conn.disconnect();

        } catch (Exception e) {
            return null;
        }
        Log.d("myLog", "마지막 버퍼?" + strBuilder);
        return strBuilder.toString();
    }

    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        JsonParser jsonParser = new JsonParser();

        JsonObject jObject = (JsonObject) jsonParser.parse(result);
        JsonArray jsonArray = (JsonArray) jObject.get("results");
        jObject = (JsonObject) jsonArray.get(0);
        jObject = (JsonObject) jObject.get("region");
        jObject = (JsonObject) jObject.get("area1");
        String data = jObject.get("name").getAsString();

        jObject = (JsonObject) jsonParser.parse(result);
        jsonArray = (JsonArray) jObject.get("results");
        jObject = (JsonObject) jsonArray.get(0);
        jObject = (JsonObject) jObject.get("region");
        jObject = (JsonObject) jObject.get("area2");
        data = data + " " + jObject.get("name").getAsString();

        jObject = (JsonObject) jsonParser.parse(result);
        jsonArray = (JsonArray) jObject.get("results");
        jObject = (JsonObject) jsonArray.get(0);
        jObject = (JsonObject) jObject.get("region");
        jObject = (JsonObject) jObject.get("area3");
        data = data + " " + jObject.get("name").getAsString();


        jObject = (JsonObject) jsonParser.parse(result);
        jsonArray = (JsonArray) jObject.get("results");
        jObject = (JsonObject) jsonArray.get(0);
        jObject = (JsonObject) jObject.get("land");
        data = data + " " + jObject.get("number1").getAsString();
        if (jObject.get("number2").getAsString().equals("") == true) ;
        else {
            data = data + "-" + jObject.get("number2").getAsString();
        }

//        Toast.makeText(mMainActivity, data, Toast.LENGTH_LONG).show();
        Log.d("test_d", data);

        mMainActivity.showInfoWindow(data);
    }
}


