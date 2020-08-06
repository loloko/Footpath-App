package com.fernando.footpath.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fernando.footpath.R;
import com.fernando.footpath.model.TypeModel;

import java.util.List;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.ViewHolder> {
    private static final String TAG = "TypeAdapter";

    private List<TypeModel> types;
    private Context context;

    public TypeAdapter(Context context, List<TypeModel> types) {
        this.context = context;
        this.types = types;
    }

    @Override
    public TypeAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_type_track, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        try {
            viewHolder.img.setImageResource(types.get(i).getIcon());
            viewHolder.text.setText(types.get(i).getName());
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView text;

        public ViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.img_adapter);
            text = view.findViewById(R.id.tv_type);

        }
    }
}