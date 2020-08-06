package com.fernando.footpath.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fernando.footpath.R;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.model.UserModel;
import com.fernando.footpath.util.MessageUtil;
import com.fernando.footpath.util.UserFirebase;
import com.fernando.footpath.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private CircleImageView circleProfilePhoto;
    private Button btSave;
    private TextView tvName;

    private StorageReference storage;
    private String currentUserName;

    private AlertDialog loading;

    private String[] requiredPermission = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int SELECT_CAMERA = 100, SELECT_GALLERY = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        storage = ConfigFirebase.getFirebaseStorage();

        //validate permission
        Util.validatePermission(Arrays.asList(requiredPermission), this, 1);

        circleProfilePhoto = findViewById(R.id.im_circle_profile_photo);
        btSave = findViewById(R.id.bt_save_profile);

        //Set photo user
        Uri url = UserFirebase.getCurrentUser().getPhotoUrl();
        if (url != null)
            Glide.with(this).load(url).
                    placeholder(R.drawable.image_loading)
                    .error(R.drawable.image_loading_error).into(circleProfilePhoto);
        else
            circleProfilePhoto.setImageResource(R.drawable.profile_default);

        currentUserName = UserFirebase.getCurrentUser().getDisplayName();

        tvName = findViewById(R.id.tv_name_profile);
        tvName.setText(currentUserName);
        tvName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!currentUserName.equals(tvName.getText().toString().trim()))
                    updateButtonUI(true);
                else
                    updateButtonUI(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void updateButtonUI(boolean enable) {
        btSave.setEnabled(enable);
        if (enable)
            btSave.setBackgroundTintList(getColorStateList(R.color.colorPrimary));
        else
            btSave.setBackgroundTintList(getColorStateList(R.color.grey));
    }

    public void galleryPhotoClick(View view) {
        if (!Util.isNetworkAvailable(this))
            return;

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (i.resolveActivity(getPackageManager()) != null)
            startActivityForResult(i, SELECT_GALLERY);
    }

    public void btUpdateUserNameClick(View view) {
        if (!Util.isNetworkAvailable(this))
            return;

        updateUser(tvName.getText().toString(), false);

        updateButtonUI(false);
        currentUserName = tvName.getText().toString();

        MessageUtil.snackBarMessage(circleProfilePhoto, R.string.success_change_name);
    }

    private void updateUser(String msg, boolean isPhoto) {
        try {
            boolean result = UserFirebase.updateUser(msg, isPhoto);

            if (result) {
                UserModel user = UserFirebase.getUserData();

                if (isPhoto)
                    user.setPhoto(msg);
                else
                    user.setName(tvName.getText().toString());

                user.updateInfo();
            }

        } catch (Exception e) {
            Log.e(TAG, "updateUser: " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        loading = MessageUtil.createLoadingDialog(this, R.string.upload_image);
        loading.show();

        Bitmap bitmap = null;
        try {

            switch (requestCode) {
                case SELECT_CAMERA:
//                    bitmap = (Bitmap) data.getExtras().get("data");
                    break;
                case SELECT_GALLERY:
                    Uri localImage = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), localImage);
                    break;
            }

            if (bitmap != null) {
                circleProfilePhoto.setImageBitmap(bitmap);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, baos);
                byte[] btsImage = baos.toByteArray();

                //save image firebase
                StorageReference ref = storage.child("image").child("profile").child(UserFirebase.getIdUserBase64() + ".png");

                ref.putBytes(btsImage).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MessageUtil.snackBarMessage(circleProfilePhoto, R.string.error_image_upload);
                        loading.dismiss();
                    }

                }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        MessageUtil.snackBarMessage(circleProfilePhoto, R.string.success_image_upload);

                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                updateUser(uri.toString(), true);

                                loading.dismiss();
                            }
                        });
                    }
                });
            }

        } catch (Exception e) {
            MessageUtil.snackBarMessage(tvName, R.string.error_image_upload);
            loading.dismiss();
            Log.e(TAG, "onActivityResult: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int resultPermission : grantResults)
            if (resultPermission == PackageManager.PERMISSION_DENIED)
                MessageUtil.createFinishDialog(this, R.string.denied_permission);
    }
}