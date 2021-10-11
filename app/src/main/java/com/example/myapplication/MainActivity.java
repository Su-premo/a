package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.PolygonOverlay;
import com.naver.maps.map.overlay.PolylineOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Serializable {

    private Spinner sinceSpinner;
    private ArrayAdapter arrayAdapter;
    private CheckBox checkBox;
    private Overlay overlay;
    private ArrayList<LatLng> ForThreeMarkeresAL = new ArrayList<>();
    private Button ButtonResetOvrays;
    private Marker markersBy;
    private ArrayList<Marker> MarkerPoligonGroupAL = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Button ButtonHeadTo;
    private Marker markersLC;
    private ArrayList<Marker> MarkerLCGroupAL = new ArrayList<>();
    private InfoWindow infoWindow = new InfoWindow();
    private EditText searchAddr;
    private Button addrButton;
    private StringBuilder urlBuilder2;
    private String getAddr = "";
    private double geoLat = 0.0;
    private double geoLng = 0.0;
    private Marker geoMarkers;
    private ArrayList<Marker> geoMarkerAL = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        setView();

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        Log.d("myLog", "여기는 내 앱의 oncreate이 실행되는 부분이야.");

        mapFragment.getMapAsync(this);
    }

    public void setView() {
        sinceSpinner = (Spinner) findViewById(R.id.since_spinner);
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.since, android.R.layout.simple_spinner_dropdown_item);
        sinceSpinner.setAdapter(arrayAdapter);

        checkBox = (CheckBox) findViewById(R.id.checkBox);

        ButtonResetOvrays = (Button) findViewById(R.id.button);
        ButtonHeadTo = (Button) findViewById(R.id.button2);

        searchAddr = (EditText) findViewById(R.id.editText1);
        addrButton = (Button) findViewById(R.id.button3);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull @org.jetbrains.annotations.NotNull NaverMap naverMap) {
        Log.d("myLog", "여기는 내 앱의 onMapReady가 실행되는 부분이야.");

        // **CURRENT LOCATION overlay
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        LocationOverlay locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);

        // set the first place of view(Gunsan univ)
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(35.9461, 126.6822));
        naverMap.moveCamera(cameraUpdate);

        // markers for Univ, Cityhall and Port in Gunsan
        Marker KunsanUniv = new Marker();
        KunsanUniv.setPosition(new LatLng(35.9461, 126.6822));
        KunsanUniv.setMap(naverMap);
        Marker GunsanCityhall = new Marker();
        GunsanCityhall.setPosition(new LatLng(35.967620, 126.736846));
        GunsanCityhall.setMap(naverMap);
        Marker GunsanPort = new Marker();
        GunsanPort.setPosition(new LatLng(35.970596, 126.617127));
        GunsanPort.setMap(naverMap);

        /*
        // Polyline for 3 markers, given the coordinate
        PolylineOverlay polyline = new PolylineOverlay();
        polyline.setCoords(Arrays.asList(
                new LatLng(35.9461, 126.6822),
                new LatLng(35.967620, 126.736846),
                new LatLng(35.970596, 126.617127)
        ));
        polyline.setMap(naverMap);
        */

        // polygon for 3 markers, given the coordinate
        PolygonOverlay polygon = new PolygonOverlay();
        polygon.setCoords(Arrays.asList(
                new LatLng(35.9461, 126.6822),
                new LatLng(35.967620, 126.736846),
                new LatLng(35.970596, 126.617127)
        ));
        polygon.setMap(naverMap);
        polygon.setColor(Color.parseColor("#80ff0000"));


        // Spinner to change Map styles
        sinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    //TODO 지도타입을 0에 맞게 변경하는 코드 작성
                    naverMap.setMapType(NaverMap.MapType.Basic);
                } else if (position == 1) {
                    //TODO 지도타입을 1에 맞게 변경하는 코드 작성
                    naverMap.setMapType(NaverMap.MapType.Satellite);
                } else if (position == 2) {
                    naverMap.setMapType(NaverMap.MapType.Terrain);
                } else {
                    //TODO 지도타입을 0도 1도 2도 아닐때에 맞게 변경하는 코드 작성
                    naverMap.setMapType(NaverMap.MapType.Hybrid);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // CheckBox (CADASTRAL)
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, isChecked);
            }
        });

        // Click Listener -> coordinate + make a polygon when the markers are more than three.
        PolygonOverlay pgForMarkers = new PolygonOverlay();
        naverMap.setOnMapClickListener((point, coord) -> {
            Toast.makeText(this, getString(R.string.format_map_click, coord.latitude, coord.longitude), Toast.LENGTH_SHORT).show();
            markersBy = new Marker();
            markersBy.setPosition(coord);
            markersBy.setMap(naverMap);

            MarkerPoligonGroupAL.add(markersBy);

            ForThreeMarkeresAL.add(markersBy.getPosition());
            if (ForThreeMarkeresAL.size() > 2) {
                pgForMarkers.setCoords(ForThreeMarkeresAL);
                pgForMarkers.setMap(naverMap);
                pgForMarkers.setColor(Color.parseColor("#8000ff00"));
            }
//            Log.d("myLog", "어레이 리스트로 추가 되니?" + ForThreeMarkeresAL);
//            Log.d("myLog", "인자로 넘어오는 point : " + point);
        });

        // LongClick Listener -> Markers + infoWindow(reverse geocoding)
        naverMap.setOnMapLongClickListener((point, coord) -> {
//            Toast.makeText(this, getString(R.string.format_map_long_click, coord.latitude, coord.longitude), Toast.LENGTH_SHORT).show();
            markersLC = new Marker();
            markersLC.setPosition(coord);
            markersLC.setMap(naverMap);

            MarkerLCGroupAL.add(markersLC);
            Log.d("myLog", "into ArrayList?" + MarkerLCGroupAL);

            RequestHttp addr = new RequestHttp(MainActivity.this);
            addr.execute(coord);
        });


        // button to reset all overlays
        ButtonResetOvrays.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pgForMarkers.setMap(null);
                for (int i = 0; i < MarkerPoligonGroupAL.size(); i++)
                    MarkerPoligonGroupAL.get(i).setMap(null);
                ForThreeMarkeresAL.clear();
                MarkerPoligonGroupAL.clear();
