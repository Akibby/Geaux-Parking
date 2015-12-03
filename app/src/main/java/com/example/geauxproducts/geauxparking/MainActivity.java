package com.example.geauxproducts.geauxparking;

import android.Manifest;
import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.ConnectionRequest;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private GoogleApiClient mLocationClient;
    public Marker marker;
    public Marker parkingLocation;
    private MarkerOptions locationMarkerOptions;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
            }
        }

        if (servicesOK()) {
            setContentView(R.layout.activity_map);

            if (initMap()) {
                gotoLocation(30.413255, -91.180035, 15);
                mLocationClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                        .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
                mLocationClient.connect();
            } else {
                Toast.makeText(this, "Map not Connected!", Toast.LENGTH_SHORT).show();
            }
        } else {
            setContentView(R.layout.activity_main);
        }

    }

    public boolean servicesOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't conncet to mapping service", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
        }
        return (mMap != null);
    }

    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    public void showCurrentLocation(View view) {
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        if (currentLocation == null) {
            Toast.makeText(this, "Couldn't Connect!", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(update);


            if (marker != null) {
                marker.remove();
            }

            locationMarkerOptions = new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            marker = mMap.addMarker(locationMarkerOptions);
            //addMarker(currentLocation.getLatitude(), currentLocation.getLongitude());

        }
//        locationMarkerOptions.addListener("dblclick", function() {
//            marker.setMap(null);
//        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
            case R.id.zone1:
                removePins();
                dropZone1Pin();
                Toast.makeText(this, "Commuter Parking", Toast.LENGTH_SHORT).show();
                findClosestCommuterLot();
                break;
            case R.id.zone2:
                removePins();
                dropZone2Pin();
                Toast.makeText(this, "Greek Parking", Toast.LENGTH_SHORT).show();
                findClosestGreekLot();
                break;
            case R.id.zone3:
                removePins();
                dropZone3Pin();
                Toast.makeText(this, "Resident Parking", Toast.LENGTH_SHORT).show();
                findClosestResidentLot();
                break;
            case R.id.zone4:
                removePins();
                dropZone4Pin();
                Toast.makeText(this, "Law Parking", Toast.LENGTH_SHORT).show();
                findClosestLawLot();
                break;
            case R.id.visitorParking:
                removePins();
                dropVisitorParking();
                Toast.makeText(this, "Visitor Parking", Toast.LENGTH_SHORT).show();
                findClosestVisitorLot();
                break;
            case R.id.allZoneParking:
                removePins();
                dropAllZoneParking();
                Toast.makeText(this, "All Zones Parking", Toast.LENGTH_SHORT).show();
                findClosestAllZonesLot();
                break;
            case R.id.removeAllPins:
                removePins();
                Toast.makeText(this, "You have removed all pins.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.removeParkingPin:
                removeParkingPin();
                Toast.makeText(this, "You have removed your parking location.", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    double[] Zone1Lat = {30.406355, 30.408178, 30.404483, 30.405076, 30.405696, 30.403947, 30.407241, 30.409231, 30.410378, 30.411747, 30.410748,
            30.411969, 30.411701, 30.412395, 30.413080, 30.415273, 30.415837, 30.417650, 30.417075, 30.419498, 30.419961, 30.420109, 30.419322};

    double[] Zone1Long = {-91.172724, -91.175278, -91.174721, -91.176995, -91.180021, -91.181614, -91.184114, -91.185101, -91.184736
            , -91.185755, -91.187279, -91.192472, -91.191249, -91.187472, -91.187848, -91.186882, -91.186431, -91.185755, -91.180777, -91.182545, -91.183521
            , -91.180710, -91.180678};


    public void dropZone1Pin() {//Commuter Pins
        for (int i = 0; i < Zone1Lat.length; i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone1Lat[i], Zone1Long[i])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }


    double[] Zone2Lat = {30.416519, 30.416602, 30.415742, 30.410662, 30.409528};
    double[] Zone2Long = {-91.174509, -91.172728, -91.169981, -91.168029, -91.170009};

    public void dropZone2Pin() { //Greek Pins

        for (int i = 0; i < Zone2Lat.length; i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone2Lat[i], Zone2Long[i])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        }
    }


    double[] Zone3Lat = {30.416954, 30.417379, 30.418684, 30.419498, 30.419961, 30.420109, 30.419322, 30.408108, 30.408779, 30.409528,
            30.410278, 30.410144, 30.411282, 30.409898, 30.410028, 30.410555, 30.412138, 30.412355, 30.412161};
    double[] Zone3Long = {-91.182416, -91.183210, -91.183703, -91.182545, -91.183521, -91.180710, -91.180678, -91.173271, -91.173576, -91.170009, -91.170428,
            -91.171313, -91.171533, -91.172487, -91.174247
            , -91.175475, -91.175202, -91.173641, -91.171892};

    public void dropZone3Pin() { //Resident Pins

        for (int i = 0; i < Zone3Lat.length; i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone3Lat[i], Zone3Long[i])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    }

    double[] Zone4Lat = {30.413974, 30.415103, 30.415626, 30.415742};
    double[] Zone4Long = {-91.174236, -91.174209, -91.173346, -91.169981};

    public void dropZone4Pin() { //Law Pins

        for (int i = 0; i < Zone4Lat.length; i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone4Lat[i], Zone4Long[i])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        }
    }

    double[] VisitorLat = {30.414007, 30.412930, 30.415488, 30.416080, 30.413318, 30.412633, 30.409893, 30.407607, 30.406809, 30.406410, 30.408739};
    double[] VisitorLong = {-91.175454, -91.174712, -91.178500, -91.177094, -91.184347, -91.185216, -91.183525, -91.181208, -91.179649, -91.179971, -91.175234};

    public void dropVisitorParking() { //Visitor Pins

        for (int i = 0; i < VisitorLat.length; i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(VisitorLat[i], VisitorLong[i])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }
    }

    double[] AllZoneLat = {30.405691, 30.406585, 30.405448, 30.404237, 30.407008, 30.406114, 30.406650, 30.409649, 30.406000, 30.411968, 30.412025, 30.413309, 30.417964,
            30.406102, 30.420241, 30.411003, 30.411336, 30.412904, 30.412709, 30.417235, 30.418429};

    double[] AllZoneLong = {-91.186789, -91.186195, -91.185338, -91.186836, -91.188795, -91.189700, -91.192875, -91.195108, -91.198207
            , -91.194369, -91.188999, -91.189574, -91.192645, -91.182273, -91.178700, -91.170437, -91.170117, -91.171040, -91.169194, -91.170541, -91.171069};

    public void dropAllZoneParking() { //All Zone Pins

        for (int i = 0; i < AllZoneLat.length; i++) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(AllZoneLat[i], AllZoneLong[i])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }
    }

    public void removePins() {

        mMap.clear();
        if (locationMarkerOptions != null) {
            marker = mMap.addMarker(locationMarkerOptions);
        }
    }

    public void removeParkingPin() {
        marker.remove();
        locationMarkerOptions = null;
    }

    public void findClosestCommuterLot() {
        int index = findShortestDistance(Zone1Lat, Zone1Long);
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone1Lat[index], Zone1Long[index])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }

    public void findClosestGreekLot() {
        int index = findShortestDistance(Zone2Lat, Zone2Long);
        mMap.addMarker(new MarkerOptions().position(new LatLng(Zone2Lat[index], Zone2Long[index])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }

    public void findClosestResidentLot() {
        int index = findShortestDistance(Zone3Lat, Zone3Long);
        mMap.addMarker(new MarkerOptions().position(new LatLng(Zone3Lat[index], Zone3Long[index])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }

    public void findClosestLawLot() {
        int index = findShortestDistance(Zone4Lat, Zone4Long);
        mMap.addMarker(new MarkerOptions().position(new LatLng(Zone4Lat[index], Zone4Long[index])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }

    public void findClosestVisitorLot() {
        int index = findShortestDistance(VisitorLat, VisitorLong);
        mMap.addMarker(new MarkerOptions().position(new LatLng(VisitorLat[index], VisitorLong[index])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }

    public void findClosestAllZonesLot() {
        int index = findShortestDistance(AllZoneLat, AllZoneLong);
        mMap.addMarker(new MarkerOptions().position(new LatLng(AllZoneLat[index], AllZoneLong[index])).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
    }

    public int findShortestDistance(double[] Lat, double[] Long) {
        int index = 0;
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        if (currentLocation == null) {
            Toast.makeText(this, "Couldn't Connect!", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            double d = 1000000000;
            double lat1 = 0;
            double lon1 = 0;
            for (int i = 0; i < Lat.length; i++) {
                double dlon = latLng.longitude - Long[i];
                double dlat = latLng.latitude - Lat[i];
                double test = (Math.sin(dlat / 2)) * (Math.sin(dlat / 2)) + Math.cos(Lat[i]) * Math.cos(latLng.latitude) * (Math.sin(dlon / 2)) * (Math.sin(dlon / 2));
                double distance = 3959 * (2 * Math.atan2(Math.sqrt(test), Math.sqrt(1 - test)));
                if (distance < d) {
                    d = distance;
                    lat1 = Lat[i];
                    lon1 = Long[i];
                    index = i;
                }
            }
        }
            return index;
        }
    }

