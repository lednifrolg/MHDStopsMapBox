package com.example.fingolfin.mapboxapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Fingolfin on 11/16/2015.
 */
public class RouteTask extends AsyncTask<MapView, Void, Boolean> {
    public static final String TAG = RouteTask.class.getSimpleName();

    private MapView mMapView = null;
    private String mRef = null;

    private ArrayList<Polyline> mPolylines = new ArrayList<>();

    public RouteTask(String ref) {
        mRef = ref;
    }

    protected Boolean doInBackground(MapView... params) {
        mMapView = params[0];
        JSONObject jObj = getJson();
        showRoutes(jObj);
        return true;
    }

    public void clearLine() {
        for (Polyline line : mPolylines) {
            line.remove();
        }
    }

    public void showRoutes(JSONObject jObj) {
        try {
            JSONArray jArray = jObj.getJSONArray("routes");

            for (int i = 0; i < jArray.length(); i++) {
                ArrayList<LatLng> points = new ArrayList<LatLng>();
                String route = jArray.getJSONObject(i).getString("route");

                JSONObject jRoute = new JSONObject(route);

                JSONArray coords = jRoute.getJSONArray("coordinates");
                for (int lc = 0; lc < coords.length(); lc++) {
                    JSONArray coord = coords.getJSONArray(lc);
                    LatLng latLng = new LatLng(coord.getDouble(1), coord.getDouble(0));
                    points.add(latLng);
                }

                if (points.size() > 0) {
                    LatLng[] pointsArray = points.toArray(new LatLng[points.size()]);

                    // Draw Points on MapView
                    mPolylines.add(mMapView.addPolyline(new PolylineOptions()
                            .add(pointsArray)
                            .color(Color.parseColor("#3bb2d0"))
                            .width(2)));

                }

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
            URL url = new URL(NetworkInfo.IP_ADDRESS + "/route.php");
            Map<String, Object> params = new LinkedHashMap<>();

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
        }

        return jObj;
    }
}
