package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_gifs;

import android.net.Uri;

import com.maya.newbulgariankeyboard.gif_model.Datum;


public interface LatestGifAdapterCallback {
    void onGifItemClicked(Uri itemUri, Datum model);
}
