package com.example.alicja.favouriteplacesapp;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.alicja.favouriteplacesapp.utils.NotificationUtils;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitionsIS";

    public GeofenceIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences);

            // Send notification
            NotificationUtils.showNotification(getApplicationContext(), getNotificationTitle(geofenceTransition), geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        }
    }

    private String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences) {

        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            // Get location name for triggering geofence
            String locationTitle = geofence.getRequestId().substring(geofence.getRequestId().indexOf("***") + 3);
            triggeringGeofencesList.add(locationTitle);
        }

        String status = null;
        if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        else if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting ";
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }


    private String getNotificationTitle(int geofenceTransition) {

        String title = null;
        if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            title = "You entered one of your favourite locations! ";
        else if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            title = "You are no longer in the area of favourite location. ";
        return title;
    }

}
