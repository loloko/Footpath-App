package com.fernando.footpath.activity;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fernando.footpath.R;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.util.MessageUtil;
import com.fernando.footpath.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ResetPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ResetPasswordActivity";

    private EditText etEmail;
    private Button btReset;

    private AlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //toolbar
        getSupportActionBar().hide();

        etEmail = findViewById(R.id.et_email_reset);
        btReset = findViewById(R.id.bt_reset);

        loading = MessageUtil.createLoadingDialog(this, null);

        // FullScreen mode
        if (Util.currentApiVersion() >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        getWindow().getDecorView().setSystemUiVisibility(Util.fullScreenFlags());
                }
            });
        }
    }

    //full screen mode
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Util.currentApiVersion() >= Build.VERSION_CODES.KITKAT && hasFocus)
            getWindow().getDecorView().setSystemUiVisibility(Util.fullScreenFlags());
    }

    public void btBackClick(View view) {
        finish();
    }

    public void btResetClick(View view) {
        if (Util.editTextValidation(this, etEmail, R.string.required_email))
            return;

        final String email = etEmail.getText().toString();

        btReset.setEnabled(false);
        loading.show();

        ConfigFirebase.getFirebaseAuth().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    loading.dismiss();

                    MessageUtil.createFinishDialog(ResetPasswordActivity.this, R.string.email_sent);
                } else {
                    String exception = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException | FirebaseAuthInvalidUserException e) {
                        exception = getString(R.string.invalid_email);
                    } catch (Exception e) {
                        exception = e.getMessage();
                        Log.e(TAG, "resetClick :" + e.getMessage());
                    }

                    loading.dismiss();
                    btReset.setEnabled(true);
                    MessageUtil.snackBarMessage(etEmail, exception);
                }
            }
        });
    }
}