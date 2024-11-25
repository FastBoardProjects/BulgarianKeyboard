package com.maya.newbulgariankeyboard.media_inputs.keyboard_giphy_stickers;

import android.net.Uri;

import com.maya.newbulgariankeyboard.gif_model.Datum;

public interface LatestGifStickerAdapterCallback {
    void onGifItemClicked(Uri itemUri, Datum model);
}
