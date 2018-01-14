package com.example.alicja.favouriteplacesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class PlacesActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        // FAB for adding new favourite place
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addPlaceBtn);
        fab.setOnClickListener(
                (view) -> {
                    Intent intent = new Intent(PlacesActivity.this, LocationEditorActivity.class);
                    startActivity(intent);
                }
        );
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}
