package com.fernando.footpath.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fernando.footpath.R;
import com.fernando.footpath.database.entity.LongLatEntity;
import com.fernando.footpath.database.entity.TrackRecordEntity;
import com.fernando.footpath.database.entity.TrackWithLocation;
import com.fernando.footpath.database.repository.TrackRecordRepository;
import com.fernando.footpath.model.TypeModel;
import com.fernando.footpath.service.LocationService;
import com.fernando.footpath.util.DataSingleton;
import com.fernando.footpath.util.MessageUtil;
import com.fernando.footpath.util.Util;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class RecordActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "RecordActivity";

    private TextView tvDistance, tvPace, tvTimer;
    private GoogleMap mMap;
    private MapView mapView;
    private Button btStart, btStop, btResume, btFinish;

    private Intent intentLocation;
    private LocationService gpsService;
    private TrackRecordRepository repository;
    private double distance;

    private boolean isRunning;
    private DataSingleton singleton = DataSingleton.getInstance();

    //Timer
    private CountDownTimer cdTimer = null;
    private long seconds = 0;
    private int counter = 0;

    private LatLng lastLocationCollected = null;

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        //Hide ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        repository = new TrackRecordRepository(this);
        tvDistance = findViewById(R.id.tv_distance);
        tvPace = findViewById(R.id.tv_pace);
        tvTimer = findViewById(R.id.tv_timer);

        //bottom buttons
        btStart = findViewById(R.id.bt_start);
        btStop = findViewById(R.id.bt_stop);
        btResume = findViewById(R.id.bt_resume);
        btFinish = findViewById(R.id.bt_finish);


        //bind to the service so can get methods from the Service
        intentLocation = new Intent(this, LocationService.class);
        bindService(intentLocation, serviceConnection, Context.BIND_AUTO_CREATE);

        init();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isRunning = bundle.getBoolean("running");

            if (isRunning) {
                btStartClick(null);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        seconds = gpsService.getSeconds();
                        //will recovery all the locations so the app can update the MapUI
                        gpsService.recoveryLocationList();

                        updateMapUIByService();

                        cdTimer.start();
                    }
                }, 2000);
            } else
                startService(intentLocation);
        } else
            startService(intentLocation);

        //map init
        mapView = findViewById(R.id.map_record);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        client = LocationServices.getFusedLocationProviderClient(this);

        //check if GPS is enable and get user location to zoom in
        if (Util.isGpsEnable(this))
            getLastLocation();

        //find if have a record track open
        if (!isRunning)
            verifyIfHaveTrackOpen();


    }

    public void btTrackTypeClick(View view) {
        MessageUtil.createDialogRecycler(this, (TextView) view, TypeModel.getTrackTypes(this), true);
    }

    public void btExerciseTypeClick(View view) {
        MessageUtil.createDialogRecycler(this, (TextView) view, TypeModel.getExerciseTypes(this), false);
    }

    //get last Location and set the map to zoom  it
    private void getLastLocation() {
        try {
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "getLastLocation :" + e.getMessage());
        }
    }

    private void init() {
        //Timer, run every second
        cdTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                seconds++;
                tvTimer.setText(DateUtils.formatElapsedTime(seconds));
            }

            @Override
            public void onFinish() {
                updateMapUIByService();
                cdTimer.start();
            }
        };

    }

    @SuppressLint("StaticFieldLeak")
    private void startRecord() {
        try {
            if (DataSingleton.getInstance().getRecordTrackId() == null) {
                //Insert a new RecordTrack, only one track can be open for each time
                new AsyncTask<Void, Void, Long>() {
                    @Override
                    protected Long doInBackground(Void... voids) {
                        return repository.insert(new TrackRecordEntity().getNewtrack());
                    }

                    @Override
                    protected void onPostExecute(Long id) {
                        super.onPostExecute(id);

                        singleton.setRecordTrackId(id);

                        //call gpsService.startTracking();
                        cdTimer.start();
                        gpsService.startTracking(0);
                    }
                }.execute();
            } else {
                cdTimer.start();
                gpsService.startTracking(0);
            }

            updateMapUIByService();

        } catch (Exception e) {
            Log.e(TAG, "startRecord :" + e.getMessage());
        }
    }

    //TODO
