package com.example.alicja.favouriteplacesapp;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;


public class LocationAdapter extends ArrayAdapter<Location> {


    public LocationAdapter(Context context, int resource, List<Location> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.location_list_item, parent, false);
        }

        TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
        TextView summaryTextview = (TextView) convertView.findViewById(R.id.summary);

        Location location = getItem(position);

        // filling name:
        nameTextView.setText(location.getName());

        // filling details:
        summaryTextview.setText(location.getDescription());

        return convertView;
    }


}
