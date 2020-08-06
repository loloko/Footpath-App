package com.fernando.footpath.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fernando.footpath.database.entity.TrackRecordEntity;

@Dao
public interface TrackRecordDao {

    @Insert
    Long insert(TrackRecordEntity trackRecordEntity);

    @Query("SELECT * FROM TrackRecordEntity WHERE id =:taskId")
    TrackRecordEntity getTrackById(int taskId);

    @Update
    void update(TrackRecordEntity trackRecordEntity);

    @Delete
    void delete(TrackRecordEntity trackRecordEntity);

}