//    private void countDownStart() {
//        try {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//            View layout = LayoutInflater.from(this).inflate(R.layout.dialog_count_down, null);
//            final TextView text = layout.findViewById(R.id.tvCounter);
//
//            builder.setView(layout);
//            builder.setCancelable(false);
//
//            final AlertDialog alert = builder.create();
//            alert.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//
//            new CountDownTimer(4000, 1000) {
//                @Override
//                public void onTick(long timeRemaining) {
//                    if (timeRemaining / 1000 >= 1)
//                        text.setText(String.valueOf(timeRemaining / 1000));
//                    else {
//                        text.setText(R.string.go);
//                        text.setTextSize(50);
//                        text.setTextColor(Color.RED);
//                    }
//                }
//
//                @Override
//                public void onFinish() {
//                    alert.dismiss();
//                    cdTimer.start();
//                    gpsService.startTracking(0);
//                }
//            }.start();
//
//            alert.show();
//        } catch (Exception e) {
//            Log.e(TAG, "countDownStart :" + e.getMessage());
//        }
//    }

    //get the track who is open(started)
    @SuppressLint("StaticFieldLeak")
    private void verifyIfHaveTrackOpen() {
        try {

            AsyncTask<Void, Void, TrackWithLocation> task = new AsyncTask<Void, Void, TrackWithLocation>() {
                @Override
                protected TrackWithLocation doInBackground(Void... voids) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return repository.getTrackWithLocationOpen();

                }

                @Override
                protected void onPostExecute(TrackWithLocation track) {
                    super.onPostExecute(track);

                    if (track != null) {

                        singleton.setRecordTrackId(track.track.getId());
                        seconds = track.track.getTime();

                        tvTimer.setText(DateUtils.formatElapsedTime(seconds));

                        updateMapUI(track.longLatList);

                        gpsService.setLocationList(track.longLatList);

                        setUIToContinueTrack();
                    }
                }
            };

            task.execute();

        } catch (Exception e) {
            Log.e(TAG, "verifyIfHaveOpenTrack :" + e.getMessage());
        }
    }

    private void setUIToContinueTrack() {
        Util.animation(this, R.anim.fade_out, btStart, View.GONE);
        Util.animation(this, R.anim.fade_in, btResume, View.VISIBLE);
        Util.animation(this, R.anim.fade_in, btFinish, View.VISIBLE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setIndoorEnabled(true);
        mMap.setMyLocationEnabled(true);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);


        distance = 0;
    }

    private void updateMapUIByService() {
        try {

            updateMapUI(gpsService.getLocationList());

        } catch (Exception e) {
            Log.e(TAG, "updateMapByService :" + e.getMessage());
        }
    }

    private synchronized void updateMapUI(List<LongLatEntity> locationList) {
        try {
            if (locationList == null || locationList.isEmpty())
                return;

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(5);

            if (lastLocationCollected != null)
                polylineOptions.add(lastLocationCollected);


            LatLng latLng = null;

            for (LongLatEntity lt : locationList) {

                counter++;
                distance += lt.getDistance();

                latLng = new LatLng(lt.getLatitude(), lt.getLongitude());
                polylineOptions.add(latLng);

                if (counter == 1)
                    mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_start)).title(getResources().getString(R.string.start)));
            }

            lastLocationCollected = latLng;

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            mMap.addPolyline(polylineOptions);

            tvDistance.setText(Util.formatMeterToKm(distance));
            tvPace.setText(Util.getPace(seconds, distance));

        } catch (Exception e) {
            Log.e(TAG, "updateMapUI :" + e.getMessage());
        }
    }

    public void btStartClick(View view) {
        //check if GPS is enable
        if (!Util.isGpsEnable(this))
            return;

        //check if battery saver is on
        if (Util.checkBatterySaverOn(this))
            return;

        isRunning = true;

        Util.animation(this, R.anim.fade_out, btStart, View.GONE);
        Util.animation(this, R.anim.fade_in, btStop, View.VISIBLE);

        if (view != null)
            startRecord();
    }

    public void btStopClick(View view) {
        isRunning = false;

        Util.animation(this, R.anim.fade_out, btStop, View.GONE);
        Util.animation(this, R.anim.fade_in, btResume, View.VISIBLE);
        Util.animation(this, R.anim.fade_in, btFinish, View.VISIBLE);

        cdTimer.cancel();
        gpsService.stopTracking();

        stopService(intentLocation);
    }

    public void btResumeClick(View view) {
        //check if GPS is enable
        if (!Util.isGpsEnable(this))
            return;

        //check if battery saver is on
        if (Util.checkBatterySaverOn(this))
            return;

        isRunning = true;

        Util.animation(this, R.anim.fade_out, btResume, View.GONE);
        Util.animation(this, R.anim.fade_out, btFinish, View.GONE);
        Util.animation(this, R.anim.fade_in, btStop, View.VISIBLE);

        cdTimer.start();
        gpsService.startTracking(seconds);
    }

    public void btFinishClick(View view) {
        gpsService.finishTracking();

        //start next activity - Save Track
        startActivity(new Intent(this, TrackSaveActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (ableToGoBack())
            super.onBackPressed();
    }

    private boolean ableToGoBack() {
        if (isRunning) {
            MessageUtil.snackBarMessage(tvPace, R.string.action_not_possible);
            return false;
        } else
            return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (ableToGoBack())
                finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRunning)
            updateMapUIByService();

        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        unbindService(serviceConnection);
        cdTimer.cancel();

        if (!isRunning)
            stopService(intentLocation);

        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //Service Bind
    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("LocationService"))
                gpsService = ((LocationService.LocationServiceBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("LocationService"))
                gpsService = null;
        }
    };

}