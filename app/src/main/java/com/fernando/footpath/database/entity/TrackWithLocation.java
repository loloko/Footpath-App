package com.fernando.footpath.database.entity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Relation;

import java.util.List;

@Entity
public class TrackWithLocation {

    @Embedded
    public TrackRecordEntity track;

    @Relation(parentColumn = "id", entityColumn = "track_id", entity = ImageEntity.class)
    public List<ImageEntity> imageList;

    @Relation(parentColumn = "id", entityColumn = "track_id", entity = LongLatEntity.class)
    public List<LongLatEntity> longLatList;


}
