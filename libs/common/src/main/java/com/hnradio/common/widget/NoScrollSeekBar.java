package com.hnradio.common.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

/**
 * 手动控制seekbar是否可以滑动
 */
public class NoScrollSeekBar extends AppCompatSeekBar {
    private boolean isCanScroll = false;

    public NoScrollSeekBar(Context context) {
        super(context);
    }

    public NoScrollSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置其是否能滑动
     * @param isCanScroll false 不能， true 可以
     */
    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return isCanScroll && super.onTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}