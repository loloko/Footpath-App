package com.fernando.footpath.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.database.entity.ImageEntity;
import com.fernando.footpath.database.entity.TrackRecordEntity;
import com.fernando.footpath.database.entity.TrackWithLocation;
import com.fernando.footpath.database.repository.TrackRecordRepository;
import com.fernando.footpath.model.ImageModel;
import com.fernando.footpath.model.TrackModel;
import com.fernando.footpath.util.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

public class UploadTrackService extends Service {
    private static final String TAG = "UploadTrackService";

    private TrackRecordRepository repository;
    private TrackModel track;
    private TrackRecordEntity trackRecord;
    private List<ImageEntity> imageList;

    public UploadTrackService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new TrackRecordRepository(getApplicationContext());
        try {

            if (!Util.isNetworkAvailable(this)) {
                stopSelf();
                return;
            }


            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    List<TrackWithLocation> tracks = repository.getTracksSendServer(1);

                    if (tracks != null && !tracks.isEmpty())
                        for (TrackWithLocation trackWithLocation : tracks) {
                            track = new TrackModel();
                            trackRecord = trackWithLocation.track;

                            track.setEntityToModel(trackWithLocation);
                            imageList = trackWithLocation.imageList;

                            uploadTrack();

                        }
                    return null;
                }

                @Override
                protected void onPostExecute(Void id) {
                    super.onPostExecute(id);

                    Log.i(TAG, "onCreate: onDestroy()");
                    stopSelf();

                }
            }.execute();

        } catch (Exception e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }
    }

    private void uploadTrack() {
        try {
            if (imageList != null && !imageList.isEmpty()) {
                uploadImage(imageList.get(0), track.getId());

            } else {
                track.save();

                trackRecord.setReadyToUpload(0);
                repository.update(trackRecord);
            }
        } catch (Exception e) {
            Log.e(TAG, "uploadTrack: " + e.getMessage());
        }
    }

    private void uploadImage(final ImageEntity localImg, String trackId) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(localImg.getUrl());

            //Recovery Image to Firebase
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] btsImage = baos.toByteArray();

            final String uniqueID = UUID.randomUUID().toString();

            //Save to Firebase
            StorageReference ref = ConfigFirebase.getFirebaseStorage().child("track").child(trackId).child(uniqueID + ".png");
            UploadTask task = ref.putBytes(btsImage);

            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            track.addImage(new ImageModel(uniqueID, uri.toString(), track.getOwnerId()));
                            imageList.remove(localImg);

                            //Delete image from database on the phone
                            Util.deleteImageLocal(localImg.getUrl());

                            uploadTrack();

                        }
                    });

                }
            });

        } catch (Exception e) {
            Log.e(TAG, "uploadImage: " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        super.onDestroy();
    }
}