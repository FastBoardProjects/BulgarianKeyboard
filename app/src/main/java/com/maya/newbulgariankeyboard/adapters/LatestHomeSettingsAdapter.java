package com.maya.newbulgariankeyboard.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.interfaces.LatestHomeItemCallback;
import com.maya.newbulgariankeyboard.models.LatestSettingsModel;

import java.util.ArrayList;

public class LatestHomeSettingsAdapter extends RecyclerView.Adapter<LatestHomeSettingsAdapter.SettingsViewHolder> {

    private final Context context;
    private final ArrayList<LatestSettingsModel> list;
    private final LatestHomeItemCallback callback;


    public LatestHomeSettingsAdapter(Context context, ArrayList<LatestSettingsModel> list, LatestHomeItemCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_rv_settings_list, parent, false);
        return new SettingsViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LatestSettingsModel model = list.get(position);
        Glide.with(context).load(model.getItemIcon()).into(holder.ivItem);
        holder.tvItem.setText(model.getItemName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onItemSelected(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class SettingsViewHolder extends RecyclerView.ViewHolder {

        ImageView ivItem;
        TextView tvItem;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItem = itemView.findViewById(R.id.ivItem);
            tvItem = itemView.findViewById(R.id.tvItem);
        }
    }
}
