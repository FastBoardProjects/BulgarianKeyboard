package com.maya.newbulgariankeyboard.main_utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import com.maya.newbulgariankeyboard.R;

public class LatestCategoryPreferenceCompat extends PreferenceCategory {
    public LatestCategoryPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LatestCategoryPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LatestCategoryPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LatestCategoryPreferenceCompat(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
//        /*Change Bg color*/
//        holder.itemView.setBackgroundColor(Color.BLACK);
        /*Change title Color*/


        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.textAppearanceBody1, typedValue, true);
        int topColor = typedValue.data;

        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        titleView.setTextSize(22);
        titleView.setTextColor(topColor);

        TextView sumamryView = (TextView) holder.findViewById(android.R.id.summary);
        sumamryView.setTextColor(topColor);
    }
}
