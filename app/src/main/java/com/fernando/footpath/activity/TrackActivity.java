package com.fernando.footpath.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fernando.footpath.R;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.database.entity.ImageEntity;
import com.fernando.footpath.database.entity.LongLatEntity;
import com.fernando.footpath.database.entity.TrackRecordEntity;
import com.fernando.footpath.database.repository.ImageRepository;
import com.fernando.footpath.database.repository.LongLatRepository;
import com.fernando.footpath.database.repository.TrackRecordRepository;
import com.fernando.footpath.interfaces.ItemClickListener;
import com.fernando.footpath.model.ImageModel;
import com.fernando.footpath.model.LatLngModel;
import com.fernando.footpath.model.RateModel;
import com.fernando.footpath.model.TrackModel;
import com.fernando.footpath.util.DataSingleton;
import com.fernando.footpath.util.ImageSlider;
import com.fernando.footpath.util.MessageUtil;
import com.fernando.footpath.util.UserFirebase;
import com.fernando.footpath.util.Util;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "TrackActivity";

    private TextView tvName, tvLocation, tvDifficult, tvDistance, tvPace, tvTypeTrack, tvDescription, tvTime, tvExerciseType, tvCreatedBy, tvRateAction, tvRateCount;
    private RatingBar ratingBar;
    private Button btDownloadTrack;
    private FloatingActionButton fabFavorite;

    private TrackModel track;

    private DataSingleton singleton = DataSingleton.getInstance();
    private RateModel userRate;
    private boolean isFavorite;

    private String[] requiredPermission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        Toolbar toolbar = findViewById(R.id.tb_options);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvName = findViewById(R.id.tv_name_track);
        tvLocation = findViewById(R.id.tv_location_track);
        tvDifficult = findViewById(R.id.tv_difficult);
        tvDistance = findViewById(R.id.tv_distance);
        tvPace = findViewById(R.id.tv_pace);
        tvTypeTrack = findViewById(R.id.tv_track_type);
        tvExerciseType = findViewById(R.id.tv_exercise_type);
        tvDescription = findViewById(R.id.tv_description);
        tvCreatedBy = findViewById(R.id.tv_created_by);
        tvTime = findViewById(R.id.tv_time);
        ratingBar = findViewById(R.id.rating_bar_track);
        btDownloadTrack = findViewById(R.id.bt_download);
        tvRateCount = findViewById(R.id.tv_rate_count);
        tvRateAction = findViewById(R.id.tv_rate_action);
        fabFavorite = findViewById(R.id.fab_favorite);


        track = singleton.getTrack();

        //set de button download text to delete or download track
        btDownloadChangeUIName();

        if (Util.isNetworkAvailable(this)) {

            userRate = singleton.checkIfRated();
            if (userRate != null)
                tvRateAction.setText(R.string.change_rate);

            //check if is a favorite
            if (singleton.getCurrentUser().getFavorites().contains(track.getId())) {
                isFavorite = true;
                fabFavorite.setImageResource(R.drawable.ic_favorite_selected);
            }
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapp);
        mapFragment.getMapAsync(this);


        List<String> imageList = new ArrayList<>();

        //Recover track images to show in the slidershow
        for (ImageModel image : track.getImageList())
            imageList.add(image.getUrl());

        //ImageSlider on the top layout
        ImageSlider imageSlider = findViewById(R.id.slider);
        imageSlider.setImageList(imageList);
        imageSlider.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemSelected(int i) {
                Intent intent = new Intent(TrackActivity.this, ImageActivity.class);
                startActivity(intent);
            }
        });
    }


    private void setValuesUI() {
        try {
            tvName.setText(track.getTitle());
            tvLocation.setText(track.getLocation());
            tvDistance.setText(Util.formatMeterToKm(track.getDistance()));
            Util.getDifficultForTextView(track.getDifficulty(), tvDifficult, this);
            tvExerciseType.setText(Util.getExerciseType(track.getActivityType()));
            tvCreatedBy.setText(track.getOwnerName());
            tvTime.setText(LocalTime.MIN.plus(Duration.ofSeconds(track.getTime())).toString());
            tvPace.setText(Util.getPace(track.getTime(), track.getDistance()) + " /km");
            tvTypeTrack.setText(Util.getTrackType(track.getTrackType()));
            tvDescription.setText(track.getDescription());

            tvRateCount.setText(new StringBuffer().append("(").append(track.getRateCount()).append(")"));
            ratingBar.setRating(track.getRate() / track.getRateCount());
        } catch (Exception e) {
            Log.e(TAG, "setValuesUI :" + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    //permission validate first
                    if (Util.validatePermission(Arrays.asList(requiredPermission), TrackActivity.this, 1))
                        startActivity(new Intent(TrackActivity.this, MapActivity.class));

                }
            });

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(5);

            int counter = 0;

            for (LatLngModel ll : track.getLatLngs()) {
                counter++;

                LatLng latLng = new LatLng(ll.getLat(), ll.getLng());

                polylineOptions.add(latLng);

                if (counter == 1) {
                    googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_start)).title(getResources().getString(R.string.start)));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                } else if (counter == track.getLatLngs().size())
                    googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_finish)).title(getResources().getString(R.string.finish)));
            }

            googleMap.addPolyline(polylineOptions);

        } catch (Exception e) {
            Log.e(TAG, "onMapReady :" + e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_edit:
                editTrack();
                break;

            case R.id.menu_delete:
                if (Util.isNetworkAvailable(this))
                    deleteTrack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editTrack() {
        if (!Util.isNetworkAvailable(this))
            return;

        singleton.setTrackType(track.getTrackType());
        singleton.setActivityType(track.getActivityType());
        singleton.setDifficult(track.getDifficulty());

        singleton.setEditing(true);
        startActivity(new Intent(this, TrackSaveActivity.class));
    }

    public void btDownloadTrackClick(View view) {
        final TrackRecordRepository repository = new TrackRecordRepository(this);

        //delete offline track
        if (track.getTrackDatabaseId() != null) {

            new AsyncTask<Void, Void, TrackRecordEntity>() {
                @Override
                protected TrackRecordEntity doInBackground(Void... voids) {
                    return repository.getTrackById(track.getTrackDatabaseId().intValue());
                }

                @Override
                protected void onPostExecute(TrackRecordEntity trackDB) {
                    super.onPostExecute(trackDB);
                    repository.delete(trackDB);


                    singleton.getTrackOffline().remove(track);
                    track.setTrackDatabaseId(null);

                    btDownloadChangeUIName();
                    MessageUtil.snackBarMessage(tvPace, R.string.offline_delete);
                }
            }.execute();

        } else {
            if (!Util.isNetworkAvailable(this))
                return;

            //loading
            final AlertDialog loading = MessageUtil.createLoadingDialog(this, R.string.upload_image);
            loading.show();

            //save to database local, so can be access offline
            final TrackRecordEntity trackEntity = new TrackRecordEntity().convertModelToEntity(track);
            final LongLatRepository lngLatRepository = new LongLatRepository(TrackActivity.this);
            final ImageRepository imageRepository = new ImageRepository(TrackActivity.this);


            new AsyncTask<Void, Void, Long>() {
                @Override
                protected Long doInBackground(Void... voids) {
                    Long id = repository.insert(trackEntity);

                    for (LatLngModel lt : track.getLatLngs())
                        lngLatRepository.insert(new LongLatEntity(id, lt.getLat(), lt.getLng(), 0, 0));

                    for (ImageModel image : track.getImageList())
                        new ImageEntity(id, image.getUrl()).downloadImage(TrackActivity.this, imageRepository);

                    return id;
                }

                @Override
                protected void onPostExecute(Long id) {
                    super.onPostExecute(id);

                    track.setTrackDatabaseId(id);

                    singleton.getTrackOffline().add(track);
                    btDownloadChangeUIName();
                    MessageUtil.snackBarMessage(tvPace, R.string.offline_download);
                    loading.dismiss();
                }
            }.execute();
        }

    }

    private void btDownloadChangeUIName() {
        if (track.getTrackDatabaseId() != null) {
            btDownloadTrack.setText(R.string.delete_offline);
            btDownloadTrack.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else {
            btDownloadTrack.setText(R.string.download);
            btDownloadTrack.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        }
    }


    private void deleteTrack() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.delete_track);
            builder.setTitle(R.string.attention);

            builder.setCancelable(false);
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ConfigFirebase.getFirebaseDatabase().child("track").child(track.getId()).removeValue();

                    for (ImageModel img : track.getImageList())
                        ConfigFirebase.getFirebaseStorage().child("track").child(track.getId()).child(img.getName() + ".png").delete();

                    singleton.setTrack(null);

                    finish();
                }
            });

            builder.create().show();

        } catch (Exception e) {
            MessageUtil.snackBarMessage(tvDifficult, R.string.error_delete_track);
            Log.e(TAG, "fabDeleteClick :" + e.getMessage());
        }
    }

    public void tvRateClick(View view) {
        try {
            if (!Util.isNetworkAvailable(this))
                return;
            if (track.getTrackDatabaseId() != null) {
                MessageUtil.snackBarMessage(tvDescription, R.string.action_not_possible_offline);
                return;
            }

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.typeDialog);
            View convertView = LayoutInflater.from(this).inflate(R.layout.dialog_rate, null);
            final RatingBar ratebar = convertView.findViewById(R.id.rating_bar_dialog);

            if (userRate != null) {
                ((TextView) convertView.findViewById(R.id.tv_rate_title)).setText(R.string.change_rate);
                ratebar.setRating(userRate.getRate());
            }

            alertDialog.setView(convertView);

            final AlertDialog dialog = alertDialog.create();

            (convertView.findViewById(R.id.bt_done_rate)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (userRate != null && userRate.getRate() == ratebar.getRating()) {
                        dialog.dismiss();
                        return;
                    }

                    if (userRate == null) {
                        track.setRate(track.getRate() + ratebar.getRating());
                        track.setRateCount(track.getRateCount() + 1);
                    }

                    //update in firebase Track
                    DatabaseReference rateRef = ConfigFirebase.getFirebaseDatabase().child("track").child(track.getId());
                    rateRef.child("rate").setValue(track.getRate());
                    rateRef.child("rateCount").setValue(track.getRateCount());

                    //update in firebase User
                    if (userRate != null)
                        singleton.getCurrentUser().getRates().remove(userRate);

                    userRate = new RateModel(track.getId(), ratebar.getRating());
                    singleton.getCurrentUser().getRates().add(userRate);
                    UserFirebase.updateUserTrackRate();

                    //update rate in the screen
                    tvRateCount.setText(new StringBuffer().append("(").append(track.getRateCount()).append(")"));
                    ratingBar.setRating(track.getRate() / track.getRateCount());

                    tvRateAction.setText(R.string.change_rate);
                    MessageUtil.snackBarMessage(tvDescription, R.string.rate_success);
                    dialog.dismiss();
                }
            });

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "fabRate: " + e.getMessage());
        }
    }

    public void fabFavoriteClick(View view) {
        try {
            if (!Util.isNetworkAvailable(this))
                return;
            if (track.getTrackDatabaseId() != null) {
                MessageUtil.snackBarMessage(tvDescription, R.string.action_not_possible_offline);
                return;
            }

            if (isFavorite)
                singleton.getCurrentUser().getFavorites().remove(track.getId());
            else
                singleton.getCurrentUser().getFavorites().add(track.getId());

            //update in firebase User
            UserFirebase.updateFavorites();

            if (isFavorite) {
                fabFavorite.setImageResource(R.drawable.ic_favorite);
                MessageUtil.snackBarMessage(tvDescription, R.string.favorite_remove);
            } else {
                fabFavorite.setImageResource(R.drawable.ic_favorite_selected);
                MessageUtil.snackBarMessage(tvDescription, R.string.favorite_add);
            }
        } catch (Exception e) {
            Log.e(TAG, "fabFavoriteClick: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setValuesUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //just show the menu if you are the owner of the track
        if (UserFirebase.getIdUserBase64().equals(track.getOwnerId()) && track.getTrackDatabaseId() == null)
            getMenuInflater().inflate(R.menu.menu_track, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isDenied = false;

        for (int resultPermission : grantResults)
            if (resultPermission == PackageManager.PERMISSION_DENIED) {
                MessageUtil.createBasicDialog(this, R.string.denied_permission);
                isDenied = true;
            }

        if (!isDenied)
            startActivity(new Intent(TrackActivity.this, MapActivity.class));
    }
}