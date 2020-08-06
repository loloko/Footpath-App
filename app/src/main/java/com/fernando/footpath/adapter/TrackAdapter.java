package com.fernando.footpath.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fernando.footpath.R;
import com.fernando.footpath.model.TrackModel;
import com.fernando.footpath.util.DataSingleton;
import com.fernando.footpath.util.UserFirebase;
import com.fernando.footpath.util.Util;

import java.util.List;


public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.MyviewHolder> {
    private static final String TAG = "TrackAdapter";

    private List<TrackModel> trackList;
    private Context context;

    public TrackAdapter(List<TrackModel> trackList, Context context) {
        this.trackList = trackList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemList = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_track, parent, false);
        return new MyviewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyviewHolder holder, int position) {
        try {
            TrackModel track = trackList.get(position);

            if (!track.getImageList().isEmpty())
                Glide.with(context).load(track.getImageList().get(0).getUrl())
                        .placeholder(R.drawable.image_loading)
                        .error(R.drawable.image_loading_error)
                        .centerCrop()
                        .into(holder.ivBackground);
            else
                Glide.with(context).load(R.drawable.no_photo_track).into(holder.ivBackground);


            holder.tvDistance.setText(Util.formatMeterToKm(track.getDistance()));
            holder.ratingBar.setRating(track.getRate() / track.getRateCount());
            holder.tvName.setText(track.getTitle());
            holder.tvLocation.setText(track.getLocation());
            holder.tvRateCount.setText(new StringBuffer().append("(").append(track.getRateCount()).append(")"));

            Util.getDifficultForTextView(track.getDifficulty(), holder.tvDifficult, context);

            //set labels in the bottom of image in case be your own track or offline
            holder.tvMytrack.setVisibility(View.GONE);
            holder.tvOffline.setVisibility(View.GONE);
            holder.ivFavorite.setVisibility(View.GONE);
            if (track.getOwnerId().equals(UserFirebase.getIdUserBase64()))
                holder.tvMytrack.setVisibility(View.VISIBLE);
            if (track.getTrackDatabaseId() != null)
                holder.tvOffline.setVisibility(View.VISIBLE);
            if (DataSingleton.getInstance().getCurrentUser() != null && DataSingleton.getInstance().getCurrentUser().getFavorites().contains(track.getId()))
                holder.ivFavorite.setVisibility(View.VISIBLE);


        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return trackList.size();
    }

    public class MyviewHolder extends RecyclerView.ViewHolder {

        TextView tvDistance, tvName, tvLocation, tvDifficult, tvRateCount, tvMytrack, tvOffline;
        ImageView ivBackground, ivFavorite;
        RatingBar ratingBar;

        public MyviewHolder(View itemView) {
            super(itemView);

            tvDistance = itemView.findViewById(R.id.tv_distance);
            ratingBar = itemView.findViewById(R.id.rating_bar_adapter);
            tvRateCount = itemView.findViewById(R.id.tv_rate_count);
            tvName = itemView.findViewById(R.id.tv_track_name);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDifficult = itemView.findViewById(R.id.tv_difficult);
            ivFavorite = itemView.findViewById(R.id.img_favorite);

            //labels to show if the track is yours and offline as well
            tvMytrack = itemView.findViewById(R.id.tv_my_track);
            tvOffline = itemView.findViewById(R.id.tv_offline_track);

            ivBackground = itemView.findViewById(R.id.img_background);
        }
    }
}
