package com.fernando.footpath.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.room.Room;

import com.fernando.footpath.database.FootpathDataBase;
import com.fernando.footpath.database.entity.ImageEntity;

public class ImageRepository {
    private static final String TAG = "ImageRepository";


    private FootpathDataBase database;

    public ImageRepository(Context context) {
        database = FootpathDataBase.getDatabase(context);
    }

    public Long insert(final ImageEntity image) {
        try {
            return database.imageDao().insert(image);
        } catch (Exception e) {
            Log.e(TAG, "insert: " + e.getMessage());
            return 0l;
        }
    }

    public void delete(final ImageEntity image) {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    database.imageDao().delete(image);
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "delete: " + e.getMessage());
        }
    }


}
