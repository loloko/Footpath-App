package com.fernando.footpath.database.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fernando.footpath.database.repository.ImageRepository;
import com.fernando.footpath.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@Entity(indices = {@Index("track_id")}, foreignKeys = @ForeignKey(entity = TrackRecordEntity.class, parentColumns = "id", childColumns = "track_id", onDelete = ForeignKey.CASCADE))
public class ImageEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "track_id")
    private long trackId;
    private String url;

    public ImageEntity(long trackId, String url) {
        this.trackId = trackId;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void downloadImage(final Context c, final ImageRepository repository) {

        Glide.with(c)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            Util.storeImage(c, resource, UUID.randomUUID().toString(), trackId, repository);
                        } catch (FileNotFoundException e) {
                            Log.e("ImageEntity", "FileNotFoundException: " + e.getMessage());
                        } catch (IOException e) {
                            Log.e("ImageEntity", "IOException: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageEntity that = (ImageEntity) o;
        return id == that.id &&
                Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url);
    }
}
