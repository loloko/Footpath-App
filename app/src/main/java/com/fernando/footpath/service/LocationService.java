package com.fernando.footpath.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.fernando.footpath.R;
import com.fernando.footpath.activity.MainActivity;
import com.fernando.footpath.activity.RecordActivity;
import com.fernando.footpath.database.entity.LongLatEntity;
import com.fernando.footpath.database.entity.TrackRecordEntity;
import com.fernando.footpath.database.repository.LongLatRepository;
import com.fernando.footpath.database.repository.TrackRecordRepository;
import com.fernando.footpath.util.DataSingleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service {
    private static final String TAG = "LocationService";

    private final LocationServiceBinder binder = new LocationServiceBinder();

    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    private final int LOCATION_INTERVAL = 1000;
    private final int LOCATION_DISTANCE = 25;

    private LongLatRepository lngLatRepository;

    private List<LongLatEntity> locationList = new ArrayList<>();
    private List<LongLatEntity> locationListBackup = new ArrayList<>();

    private CountDownTimer cdt = null;
    private long seconds;
    private boolean didSaveTime;

    private long recordTrackId;
    private Location lastLocation;

    private String address = "";
    private LongLatEntity firstLocation;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            try {

                if (lngLatRepository != null) {
                    double distance = 0;

                    if (lastLocation != null)
                        distance = lastLocation.distanceTo(location);

                    //get address, just happen once
                    if (address == null || address.isEmpty())
                        address = getLocationNameByCoordinates(location.getLatitude(), location.getLongitude());

                    LongLatEntity loc = new LongLatEntity(recordTrackId, location.getLatitude(), location.getLongitude(), location.getAltitude(), distance);

                    locationList.add(loc);
                    locationListBackup.add(loc);

                    lngLatRepository.insert(loc);

                    lastLocation = location;

                    //get the first location, so it will be the main location for the track
                    if (firstLocation == null)
                        firstLocation = loc;
                }

            } catch (Exception e) {
                Log.e(TAG, "onLocationChanged: " + e.getMessage());
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }

    private String getLocationNameByCoordinates(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.ENGLISH);
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 10);
            if (addresses != null && addresses.size() > 0) {
                for (Address adr : addresses) {
                    if (adr.getLocality() != null && adr.getLocality().length() > 0) {

                        return adr.getLocality() + " - " + adr.getAdminArea() + " - " + adr.getCountryName();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getLocationNameByCoordinates: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {

        Log.d(TAG, "onCreate: LocationService");

        lngLatRepository = new LongLatRepository(getApplicationContext());

        cdt = new CountDownTimer(8000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                seconds++;
            }

            @Override
            public void onFinish() {
                cdt.start();
            }
        };

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cdt.cancel();
        stopForeground(true);

        //in case the service crash for any reason, it will save the time in the DB
        if (!didSaveTime && recordTrackId > 0)
            saveTime(false);


        stopSelf();

        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
                lngLatRepository = null;
            } catch (Exception e) {
                Log.e(TAG, "onDestroy: " + e.getMessage());
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    private void saveTime(final boolean destroy) {
        try {
            didSaveTime = true;

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    //get track
                    TrackRecordRepository repository = new TrackRecordRepository(getApplicationContext());
                    TrackRecordEntity track = repository.getTrackById(DataSingleton.getInstance().getRecordTrackId().intValue());

                    //update track com o tempo
                    if (track.getTime() < seconds) {
                        track.setTime(seconds);
                        track.setLocation(address);

                        track.setLatitude(firstLocation.getLatitude());
                        track.setLongitude(firstLocation.getLongitude());

                        repository.update(track);
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void id) {
                    super.onPostExecute(id);
                    if (destroy)
                        onDestroy();

                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "saveTime: " + e.getMessage());
        }
    }

    public void stopTracking() {
        mLocationManager.removeUpdates(mLocationListener);

        saveTime(false);

        stopForeground(true);
        cdt.cancel();
    }

    //method used from RecordActivity to get all the locations the GPS got, when got the information
    //clean and add more
    public List<LongLatEntity> getLocationList() {
        List<LongLatEntity> list = locationList;

        locationList = new ArrayList<>();

        return list;
    }

    //call when the service is running, the user close the app and open again to reload all the screen;
    public void recoveryLocationList() {
        locationList = locationListBackup;
    }

    public void setLocationList(List<LongLatEntity> list) {
        locationListBackup = list;
    }

    public void finishTracking() {
        try {
            saveTime(true);

        } catch (Exception e) {
            Log.e(TAG, "finishTracking: " + e.getMessage());
        }
    }

    public void startTracking(long time) {
        initializeLocationManager();

        startForeground(12345678, getNotification());


        cdt.start();
        recordTrackId = DataSingleton.getInstance().getRecordTrackId();

        seconds = time;
        didSaveTime = false;

        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "SecurityException :", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "IllegalArgumentException :" + ex.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification() {

        NotificationChannel channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        channel.setSound(null, null);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_01").setAutoCancel(true);
        builder.setContentTitle(getResources().getString(R.string.app_name)).setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText(getResources().getString(R.string.background_running));
        builder.setDefaults(Notification.DEFAULT_ALL);

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //builder.addAction(new NotificationCompat.Action(R.drawable.ic_bookmark_24dp, getResources().getString(R.string.open), pIntent));
        builder.setContentIntent(pIntent);

        return builder.build();
    }

    public long getSeconds() {
        return seconds;
    }

    public class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }
}