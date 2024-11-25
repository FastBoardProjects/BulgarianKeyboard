package com.maya.newbulgariankeyboard.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.interfaces.LatestDefaultThemeCallback;
import com.maya.newbulgariankeyboard.models.LatestThemeModel;

import java.util.ArrayList;

public class LatestColoredThemesAdapter extends RecyclerView.Adapter<LatestColoredThemesAdapter.ThemesViewHolder> {

    private final Context context;
    private final ArrayList<LatestThemeModel> list;
    private final LatestDefaultThemeCallback callback;
    private final SharedPreferences sharedPreferences;
    private int themeSelected = 0;
    public static Boolean isFreeAllowedColorTheme = false;
    
    public static ArrayList<Integer> freeIndexsColorTheme = new ArrayList<Integer>();


    public LatestColoredThemesAdapter(Context context, ArrayList<LatestThemeModel> list, LatestDefaultThemeCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
        sharedPreferences = context.getSharedPreferences("Themes", Context.MODE_PRIVATE);
        themeSelected = sharedPreferences.getInt("Theme", 48);
    }

    @NonNull
    @Override
    public ThemesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_theme_colored, parent, false);
        return new ThemesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemesViewHolder holder, @SuppressLint("RecyclerView") int position) {


        
        if (!differentiateTheme(position) && !isFreeAllowedColorTheme && !freeIndexsColorTheme.contains(position)) {
            Log.d("onBindViewHolder123", "onBindViewHolder: 1 "+!differentiateTheme(position)+" 2 "+!isFreeAllowedColorTheme+" 3 "+!freeIndexsColorTheme.contains(position));
            holder.premiumButton.setText("Premium");
            holder.premiumButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));  // Red tint
            holder.premiumButton.setTextColor(Color.BLACK);
        } else {
            Log.d("onBindViewHolder123", "onBindViewHolder: Bottom");
            holder.premiumButton.setText("Free");
            holder.premiumButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF377EF1")));  // Red tint
            holder.premiumButton.setTextColor(Color.WHITE);
        }
        LatestThemeModel model = list.get(position);
        themeSelected = sharedPreferences.getInt("Theme", 48);
        if (themeSelected == model.getItemId()) {
            holder.ivSelected.setVisibility(View.VISIBLE);
        } else {
            holder.ivSelected.setVisibility(View.GONE);
        }
        holder.viewMain.setBackgroundColor(Color.parseColor(model.getItemBgColor()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putInt("Theme", model.getItemId()).apply();
                themeSelected = model.getItemId();
                callback.onThemeSelected(model,position);
                //notifyDataSetChanged();
            }
        });

        holder.premiumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedPreferences.edit().putInt("Theme", model.getItemId()).apply();
                themeSelected = model.getItemId();
                callback.onThemeSelected(model,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ThemesViewHolder extends RecyclerView.ViewHolder {

        View viewMain;
        View viewTop;
        TextView tvViewBottom;
        ImageView ivSelected;
        Button premiumButton;

        public ThemesViewHolder(@NonNull View itemView) {
            super(itemView);
            viewMain = itemView.findViewById(R.id.viewMain);
            viewTop = itemView.findViewById(R.id.viewTop);
            tvViewBottom = itemView.findViewById(R.id.tvViewBottom);
            ivSelected = itemView.findViewById(R.id.ivSelected);
            premiumButton = itemView.findViewById(R.id.premiumButton);
        }
    }

    public static boolean differentiateTheme(int i) {
        switch (i % 8) {
            case 0:
            case 1:
            case 2:
            case 3:

                return true;
            case 6:
            case 4:
            case 5:
            case 7:
                return false;
            default:
                return false; // To handle all cases (although unnecessary here)
        }
    }
}
