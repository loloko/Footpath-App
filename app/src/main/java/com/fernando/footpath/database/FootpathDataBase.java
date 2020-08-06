package com.fernando.footpath.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.fernando.footpath.database.dao.ImageDao;
import com.fernando.footpath.database.dao.LongLatDao;
import com.fernando.footpath.database.dao.TrackRecordDao;
import com.fernando.footpath.database.dao.TrackWithLocationDao;
import com.fernando.footpath.database.entity.ImageEntity;
import com.fernando.footpath.database.entity.LongLatEntity;
import com.fernando.footpath.database.entity.TrackRecordEntity;

@Database(entities = {TrackRecordEntity.class, LongLatEntity.class, ImageEntity.class}, version = 1, exportSchema = false)
public abstract class FootpathDataBase extends RoomDatabase {

    public abstract TrackRecordDao trackRecordDao();

    public abstract TrackWithLocationDao trackWithLocationDao();

    public abstract LongLatDao longLatDao();

    public abstract ImageDao imageDao();

    private static FootpathDataBase INSTANCE;
    private String DB_NAME = "";

    public static FootpathDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FootpathDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FootpathDataBase.class, "db_footpath").build();
                }
            }
        }
        return INSTANCE;
    }
}
