package com.fernando.footpath.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

import com.fernando.footpath.database.entity.ImageEntity;

@Dao
public interface ImageDao {

    @Insert
    Long insert(ImageEntity image);

    @Delete
    void delete(ImageEntity image);

}
