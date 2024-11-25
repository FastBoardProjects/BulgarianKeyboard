package com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maya.newbulgariankeyboard.R;

import java.util.ArrayList;

public class LatestRecentEmojiAdapter extends RecyclerView.Adapter<LatestRecentEmojiAdapter.EmojiViewHolder> {
    private final Context context;
    private final ArrayList<LatestEmojiDbModel> list;
    private final LatestRecentAdapterCallback callback;

    public LatestRecentEmojiAdapter(Context context, ArrayList<LatestEmojiDbModel> list, LatestRecentAdapterCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_recent_emoji_recycler_view, parent, false);
        return new EmojiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        LatestEmojiDbModel model = list.get(position);
        holder.tvEmoji.setText(model.getItemEmoji());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ClickDet:"," Emoji Clicked");
                callback.onEmojiItemClicked(model);
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class EmojiViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmoji;

        public EmojiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);

        }
    }
}
