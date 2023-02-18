package com.hnradio.common.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.hnradio.common.R
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.random.Random

class WaveIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var color = Color.WHITE
    private var barWidth = 1f
    private var barInterval = 3
    private var textSize = 10f

    /**单位秒*/
    private var duration = 0

    fun updateDuration(du : Int){
        duration = if(du > 60){
            60
        }else{
            du
        }
        if(duration == 0){
            barCount = 15
        }
        requestLayout()
        invalidate()
    }

    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WaveIndicator)
        color = ta.getColor(R.styleable.WaveIndicator_wic_color, Color.BLACK)
        barWidth = ta.getDimension(R.styleable.WaveIndicator_wic_bar_width, 1f)
        textSize = ta.getDimension(R.styleable.WaveIndicator_wic_txt_size, 10f)
        ta.recycle()
        mPaint.setColor(color)
    }

    private var barsPer10Seconds = 10
    private var barCount = 0
    private var margin = 20

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var twid = 0f
        if(duration != 0){
            val txt = "${duration}\""
            mPaint.textSize = textSize
            twid = mPaint.measureText(txt)
            barCount = (ceil(duration / 10f) * barsPer10Seconds).toInt()
        }

        val vwid = barCount * barWidth + (barCount-1)*barInterval
        setMeasuredDimension((vwid + margin + twid).toInt(), heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = barWidth
        mPaint.strokeCap = Paint.Cap.ROUND
        canvas.translate(0f, measuredHeight/2f)
        val halfBar = barWidth / 2

        for (i in 0 until barCount){
            val bx = halfBar +  i * (barWidth+barInterval)
            val barHeight = getBarHeight(i)
            val half = barHeight / 2
            canvas.drawLine(bx, -half + halfBar, bx, half-halfBar, mPaint)
        }
        if(duration != 0){
            mPaint.style = Paint.Style.FILL
            canvas.drawText("${duration}\"",
                halfBar +  (barCount-1) * (barWidth+barInterval) + margin,
                mPaint.baseline(),mPaint)
        }
    }

    fun Paint.baseline() : Float{
        val fm = fontMetrics
        return (fm.descent - fm.ascent) / 2 - fm.descent
    }

    private fun getBarHeight(index : Int) : Float{
        val height = measuredHeight.toFloat()
        var hei =  (height * abs(sin((index * 2 * Math.PI / 180) * Random.nextFloat()* 5f))).toFloat()
        if(hei > height){
            hei = height
        }
        return hei
    }
}