package com.khanhtq.circleprogressview.util;

import android.content.res.Resources;

/**
 * Created by khanhtq on 25/09/2017.
 */

public class ScreenUtil {
    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }
}
