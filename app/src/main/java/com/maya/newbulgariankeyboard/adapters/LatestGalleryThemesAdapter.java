package com.maya.newbulgariankeyboard.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.interfaces.LatestGalleryThemeCallback;
import com.maya.newbulgariankeyboard.models.LatestGalleryThemeModel;

import java.io.File;
import java.util.ArrayList;

public class LatestGalleryThemesAdapter extends RecyclerView.Adapter<LatestGalleryThemesAdapter.GalleryThemesViewHolder> {

    private final Context context;
    private final ArrayList<LatestGalleryThemeModel> list;
    private final LatestGalleryThemeCallback callback;
    private final SharedPreferences sharedPreferences;
    private int themeSelected = 0;

    public LatestGalleryThemesAdapter(Context context, ArrayList<LatestGalleryThemeModel> list, LatestGalleryThemeCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
        sharedPreferences = context.getSharedPreferences("Themes", Context.MODE_PRIVATE);
        themeSelected = sharedPreferences.getInt("Theme", 48);
    }

    @NonNull
    @Override
    public GalleryThemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_theme_gallery, parent, false);
        return new GalleryThemesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryThemesViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LatestGalleryThemeModel model = list.get(position);
        File file = new File(model.getItemBgImage());
        themeSelected = sharedPreferences.getInt("Theme", 48);
        if (position == 0) {
            holder.ivMenu.setVisibility(View.GONE);
            holder.ivAdd.setVisibility(View.VISIBLE);
        } else {
            holder.ivMenu.setVisibility(View.VISIBLE);
            holder.ivAdd.setVisibility(View.GONE);
        }
        if (themeSelected == ((int) model.getItemId()) /*&& file.exists()*/) {
            holder.ivSelected.setVisibility(View.VISIBLE);
        } else {
            holder.ivSelected.setVisibility(View.GONE);
        }

        if (file.exists()) {
                Glide.with(context).load(model.getItemBgImage()).into(holder.viewMain);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
//                Toast.makeText(context, "Clicked. "+position, Toast.LENGTH_LONG)
//                        .show();
                if (position == 0) {
                    callback.onThemePickingClicked();
                } else {
                    /*if (file.exists()) {*/
                    sharedPreferences.edit().putInt("Theme", (int) model.getItemId()).apply();
                    themeSelected = (int) model.getItemId();
                    callback.onThemeSelected(model);
                    /*  }*/
                }
            }
        });
        holder.ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (themeSelected == ((int) model.getItemId())) {
                    Toast.makeText(context, "Theme is already in use.\n Change theme first to delete this theme.", Toast.LENGTH_LONG)
                            .show();
                } else {
                    callback.onThemeDeleted(model);
                }
            }
        });
    }

    public void setSpecificTheme(int themeId) {
        sharedPreferences.edit().putInt("Theme", themeId).apply();
        themeSelected = themeId;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class GalleryThemesViewHolder extends RecyclerView.ViewHolder {

        ImageView viewMain;
        View viewTop;
        TextView tvViewBottom;
        ImageView ivSelected;
        ImageView ivAdd;
        ImageView ivMenu;

        public GalleryThemesViewHolder(@NonNull View itemView) {
            super(itemView);
            viewMain = itemView.findViewById(R.id.viewMain);
            viewTop = itemView.findViewById(R.id.viewTop);
            tvViewBottom = itemView.findViewById(R.id.tvViewBottom);
            ivSelected = itemView.findViewById(R.id.ivSelected);
            ivAdd = itemView.findViewById(R.id.ivAdd);
            ivMenu = itemView.findViewById(R.id.ivMenu);
        }
    }
}
