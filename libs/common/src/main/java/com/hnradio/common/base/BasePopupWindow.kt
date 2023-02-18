package com.hnradio.common.base

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.annotation.LayoutRes
import com.hnradio.common.R
import com.hnradio.common.util.ScreenUtils

/**
 * Created by sand on 2018/3/30.
 */
abstract class BasePopupWindow : PopupWindow, View.OnClickListener {
    /**
     * 上下文
     */
    private var context: Context

    /**
     * 最上边的背景视图
     */
    private var vBgBasePicker: View

    /**
     * 内容viewgroup
     */
    private var llBaseContentPicker: LinearLayout

    constructor(context: Context) : super(context) {
        this.context = context
        val view = View.inflate(context, R.layout.base_popupwindow_layou, null)
        vBgBasePicker = view.findViewById(R.id.v_bg_base_picker)
        llBaseContentPicker = view.findViewById<View>(R.id.ll_base_content_picker) as LinearLayout
        /***
         * 添加布局到界面中
         */
        llBaseContentPicker.addView(View.inflate(context, contentViews, null))
        contentView = view
        //设置PopupWindow弹出窗体的宽
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        //设置PopupWindow弹出窗体的高
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true //设置获取焦点
        isTouchable = true //设置可以触摸
        isOutsideTouchable = true //设置外边可以点击
        val dw = ColorDrawable(0xffffff)
        setBackgroundDrawable(dw)

        // 设置背景颜色变暗
        val lp = (context as Activity).window
            .attributes
        lp.alpha = 0.4f
        //        ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.window.attributes = lp


        //消失的时候设置窗体背景变亮
        setOnDismissListener { //                ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            val lp = context.window.attributes
            lp.alpha = 1.0f
            context.window.attributes = lp
        }
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.animationStyle = R.style.BottomDialogWindowAnim
        initView(view)
        initListener()
        initData()
        vBgBasePicker.setOnClickListener(this)
    }

    constructor(context: Context, onBackGround: Boolean) : super(context) {
        this.context = context
        val view = View.inflate(context, R.layout.base_popupwindow_layou, null)
        vBgBasePicker = view.findViewById(R.id.v_bg_base_picker)
        llBaseContentPicker = view.findViewById<View>(R.id.ll_base_content_picker) as LinearLayout
        /***
         * 添加布局到界面中
         */
        llBaseContentPicker.addView(View.inflate(context, contentViews, null))
        contentView = view
        //设置PopupWindow弹出窗体的宽
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        //设置PopupWindow弹出窗体的高
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true //设置获取焦点
        isTouchable = true //设置可以触摸
        isOutsideTouchable = true //设置外边可以点击
        val dw = ColorDrawable(0xffffff)
        setBackgroundDrawable(dw)
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.animationStyle = R.style.BottomDialogWindowAnim
        initView(view)
        initListener()
        initData()
        vBgBasePicker.setOnClickListener(this)
    }

    constructor(context: Context, isShowBackGround: Boolean, h: Int) : super(context) {
        this.context = context
        val view = View.inflate(context, R.layout.base_popupwindow_layou, null)
        vBgBasePicker = view.findViewById(R.id.v_bg_base_picker)
        llBaseContentPicker = view.findViewById<View>(R.id.ll_base_content_picker) as LinearLayout
        /***
         * 添加布局到界面中
         */
        llBaseContentPicker.addView(View.inflate(context, contentViews, null))
        contentView = view
        //设置PopupWindow弹出窗体的宽
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        //设置PopupWindow弹出窗体的高
        this.height = h
        isFocusable = true //设置获取焦点
        isTouchable = true //设置可以触摸
        isOutsideTouchable = true //设置外边可以点击
        val dw = ColorDrawable(0xffffff)
        setBackgroundDrawable(dw)
        if (isShowBackGround) {
            //设置PopupWindow弹出窗体的高
            this.height = ViewGroup.LayoutParams.WRAP_CONTENT

            // 设置背景颜色变暗
            val lp = (context as Activity).window
                .attributes
            lp.alpha = 0.4f
            //            ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            context.window.attributes = lp
            backgroundAlpha(0.5f)
            //消失的时候设置窗体背景变亮
            setOnDismissListener { //                    ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                val lp = context.window.attributes
                lp.alpha = 1.0f
                context.window.attributes = lp
            }
        }

        //设置SelectPicPopupWindow弹出窗体动画效果
        this.animationStyle = R.style.BottomDialogWindowAnim
        initView(view)
        initListener()
        initData()
        vBgBasePicker.setOnClickListener(this)
    }

    fun setBackTrans(context: Context) {
        llBaseContentPicker.setBackgroundColor(context.resources.getColor(R.color.trans))
    }

    fun setContentHeight() {
        val param: ViewGroup.LayoutParams
        param = llBaseContentPicker.layoutParams
        param.width = ViewGroup.LayoutParams.MATCH_PARENT
        param.height = ScreenUtils.getScreenHeight(context)
        llBaseContentPicker.layoutParams = param
    }

    fun setContent3Divide4Height() {
        val param: ViewGroup.LayoutParams
        param = llBaseContentPicker.layoutParams
        param.width = ViewGroup.LayoutParams.MATCH_PARENT
        param.height = ViewGroup.LayoutParams.MATCH_PARENT
        llBaseContentPicker.layoutParams = param
    }

    /**
     * 初始化数据
     */
    protected abstract fun initData()

    /**
     * 初始化监听
     */
    protected abstract fun initListener()

    /**
     * 初始化view
     *
     * @param view
     */
    protected abstract fun initView(view: View?)

    /**
     * 初始化布局
     *
     * @return
     */
    protected abstract val contentViews: Int

    /**
     * 为了适配7.0系统以上显示问题(显示在控件的底部)
     *
     * @param anchor
     */
    override fun showAsDropDown(anchor: View) {
        if (Build.VERSION.SDK_INT >= 24) {
            val rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            val h = anchor.resources.displayMetrics.heightPixels - rect.bottom
            height = h
        }
        super.showAsDropDown(anchor)
    }

    /**
     * 展示在屏幕的底部
     *
     * @param layoutid rootview
     */
    fun showAtLocation(@LayoutRes layoutid: Int) {
        vBgBasePicker.visibility = View.VISIBLE
        showAtLocation(
            LayoutInflater.from(context).inflate(layoutid, null),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0
        )
    }

    /**
     * 展示在屏幕的底部
     *
     * @param layoutid rootview
     */
    fun showAtLocationBottom(@LayoutRes layoutid: Int) {
        vBgBasePicker.visibility = View.VISIBLE
        showAtLocation(
            LayoutInflater.from(context).inflate(layoutid, null),
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0
        )
    }

    /**
     * 展示在屏幕的底部
     *
     * @param layoutid rootview
     */
    fun showAtLocationCenter(@LayoutRes layoutid: Int) {
        this.animationStyle = R.style.CenterDialogWindowAnim
        vBgBasePicker.visibility = View.GONE
        showAtLocation(
            LayoutInflater.from(context).inflate(layoutid, null),
            Gravity.CENTER, 0, 0
        )
    }

    /**
     * 最上边视图的点击事件的监听
     *
     * @param v
     */
    override fun onClick(v: View) {
        if (v.id == R.id.v_bg_base_picker) {
            vBgBasePicker.visibility = View.GONE
            dismiss()
        }
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    fun backgroundAlpha(bgAlpha: Float) {
        val lp = (context as Activity).window.attributes
        lp.alpha = bgAlpha //0.0-1.0
        (context as Activity).window.attributes = lp
    }
}