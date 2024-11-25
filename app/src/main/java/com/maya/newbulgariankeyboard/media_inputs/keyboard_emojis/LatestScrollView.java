package com.maya.newbulgariankeyboard.media_inputs.keyboard_emojis;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

public class LatestScrollView extends ScrollView { OnBottomReachedListener mListener;

    public LatestScrollView(Context context, AttributeSet attrs,
                            int defStyle) {
        super(context, attrs, defStyle);
    }

    public LatestScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LatestScrollView(Context context) {
        super(context);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = getChildAt(getChildCount() - 1);
        int diff = (view.getBottom() - (getHeight() + getScrollY())) -  view.getPaddingBottom();

        if (diff <= 0 && mListener != null) {
            mListener.onBottomReached();
        }

        super.onScrollChanged(l, t, oldl, oldt);
    }

    // Getters & Setters

    public OnBottomReachedListener getOnBottomReachedListener() {
        return mListener;
    }

    public void setOnBottomReachedListener(
            OnBottomReachedListener onBottomReachedListener) {
        mListener = onBottomReachedListener;
    }

    //Event listener.

    public interface OnBottomReachedListener {
        void onBottomReached();
    }
}
