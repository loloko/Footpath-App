package com.fernando.footpath.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.fernando.footpath.R;
import com.fernando.footpath.activity.TrackActivity;
import com.fernando.footpath.adapter.TrackAdapter;
import com.fernando.footpath.config.ConfigFirebase;
import com.fernando.footpath.database.entity.ImageEntity;
import com.fernando.footpath.database.entity.TrackWithLocation;
import com.fernando.footpath.database.repository.TrackRecordRepository;
import com.fernando.footpath.model.ImageModel;
import com.fernando.footpath.model.TrackModel;
import com.fernando.footpath.util.DataSingleton;
import com.fernando.footpath.util.RecyclerItemClickListener;
import com.fernando.footpath.util.UserFirebase;
import com.fernando.footpath.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class TrackFragment extends Fragment {
    private static final String TAG = "TrackFragment";

    private TextView tvNoTracks;

    private TrackAdapter adapter;
    private List<TrackModel> trackList = new ArrayList<>();
    private ValueEventListener valueEventListener;
    private DatabaseReference trackRef;

    private ShimmerFrameLayout shimmerView;
    private SwipeRefreshLayout refreshPage;
    private boolean isMyTracksTab;

    private DataSingleton singleton = DataSingleton.getInstance();

    public TrackFragment() {
        // Required empty public constructor
    }

    public TrackFragment(boolean isMyTracksTab) {
        this.isMyTracksTab = isMyTracksTab;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        trackRef = ConfigFirebase.getFirebaseDatabase().child("track");

        RecyclerView rvTrackList = view.findViewById(R.id.recycle_track);
        shimmerView = view.findViewById(R.id.shimmerLayout);
        tvNoTracks = view.findViewById(R.id.tv_no_tracks);

        //Adapter
        adapter = new TrackAdapter(trackList, getActivity());

        //config Recycleview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvTrackList.setLayoutManager(layoutManager);
        rvTrackList.setHasFixedSize(true);
        rvTrackList.setAdapter(adapter);

        rvTrackList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), rvTrackList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //check if there is a offline track downloaded, if yes, in the Track Activity wont show the option to download the track
                for (TrackModel offlineTrack : DataSingleton.getInstance().getTrackOffline())
                    if (offlineTrack.getId().equals(trackList.get(position).getId()))
                        trackList.get(position).setTrackDatabaseId(offlineTrack.getTrackDatabaseId());


                singleton.setTrack(trackList.get(position));
                startActivity(new Intent(getActivity(), TrackActivity.class));
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));

        if (!isMyTracksTab)
            fetchAllTracks(0, 0);


        refreshPage = view.findViewById(R.id.swipeRefresh);
        refreshPage.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (isMyTracksTab)
                            fetchMyTracks();
                        else
                            fetchAllTracks(0, 0);
                    }
                }
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isMyTracksTab)
            fetchMyTracks();

    }

    @Override
    public void onPause() {
        shimmerView.stopShimmerAnimation();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null)
            trackRef.removeEventListener(valueEventListener);
    }

    private void fetchMyTracks() {
        trackList.clear();

        if (!Util.isNetworkAvailable(getActivity().getApplicationContext())) {
            if (refreshPage.isRefreshing())
                refreshPage.setRefreshing(false);

            fetchOfflineTracks();
            return;
        }

        fetchOfflineTracks();

        shimmerView.startShimmerAnimation();
        shimmerView.setVisibility(View.VISIBLE);
        tvNoTracks.setVisibility(View.GONE);

        Query query = trackRef.orderByChild("ownerId").equalTo(UserFirebase.getIdUserBase64());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChildren())
                    for (DataSnapshot dados : dataSnapshot.getChildren()) {
                        TrackModel track = dados.getValue(TrackModel.class);

                        if (!trackList.contains(track))
                            trackList.add(track);
                    }

                adapter.notifyDataSetChanged();

                shimmerView.stopShimmerAnimation();
                shimmerView.setVisibility(View.GONE);
                refreshPage.setRefreshing(false);

                fetchMyFavorite();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fetchOfflineTracks() {
        final TrackRecordRepository repository = new TrackRecordRepository(getContext());

        singleton.getTrackOffline().clear();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    List<TrackWithLocation> tracks = repository.getTracksOffline();

                    if (tracks != null && !tracks.isEmpty())
                        for (TrackWithLocation trackWithLocation : tracks) {

                            TrackModel track = new TrackModel().setEntityToModel(trackWithLocation);
                            track.setId(trackWithLocation.track.getTrackId());
                            track.setTrackDatabaseId(trackWithLocation.track.getId());

                            for (ImageEntity img : trackWithLocation.imageList)
                                track.addImage(new ImageModel("", img.getUrl(), ""));

                            singleton.getTrackOffline().add(track);

                            if (isMyTracksTab)
                                trackList.add(track);

                        }

                } catch (Exception e) {
                    Log.e(TAG, "fetchOfflineTracks: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void id) {
                super.onPostExecute(id);
                if (isMyTracksTab)
                    adapter.notifyDataSetChanged();
            }
        }.execute();

    }

    private void fetchMyFavorite() {
        if (singleton.getCurrentUser() == null)
            return;

        //firebase does not support query with in, so need to make a query for each favorite
        if (singleton.getCurrentUser().getFavorites().isEmpty()) {
            if (trackList.isEmpty())
                tvNoTracks.setVisibility(View.VISIBLE);
            return;
        }

        for (String fav : singleton.getCurrentUser().getFavorites()) {

            trackRef.orderByChild("id").equalTo(fav).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dados : dataSnapshot.getChildren()) {
                        TrackModel track = dados.getValue(TrackModel.class);

                        if (!trackList.contains(track))
                            trackList.add(track);
                    }

                    adapter.notifyDataSetChanged();

                    if (trackList.isEmpty())
                        tvNoTracks.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


    }

    public void fetchAllTracks(double latitude, final double longitude) {
        try {

            if (!Util.isNetworkAvailable(getActivity().getApplicationContext())) {
                if (refreshPage.isRefreshing())
                    refreshPage.setRefreshing(false);
                return;
            }


            shimmerView.startShimmerAnimation();
            shimmerView.setVisibility(View.VISIBLE);
            tvNoTracks.setVisibility(View.GONE);

            fetchOfflineTracks();

            Query query;
            if (latitude != 0)
                query = trackRef.orderByChild("latitude").startAt((latitude - 1)).endAt((latitude + 1));
            else
                query = trackRef.orderByChild("latitude").limitToFirst(20);


            valueEventListener = query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    trackList.clear();

                    for (DataSnapshot dados : dataSnapshot.getChildren()) {
                        TrackModel track = dados.getValue(TrackModel.class);
                        if (longitude != 0) {
                            if (track.getLongitude() < longitude + 1 && track.getLongitude() > longitude - 1)
                                trackList.add(track);

                        } else
                            trackList.add(track);
                    }

                    if (trackList.isEmpty())
                        tvNoTracks.setVisibility(View.VISIBLE);

                    adapter.notifyDataSetChanged();
                    shimmerView.stopShimmerAnimation();
                    shimmerView.setVisibility(View.GONE);
                    refreshPage.setRefreshing(false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            Log.e(TAG, "fetchAllTracks: " + e.getMessage());
        }
    }

}