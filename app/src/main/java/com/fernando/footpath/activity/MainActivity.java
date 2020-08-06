package com.fernando.footpath.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.facebook.login.LoginManager;
import com.fernando.footpath.R;
import com.fernando.footpath.anim.BottomNavigationBehavior;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.fragment.TrackFragment;
import com.fernando.footpath.service.LocationService;
import com.fernando.footpath.util.DataSingleton;
import com.fernando.footpath.util.MessageUtil;
import com.fernando.footpath.util.UserFirebase;
import com.fernando.footpath.util.Util;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BottomNavigationView bottonNavigationBar;
    private CardView cvPlaceSearch;

    private DataSingleton singleton = DataSingleton.getInstance();

    final Fragment fragmentExplore = new TrackFragment(false);
    final Fragment fragmentMyTracks = new TrackFragment(true);
    final FragmentManager fm = getSupportFragmentManager();

    private List<String> requiredPermission = new ArrayList<>();
    private boolean isFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {

            //toolbar
            Toolbar toolbar = findViewById(R.id.tb_options);
            toolbar.setTitle("");
            setSupportActionBar(toolbar);

            requiredPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
            requiredPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION);

            //add permission if android is 10
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requiredPermission.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }

            //get all the user data on firebase
            if (singleton.getCurrentUser() == null && Util.isNetworkAvailable(this))
                UserFirebase.getUserDataByFirebase();

            cvPlaceSearch = findViewById(R.id.cv_places);

            //bottom navigation
            bottonNavigationBar = findViewById(R.id.bottom_navigation_main);
            bottonNavigationBar.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            // attaching bottom sheet behaviour - hide / show on scroll
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottonNavigationBar.getLayoutParams();
            layoutParams.setBehavior(new BottomNavigationBehavior());

            fm.beginTransaction().add(R.id.frame_layout_main, fragmentMyTracks).hide(fragmentMyTracks).commit();
            fm.beginTransaction().add(R.id.frame_layout_main, fragmentExplore).commit();


            //maps search places
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            // Specify the types of place data to return.
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            // Set up a PlaceSelectionListener to handle the response.
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    try {
                        TrackFragment trackFragment = (TrackFragment) fm.findFragmentById(R.id.frame_layout_main);

                        trackFragment.fetchAllTracks(place.getLatLng().latitude, place.getLatLng().longitude);
                    } catch (Exception e) {
                        Log.e(TAG, "onPlaceSelected: " + e.getMessage());
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Log.i(TAG, "onError: " + status);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "onCreate :" + e.getMessage());
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_explore:
                    switchFragment(fragmentMyTracks, fragmentExplore);
                    singleton.setMyTrackScreen(false);
                    cvPlaceSearch.setVisibility(View.VISIBLE);
                    return true;

                case R.id.navigation_record:
                    //permission validate first
                    if (Util.validatePermission(requiredPermission, MainActivity.this, 1)) {
                        startActivity(new Intent(MainActivity.this, RecordActivity.class));
                        return true;
                    }
                    return false;

                case R.id.navigation_my_tracks:
                    switchFragment(fragmentExplore, fragmentMyTracks);
                    singleton.setMyTrackScreen(true);
                    cvPlaceSearch.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }
    };

    private void switchFragment(Fragment hide, Fragment show) {
        fm.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .hide(hide).show(show).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;

            case R.id.menu_signout:
                userLogout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userLogout() {
        try {
            ConfigFirebase.getFirebaseAuth().signOut();
            LoginManager.getInstance().logOut();

            singleton.setCurrentUser(null);

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } catch (Exception e) {
            MessageUtil.toastMessage(this, 0, e.getMessage());
            Log.e(TAG, "userLogout :" + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (singleton.isMyTrackScreen())
            bottonNavigationBar.setSelectedItemId(R.id.navigation_my_tracks);
        else
            bottonNavigationBar.setSelectedItemId(R.id.navigation_explore);

        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                if (!isFirstTime && Util.isMyServiceRunning(getApplicationContext(), LocationService.class)) {
                    Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                    intent.putExtra("running", true);

                    startActivity(intent);
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Set Sign out to RED
        int positionOfMenuItem = 1;
        MenuItem item = menu.getItem(positionOfMenuItem);
        SpannableString s = new SpannableString(getString(R.string.signout));
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        item.setTitle(s);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isDenied = false;

        for (int resultPermission : grantResults) {
            if (resultPermission == PackageManager.PERMISSION_DENIED) {
                MessageUtil.createBasicDialog(this, R.string.denied_permission_gps);
                isDenied = true;
            }
        }

        if (!isDenied) {
            isFirstTime = true;
            startActivity(new Intent(MainActivity.this, RecordActivity.class));
        }
    }
}