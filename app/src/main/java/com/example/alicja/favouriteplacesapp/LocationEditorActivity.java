package com.example.alicja.favouriteplacesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LocationEditorActivity extends AppCompatActivity {

    private static final String TAG = "ProductEditorActivity";

    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText radiusEditText;
    private TextView latitudeValue;
    private TextView longitudeValue;

    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;

    private boolean isEditionMode;
    private String locationtId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_editor);

        nameEditText = (EditText) findViewById(R.id.edit_name);
        descriptionEditText = (EditText) findViewById(R.id.edit_description);
        radiusEditText = (EditText) findViewById(R.id.edit_radius);

        latitudeValue = (TextView) findViewById(R.id.latitude_value);
        longitudeValue = (TextView) findViewById(R.id.longitude_value);

        //setting up firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Places");

        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            //checking if editing exisitng item
            if (intent.getParcelableExtra("location") != null) {

                isEditionMode = true;
                Log.i(TAG, "onCreate: existing location");

                Location location = intent.getParcelableExtra("location");
                locationtId = location.getId();
                Log.i(TAG, "onCreate: location id:" + locationtId);

                //if it is an existing location - fill the editTexts
                setEditTextValues(location.getName(), location.getDescription(), location.getRadius() + "");
                //and latitude&longitude with this locations data
                setLatLongValues(location.getLatitude(), location.getLongitude());

            } else if ((intent.getDoubleExtra("latitude", -1) != -1) &&
                    (intent.getDoubleExtra("longitude", -1) != -1)) {
                isEditionMode = false;
                //adding new favourite location based on current location - set latitude&longitude to current location
                setLatLongValues(intent.getDoubleExtra("latitude", -1), intent.getDoubleExtra("longitude", -1));
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!isEditionMode) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                deleteProduct();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String locationname = nameEditText.getText().toString().trim();
        String locationDescription = descriptionEditText.getText().toString().trim();
        String locationRadius = radiusEditText.getText().toString().trim();
        String latitude = latitudeValue.getText().toString().trim();
        String longitude = longitudeValue.getText().toString().trim();

        //return if one of fields is empty
        if (TextUtils.isEmpty(locationname) || TextUtils.isEmpty(locationDescription) || TextUtils.isEmpty(locationRadius)) {
            return;
        }

        if (isEditionMode) {

            //update fields of current product
            databaseReference.child(locationtId).child("name").setValue(locationname);
            databaseReference.child(locationtId).child("description").setValue(Float.parseFloat(locationDescription));
            databaseReference.child(locationtId).child("radius").setValue(Integer.parseInt(locationRadius));
//            databaseReference.child(locationtId).child("latitude").setValue(Double.parseDouble(latitude));
//            databaseReference.child(locationtId).child("longitude").setValue(Double.parseDouble(longitude));

        } else {
            //new location ID from firebase database
            String newLocationId = databaseReference.push().getKey();

            //create and save new location
            Location newLocation = new Location(newLocationId, locationname, locationDescription,
                    Double.parseDouble(latitude), Double.parseDouble(longitude), Integer.parseInt(locationRadius));

            databaseReference.child(newLocationId).setValue(newLocation);
        }

    }

    private void setEditTextValues(String name, String description, String radius) {

        nameEditText.setText(name);
        descriptionEditText.setText(description);
        radiusEditText.setText(radius);

    }

    private void setLatLongValues(double latitude, double longitude) {

        latitudeValue.setText(latitude + "");
        longitudeValue.setText(longitude + "");
    }


    private void deleteProduct() {
        if (locationtId == null) {
            return;
        }

        databaseReference.child(locationtId).removeValue();
    }
}
