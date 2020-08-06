package com.fernando.footpath.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.fernando.footpath.R;
import com.fernando.footpath.interfaces.ItemClickListener;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private List<String> imageUrls;

    private ItemClickListener itemClickListener;

    public ViewPagerAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        if (imageUrls.isEmpty())
            return 1;

        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.pager_row, container, false);

        ImageView image = view.findViewById(R.id.image_view_pager);

        if (imageUrls.isEmpty()) {
            Glide.with(context)
                    .load(R.drawable.add_image)
                    .error(R.drawable.image_loading_error)
                    .placeholder(R.drawable.image_loading)
                    .centerCrop()
                    .into(image);

        } else {
            Glide.with(context)
                    .load(imageUrls.get(position))
                    .error(R.drawable.image_loading_error)
                    .placeholder(R.drawable.image_loading)
                    .centerCrop()
                    .into(image);
        }


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemSelected(position);
            }
        });

        container.addView(view);
        return view;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}