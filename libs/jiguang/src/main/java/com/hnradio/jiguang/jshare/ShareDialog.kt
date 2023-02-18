package com.hnradio.jiguang.jshare

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import androidx.recyclerview.widget.GridLayoutManager
import com.hnradio.common.util.ScreenUtils
import com.hnradio.jiguang.R
import com.hnradio.jiguang.databinding.DialogShareBinding

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-07-14 15:20
 * @Version: 1.0
 */
class ShareDialog(context: Context, val list: MutableList<ShareBean>) :
    Dialog(context, R.style.CustomDialog) {
    private var adapter: ShareListAdapter? = null
    private val mContext: Context = context
    private val binding by lazy { DialogShareBinding.inflate(layoutInflater) }
    private lateinit var share_title: String
    private var share_text: String? = null
    private var share_url: String? = null
    private var share_image_url: String? = null
    private var share_video_url: String? = null
    private var share_music_url: String? = null
    private var music_share_url: String? = null
    private var share_image_data: Bitmap? = null

    fun setShareInfo(
        share_title: String ?= "铁粉生活",
        share_text: String? = null,
        share_url: String? = null,
        share_image_url: String? = null,
        share_video_url: String? = null,
        share_music_url: String? = null,
        music_share_url: String? = null,
        share_image_data: Bitmap? = null


    ) {
        share_title?.let { this.share_title = share_title }
        share_text?.let { this.share_text = share_text }
        share_url?.let { this.share_url = share_url }
        share_image_url?.let { this.share_image_url = share_image_url }
        share_video_url?.let { this.share_video_url = share_video_url }
        share_music_url?.let { this.share_music_url = share_music_url }
        music_share_url?.let { this.music_share_url = music_share_url }
        share_image_data?.let { this.share_image_data = share_image_data }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initRecycleView()
        val lp = window!!.attributes
        lp.width = ScreenUtils.getScreenWidth(context)
        window!!.attributes = lp
        window!!.setGravity(Gravity.BOTTOM)

        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initRecycleView() {
        binding.rvList.layoutManager = GridLayoutManager(context, 4)
        adapter = ShareListAdapter(list)
        binding.rvList.adapter = adapter
        adapter?.setOnItemClickListener { adapter, view, position ->
            val shareBean = list[position]
            ShareUtils(context).share(
                alias = shareBean.alias,
                shareType = shareBean.id,
                share_title = share_title,
                share_text = share_text,
                share_url = share_url,
                share_image_url = share_image_url,
                share_image_data = share_image_data,
                music_share_url = music_share_url,
                share_music_url = share_music_url,
                share_video_url = share_video_url
            )
            dismiss()
        }
    }

}