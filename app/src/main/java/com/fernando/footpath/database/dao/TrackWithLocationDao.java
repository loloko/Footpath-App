package com.fernando.footpath.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.fernando.footpath.database.entity.TrackWithLocation;

import java.util.List;

@Dao
public interface TrackWithLocationDao {

    @Transaction
    @Query("SELECT * FROM TrackRecordEntity WHERE id = :id ")
    TrackWithLocation getTrackWithLocationById(Long id);

    @Transaction
    @Query("SELECT * FROM TrackRecordEntity WHERE open =1 limit 1 ")
    TrackWithLocation getTrackWithLocationOpen();

    @Transaction
    @Query("SELECT * FROM TrackRecordEntity WHERE ready_upload = :ready ")
    List<TrackWithLocation> getTracksSendServer(int ready);

    @Transaction
    @Query("SELECT * FROM TrackRecordEntity WHERE track_id is not null ")
    List<TrackWithLocation> getTracksOffline();
}
