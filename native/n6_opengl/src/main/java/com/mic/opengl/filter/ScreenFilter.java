package com.mic.opengl.filter;

import android.content.Context;

import com.mic.opengl.R;

/**
 * 负责往屏幕上渲染
 */
public class ScreenFilter extends BaseFilter {

    public ScreenFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_fragment);
    }

}
