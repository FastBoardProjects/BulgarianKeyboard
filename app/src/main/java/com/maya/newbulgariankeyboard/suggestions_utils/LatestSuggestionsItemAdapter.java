package com.maya.newbulgariankeyboard.suggestions_utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.main_classes.LatestPreferencesHelper;

import java.util.ArrayList;

public class LatestSuggestionsItemAdapter extends RecyclerView.Adapter<LatestSuggestionsItemAdapter.SuggestionViewHolder> {

    private final Context context;
    private final ArrayList<String> list;
    private final LatestSuggestionsItemCallback callback;
    private final LatestPreferencesHelper prefs;

    public LatestSuggestionsItemAdapter(Context context, ArrayList<String> list, LatestSuggestionsItemCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
        prefs = LatestPreferencesHelper.Companion.getDefaultInstance(context);
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_rv_suggestions, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final String model = list.get(position);
        holder.cvItemMain.setCardBackgroundColor(prefs.getMThemingApp().getSmartbarButtonBgColor());
        holder.tvSuggestions.setTextColor(prefs.getMThemingApp().getSmartbarButtonFgColor());
        holder.tvSuggestions.setText(model);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSuggestionItemClicked(model, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestions;
        CardView cvItemMain;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSuggestions = itemView.findViewById(R.id.tvSuggestions);
            cvItemMain = itemView.findViewById(R.id.cvItemMain);
            //setScaleAnimation(itemView);
        }
    }

    public static void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(500);
        view.startAnimation(anim);
    }
}
