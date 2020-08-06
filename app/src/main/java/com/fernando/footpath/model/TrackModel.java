package com.fernando.footpath.model;

import android.util.Log;

import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.database.entity.LongLatEntity;
import com.fernando.footpath.database.entity.TrackWithLocation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrackModel implements Serializable {
    private static final String TAG = "TrackModel";

    private String id;
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private String location;
    private Long time;
    private String ownerId;
    private String ownerName;
    private int difficulty;
    private float rate;
    private int rateCount;
    private double elevation;
    private double distance;
    private int trackType;
    private int activityType;
    private List<ImageModel> imageList;
    private List<LatLngModel> latLngs;
    @Exclude
    private Long trackDatabaseId;


    //Road Type enums
    @Exclude
    public static int POINT_TO_POINT = 1, LOOP = 2;
    @Exclude
    public static int EASY = 1, MODERATE = 2, DIFFICULT = 3;

    public TrackModel() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LatLngModel> getLatLngs() {
        return latLngs;
    }

    public void setLatLngs(List<LatLngModel> latLngs) {
        this.latLngs = latLngs;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }


    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public int getRateCount() {
        return rateCount;
    }

    public void setRateCount(int rateCount) {
        this.rateCount = rateCount;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTrackType() {
        return trackType;
    }

    public void setTrackType(int trackType) {
        this.trackType = trackType;
    }

    public List<ImageModel> getImageList() {
        if (imageList == null)
            return new ArrayList<>();
        else
            return imageList;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setImageList(List<ImageModel> imageList) {
        this.imageList = imageList;
    }

    public void save() {
        try {
            DatabaseReference track = ConfigFirebase.getFirebaseDatabase().child("track").child(getId());

            track.setValue(this);

        } catch (Exception e) {
            Log.e(TAG, "save: " + e.getMessage());
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void addImage(ImageModel image) {
        imageList = getImageList();
        imageList.add(image);
    }

    public TrackModel setEntityToModel(TrackWithLocation t) {
        try {

            this.id = UUID.randomUUID().toString();
            this.title = t.track.getTitle();
            this.description = t.track.getDescription();
            this.latitude = t.track.getLatitude();
            this.longitude = t.track.getLongitude();
            this.location = t.track.getLocation();
            this.time = t.track.getTime();
            this.ownerId = t.track.getOwnerId();
            this.ownerName = t.track.getOwnerName();
            this.difficulty = t.track.getDifficulty();
            this.rate = 0;
            this.rateCount = 0;
            this.distance = t.track.getDistance();
            this.trackType = t.track.getTrackType();
            this.activityType = t.track.getActivityType();

            latLngs = new ArrayList<>();

            for (LongLatEntity ll : t.longLatList)
                latLngs.add(new LatLngModel(ll.getLatitude(), ll.getLongitude()));

            return this;

        } catch (Exception e) {
            Log.e(TAG, "setTrackReadyToSend: " + e.getMessage());
        }

        return new TrackModel();
    }

    public Long getTrackDatabaseId() {
        return trackDatabaseId;
    }

    public void setTrackDatabaseId(Long trackDatabaseId) {
        this.trackDatabaseId = trackDatabaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackModel that = (TrackModel) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}