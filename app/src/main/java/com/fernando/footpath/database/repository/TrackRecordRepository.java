package com.fernando.footpath.database.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.fernando.footpath.database.FootpathDataBase;
import com.fernando.footpath.database.entity.TrackRecordEntity;
import com.fernando.footpath.database.entity.TrackWithLocation;

import java.util.List;

public class TrackRecordRepository {
    private static final String TAG = "TrackRecordRepository";

    private FootpathDataBase database;


    public TrackRecordRepository(Context context) {
        database = FootpathDataBase.getDatabase(context);
    }


    public Long insert(TrackRecordEntity track) {
        try {
            return database.trackRecordDao().insert(track);
        } catch (Exception e) {
            Log.e(TAG, "insert: " + e.getMessage());
            return 0l;
        }
    }

    public void update(final TrackRecordEntity track) {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    database.trackRecordDao().update(track);
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "update: " + e.getMessage());
        }
    }

    public void delete(final TrackRecordEntity track) {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    database.trackRecordDao().delete(track);
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            Log.e(TAG, "delete: " + e.getMessage());
        }
    }

    public TrackRecordEntity getTrackById(int id) {
        try {
            return database.trackRecordDao().getTrackById(id);
        } catch (Exception e) {
            Log.e(TAG, "getTrackById: " + e.getMessage());
            return null;
        }
    }

    public TrackWithLocation getTrackWithLocationOpen() {
        try {
            return database.trackWithLocationDao().getTrackWithLocationOpen();
        } catch (Exception e) {
            Log.e(TAG, "getTrackWithLocationOpen: " + e.getMessage());
            return null;
        }
    }

    public TrackWithLocation getTrackWithLocationById(Long id) {
        try {
            return database.trackWithLocationDao().getTrackWithLocationById(id);
        } catch (Exception e) {
            Log.e(TAG, "getTrackWithLocationById: " + e.getMessage());
            return null;
        }
    }

    public List<TrackWithLocation> getTracksSendServer(int ready) {
        try {
            return database.trackWithLocationDao().getTracksSendServer(ready);
        } catch (Exception e) {
            Log.e(TAG, "getTracksSendServer: " + e.getMessage());
            return null;
        }
    }

    public List<TrackWithLocation> getTracksOffline() {
        try {
            return database.trackWithLocationDao().getTracksOffline();
        } catch (Exception e) {
            Log.e(TAG, "getTracksOffline: " + e.getMessage());
            return null;
        }
    }
}