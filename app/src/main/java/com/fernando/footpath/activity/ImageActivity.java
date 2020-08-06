package com.fernando.footpath.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fernando.footpath.R;
import com.fernando.footpath.adapter.ImageAdapter;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.database.entity.ImageEntity;
import com.fernando.footpath.database.repository.ImageRepository;
import com.fernando.footpath.model.ImageModel;
import com.fernando.footpath.util.DataSingleton;
import com.fernando.footpath.util.MessageUtil;
import com.fernando.footpath.util.RecyclerItemClickListener;
import com.fernando.footpath.util.UserFirebase;
import com.fernando.footpath.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageActivity extends AppCompatActivity {
    private static final String TAG = "ImageActivity";

    private List<ImageModel> imageList;
    private RecyclerView rvImage;
    private ImageAdapter adapter;
    private TextView tvNoImage;

    private DataSingleton singleton;
    private static final int GALLERY = 200;

    private AlertDialog loading;
    private ImageRepository imageRepository;
    private String userLoggedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        tvNoImage = findViewById(R.id.tv_no_images);

        singleton = DataSingleton.getInstance();
        userLoggedID = UserFirebase.getIdUserBase64();

        //layout to display images
        rvImage = findViewById(R.id.recycler_images);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvImage.setLayoutManager(gridLayoutManager);

        imageRepository = new ImageRepository(this);

        //Verify if its has a track  in the Firebase or a database
        if (singleton.isRecordPhoto()) {
            imageList = new ArrayList<>();

            if (singleton.getImagesDatabase() != null)
                for (ImageEntity img : singleton.getImagesDatabase())
                    imageList.add(new ImageModel("default", img.getUrl(), userLoggedID));

        } else
            imageList = singleton.getTrack().getImageList();

        if (imageList.isEmpty())
            tvNoImage.setVisibility(View.VISIBLE);

        adapter = new ImageAdapter(this, imageList);
        rvImage.setAdapter(adapter);
        rvImage.addOnItemTouchListener(new RecyclerItemClickListener(this, rvImage, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);

                View layout = LayoutInflater.from(ImageActivity.this).inflate(R.layout.adapter_image, rvImage, false);
                ImageView image = layout.findViewById(R.id.img_adapter);

                Glide.with(ImageActivity.this).load(imageList.get(position).getUrl())
                        .placeholder(R.drawable.image_loading)
                        .centerCrop()
                        .error(R.drawable.image_loading_error).into(image);

                builder.setView(layout);
                builder.setCancelable(true);

                final AlertDialog dialog = builder.create();
                dialog.show();

                if (singleton.getTrack().getTrackDatabaseId() == null)
                    if (imageList.get(position).getOwner().equals(userLoggedID)) {
                        Button btDelete = layout.findViewById(R.id.bt_delete_photo);
                        btDelete.setVisibility(View.VISIBLE);

                        btDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteImage(imageList.get(position));
                                dialog.dismiss();
                            }
                        });
                    }
            }

            @Override
            public void onLongItemClick(View view, int position) {
            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        }));
    }

    private void deleteImage(ImageModel image) {
        if (!singleton.isRecordPhoto() && !Util.isNetworkAvailable(this))
            return;

        imageList.remove(image);

        //delete from database
        if (singleton.isRecordPhoto()) {

            for (ImageEntity img : singleton.getImagesDatabase())
                if (img.getUrl().equals(image.getUrl())) {
                    imageRepository.delete(img);
                    singleton.getImagesDatabase().remove(img);


                    Util.deleteImageLocal(image.getUrl());

                    adapter.notifyDataSetChanged();
                    return;
                }

        } else {
            //delete from firebase
            DatabaseReference imageRef = ConfigFirebase.getFirebaseDatabase().child("track").child(singleton.getTrack().getId()).child("imageList");
            imageRef.setValue(imageList);

            ConfigFirebase.getFirebaseStorage().child("track").child(singleton.getTrack().getId()).child(image.getName() + ".png").delete();
        }

        adapter.notifyDataSetChanged();
    }

    public void fabUploadClick(View view) {
        if (singleton.getTrack().getTrackDatabaseId() != null) {
            MessageUtil.snackBarMessage(rvImage, R.string.action_not_possible_offline);
            return;
        }

        if (!singleton.isRecordPhoto() && !Util.isNetworkAvailable(this))
            return;

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (i.resolveActivity(getPackageManager()) != null)
            startActivityForResult(i, GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        try {
            //loading dialog
            loading = MessageUtil.createLoadingDialog(this, R.string.upload_image);
            loading.show();

            Uri localImagem = data.getData();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagem);

            tvNoImage.setVisibility(View.GONE);

            if (bitmap != null)
                if (singleton.isRecordPhoto())
                    storeImage(bitmap, localImagem.getLastPathSegment());
                else
                    uploadImage(bitmap);


        } catch (Exception e) {
            loading.dismiss();
            Log.e(TAG, "onActivityResult: " + e.getMessage());
        }
    }

    private void storeImage(Bitmap imageData, String fileName) {
        try {
            String imageUrl = Util.storeImage(this, imageData, fileName, singleton.getRecordTrackId(), imageRepository);

            //just to show in the view
            imageList.add(new ImageModel("", imageUrl, userLoggedID));

            adapter.notifyDataSetChanged();

            loading.dismiss();
        } catch (FileNotFoundException e) {
            loading.dismiss();
            Log.e(TAG, "FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            loading.dismiss();
            Log.e(TAG, "IOException: " + e.getMessage());
        }
    }

    private void uploadImage(Bitmap bitmap) {
        try {

            //Recovery Image to Firebase
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] btsImage = baos.toByteArray();

            final String uniqueID = UUID.randomUUID().toString();

            //Save to Firebase
            StorageReference ref = ConfigFirebase.getFirebaseStorage().child("track").child(singleton.getTrack().getId()).child(uniqueID + ".png");

            ref.putBytes(btsImage).addOnFailureListener(ImageActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    MessageUtil.snackBarMessage(rvImage, R.string.error_image_upload);
                    Log.e(TAG, "uploadImage onFailure: " + e.getMessage());
                }
            }).addOnSuccessListener(ImageActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    MessageUtil.snackBarMessage(rvImage, R.string.upload_image_success);

                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            imageList.add(new ImageModel(uniqueID, uri.toString(), userLoggedID));

                            singleton.getTrack().setImageList(imageList);

                            DatabaseReference imageRef = ConfigFirebase.getFirebaseDatabase().child("track").child(singleton.getTrack().getId()).child("imageList");
                            imageRef.setValue(imageList);

                            adapter.notifyDataSetChanged();
                            loading.dismiss();
                        }
                    });
                }
            });

        } catch (Exception e) {
            loading.dismiss();
            Log.e(TAG, "uploadImage: " + e.getMessage());
        }
    }
}