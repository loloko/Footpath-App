package com.fernando.footpath.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fernando.footpath.R;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.model.UserModel;
import com.fernando.footpath.util.Base64Custom;
import com.fernando.footpath.util.MessageUtil;
import com.fernando.footpath.util.UserFirebase;
import com.fernando.footpath.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;


public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btRegister;
    private AlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //toolbar
        getSupportActionBar().hide();

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btRegister = findViewById(R.id.bt_register);

        loading = MessageUtil.createLoadingDialog(this, null);

        // FullScreen mode
        if (Util.currentApiVersion() >= Build.VERSION_CODES.KITKAT) {

            //Explanation about this method on LoginActivity
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        getWindow().getDecorView().setSystemUiVisibility(Util.fullScreenFlags());

                }
            });
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Util.currentApiVersion() >= Build.VERSION_CODES.KITKAT && hasFocus)
            getWindow().getDecorView().setSystemUiVisibility(Util.fullScreenFlags());

    }

    public void btBackClick(View view) {
        goToLogin();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToLogin();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void btCloseClick(View view) {
        finish();
    }

    public void btRegisterClick(View view) {
        if (Util.editTextValidation(this, etName, R.string.required_name))
            return;
        if (Util.editTextValidation(this, etEmail, R.string.required_email))
            return;
        if (Util.editTextValidation(this, etPassword, R.string.required_password))
            return;
        if (Util.editTextValidation(this, etConfirmPassword, R.string.required_confirm_password))
            return;
        if (!etPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            MessageUtil.snackBarMessage(etConfirmPassword, R.string.password_dont_match);
            return;
        }

        final String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        final UserModel user = new UserModel(Base64Custom.codeToBase64(email), name, email, password);

        btRegister.setEnabled(false);
        loading.show();

        try {
            FirebaseAuth auth = ConfigFirebase.getFirebaseAuth();
            auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        MessageUtil.snackBarMessage(etPassword, R.string.success_register);

                        try {
                            user.save();
                            UserFirebase.updateUser(name, false);

                            loading.dismiss();

                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();

                        } catch (Exception e) {
                            Log.e(TAG, "save user: " + e.getMessage());
                        }

                    } else {
                        int exception;
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            exception = R.string.weak_password;
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            exception = R.string.invalid_email;
                        } catch (FirebaseAuthUserCollisionException e) {
                            exception = R.string.user_collision;
                        } catch (Exception e) {
                            exception = R.string.error_user_register;
                            Log.e(TAG, "registerClick: FireBase - " + exception);
                        }

                        btRegister.setEnabled(true);
                        loading.dismiss();
                        MessageUtil.snackBarMessage(etPassword, exception);
                    }
                }
            });

        } catch (Exception e) {
            btRegister.setEnabled(true);
            loading.dismiss();
            Log.e(TAG, "registerClick: " + e.getMessage());
        }
    }
}