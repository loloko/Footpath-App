package com.fernando.footpath.util;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class UserFirebase {
    private static final String TAG = "UserFirebase";

    public static String getIdUserBase64() {
        return Base64Custom.codeToBase64(getCurrentUser().getEmail());
    }

    public static FirebaseUser getCurrentUser() {

        return ConfigFirebase.getFirebaseAuth().getCurrentUser();
    }


    public static UserModel getUserData() {
        FirebaseUser dataUser = getCurrentUser();

        UserModel user = new UserModel();
        user.setEmail(dataUser.getEmail());
        user.setName(dataUser.getDisplayName());

        if (dataUser.getPhotoUrl() != null)
            user.setPhoto(dataUser.getPhotoUrl().toString());

        return user;
    }

    public static void updateUserTrackRate() {
        ConfigFirebase.getFirebaseDatabase().child("user").child(getIdUserBase64()).child("rates")
                .setValue(DataSingleton.getInstance().getCurrentUser().getRates());
    }

    public static void updateFavorites() {
        ConfigFirebase.getFirebaseDatabase().child("user").child(getIdUserBase64()).child("favorites")
                .setValue(DataSingleton.getInstance().getCurrentUser().getFavorites());
    }

    public static void getUserDataByFirebase() {
        ConfigFirebase.getFirebaseDatabase().child("user").child(getIdUserBase64()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSingleton.getInstance().setCurrentUser(dataSnapshot.getValue(UserModel.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static boolean updateUser(String info, boolean isPhoto) {
        try {
            UserProfileChangeRequest profile;

            if (isPhoto)
                profile = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(info)).build();
            else
                profile = new UserProfileChangeRequest.Builder().setDisplayName(info).build();

            getCurrentUser().updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful())
                        Log.e(TAG, "is not Successful");
                }
            });
            return true;
        } catch (Exception e) {
            Log.e(TAG, "updateUser: " + e.getMessage());
            return false;
        }
    }
}