package com.maya.newbulgariankeyboard.media_inputs.keyboard_stickers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.maya.newbulgariankeyboard.BuildConfig;
import com.maya.newbulgariankeyboard.R;
import com.tuyenmonkey.mkloader.MKLoader;

import java.io.File;
import java.util.ArrayList;

public class LatestStickersAdapter extends RecyclerView.Adapter<LatestStickersAdapter.StickerViewHolder> {
    private final Context context;
    private final ArrayList<LatestStickerModel> list;
    private final LatestStickerAdapterCallback callback;
    private final ArrayList<Request> requestQueue = new ArrayList<>();
    // private AppPreferencesHelper prefs;

    public LatestStickersAdapter(Context context, ArrayList<LatestStickerModel> list, LatestStickerAdapterCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
        //prefs = AppPreferencesHelper.Companion.getDefaultInstance(context);
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_sticker_recycler_view, parent, false);
        return new StickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        LatestStickerModel model = list.get(position);
        setAndLoadImage(context, holder.ivGif, holder.mkLoader, model, model.getUrl());
    }

    private void setAndLoadImage(final Context context, final ImageView img, final MKLoader mkLoader, LatestStickerModel modelSticker, final String url) {
        mkLoader.setVisibility(View.VISIBLE);
        Log.d("StickerLogger:", "setAndLoadImage Url: " + url);
        Request request = Glide.with(context).asFile().apply(new RequestOptions().timeout(30000)).load(url).listener(new RequestListener<File>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                Log.d("StickerLogger:", "onLoadFailed 1");
                //img.setImageDrawable(context.getResources().getDrawable(R.drawable.expand_icon));
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        setAndLoadImage(context, img, mkLoader, modelSticker, url);
                    }
                });
                return false;
            }

            @Override
            public boolean onResourceReady(final File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                Log.d("StickerLogger:", "onResourceReady 1: " + resource);
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("UI thread", "I am the UI thread");
                        Glide.with(context).load(resource)
                                .addListener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        Log.d("StickerLogger:", "onLoadFailed 2: " + e.getLocalizedMessage());
                                        mkLoader.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable drawable, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        Log.d("StickerLogger:", "onResourceReady 2");
                                        mkLoader.setVisibility(View.GONE);
                                        float width = drawable.getIntrinsicWidth();
                                        float height = drawable.getIntrinsicHeight();
                                        mkLoader.setVisibility(View.GONE);

                                        img.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                File gifFile = new File(resource.getAbsolutePath() + ".webp");

                                                if (resource.renameTo(gifFile) || gifFile.exists()) {
                                                    Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, gifFile);
                                                    Log.d("StickerLogger:", "Storage Path: " + gifFile.getAbsolutePath());
                                                    Log.d("StickerLogger:", "Storage Uri: " + uri);
                                                    callback.onStickerItemClicked(modelSticker, uri);
                                                }
                                            }
                                        });

                                        return false;
                                    }
                                }).into(img);

                    }
                });

                return false;
            }
        }).submit().getRequest();
        requestQueue.add(request);
        //clearAllRequests();
    }

    private void clearAllRequests() {
        for (int i = 0; i < requestQueue.size(); i++) {
            requestQueue.get(i).clear();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class StickerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivGif;
        private final MKLoader mkLoader;

        public StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGif = itemView.findViewById(R.id.ivGif);
            mkLoader = itemView.findViewById(R.id.mkLoader);
            // mkLoader.setForegroundTintList(ColorStateList.valueOf(prefs.getTheme().getMediaFgColor()));

        }
    }
}
