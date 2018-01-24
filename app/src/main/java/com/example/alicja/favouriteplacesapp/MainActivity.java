package com.example.alicja.favouriteplacesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;

    private List<com.example.alicja.favouriteplacesapp.Location> favouriteLocationsList;

    private ListView locationListView;
    private LocationAdapter locationAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize message ListView
        locationListView = (ListView) findViewById(R.id.locationListView);

        //setting up firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Places");

        //add listener to respond to changes in firebase DB with favourite locations and update list
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                favouriteLocationsList = new ArrayList<>();
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    Location location = noteDataSnapshot.getValue(Location.class);
                    favouriteLocationsList.add(location);
                }

                Log.d(TAG, "onDataChange: " + favouriteLocationsList.size());
                setListViewData();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "onCancelled: ", error.toException() );
            }
        });
    }

    public void goToMap(View view) {
        Intent intent1 = new Intent(this, PlacesActivity.class);

        //pass the list of favourite places to placesActivity
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("favouritePlaces", (ArrayList<Location>)favouriteLocationsList);
        intent1.putExtras(bundle);
        startActivity(intent1);
    }

    public void setListViewData(){
        locationAdapter = new LocationAdapter(this, R.layout.location_list_item, favouriteLocationsList);
        locationListView.setAdapter(locationAdapter);
    }

}

