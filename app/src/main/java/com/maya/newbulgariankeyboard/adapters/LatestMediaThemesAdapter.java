package com.maya.newbulgariankeyboard.adapters;

import static com.maya.newbulgariankeyboard.adapters.LatestColoredThemesAdapter.differentiateTheme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.interfaces.LatestMediaThemeCallback;
import com.maya.newbulgariankeyboard.models.LatestMediaThemeModel;

import java.util.ArrayList;

public class LatestMediaThemesAdapter extends RecyclerView.Adapter<LatestMediaThemesAdapter.ThemesViewHolder> {

    private final Context context;
    private final ArrayList<LatestMediaThemeModel> list;
    private final LatestMediaThemeCallback callback;
    private final SharedPreferences sharedPreferences;
    private int themeSelected = 0;
    public static Boolean isFreeAllowedMediaTheme = false;
    
    public static ArrayList<Integer> freeIndexsMediaTheme = new ArrayList<Integer>();

    public LatestMediaThemesAdapter(Context context, ArrayList<LatestMediaThemeModel> list, LatestMediaThemeCallback callback) {
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
                .inflate(R.layout.layout_item_theme_gradients, parent, false);
        return new ThemesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemesViewHolder holder, @SuppressLint("RecyclerView") int position) {

        
        if (!differentiateTheme(position) && !isFreeAllowedMediaTheme && !freeIndexsMediaTheme.contains(position)) {
            holder.premiumButton.setText("Premium");
            holder.premiumButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFD700")));  // Red tint
            holder.premiumButton.setTextColor(Color.BLACK);
        } else {
            holder.premiumButton.setText("Free");
            holder.premiumButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF377EF1")));  // Red tint
            holder.premiumButton.setTextColor(Color.WHITE);
        }

        LatestMediaThemeModel model = list.get(position);
        try {
            themeSelected = sharedPreferences.getInt("Theme", 48);
            if (themeSelected == model.getItemId()) {
                holder.ivSelected.setVisibility(View.VISIBLE);
            } else {
                holder.ivSelected.setVisibility(View.GONE);
            }
            try {
                holder.viewMain.setBackgroundResource(model.getItemBgShape());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sharedPreferences.edit().putInt("Theme", model.getItemId()).apply();
                        themeSelected = model.getItemId();
                        callback.onThemeSelected(model,position);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.premiumButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharedPreferences.edit().putInt("Theme", model.getItemId()).apply();
                    themeSelected = model.getItemId();
                    callback.onThemeSelected(model,position);
                    //notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
