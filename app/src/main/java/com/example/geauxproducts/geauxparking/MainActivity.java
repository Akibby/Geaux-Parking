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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private GoogleApiClient mLocationClient;
    public Marker marker;
    private MarkerOptions locationMarkerOptions;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }

        if (servicesOK()) {
            setContentView(R.layout.activity_map);

            if (initMap()) {
                gotoLocation(30.413255, -91.180035, 15);
                mLocationClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                        .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
                mLocationClient.connect();

                mMap.setMyLocationEnabled(true);
            } else
                Toast.makeText(this, "Map not Connected!", Toast.LENGTH_SHORT).show();

        } else
            setContentView(R.layout.activity_main);

    }

    public boolean servicesOK() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS)
            return true;
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else
            Toast.makeText(this, "Can't connect to mapping service", Toast.LENGTH_SHORT).show();

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
        if (currentLocation == null)
            Toast.makeText(this, "Couldn't Connect!", Toast.LENGTH_SHORT).show();
        else {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(update);

            if (marker != null)
                marker.remove();

            locationMarkerOptions = new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            marker = mMap.addMarker(locationMarkerOptions);
        }

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
            case R.id.removeLotPins:
                removePins();
                Toast.makeText(this, "You have removed all lot pins.", Toast.LENGTH_SHORT).show();
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

    //Arrays Holding all of the parking lot coordinates.
    LatLng[] Zone1 = new LatLng[23];
    LatLng[] Zone2 = new LatLng[5];
    LatLng[] Zone3 = new LatLng[19];
    LatLng[] Zone4 = new LatLng[4];
    LatLng[] VisitorParking = new LatLng[11];
    LatLng[] AllZones = new LatLng[21];

    public void createZone1Coordinates() {

        Zone1[0] = new LatLng(30.406355, -91.172724);
        Zone1[1] = new LatLng(30.408178, -91.175278);
        Zone1[2] = new LatLng(30.404483, -91.174721);
        Zone1[3] = new LatLng(30.405076, -91.176995);
        Zone1[4] = new LatLng(30.405696, -91.180021);
        Zone1[5] = new LatLng(30.403947, -91.181614);
        Zone1[6] = new LatLng(30.407241, -91.184114);
        Zone1[7] = new LatLng(30.409231, -91.185101);
        Zone1[8] = new LatLng(30.410378, -91.184736);
        Zone1[9] = new LatLng(30.411747, -91.185755);
        Zone1[10] = new LatLng(30.410748, -91.187279);
        Zone1[11] = new LatLng(30.411969, -91.192472);
        Zone1[12] = new LatLng(30.411701, -91.191249);
        Zone1[13] = new LatLng(30.412395, -91.187472);
        Zone1[14] = new LatLng(30.413080, -91.187848);
        Zone1[15] = new LatLng(30.415273, -91.186882);
        Zone1[16] = new LatLng(30.415837, -91.186431);
        Zone1[17] = new LatLng(30.417650, -91.185755);
        Zone1[18] = new LatLng(30.417075, -91.180777);
        Zone1[19] = new LatLng(30.419498, -91.182545);
        Zone1[20] = new LatLng(30.419961, -91.183521);
        Zone1[21] = new LatLng(30.420109, -91.180710);
        Zone1[22] = new LatLng(30.419322, -91.180678);
    }

    public void createZone2Coordinates(){

        Zone2[0] = new LatLng(30.416519, -91.174509);
        Zone2[1] = new LatLng(30.416602, -91.172728);
        Zone2[2] = new LatLng(30.415742, -91.169981);
        Zone2[3] = new LatLng(30.410662, -91.168029);
        Zone2[4] = new LatLng(30.409528, -91.170009);

    }

    public void createZone3Coordinates(){

        Zone3[0] = new LatLng(30.416954, -91.182416);
        Zone3[1] = new LatLng(30.417379, -91.183210);
        Zone3[2] = new LatLng(30.418684, -91.183703);
        Zone3[3] = new LatLng(30.419498, -91.182545);
        Zone3[4] = new LatLng(30.419961, -91.183521);
        Zone3[5] = new LatLng(30.420109, -91.180710);
        Zone3[6] = new LatLng(30.419322, -91.180678);
        Zone3[7] = new LatLng(30.408108, -91.173271);
        Zone3[8] = new LatLng(30.408779, -91.173576);
        Zone3[9] = new LatLng(30.409528, -91.170009);
        Zone3[10] = new LatLng(30.410278, -91.170428);
        Zone3[11] = new LatLng(30.410144, -91.171313);
        Zone3[12] = new LatLng(30.411282, -91.171533);
        Zone3[13] = new LatLng(30.409898, -91.172487);
        Zone3[14] = new LatLng(30.410028, -91.174247);
        Zone3[15] = new LatLng(30.410555, -91.175475);
        Zone3[16] = new LatLng(30.412138, -91.175202);
        Zone3[17] = new LatLng(30.412355, -91.173641);
        Zone3[18] = new LatLng(30.412161, -91.171892);

    }

    public void createZone4Coordinates(){

        Zone4[0] = new LatLng(30.413974, -91.174236);
        Zone4[1] = new LatLng(30.415103, -91.174209);
        Zone4[2] = new LatLng(30.415626, -91.173346);
        Zone4[3] = new LatLng(30.415742, -91.169981);

    }

    public void createVisitorParkingCoordinates(){

        VisitorParking[0] = new LatLng(30.414007, -91.175454);
        VisitorParking[1] = new LatLng(30.412930, -91.174712);
        VisitorParking[2] = new LatLng(30.415488, -91.178500);
        VisitorParking[3] = new LatLng(30.416080, -91.177094);
        VisitorParking[4] = new LatLng(30.413318, -91.184347);
        VisitorParking[5] = new LatLng(30.412633, -91.185216);
        VisitorParking[6] = new LatLng(30.409893, -91.183525);
        VisitorParking[7] = new LatLng(30.407607, -91.181208);
        VisitorParking[8] = new LatLng(30.406809, -91.179649);
        VisitorParking[9] = new LatLng(30.406410, -91.179971);
        VisitorParking[10] = new LatLng(30.408739, -91.175234);

    }

    public void createAllZoneCoordinates(){

        AllZones[0] = new LatLng(30.405691, -91.186789);
        AllZones[1] = new LatLng(30.406585, -91.186195);
        AllZones[2] = new LatLng(30.405448, -91.185338);
        AllZones[3] = new LatLng(30.404237, -91.186836);
        AllZones[4] = new LatLng(30.407008, -91.188795);
        AllZones[5] = new LatLng(30.406114, -91.189700);
        AllZones[6] = new LatLng(30.406650, -91.192875);
        AllZones[7] = new LatLng(30.409649, -91.195108);
        AllZones[8] = new LatLng(30.406000, -91.198207);
        AllZones[9] = new LatLng(30.411968, -91.194369);
        AllZones[10] = new LatLng(30.412025, -91.188999);
        AllZones[11] = new LatLng(30.413309, -91.189574);
        AllZones[12] = new LatLng(30.417964, -91.192645);
        AllZones[13] = new LatLng(30.406102, -91.182273);
        AllZones[14] = new LatLng(30.420241, -91.178700);
        AllZones[15] = new LatLng(30.411003, -91.170437);
        AllZones[16] = new LatLng(30.411336, -91.170117);
        AllZones[17] = new LatLng(30.412904, -91.171040);
        AllZones[18] = new LatLng(30.412709, -91.169194);
        AllZones[19] = new LatLng(30.417235, -91.170541);
        AllZones[20] = new LatLng(30.418429, -91.171069);

    }

    public void dropZone1Pin() {//Commuter Pins

        createZone1Coordinates();
        for (int i = 0; i < Zone1.length; i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone1[i].latitude, Zone1[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

    }

    public void dropZone2Pin() { //Greek Pins

        createZone2Coordinates();
        for (int i = 0; i < Zone2.length; i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone2[i].latitude, Zone2[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

    }

    public void dropZone3Pin() { //Resident Pins

        createZone3Coordinates();
        for (int i = 0; i < Zone3.length; i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone3[i].latitude, Zone3[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

    }

    public void dropZone4Pin() { //Law Pins

        createZone4Coordinates();
        for (int i = 0; i < Zone4.length; i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(Zone4[i].latitude, Zone4[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

    }

    public void dropVisitorParking() { //Visitor Pins

        createVisitorParkingCoordinates();
        for (int i = 0; i < VisitorParking.length; i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(VisitorParking[i].latitude, VisitorParking[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

    }

    public void dropAllZoneParking() { //All Zone Pins

        createAllZoneCoordinates();
        for (int i = 0; i < AllZones.length; i++)
            mMap.addMarker(new MarkerOptions().position(new LatLng(AllZones[i].latitude, AllZones[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

    }

    public void removePins() {

        mMap.clear();
        if (locationMarkerOptions != null)
            marker = mMap.addMarker(locationMarkerOptions);

    }

    public void removeParkingPin(View view) {

        if(locationMarkerOptions != null){
            marker.remove();
            locationMarkerOptions = null;
            deleteFile("data");
            Toast.makeText(this, "You have removed your parking location.", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(this, "There is no Parking Pin to remove.", Toast.LENGTH_SHORT).show();

    }

    public void findClosestCommuterLot() {

        int index = findShortestDistance(Zone1);
        mMap.addMarker(new MarkerOptions().position(new LatLng(Zone1[index].latitude, Zone1[index].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

    }

    public void findClosestGreekLot() {

       int index = findShortestDistance(Zone2);
       mMap.addMarker(new MarkerOptions().position(new LatLng(Zone2[index].latitude, Zone2[index].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

    }

    public void findClosestResidentLot() {

       int index = findShortestDistance(Zone3);
       mMap.addMarker(new MarkerOptions().position(new LatLng(Zone3[index].latitude, Zone3[index].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

    }

    public void findClosestLawLot() {

        int index = findShortestDistance(Zone4);
        mMap.addMarker(new MarkerOptions().position(new LatLng(Zone4[index].latitude, Zone4[index].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

    }

    public void findClosestVisitorLot() {

        int index = findShortestDistance(VisitorParking);
        mMap.addMarker(new MarkerOptions().position(new LatLng(VisitorParking[index].latitude, VisitorParking[index].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

    }

    public void findClosestAllZonesLot() {

        int index = findShortestDistance(AllZones);
        mMap.addMarker(new MarkerOptions().position(new LatLng(AllZones[index].latitude, AllZones[index].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

    }

    public int findShortestDistance(LatLng[] zone) {

        int index = 0;
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);

        if (currentLocation == null)
            Toast.makeText(this, "Couldn't Connect!", Toast.LENGTH_SHORT).show();
        else {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            double distance = calculateDistance(zone[0].latitude, zone[0].longitude, latLng.latitude, latLng.longitude);
            for (int i = 1; i < zone.length ; i++) {
                double testingDistance = calculateDistance(zone[i].latitude, zone[i].longitude, latLng.latitude, latLng.longitude);
                if (testingDistance < distance) {
                    distance = testingDistance;
                    index = i;
                }
            }
        }
        return index;

    }

    public double calculateDistance(double zoneLat, double zoneLong, double latLngLat, double latLngLong){

        double testingDistance = 3959 * (2 * Math.atan2(Math.sqrt(((Math.sin((latLngLat - zoneLat) / 2)) * (Math.sin((latLngLat - zoneLat) / 2))
                                 + Math.cos(zoneLat) * Math.cos(latLngLat) * (Math.sin((latLngLong - zoneLong) / 2)) *
                                 (Math.sin((latLngLong - zoneLong) / 2)))), Math.sqrt(1 - ((Math.sin((latLngLat - zoneLat) / 2))
                                 * (Math.sin((latLngLat -zoneLat) / 2)) + Math.cos(zoneLat) * Math.cos(latLngLat) *
                                 (Math.sin((latLngLong - zoneLong) / 2)) * (Math.sin((latLngLong - zoneLong) / 2))))));

        return testingDistance;

    }

    @Override
    protected void onStart() {

        super.onStart();

        try {
            readDataFile();
        } catch (IOException e) {
        } catch (JSONException e) {
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            if(locationMarkerOptions != null) {
                createDataFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createDataFile() throws  IOException, JSONException {

        JSONArray data = new JSONArray();
        JSONObject currentLoc;
        currentLoc = new JSONObject();
        double latitude = locationMarkerOptions.getPosition().latitude;
        double longitude = locationMarkerOptions.getPosition().longitude;
        currentLoc.put("Latitude", latitude);
        currentLoc.put("Longitude", longitude);
        data.put(currentLoc);
        String text = data.toString();
        FileOutputStream fos = openFileOutput("data", MODE_PRIVATE);
        fos.write(text.getBytes());
        fos.close();

    }

    public void readDataFile() throws IOException, JSONException {

        FileInputStream fis = openFileInput("data");
        BufferedInputStream bis = new BufferedInputStream(fis);
        StringBuffer b = new StringBuffer();
        while(bis.available() != 0) {
            char c = (char) bis.read();
            b.append(c);
        }

        bis.close();
        fis.close();
        JSONArray data = new JSONArray(b.toString());
        for(int i = 0; i < data.length(); i++) {
            double latitude = data.getJSONObject(i).getDouble("Latitude");
            double longitude = data.getJSONObject(i).getDouble("Longitude");
            LatLng latLng = new LatLng(latitude, longitude);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(update);
            if (marker != null)
                marker.remove();

            locationMarkerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
            marker = mMap.addMarker(locationMarkerOptions);
        }

    }

}