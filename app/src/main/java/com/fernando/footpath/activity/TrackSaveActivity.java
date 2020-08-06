package com.fernando.footpath.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fernando.footpath.R;
import com.fernando.footpath.anim.AnimationFAB;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.database.entity.ImageEntity;
import com.fernando.footpath.database.entity.LongLatEntity;
import com.fernando.footpath.database.entity.TrackRecordEntity;
import com.fernando.footpath.database.entity.TrackWithLocation;
import com.fernando.footpath.database.repository.TrackRecordRepository;
import com.fernando.footpath.interfaces.ItemClickListener;
import com.fernando.footpath.model.LatLngModel;
import com.fernando.footpath.model.TrackModel;
import com.fernando.footpath.model.TypeModel;
import com.fernando.footpath.service.UploadTrackService;
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
import java.util.List;

public class TrackSaveActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "TrackSaveActivity";

    private TextView tvTime, tvDistance, tvPace, tvDifficult;
    private EditText etTittle, etDescription;
    private LinearLayout fabSave, fabDelete;
    private FloatingActionButton fabOptions;

    private GoogleMap mMap;

    private TrackWithLocation trackWithLocation;
    private TrackRecordRepository trackRepository;
    private boolean isFabRotate = false;

    private DataSingleton singleton = DataSingleton.getInstance();
    private double distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_save);

        //toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        tvTime = findViewById(R.id.tv_time);
        tvDistance = findViewById(R.id.tv_distance);
        tvPace = findViewById(R.id.tv_pace);
        tvDifficult = findViewById(R.id.tv_difficult);
        etTittle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);


        //set Images to the bottom of the textview
        TextView ivActivityType = findViewById(R.id.tv_exercise_type);

        ivActivityType.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, TypeModel.getImageActivitybyId(singleton.getActivityType()));

        TextView ivTrackType = findViewById(R.id.tv_track_type);
        ivTrackType.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, TypeModel.getImageTrackbyId(singleton.getTrackType()));

        fabSave = findViewById(R.id.layoutFabSave);
        fabDelete = findViewById(R.id.layoutFabDelete);
        fabOptions = findViewById(R.id.fab_plus);

        AnimationFAB.init(fabSave);
        AnimationFAB.init(fabDelete);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapp);
        mapFragment.getMapAsync(this);

        RadioGroup radioGroupDifficult = findViewById(R.id.groupRadio);
        radioGroupDifficult.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {

                            case R.id.rb_easy:
                                singleton.setDifficult(1);
                                tvDifficult.setError(null);
                                break;

                            case R.id.rb_moderate:
                                singleton.setDifficult(2);
                                tvDifficult.setError(null);
                                break;

                            default:
                                singleton.setDifficult(3);
                                tvDifficult.setError(null);
                                break;

                        }
                    }
                }
        );


        //the track is coming from TrackActivity, its for editing
        if (singleton.isEditing()) {
            //set values

            etTittle.setText(singleton.getTrack().getTitle());
            etDescription.setText(singleton.getTrack().getDescription());

            distance = singleton.getTrack().getDistance();
            tvDistance.setText(Util.formatMeterToKm(distance));

            tvTime.setText(LocalTime.MIN.plus(Duration.ofSeconds(singleton.getTrack().getTime())).toString());
            tvDistance.setText(Util.formatMeterToKm(singleton.getTrack().getDistance()));
            tvPace.setText(Util.getPace(singleton.getTrack().getTime(), singleton.getTrack().getDistance()) + " /km");


            findViewById(R.id.layoutImage).setVisibility(View.GONE);

            if (singleton.getDifficult() == 1)
                radioGroupDifficult.check(R.id.rb_easy);
            else if (singleton.getDifficult() == 2)
                radioGroupDifficult.check(R.id.rb_moderate);
            else
                radioGroupDifficult.check(R.id.rb_difficult);

            ((FloatingActionButton) findViewById(R.id.fab_plus)).setImageResource(R.drawable.ic_done);


        } else {
            //Normal cicle life, comming from RecordActivity

            singleton.setRecordPhoto(true);
            trackRepository = new TrackRecordRepository(this);
        }
    }

    public void btTrackTypeClick(View view) {
        MessageUtil.createDialogRecycler(this, (TextView) view, TypeModel.getTrackTypes(this), true);
    }

    public void btExerciseTypeClick(View view) {
        MessageUtil.createDialogRecycler(this, (TextView) view, TypeModel.getExerciseTypes(this), false);
    }

    private void loadTrackFromDatabase() {
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    trackWithLocation = trackRepository.getTrackWithLocationById(DataSingleton.getInstance().getRecordTrackId());

                    return null;
                }

                @Override
                protected void onPostExecute(Void id) {
                    super.onPostExecute(id);

                    updateMapUI();
                    loadImagesDataBase();

                }
            }.execute();


        } catch (Exception e) {
            Log.e(TAG, "loadTrack :" + e.getMessage());
        }
    }

    private void loadImagesDataBase() {
        try {
            List<String> imageList = new ArrayList<>();

            if (trackWithLocation.imageList != null && !trackWithLocation.imageList.isEmpty()) {
                singleton.setImagesDatabase(trackWithLocation.imageList);
                for (ImageEntity image : trackWithLocation.imageList)
                    imageList.add(image.getUrl());

            }

            //ImageSlider on the top layout
            ImageSlider imageSlider = findViewById(R.id.view_pager);
            imageSlider.setImageList(imageList);
            imageSlider.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemSelected(int i) {
                    Intent intent = new Intent(getApplicationContext(), ImageActivity.class);
                    startActivity(intent);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "loadImagesDataBase :" + e.getMessage());
        }
    }

    public void fabOptionClick(View view) {
        //fab acts like save
        if (singleton.isEditing()) {

            updateTrackToFirebase();
        } else {
            //normal button rotation
            isFabRotate = AnimationFAB.rotateFab(view, !isFabRotate);
            if (isFabRotate) {
                AnimationFAB.showIn(fabSave);
                AnimationFAB.showIn(fabDelete);
            } else {
                AnimationFAB.showOut(fabSave);
                AnimationFAB.showOut(fabDelete);
            }
        }
    }

    //option just when is editing the track
    private void updateTrackToFirebase() {
        if (trackUIValidation())
            return;

        singleton.getTrack().setTitle(etTittle.getText().toString());
        singleton.getTrack().setDescription(etDescription.getText().toString());
        singleton.getTrack().setDifficulty(singleton.getDifficult());
        singleton.getTrack().setTrackType(singleton.getTrackType());
        singleton.getTrack().setActivityType(singleton.getActivityType());

        DatabaseReference imageRef = ConfigFirebase.getFirebaseDatabase().child("track").child(singleton.getTrack().getId());
        imageRef.setValue(singleton.getTrack());

        finish();
    }

    private boolean trackUIValidation() {
        if (distance < 200) {
            MessageUtil.createBasicDialog(this, R.string.min_distance);
            return true;
        }
        if (singleton.getDifficult() == 0) {
            tvDifficult.setError(getString(R.string.required));
            return true;
        }
        if (Util.editTextValidation(this, etTittle, R.string.required_title))
            return true;

        if (etTittle.getText().toString().trim().length() > 35) {
            etTittle.setError(getResources().getString(R.string.title_limit));
            etTittle.setHintTextColor(Color.RED);
            return true;
        }

        if (Util.editTextValidation(this, etDescription, R.string.required_description))
            return true;

        return false;
    }

    public void fabSaveClick(View view) {
        if (trackUIValidation()) {
            fabOptions.performClick();
            return;
        }

        try {
            TrackRecordEntity track = trackWithLocation.track;

            track.setTitle(etTittle.getText().toString());
            track.setDescription(etDescription.getText().toString());
            track.setDistance(distance);
            track.setOwnerId(UserFirebase.getIdUserBase64());
            track.setOwnerName(UserFirebase.getCurrentUser().getDisplayName());
            track.setOpen(0);
            track.setDifficulty(singleton.getDifficult());
            track.setTrackType(singleton.getTrackType());
            track.setActivityType(singleton.getActivityType());
            track.setReadyToUpload(1);

            trackRepository.update(track);

            DataSingleton.getInstance().setRecordTrackId(null);

            //service to upload the track to the firebase
            startService(new Intent(TrackSaveActivity.this, UploadTrackService.class));

            //call dialog to confirm track save
            dialogSaveTrackFinish();

        } catch (Exception e) {
            Log.e(TAG, "fabSaveClick :" + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        singleton.setRecordPhoto(false);
        singleton.setEditing(false);
    }

    public void fabDeleteClick(View view) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.delete_track);
            builder.setTitle(R.string.attention);

            builder.setCancelable(false);
            builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    trackRepository.delete(trackWithLocation.track);
                    DataSingleton.getInstance().setRecordTrackId(null);

                    finish();

                }
            });
            builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } catch (Exception e) {
            MessageUtil.snackBarMessage(tvDistance, R.string.error_delete_track);
            Log.e(TAG, "fabDeleteClick :" + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!singleton.isEditing())
            loadTrackFromDatabase();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, RecordActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, RecordActivity.class));
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                startActivity(new Intent(TrackSaveActivity.this, MapActivity.class));
            }
        });

        if (singleton.isEditing())
            updateMapUI();
    }

    private void updateMapUI() {
        try {

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.RED);
            polylineOptions.width(5);

            int counter = 0;

            if (singleton.isEditing()) {

                for (LatLngModel ll : singleton.getTrack().getLatLngs()) {
                    counter++;

                    LatLng latLng = new LatLng(ll.getLat(), ll.getLng());

                    polylineOptions.add(latLng);

                    if (counter == 1) {
                        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_start)).title(getResources().getString(R.string.start)));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                    } else if (counter == singleton.getTrack().getLatLngs().size())
                        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_finish)).title(getResources().getString(R.string.finish)));

                }

            } else {

                tvTime.setText(DateUtils.formatElapsedTime(trackWithLocation.track.getTime()));

                distance = 0;
                //information to populate the map
                TrackModel track = new TrackModel();
                List<LatLngModel> latLngEntity = new ArrayList<>();

                for (LongLatEntity ll : trackWithLocation.longLatList) {
                    counter++;

                    //in case need to open the map
                    latLngEntity.add(new LatLngModel(ll.getLatitude(), ll.getLongitude()));

                    LatLng latLng = new LatLng(ll.getLatitude(), ll.getLongitude());
                    distance += ll.getDistance();

                    polylineOptions.add(latLng);

                    if (counter == 1)
                        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_start)).title(getResources().getString(R.string.start)));
                    else if (counter == Math.round(trackWithLocation.longLatList.size() / 2))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                    else if (counter == trackWithLocation.longLatList.size())
                        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_finish)).title(getResources().getString(R.string.finish)));
                }

                tvDistance.setText(Util.formatMeterToKm(distance));
                tvPace.setText(Util.getPace(trackWithLocation.track.getTime(), distance));

                track.setLatLngs(latLngEntity);
                singleton.setTrack(track);
            }

            mMap.addPolyline(polylineOptions);

        } catch (Exception e) {
            Log.e(TAG, "updateMap :" + e.getMessage());
        }
    }

    private void dialogSaveTrackFinish() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.hello);
        builder.setMessage(R.string.save_track);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                TrackSaveActivity.this.finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}