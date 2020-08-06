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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.fernando.footpath.R;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.model.UserModel;
import com.fernando.footpath.util.Base64Custom;
import com.fernando.footpath.util.MessageUtil;
import com.fernando.footpath.util.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;

import java.util.Arrays;

public class LoginActivity extends IntroActivity {
    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btSignin;

    private AlertDialog loading;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;

    private final int GOOGLE_SIGN_IN = 1158;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //toolbar
        getSupportActionBar().hide();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btSignin = findViewById(R.id.bt_signin);

        //create loading dialog
        loading = MessageUtil.createLoadingDialog(this, null);

        auth = ConfigFirebase.getFirebaseAuth();

        //FACEBOOK
        mCallbackManager = CallbackManager.Factory.create();

        //GOOGLE
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // FullScreen mode
        if (Util.currentApiVersion() >= Build.VERSION_CODES.KITKAT) {

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        getWindow().getDecorView().setSystemUiVisibility(Util.fullScreenFlags());

                }
            });
        }

    }

    public void btSignInGoogleClick(View view) {
        if (!Util.isNetworkAvailable(this))
            return;

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                MessageUtil.snackBarMessage(etEmail, R.string.google_failed);
                Log.w(TAG, "Google sign in failed", e);
            }

            return;
        }

        //facebook
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                            saveUserFirebase();
                        else {
                            MessageUtil.snackBarMessage(etEmail, R.string.google_failed);
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }

    public void btFacebookClick(View view) {
        if (!Util.isNetworkAvailable(this))
            return;

        LoginManager loginManager = LoginManager.getInstance();

        loginManager.logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        loginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                MessageUtil.snackBarMessage(etPassword, R.string.facebook_failed);
                Log.d(TAG, "facebook:onError", error);

            }
        });

    }

    private void handleFacebookAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");

                            saveUserFirebase();
                        } else {
                            int exception;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                exception = R.string.invalid_email;
                            } catch (FirebaseAuthUserCollisionException e) {
                                exception = R.string.user_collision;
                            } catch (Exception e) {
                                exception = R.string.error_user_register;
                                Log.e(TAG, "handleFacebookAccessToken: FireBase - " + exception);
                            }

                            MessageUtil.snackBarMessage(etPassword, exception);
                        }
                    }
                });
    }

    private void saveUserFirebase() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        final UserModel user = new UserModel(Base64Custom.codeToBase64(firebaseUser.getEmail()), firebaseUser.getDisplayName(), firebaseUser.getEmail(), "");

        ConfigFirebase.getFirebaseDatabase().child("user").child(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    user.save();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    public void btSignUpClick(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }

    //full screen mode
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Util.currentApiVersion() >= Build.VERSION_CODES.KITKAT && hasFocus)
            getWindow().getDecorView().setSystemUiVisibility(Util.fullScreenFlags());
    }

    public void btCloseClick(View view) {
        finish();
    }

    public void resetPasswordClick(View view) {
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }

    public void btSignInWithEmailClick(View view) {
        if (!Util.isNetworkAvailable(this))
            return;

        if (Util.editTextValidation(this, etEmail, R.string.required_email))
            return;

        if (Util.editTextValidation(this, etPassword, R.string.required_password))
            return;

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        btSignin.setEnabled(false);
        loading.show();
        try {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        loading.dismiss();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        String exception = "";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException | FirebaseAuthInvalidUserException e) {
                            exception = getString(R.string.invalid_credentials);
                        } catch (Exception e) {
                            exception = e.getMessage();
                            Log.e(TAG, "task.getException: " + e.getMessage());
                        }

                        loading.dismiss();
                        btSignin.setEnabled(true);

                        MessageUtil.snackBarMessage(etEmail, exception);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "signInClick: " + e.getMessage());
        }
    }
}