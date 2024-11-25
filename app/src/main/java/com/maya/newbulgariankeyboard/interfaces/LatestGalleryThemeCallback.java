package com.maya.newbulgariankeyboard.interfaces;

import com.maya.newbulgariankeyboard.models.LatestGalleryThemeModel;

public interface LatestGalleryThemeCallback {

    void onThemeSelected(LatestGalleryThemeModel modelop);

    void onThemePickingClicked();

    void onThemeDeleted(LatestGalleryThemeModel model);
}
