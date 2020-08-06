package com.fernando.footpath.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.fernando.footpath.R;
import com.fernando.footpath.adapter.ViewPagerAdapter;
import com.fernando.footpath.interfaces.ItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ImageSlider extends RelativeLayout {

    private Context context;
    private ViewPager viewPager;
    private LinearLayout pagerDots;
    private ViewPagerAdapter viewPagerAdapter;
    private List<ImageView> dots;

    private int imageCount;
    private int selectedDot;
    private int unselectedDot;

    public ImageSlider(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        LayoutInflater.from(getContext()).inflate(R.layout.image_slider, this, true);
        viewPager = findViewById(R.id.view_pager_slider);
        pagerDots = findViewById(R.id.pager_dots);

        selectedDot = R.drawable.selected_dot;
        unselectedDot = R.drawable.unselected_dot;
    }

    public void setImageList(List<String> imageList) {
        viewPagerAdapter = new ViewPagerAdapter(context, imageList);
        viewPager.setAdapter(viewPagerAdapter);
        imageCount = imageList.size();

        pagerDots.removeAllViews();

        if (imageList.size() > 1)
            setDots(imageList.size());
    }

    private void setDots(int size) {
        dots = new ArrayList<>(imageCount);

        for (int i = 0; i < size; i++) {
            ImageView img = new ImageView(context);
            img.setImageDrawable(ContextCompat.getDrawable(context, unselectedDot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            pagerDots.addView(img, params);
            dots.add(i, img);

        }
        dots.get(0).setImageDrawable(ContextCompat.getDrawable(context, selectedDot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (ImageView dot : dots)
                    dot.setImageDrawable(ContextCompat.getDrawable(context, unselectedDot));

                dots.get(position).setImageDrawable(ContextCompat.getDrawable(context, selectedDot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        viewPagerAdapter.setItemClickListener(itemClickListener);
    }
}
