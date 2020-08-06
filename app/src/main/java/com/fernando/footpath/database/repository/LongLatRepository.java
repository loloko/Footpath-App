package com.fernando.footpath.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.fernando.footpath.database.FootpathDataBase;
import com.fernando.footpath.database.entity.LongLatEntity;

import java.util.List;

public class LongLatRepository {
    private static final String TAG = "LongLatRepository";

    private FootpathDataBase database;

    public LongLatRepository(Context context) {
        database = FootpathDataBase.getDatabase(context);
    }

    public void insert(final LongLatEntity longLat) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    database.longLatDao().insertTask(longLat);
                } catch (Exception e) {
                    Log.e(TAG, "insert: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    public void delete(final LongLatEntity longLat) {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    database.longLatDao().deleteLongLat(longLat);
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "delete: " + e.getMessage());
        }
    }

    public List<LongLatEntity> getAllByTrack(long trackId) {
        try {
            return database.longLatDao().getAllByTrack(trackId);
        } catch (Exception e) {
            Log.e(TAG, "getAllByTrack: " + e.getMessage());
            return null;
        }
    }
}