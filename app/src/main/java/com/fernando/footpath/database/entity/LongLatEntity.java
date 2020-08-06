package com.fernando.footpath.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(indices = {@Index("track_id")}, foreignKeys = @ForeignKey(entity = TrackRecordEntity.class, parentColumns = "id", childColumns = "track_id", onDelete = ForeignKey.CASCADE))
public class LongLatEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "track_id")
    private long trackId;
    private double latitude;
    private double longitude;
    private double elevation;
    private double distance;

    public LongLatEntity(long trackId, double latitude, double longitude, double elevation, double distance) {
        this.trackId = trackId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.distance = distance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongLatEntity that = (LongLatEntity) o;
        return trackId == that.trackId &&
                id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trackId);
    }
}
