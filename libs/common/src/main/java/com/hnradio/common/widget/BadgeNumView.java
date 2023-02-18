package com.hnradio.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ytf on 2019/1/10.
 * Description:
 */
public class BadgeNumView extends View
{
    private Paint mPaint;
    private TextPaint textPaint;
    private int bgColor = Color.RED;
    private int textColor = Color.WHITE;

    public BadgeNumView(Context context)
    {
        this(context, null);
    }

    public BadgeNumView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public BadgeNumView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(bgColor);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStrokeMiter(2f);
        textPaint.setColor(textColor);
    }

    public void setBgColor(int bgColor)
    {
        this.bgColor = bgColor;
        mPaint.setColor(bgColor);
        invalidate();
    }

    public void setTextColor(int textColor)
    {
        this.textColor = textColor;
        textPaint.setColor(textColor);
        invalidate();
    }

    private int num;
    public void setNum(int n)
    {
        if(n >= 0)
        {
            num = n;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if(num > 0)
        {
            canvas.drawCircle(halfWidth, halfHeight, halfHeight, mPaint);
            String nv = num > 99 ? "99+" : String.valueOf(num);
            float unitWid = getMeasuredWidth() * 1.0f/ nv.length();
            textPaint.setTextSize(unitWid);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            canvas.drawText(String.valueOf(num), halfWidth, halfHeight + (fm.descent - fm.ascent)/2 - fm.descent , textPaint);
        }
    }

    private float halfWidth = 0f;
    private float halfHeight = 0f;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        halfWidth = w / 2f;
        halfHeight = h / 2f;
    }
}
