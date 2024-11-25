package com.maya.newbulgariankeyboard.main_utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.maya.newbulgariankeyboard.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

public class LatestSliderAdapter extends
        SliderViewAdapter<LatestSliderAdapter.SliderAdapterVH>{

    public Context context;
    public int mCount;

    public LatestSliderAdapter(Context context) {
        this.context = context;
    }

    public void setCount(int count) {
        this.mCount = count;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_items, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {
        switch (position) {
            case 0:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.ic_totorial_1)
                        .fitCenter()
                        .into(viewHolder.iv_auto_image_slider);
                break;
            case 1:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.ic_totorial_2)
                        .fitCenter()
                        .into(viewHolder.iv_auto_image_slider);
                break;
            case 2:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.ic_totorial_3)
                        .fitCenter()
                        .into(viewHolder.iv_auto_image_slider);
                break;
            case 3:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.ic_totorial_4)
                        .fitCenter()
                        .into(viewHolder.iv_auto_image_slider);
                break;
            default:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.ic_totorial_2)
                        .fitCenter()
                        .into(viewHolder.iv_auto_image_slider);
                break;

        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView iv_auto_image_slider;



        public SliderAdapterVH(View itemView) {
            super(itemView);
            iv_auto_image_slider = itemView.findViewById(R.id.iv_auto_image_slider);
            this.itemView = itemView;
        }
    }


}