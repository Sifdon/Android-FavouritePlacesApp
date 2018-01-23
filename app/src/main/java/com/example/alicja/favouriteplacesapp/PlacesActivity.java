package com.example.alicja.favouriteplacesapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class PlacesActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "PlacesActivity";

    // MAP
    private GoogleMap mMap;

    // LOCATION
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Location lastLocation;

    // LOCATION CONSTANTS
    private long UPDATE_INTERVAL = 10000;
    private long FASTEST_INTERVAL = 2000;
    private static final int PERMISSION_REQUEST_CODE = 802;

    // FIREBASE
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private ChildEventListener childEventListener;

    // LIST WITH FAVOURITE LOCATIONS FOR GEOFENCE
    private List<com.example.alicja.favouriteplacesapp.Location> favouritePlacesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        // Getting list of favourite places passed from MainActivity
        favouritePlacesList = new ArrayList<>();
        Intent intent = getIntent();
        if(intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            if(bundle.getParcelableArrayList("favouritePlaces") != null) {
                favouritePlacesList = bundle.getParcelableArrayList("favouritePlaces");
                Log.d(TAG, "onCreate: " + favouritePlacesList.size());
            }
        }


        // FAB for adding new favourite place
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addPlaceBtn);
        fab.setOnClickListener(
                (view) -> {
                    Intent intentToEditor = new Intent(PlacesActivity.this, LocationEditorActivity.class);
                    intentToEditor.putExtra("latitude", lastLocation.getLatitude());
                    intentToEditor.putExtra("longitude", lastLocation.getLongitude());
                    startActivity(intentToEditor);
                }
        );

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create instance of FusedLocationProviderClient
        mFusedLocationClient = getFusedLocationProviderClient(this);

        // Start location updates
        startLocationUpdates();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(checkPermission()) {
            googleMap.setMyLocationEnabled(true);
        }
        // Get last location
        getLastKnownLocation();
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    public void getLastKnownLocation() {
        if (checkPermission()) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            lastLocation = location;
                            onLocationChanged(location);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

        if (checkPermission()) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            // do work here
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());
        }
    }

    public void onLocationChanged(Location location) {

        // Update last known location
        lastLocation = location;

        // LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        showCurrentLocation(latLng);

    }

    public void showCurrentLocation(LatLng currentLatLng) {
        if(mMap == null) return;
        // Center camera on current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15.0f));
    }
}
