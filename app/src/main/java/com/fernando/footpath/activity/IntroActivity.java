package com.fernando.footpath.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.fernando.footpath.R;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.service.UploadTrackService;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {
    private static final String TAG = "IntroActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonBackVisible(false);
        setButtonNextVisible(false);
        setFullscreen(true);

        addSlide(new FragmentSlide.Builder()
                .background(R.color.intro1)
                .fragment(R.layout.intro_1)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(R.color.intro2)
                .fragment(R.layout.intro_2)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(R.color.intro3)
                .fragment(R.layout.intro_3)
                .build());

        addSlide(new FragmentSlide.Builder()
                .background(android.R.color.white)
                .fragment(R.layout.intro_4)
                .canGoForward(false)
                .build());



    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            //Service to upload tracks to firebase
            startService(new Intent(this, UploadTrackService.class));

            //check if exist a logged user
            if (ConfigFirebase.getFirebaseAuth().getCurrentUser() != null) {
                startActivity(new Intent(IntroActivity.this, MainActivity.class));
                finish();
                return;
            }


            //The Intro will happen just once
            SharedPreferences pref = getApplicationContext().getSharedPreferences("FootPath", 0);
            boolean firstTime = pref.getBoolean("first_time", true);

            if (firstTime)
                pref.edit().putBoolean("first_time", Boolean.FALSE).apply();
            else {
                startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                finish();
            }

        } catch (Exception e) {
            Log.e(TAG, "onStart: " + e.getMessage());
        }
    }

    public void btCloseClick(View view) {
        finish();
    }

    public void btLogInClick(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}
