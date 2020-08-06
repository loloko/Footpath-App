package com.fernando.footpath.util;

import com.fernando.footpath.database.entity.ImageEntity;
import com.fernando.footpath.database.entity.TrackRecordEntity;
import com.fernando.footpath.model.RateModel;
import com.fernando.footpath.model.TrackModel;
import com.fernando.footpath.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class DataSingleton {

    private static DataSingleton INSTANCE = null;
    private TrackModel track;
    private UserModel currentUser;
    private Long recordTrackId;
    private boolean isRecordPhoto;
    private boolean isEditing;
    private List<TrackModel> trackOffline = new ArrayList<>();
    private boolean isMyTrackScreen;

    private List<ImageEntity> imagesDatabase;

    private int trackType, activityType, difficult;

    private DataSingleton() {
    }

    public static DataSingleton getInstance() {
        if (INSTANCE == null) {
            synchronized (DataSingleton.class) {
                if (INSTANCE == null)
                    INSTANCE = new DataSingleton();

            }
        }

        return INSTANCE;
    }

    public int getTrackType() {
        if (trackType == 0)
            trackType = 1;

        return trackType;
    }

    public void setTrackType(int trackType) {
        this.trackType = trackType;
    }

    public int getActivityType() {
        if (this.activityType == 0)
            this.activityType = 2;

        return activityType;
    }

    public RateModel checkIfRated() {
        for (RateModel rate : currentUser.getRates())
            if (rate.getTrackId().equals(track.getId()))
                return rate;

        return null;
    }


    public void setActivityType(int exerciseType) {
        this.activityType = exerciseType;
    }

    public TrackModel getTrack() {
        return track;
    }


    public void setTrack(TrackModel track) {
        this.track = track;
    }

    public Long getRecordTrackId() {
        return recordTrackId;
    }

    public void setRecordTrackId(Long recordTrackId) {
        this.recordTrackId = recordTrackId;
    }

    public int getDifficult() {
        return difficult;
    }

    public void setDifficult(int difficult) {
        this.difficult = difficult;
    }

    public List<ImageEntity> getImagesDatabase() {
        if (imagesDatabase == null)
            imagesDatabase = new ArrayList<>();

        return imagesDatabase;
    }

    public void setImagesDatabase(List<ImageEntity> imagesDatabase) {
        this.imagesDatabase = imagesDatabase;
    }

    public boolean isRecordPhoto() {
        return isRecordPhoto;
    }

    public void setRecordPhoto(boolean recordPhoto) {
        isRecordPhoto = recordPhoto;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public List<TrackModel> getTrackOffline() {
        return trackOffline;
    }

    public UserModel getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserModel currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isMyTrackScreen() {
        return isMyTrackScreen;
    }

    public void setMyTrackScreen(boolean myTrackScreen) {
        isMyTrackScreen = myTrackScreen;
    }
}