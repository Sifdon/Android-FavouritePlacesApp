package com.example.alicja.favouriteplacesapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.alicja.favouriteplacesapp.utils.NotificationUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    private static final int PERMISSION_REQUEST_CODE = 802;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 408;

    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private android.location.Location lastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Marker currentLocationMarker;
    private Location currentLocation;


    //Firebase & Geofire
    private DatabaseReference firebaseLocationsDB;
    private GeoFire geoFire;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Show Current Location on the map
        showCurrentLocation();

        // Connect with Firebase
        firebaseLocationsDB = FirebaseDatabase.getInstance().getReference("Locations");
        geoFire = new GeoFire(firebaseLocationsDB);

    }

    private boolean checkLocationPermissionsGranted() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {

                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();

                    }
                }
                break;
        }
    }

    private void showCurrentLocation() {
        if (!checkLocationPermissionsGranted()) {
            //Runtime permission - request if the permission is not granted
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {

            if (checkPlayServices()) {

                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }
        }
    }

    private void displayLocation() {
        if (!checkLocationPermissionsGranted()) {
            return;
        }

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            final double latitude = lastLocation.getLatitude();
            final double longitude = lastLocation.getLongitude();

            //TODO - Adjust geofire to save Location objects or switch to Firebase Realtime DB without geofire
            // Save to Firebase DB
            geoFire.setLocation("CurrentLocation", new GeoLocation(latitude, longitude),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            // Once current location in saved - update marker
                            if(currentLocationMarker != null) {
                                currentLocationMarker.remove();
                            }

                            currentLocationMarker = mMap
                                    .addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                                    .title("Current Location"));

                            // Adjust camera
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
                        }
                    });



            Log.d(TAG, String.format("displayLocation: location changed to %f / %f ", latitude, longitude));
        } else {
            Log.d(TAG, "displayLocation: Cannot get location");
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this, "Device not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // TODO - Remove dummy favourite place and add list of real favourite places
        // Add dummy favourite place to check geofence

        LatLng favouritePlace = new LatLng(38.0000, 102.0000);
        Log.d(TAG, "onMapReady: " + favouritePlace.latitude + " / " + favouritePlace.longitude);
        mMap.addCircle(new CircleOptions()
        .center(favouritePlace)
        .radius(500)
        .strokeColor(getColor(R.color.favouriteMarkerStrokeColor))
        .fillColor(getColor(R.color.favouriteMarkerFillColor))
        .strokeWidth(5.0f));

        Marker marker = mMap.addMarker(new MarkerOptions().position(favouritePlace).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_beenhere_black_24dp))
                .title("FavouritePlace"));

        // Add geoquery to this location
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(favouritePlace.latitude, favouritePlace.longitude), 0.5f);

        // Add listeners for managing notifications
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                NotificationUtils.showNotification(getApplicationContext(), location.toString());
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

                Log.d(TAG, "onGeoQueryError: " + error.getMessage());

            }
        });





    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        setLocationUpdates();
    }

    private void setLocationUpdates() {
        if (!checkLocationPermissionsGranted()) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(android.location.Location location) {

        lastLocation = location;
        displayLocation();

    }
}
