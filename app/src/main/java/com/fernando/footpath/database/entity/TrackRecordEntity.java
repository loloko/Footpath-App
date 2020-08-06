package com.fernando.footpath.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.model.TrackModel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity
public class TrackRecordEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "track_id")
    private String trackId;

    @ColumnInfo(name = "user_email")
    private String userEmail;
    private String title;
    private String description;
    private String location;
    private double distance;
    private double latitude;
    private double longitude;
    @ColumnInfo(name = "activity_type")
    private int activityType;
    @ColumnInfo(name = "track_type")
    private int trackType;
    private int difficulty;
    private long time;
    @ColumnInfo(name = "owner_id")
    private String ownerId;
    @ColumnInfo(name = "owner_name")
    private String ownerName;
    private String created;
    private int open;

    @ColumnInfo(name = "ready_upload")
    private int readyToUpload;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getLocation() {
        return location;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public int getTrackType() {
        return trackType;
    }

    public void setTrackType(int trackType) {
        this.trackType = trackType;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
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

    public void setLocation(String location) {
        if (this.location == null || this.location.isEmpty())
            this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if (this.latitude == 0)
            this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if (this.longitude == 0)
            this.longitude = longitude;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getReadyToUpload() {
        return readyToUpload;
    }

    public void setReadyToUpload(int readyToUpload) {
        this.readyToUpload = readyToUpload;
    }

    public TrackRecordEntity getNewtrack() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.UK);

        setUserEmail(ConfigFirebase.getFirebaseAuth().getCurrentUser().getEmail());
        setCreated(sdf.format(new Date()));
        setOpen(1);

        return this;
    }

    public TrackRecordEntity convertModelToEntity(TrackModel model) {

        this.trackId = model.getId();
        this.userEmail = "";
        this.title = model.getTitle();
        this.description = model.getDescription();
        this.location = model.getLocation();
        this.distance = model.getDistance();
        this.latitude = model.getLatitude();
        this.longitude = model.getLongitude();
        this.activityType = model.getActivityType();

        this.trackType = model.getTrackType();
        this.difficulty = model.getDifficulty();
        this.time = model.getTime();
        this.ownerId = model.getOwnerId();
        this.ownerName = model.getOwnerName();

        return this;
    }
}
