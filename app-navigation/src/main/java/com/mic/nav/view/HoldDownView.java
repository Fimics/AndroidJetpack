package com.mic.nav.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class HoldDownView extends FrameLayout {

    // 事件监听接口（每个事件只回调一次）
    public interface OnHoldDownListener {
        void onPress();       // 按下时立即触发（1次）
        void onRelease();     // 抬起时立即触发（1次）
        void onLongPress();   // 长按达到阈值后触发（1次）
    }

    private static final int LONG_PRESS_TIMEOUT = 10; // 长按判定时间（毫秒）
    private final Handler mHandler = new Handler();
    private OnHoldDownListener mListener;
    private boolean isLongPressTriggered = false; // 长按是否已触发

    public HoldDownView(Context context) {
        super(context);
    }

    public HoldDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HoldDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 设置事件监听器
    public void setOnHoldDownListener(OnHoldDownListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handlePressAction();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handleReleaseAction();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handlePressAction() {
        isLongPressTriggered = false;
        if (mListener != null) mListener.onPress();

        // 启动长按检测
        mHandler.postDelayed(() -> {
            if (!isLongPressTriggered) {
                isLongPressTriggered = true;
                if (mListener != null) mListener.onLongPress();
            }
        }, LONG_PRESS_TIMEOUT);
    }

    private void handleReleaseAction() {
        mHandler.removeCallbacksAndMessages(null); // 清除所有回调
        if (mListener != null) mListener.onRelease();
    }
}
