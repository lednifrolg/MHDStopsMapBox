package com.example.fingolfin.mapboxapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Sprite;
import com.mapbox.mapboxsdk.annotations.SpriteFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Fingolfin on 11/17/2015.
 */
public class CloseStopTask extends AsyncTask<MapView, Void, Boolean> {
    public static final String TAG = CloseStopTask.class.getSimpleName();

    private Context mContext;

    private MapView mMapView = null;
    private Marker mMarker = null;
    private String mRef = null;

    public CloseStopTask(Context c, String ref) {
        mContext = c;
        mRef = ref;
    }

    public void deleteMarker() {
        if (mMarker != null)
            mMarker.remove();
    }

    @Override
    protected Boolean doInBackground(MapView... params) {
        mMapView = params[0];
        JSONObject jObj = getJson();

        if (jObj == null)
            return false;

        updateStops(jObj);
        return true;
    }


    public void updateStops(JSONObject jObj) {
        JSONArray jArray = null;

        SpriteFactory spriteFactory = new SpriteFactory(mMapView);
        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_location_searching_black_24dp);
        Sprite icon = spriteFactory.fromDrawable(drawable);

        mMapView.setCenterCoordinate(new LatLng(mMapView.getMyLocation().getLatitude(), mMapView.getMyLocation().getLongitude()));

        try {
            jArray = jObj.getJSONArray("stops");
            Log.d(TAG, "Rows : " + jArray.length());
            Map<String, ArrayList<JSONObject>> stopsMap = new HashMap<>();

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject obj = jArray.getJSONObject(i);
                ArrayList<JSONObject> stop;

                if (stopsMap.containsKey(obj.getString("id")) == false) {
                    stop = new ArrayList<>();
                    stop.add(obj);
                    stopsMap.put(obj.getString("id"), stop);
                } else {
                    stop = stopsMap.get(obj.getString("id"));
                    stop.add(obj);
                }


            }

            for (Map.Entry<String, ArrayList<JSONObject>> entry : stopsMap.entrySet()) {
                ArrayList<JSONObject> stop = entry.getValue();
                StringBuilder stopsNum = new StringBuilder();

                String name = stop.get(0).getString("name");
                String coords = stop.get(0).getString("point").substring(31);
                coords = coords.substring(0, coords.length() - 3);
                String[] cor = coords.split(",");

                double lat = Double.parseDouble(cor[1]);
                double lng = Double.parseDouble(cor[0]);


                for (JSONObject obj : stop) {
                    stopsNum.append(obj.getString("link") + " ");
                }


                Marker marker = mMapView.addMarker(new MarkerOptions().position(new LatLng(lat, lng))
                        .title(name).snippet(stopsNum.toString()).icon(icon));
                mMarker = marker;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getJson() {
        InputStream is = null;
        String json = "";
        JSONObject jObj = null;

        try {
            URL url = new URL(NetworkInfo.IP_ADDRESS + "/stop.php");
            Map<String, Object> params = new LinkedHashMap<>();

            //TODO add current loc
            //params.put("long", "17.1062922");
            //params.put("lat", "48.1569394");

            params.put("long", mMapView.getMyLocation().getLongitude());
            params.put("lat", mMapView.getMyLocation().getLatitude());
            params.put("ref", mRef);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();


            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            // Starts the query
            conn.connect();

            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            is = conn.getInputStream();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            is.close();
            json = sb.toString();
            Log.e("JSON", json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            return null;
        }

        return jObj;
    }

}
