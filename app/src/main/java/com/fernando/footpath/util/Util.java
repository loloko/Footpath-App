package com.fernando.footpath.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fernando.footpath.R;
import com.fernando.footpath.database.entity.ImageEntity;
import com.fernando.footpath.database.entity.TrackRecordEntity;
import com.fernando.footpath.database.repository.ImageRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Util {
    private static final String TAG = "Util";

    public static boolean isGpsEnable(final Context c) {
        try {
            final LocationManager manager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);

            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setMessage(R.string.gps_disabled).setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                c.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();

                return false;
            } else
                return true;


        } catch (Exception e) {
            Log.e(TAG, "checkGPS: " + e.getMessage());
            return false;
        }
    }

    public static boolean checkBatterySaverOn(Activity activity) {
        try {
            PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            boolean isPowerSave = powerManager.isPowerSaveMode();

            //show message just if the user is with power saver mode ON
            if (isPowerSave)
                MessageUtil.createBasicDialog(activity, R.string.battery_saver_on);

            return isPowerSave;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("DefaultLocale")
    public static String formatMeterToKm(double distance) {
        return String.format("%.2f Km", distance / 1000);
    }

    public static boolean editTextValidation(Context c, EditText editText, int msg) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError(c.getResources().getString(R.string.required));
            editText.setHint(c.getResources().getString(msg));
            editText.setHintTextColor(Color.RED);
            return true;
        } else {
            return false;
        }
    }

    public static String getPace(long time, double distance) {
        if (distance > 0) {
            double x = (time / (distance / 1000));
            return DateUtils.formatElapsedTime(Math.round(x));
        } else
            return "0:00";
    }

    public static int getExerciseType(int difficult) {

        switch (difficult) {
            case 1:
                return R.string.walking;
            case 3:
                return R.string.hiking;
            default:
                return R.string.running;
        }
    }

    public static void getDifficultForTextView(int difficult, TextView textView, Context context) {

        switch (difficult) {
            case 1:
                textView.setText(R.string.easy);
                textView.setTextColor(ContextCompat.getColor(context, R.color.easy));
                break;
            case 2:
                textView.setText(R.string.moderate);
                textView.setTextColor(ContextCompat.getColor(context, R.color.moderate));
                break;
            case 3:
                textView.setText(R.string.difficult);
                textView.setTextColor(ContextCompat.getColor(context, R.color.difficult));
                break;
        }
    }

    public static int getTrackType(int type) {

        if (type == 2) {
            return R.string.loop;
        }
        return R.string.point_to_point;
    }

    public static boolean isMyServiceRunning(Context c, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);

        if (manager != null)
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
                if (serviceClass.getName().equals(service.service.getClassName()))
                    return true;


        return false;
    }

    public static boolean validatePermission(List<String> permissions, Activity activity, int requestCode) {

        List<String> listPermission = new ArrayList<String>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                listPermission.add(permission);
        }

        if (listPermission.isEmpty()) return true;

        String[] newPermissions = new String[listPermission.size()];
        listPermission.toArray(newPermissions);

        //request permission
        ActivityCompat.requestPermissions(activity, newPermissions, requestCode);
        return false;
    }

    public static void animation(Context c, int anim, View view, int visibility) {
        Animation animation = AnimationUtils.loadAnimation(c, anim);
        view.startAnimation(animation);
        view.setVisibility(visibility);
    }

    public static int fullScreenFlags() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    }

    public static int currentApiVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static void deleteImageLocal(String fileName) {
        try {
            File file = new File(fileName);

            if (file.exists())
                file.delete();
        } catch (Exception e) {
            Log.e(TAG, "deleteImageLocal: " + e.getMessage());
        }
    }

    public static String storeImage(Context c, Bitmap imageData, String fileName, long trackId, final ImageRepository repository) throws FileNotFoundException, IOException {

        File storageDir = new File(c.getFilesDir() + "/footpath/");

        if (!storageDir.exists())
            storageDir.mkdirs();

        File file = new File(storageDir.toString(), fileName + ".png");
        if (file.exists())
            file.delete();


        FileOutputStream out = new FileOutputStream(file);
        imageData.compress(Bitmap.CompressFormat.JPEG, 60, out);
        out.flush();
        out.close();

        //Insert image URL into the database
        final ImageEntity imageEntity = new ImageEntity(trackId, file.toString());
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                return repository.insert(imageEntity).intValue();
            }

            @Override
            protected void onPostExecute(Integer id) {
                super.onPostExecute(id);

                imageEntity.setId(id);

                if (DataSingleton.getInstance().isRecordPhoto())
                    DataSingleton.getInstance().getImagesDatabase().add(imageEntity);


            }
        }.execute();


        return file.toString();
    }

    public static boolean isNetworkAvailable(Context mContext) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = connectivityManager.getNetworkInfo(mNetwork);
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED))
                    return true;

            }

            MessageUtil.toastMessage(mContext, R.string.no_internet, null);
            return false;

        } catch (Exception e) {
            Log.e(TAG, "isNetworkAvailable: " + e.getMessage());
            return false;
        }
    }


}