package com.mic.nav.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;


import java.util.ArrayList;
import java.util.List;

import com.noetix.robotics.AppConfig;
import com.noetix.robotics.util.VisualizerHelper;

/**
 * 柱状
 */
public class ColumnarView extends View implements VisualizerHelper.OnVisualizerEnergyCallBack {
    private static final String TAG ="ColumnarView";
    //每一个能量柱的宽度
    private int width;
    //每一个能量柱之间的间距
    private final int spacing = 1;
    //能量块高度
    private final int blockHeight = 5;
    //能量块下将速度
    private int blockSpeed = 3;
    //能量块与能量柱之间的距离
    private final int distance = 2;

    private final Paint paint = new Paint();
    private final List<Rect> newData = new ArrayList<>();
    private final List<Rect> blockData = new ArrayList<>();

    public void setBlockSpeed(int blockSpeed) {
        this.blockSpeed = blockSpeed;
    }

    public ColumnarView(Context context) {
        super(context);
    }

    public ColumnarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColumnarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setWaveData(byte[] data, float totalEnergy) {
//        KLog.d(TAG,"width ->"+getWidth());
        int lumpCount = AppConfig.smallScreen?AppConstant.LUMP_COUNT_SMALL:AppConstant.LUMP_COUNT;
        width = getWidth() /lumpCount ;
//        paint.setColor(AppConstant.COLOR);
        if (newData.size() > 0) {
            if (blockData.size() == 0 || newData.size() != blockData.size()) {
                blockData.clear();
                for (int i = 0; i < data.length; i++) {
                    Rect rect = new Rect();
                    rect.top = getHeight() - blockHeight;
                    rect.bottom = getHeight();
                    blockData.add(rect);
                }
            }
            for (int i = 0; i < blockData.size(); i++) {
                blockData.get(i).left = newData.get(i).left;
                blockData.get(i).right = newData.get(i).right;
//                Log.e("SSSSS", newData.get(i).top + "---" + blockData.get(i).top);
                if (newData.get(i).top < blockData.get(i).top) {
                    blockData.get(i).top = newData.get(i).top - blockHeight - distance;
                } else {
                    blockData.get(i).top = blockData.get(i).top + blockSpeed;
                }
                blockData.get(i).bottom = blockData.get(i).top + blockHeight;


            }
        }

        newData.clear();
        for (int i = 0; i < data.length; i++) {
            Rect rect = new Rect();
            if (newData.size() == 0) {
                rect.left = 0;
            } else {
                rect.left = newData.get(newData.size() - 1).right + spacing;
            }
            rect.top = getHeight() - data[i] * AppConstant.LAGER_OFFSET;
            rect.right = rect.left + width-spacing;
            rect.bottom = getHeight();
            newData.add(rect);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        paint.setShader(new LinearGradient(0f, 0f, getWidth(), getHeight(), new int[]{0xffff0000, 0xff00ff00, 0xff0000ff}, new float[]{0, 0.5f, 1f}, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < newData.size(); i++) {
            canvas.drawRect(newData.get(i), paint);
        }
        for (int i = 0; i < blockData.size(); i++) {
            canvas.drawRect(blockData.get(i), paint);
        }
        invalidate();
    }
}
