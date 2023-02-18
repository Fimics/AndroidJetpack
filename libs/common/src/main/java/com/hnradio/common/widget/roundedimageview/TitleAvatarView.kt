package com.hnradio.common.widget.roundedimageview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.hnradio.common.R
import com.hnradio.common.util.GlideUtil
import de.hdodenhof.circleimageview.CircleImageView

class TitleAvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {


    private var avatarIv : CircleImageView
    private var markIv : ImageView

    private var borderWidth = 0
    private var borderColor = Color.WHITE
    private var direction = 1

    init {
        avatarIv = CircleImageView(context)
        addView(avatarIv)
        markIv = ImageView(context)
        addView(markIv)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.TitleAvatarView)
        borderColor = ta.getColor(R.styleable.TitleAvatarView_tav_border_color, Color.WHITE)
        borderWidth = ta.getDimensionPixelOffset(R.styleable.TitleAvatarView_tav_border_width, 0)
        direction = ta.getInteger(R.styleable.TitleAvatarView_tav_mark_direction, 1)
        ta.recycle()
        if(borderWidth > 0){
            avatarIv.borderWidth = borderWidth
            avatarIv.borderColor = borderColor
        }
    }

    private var markViewSize = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
//        val height = MeasureSpec.getSize(heightMeasureSpec)
        markViewSize = (width / 3f).toInt()
        avatarIv.measure(widthMeasureSpec, heightMeasureSpec)
        markIv.measure(MeasureSpec.makeMeasureSpec(markViewSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(markViewSize, MeasureSpec.EXACTLY))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val wid = r - l;
        val hei = b - t
        avatarIv.layout(0, 0, wid, hei)

        if(direction == 1){
            //右上
            markIv.layout(wid-markViewSize, 0, wid, markViewSize)
        }else{
            //右下
            markIv.layout(wid-markViewSize, hei-markViewSize, wid, hei)
        }
    }

    fun setImages(avatar : String?, mark : String?){
        avatarIv.post {
//            if(avatar.isNotEmpty()){
                GlideUtil.loadImage(avatar?:"", avatarIv)
//            }
            if(!mark.isNullOrEmpty()){
                GlideUtil.loadImage(mark, markIv)
            }
        }
    }
}