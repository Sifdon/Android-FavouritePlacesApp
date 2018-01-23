package com.example.alicja.favouriteplacesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
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
    private ChildEventListener childEventListener;

    private List<com.example.alicja.favouriteplacesapp.Location> favouriteLocationsList;

    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "onCancelled: ", error.toException() );
            }
        });
    }

    public void goToMap(View view) {
        Intent intent1 = new Intent(this, PlacesActivity.class);
        startActivity(intent1);
    }

}

