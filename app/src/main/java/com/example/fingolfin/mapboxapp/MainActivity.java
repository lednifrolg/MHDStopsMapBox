package com.example.fingolfin.mapboxapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSIONS_LOCATION = 0;
    private static final String TAG = MainActivity.class.getSimpleName();

    private MapView mMapView = null;
    private String mRoute = null;

    private BusStopsTask mBusStops = null;
    private TramStopsTask mTramStops = null;
    private TrolleybusStopsTask mTrolleybusStops = null;
    private RouteTask mRoutes = null;
    private CloseStopTask mCloseStop = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMapView.setCenterCoordinate(new LatLng(mMapView.getMyLocation().getLatitude(), mMapView.getMyLocation().getLongitude()));
                mMapView.setZoomLevel(14);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        /** Create a MapView and give it some properties */
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.setStyleUrl(Style.EMERALD);

        mMapView.setCenterCoordinate(new LatLng(0, 0));

        // Show user location (purposely not in follow mode)
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
        } else {
            mMapView.setMyLocationEnabled(true);
        }

        mMapView.setCenterCoordinate(new LatLng(mMapView.getMyLocation().getLatitude(), mMapView.getMyLocation().getLongitude()));
        mMapView.setZoomLevel(14);
        mMapView.onCreate(savedInstanceState);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMapView.setMyLocationEnabled(true);
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_bus) {
            if (mBusStops == null) {
                mBusStops = new BusStopsTask(getApplicationContext());
                mBusStops.execute(mMapView);
            } else {
                mBusStops.deleteMarkers();
                mBusStops = null;
            }
        } else if (id == R.id.nav_tram) {
            if (mTramStops == null) {
                mTramStops = new TramStopsTask(getApplicationContext());
                mTramStops.execute(mMapView);
            } else {
                mTramStops.deleteMarkers();
                mTramStops = null;
            }
        } else if (id == R.id.nav_trolleybus) {
            if (mTrolleybusStops == null) {
                mTrolleybusStops = new TrolleybusStopsTask(getApplicationContext());
                mTrolleybusStops.execute(mMapView);
            } else {
                mTrolleybusStops.deleteMarkers();
                mTrolleybusStops = null;
            }
        } else if (id == R.id.nav_route) {
            openRouteInputDialog();
        } else if (id == R.id.close_stop) {
            openCloseStopInputDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openRouteInputDialog() {
        View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.user_input, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setView(view);

        final EditText userInput = (EditText) view.findViewById(R.id.userInputText);

        alertBuilder.setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRoute = userInput.getText().toString();

                if (mRoute != null && mRoute.length() >= 1) {
                    if (mRoutes != null) {
                        mRoutes.clearLine();
                        mRoutes = new RouteTask(mRoute);
                        mRoutes.execute(mMapView);
                    } else {
                        mRoutes = new RouteTask(mRoute);
                        mRoutes.execute(mMapView);
                    }
                }
            }
        });

        Dialog dialog = alertBuilder.create();
        dialog.show();
    }


    public void openCloseStopInputDialog() {
        View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.user_input, null);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
        alertBuilder.setView(view);

        final EditText userInput = (EditText) view.findViewById(R.id.userInputText);

        alertBuilder.setCancelable(true).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mRoute = userInput.getText().toString();

                if (mRoute != null && mRoute.length() >= 1) {
                    if (mCloseStop != null) {
                        mCloseStop.deleteMarker();
                        mCloseStop = new CloseStopTask(getApplicationContext(), mRoute);
                        mCloseStop.execute(mMapView);
                    } else {
                        mCloseStop = new CloseStopTask(getApplicationContext(), mRoute);
                        mCloseStop.execute(mMapView);
                    }
                }
            }
        });

        Dialog dialog = alertBuilder.create();
        dialog.show();

    }
}