//                markersLC.setMap(null);
                for (int i = 0; i < MarkerLCGroupAL.size(); i++)
                    MarkerLCGroupAL.get(i).setMap(null);
                MarkerLCGroupAL.clear();
                for (int i = 0; i < geoMarkerAL.size(); i++)
                    geoMarkerAL.get(i).setMap(null);
                geoMarkerAL.clear();
            }
        });

        // let camera move to three spots in Gunsan after press button
        ButtonHeadTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraUpdate cameraUpdate1 = CameraUpdate.scrollAndZoomTo(new LatLng(35.9606, 126.6832), 11);
                naverMap.moveCamera(cameraUpdate1);
            }
        });

        addrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 에디트 텍스트 입력 받은 값 가져오기
                        getAddr = searchAddr.getText().toString(); // test 값 : 전라북도 군산시 신관동 290
                        Log.d("test_Text", getAddr);


                        StringBuilder urlGeoBuilder = new StringBuilder("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + getAddr);
                        StringBuilder output = new StringBuilder();
                        URL gURL;
                        HttpURLConnection connection;


                        try {
                            gURL = new URL(urlGeoBuilder.toString());

                            connection = (HttpURLConnection) gURL.openConnection();
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("Content-type", "application/json");
                            connection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "API-KEY-ID_HERE");
                            connection.setRequestProperty("X-NCP-APIGW-API-KEY", "API-KEY_HERE");


                            BufferedReader geoBuffer;
                            if (connection.getResponseCode() >= 200 && connection.getResponseCode() <= 300) {
                                geoBuffer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            } else {
                                geoBuffer = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                            }
                            String line;
                            while ((line = geoBuffer.readLine()) != null) {
                                output.append(line);
                            }
                            // Log.d("test_call","");
                            geoBuffer.close();
                            connection.disconnect();

                        } catch (Exception e) {
                            Log.d("test_d", "connection failed");
                        }
                        Log.d("test", output.toString());

                        JsonParser jsonGeoParser = new JsonParser();

                        JsonObject jObject = (JsonObject) jsonGeoParser.parse(output.toString());
                        JsonArray jsonArray = (JsonArray) jObject.get("addresses");
                        jObject = (JsonObject) jsonArray.get(0);
                        geoLat = jObject.get("y").getAsDouble();
                        geoLng = jObject.get("x").getAsDouble();

                        // 스레드에서 UI 스레드가 할 일은 처리할 수 없음. 그래서 UI 스레드에서 하는 일을 처리할 수 있도록 아래와 같이 코딩
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                geoMarkers = new Marker();
                                geoMarkers.setPosition(new LatLng(geoLat, geoLng));
                                geoMarkers.setMap(naverMap);

                                geoMarkerAL.add(geoMarkers);

                                CameraUpdate cameraUpdate2 = CameraUpdate.scrollAndZoomTo(new LatLng(geoLat, geoLng), 15);
                                naverMap.moveCamera(cameraUpdate2);
                            }
                        });
                    }
                }).start();
            }
        });

    }

    public void showInfoWindow(String message) {
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getApplicationContext()) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return message;
            }
        });
        infoWindow.open(markersLC);
    }


}

