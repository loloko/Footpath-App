package com.fernando.footpath.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.fernando.footpath.R;
import com.fernando.footpath.model.LatLngModel;
import com.fernando.footpath.model.TrackModel;
import com.fernando.footpath.util.DataSingleton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            googleMap.setMyLocationEnabled(true);
            UiSettings uiSettings = googleMap.getUiSettings();
            uiSettings.setIndoorLevelPickerEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
            uiSettings.setMapToolbarEnabled(true);
            uiSettings.setCompassEnabled(true);
            uiSettings.setZoomControlsEnabled(true);


            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(5);

            int counter = 0;

            //current track
            TrackModel track = DataSingleton.getInstance().getTrack();

            for (LatLngModel ll : track.getLatLngs()) {
                counter++;

                LatLng latLng = new LatLng(ll.getLat(), ll.getLng());

                polylineOptions.add(latLng);

                //set the flags to start and end
                if (counter == 1)
                    googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_start)).title(getResources().getString(R.string.start)));
                else if (counter == Math.round(track.getLatLngs().size() / 2))
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                else if (counter == track.getLatLngs().size())
                    googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_finish)).title(getResources().getString(R.string.finish)));
            }

            googleMap.addPolyline(polylineOptions);

        } catch (Exception e) {
            Log.e(TAG, "onMapReady: " + e.getMessage());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}