package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs;

import android.content.Context;
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
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.maya.newbulgariankeyboard.BuildConfig;
import com.maya.newbulgariankeyboard.R;
import com.maya.newbulgariankeyboard.gif_model.Datum;
import com.tuyenmonkey.mkloader.MKLoader;

import java.io.File;
import java.util.ArrayList;

public class LatestAppGifAdapter extends RecyclerView.Adapter<LatestAppGifAdapter.GifViewHolder> {

    private final Context context;
    private final ArrayList<Datum> list;
    private final LatestGifAdapterCallback callback;
    private final ArrayList<Request> requestQueue = new ArrayList<>();

    public LatestAppGifAdapter(Context context, ArrayList<Datum> list, LatestGifAdapterCallback callback) {
        this.context = context;
        this.list = list;
        this.callback = callback;
    }

    @NonNull
    @Override
    public GifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_gif_recycler_view, parent, false);
        return new GifViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull GifViewHolder holder, int position) {
        Datum model = list.get(position);
        setAndLoadImage(context, holder.ivGif,model, holder.mkLoader, model.getImages()
                .getDownsized().getUrl(), model.getImages()
                .getOriginal().getUrl(), null);

    }

    private void setAndLoadImage(final Context context, final ImageView img,Datum modelDatum ,final MKLoader mkLoader, final String url, final String originalUrl, final LatestImageCallback latestImageCallback) {
        mkLoader.setVisibility(View.VISIBLE);
        Log.d("ZohaibLogger:", "setAndLoadImage Url: " + url);
        //img.setLayoutParams(new ConstraintLayout.LayoutParams(300, 300));
        //img.setPadding(10, 5, 10, 5);
        Request request = Glide.with(context).asFile().apply(new RequestOptions().timeout(30000)).load(url).listener(new RequestListener<File>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                Log.d("ZohaibLogger:", "onLoadFailed 1");
                //img.setImageDrawable(context.getResources().getDrawable(R.drawable.expand_icon));
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        setAndLoadImage(context, img,modelDatum, mkLoader, url, originalUrl, latestImageCallback);
                    }
                });
                return false;
            }

            @Override
            public boolean onResourceReady(final File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                Log.d("ZohaibLogger:", "onResourceReady 1: " + resource);
                new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("UI thread", "I am the UI thread");
                        Glide.with(context)
                                .asGif()
                                .load(resource).addListener(new RequestListener<GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                Log.d("ZohaibLogger:", "onLoadFailed 2: " + e.getLocalizedMessage());
                                mkLoader.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable drawable, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {

                                Log.d("ZohaibLogger:", "onResourceReady 2");
                                mkLoader.setVisibility(View.GONE);
                                float width = drawable.getIntrinsicWidth();
                                float height = drawable.getIntrinsicHeight();
                                mkLoader.setVisibility(View.GONE);

                                img.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        File gifFile = new File(resource.getAbsolutePath() + ".gif");


                                        if (resource.renameTo(gifFile) || gifFile.exists()) {
                                            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, gifFile);
                                            Log.d("ZohaibLogger:", "Storage Path: " + gifFile.getAbsolutePath());
                                            Log.d("ZohaibLogger:", "Storage Uri: " + uri);
                                            callback.onGifItemClicked(uri,modelDatum);
                                        }
                                    }
                                });

                                if (latestImageCallback != null) latestImageCallback.onLoaded();

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

    class GifViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivGif;
        private final MKLoader mkLoader;

        public GifViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGif = itemView.findViewById(R.id.ivGif);
            mkLoader = itemView.findViewById(R.id.mkLoader);
            // mkLoader.setForegroundTintList(ColorStateList.valueOf(prefs.getTheme().getMediaFgColor()));

        }
    }
}
