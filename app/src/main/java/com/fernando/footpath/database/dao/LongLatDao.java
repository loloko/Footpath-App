package com.fernando.footpath.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.fernando.footpath.database.entity.LongLatEntity;

import java.util.List;

@Dao
public interface LongLatDao {

    @Insert
    Long insertTask(LongLatEntity longLat);


    @Query("SELECT * FROM LongLatEntity WHERE track_id =:trackId ORDER BY id ")
    List<LongLatEntity> getAllByTrack(long trackId);

    @Delete
    void deleteLongLat(LongLatEntity longLat);

}
