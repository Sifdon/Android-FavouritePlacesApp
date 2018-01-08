package com.example.alicja.favouriteplacesapp.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.alicja.favouriteplacesapp.MapsActivity;
import com.example.alicja.favouriteplacesapp.R;

public class NotificationUtils {

    private static final String TAG = "NotificationUtils";

    private static final String NOTIFICATION_TITLE = "One of your favourite places is nearby!";

    private static final int NOTIFICATION_ID = 8285;
    private static final int PENDING_INTENT_ID = 4893;


    public static void showNotification(Context context, String message) {

        Log.i(TAG, "showNotification: getting the notification manager");
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Log.i(TAG, "showNotification: preparing notification");
        Notification.Builder notificationBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.ic_favorite_black_24dp)
                        .setColor(ContextCompat.getColor(context, R.color.favouriteMarkerStrokeColor))
                        .setContentTitle(NOTIFICATION_TITLE)
                        .setContentText(message)
                        .setContentIntent(getPendingIntent(context))
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true);


        Log.i(TAG, "showNotification: showing notification");
        mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }


    public static PendingIntent getPendingIntent(Context context) {

        //Intent opening the MapsActivity:
        Intent mapsActivityIntent = new Intent(context, MapsActivity.class);

        return PendingIntent.getActivity(
                context,
                PENDING_INTENT_ID,
                mapsActivityIntent,
                PendingIntent.FLAG_IMMUTABLE);
    }


}
