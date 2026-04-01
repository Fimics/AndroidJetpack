package com.mic.nav.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;


import static android.view.animation.Animation.REVERSE;

public class WHEqualImageView extends ImageView implements View.OnClickListener {

    public WHEqualImageView(Context context) {
        this(context, null);
    }

    public WHEqualImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WHEqualImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);

    }
    public void shakeImage() {
        clearAnimation();
        AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation1.setDuration(500);
        alphaAnimation1.setRepeatCount(3);
        alphaAnimation1.setRepeatMode(REVERSE);
        startAnimation(alphaAnimation1);
        Log.i("Image", "shake image");
    }

    @Override
    public void onClick(View v) {
        shakeImage();
    }
}