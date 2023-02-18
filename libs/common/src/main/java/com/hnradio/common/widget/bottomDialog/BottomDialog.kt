package com.hnradio.common.widget.bottomDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hnradio.common.R
import com.hnradio.common.http.bean.AlbumContentBean


/**
 *  底部弹出的dialog
 * created by qiaoyan on 2021/8/11
 */
class BottomDialog<T> : Dialog {

    var tvTitle: TextView? = null
    var tvClose: TextView? = null
    private var closeStr: String = "关闭"

    private var list: ArrayList<BottomDialogMultiItem<T>>

    private var onDialogItemClickListener: OnDialogItemClickListener<T>? = null
    private var selectText: String? = null
    private var selectId: Int? = -2

    constructor(
        context: Context, list: ArrayList<BottomDialogMultiItem<T>>,
        closeStr: String = "关闭"
    ) : super(
        context,
        R.style.CustomDialog
    ) {
        this.list = list
        this.closeStr = closeStr
    }

    constructor(
        context: Context,
        selectText: String,
        list: ArrayList<BottomDialogMultiItem<T>>,
        closeStr: String = "关闭"
    ) : super(context, R.style.CustomDialog) {
        this.selectText = selectText
        this.list = list
        this.closeStr = closeStr
    }

    constructor(
        context: Context,
        selectId: Int,
        list: ArrayList<BottomDialogMultiItem<T>>
    ) : super(context, R.style.CustomDialog) {
        this.selectId = selectId
        this.list = list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dialog_bottom)
        initView()
        window?.setGravity(Gravity.BOTTOM)
        window?.setWindowAnimations(R.style.Bottom_Dialog_Anim_Style)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initView() {
        tvTitle = findViewById(R.id.tv_title)
        tvClose = findViewById(R.id.tv_close)
        tvClose?.text = closeStr
        val ivPlay = findViewById<ImageView>(R.id.iv_play)
        val tvPlayAll = findViewById<TextView>(R.id.tv_play_all)
        val ivOrder = findViewById<ImageView>(R.id.iv_order)
        when (list[0].data) {
            is AlbumContentBean -> {//如果音频播放列表
//                ivPlay.visibility = View.VISIBLE
//                tvPlayAll.visibility = View.VISIBLE
//                ivOrder.visibility = View.VISIBLE
//                tvPlayAll.setOnClickListener {
//                    ToastUtils.show("播放全部")
//                }
//                ivOrder.setOnClickListener {
//                    ToastUtils.show("排序")
//                }
            }
            is Int -> {//打赏
                tvClose?.text = "答谢"
                tvClose?.setTextColor(context.resources.getColor(R.color.white))
                tvClose?.setBackgroundResource(R.drawable.shape_solid_orange_radius_50)
            }
        }
        //通用设置
        tvClose?.setOnClickListener {
            onDialogItemClickListener?.onCloseClick()
            dismiss()
        }
        val rvRecycler = findViewById<RecyclerView>(R.id.rv_recycler)
        rvRecycler.apply {
            val bottomAdapter = if (selectText != null) {
                BottomDialogMultiItemAdapter(list, selectText!!)
            } else if (selectId!! > -2) {
                BottomDialogMultiItemAdapter(list, selectId!!)
            } else {
                BottomDialogMultiItemAdapter(list)
            }
            bottomAdapter.setOnAdapterItemClickListener(object :
                BottomDialogMultiItemAdapter.OnAdapterItemClickListener<T> {
                override fun onItemClick(data: T) {
                    onDialogItemClickListener?.onItemClick(data)
                }
            })
            adapter = bottomAdapter
            layoutManager = if (list[0].data is Int)//打赏
                GridLayoutManager(context, 2)
            else
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    fun setTitleText(title: String) {
        tvTitle?.visibility = View.VISIBLE
        tvTitle?.text = title
    }

    fun setOnDialogItemClickListener(onDialogItemClickListener: OnDialogItemClickListener<T>) {
        this.onDialogItemClickListener = onDialogItemClickListener
    }

    interface OnDialogItemClickListener<T> {

        fun onItemClick(data: T)

        fun onCloseClick()
    }


}