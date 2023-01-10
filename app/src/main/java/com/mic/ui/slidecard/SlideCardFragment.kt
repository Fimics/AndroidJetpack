package com.mic.ui.slidecard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mic.R
import com.mic.databinding.FragmentSlideCardBinding
import com.mic.ui.slidecard.adapter.UniversalAdapter
import com.mic.ui.slidecard.adapter.ViewHolder


class SlideCardFragment : Fragment() {

    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentSlideCardBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSlideCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val rv = binding.rv
        val data=SlideCardBean.initDatas()
        rv.layoutManager=SlideCardLayoutManager()
        val adapter:UniversalAdapter<SlideCardBean> = object : UniversalAdapter<SlideCardBean>(activity,data, R.layout.item_swipe_card) {
            override fun convert(viewHolder: ViewHolder, slideCardBean: SlideCardBean) {
                viewHolder.setText(R.id.tvName, slideCardBean.name)
                viewHolder.setText(R.id.tvPrecent, "${slideCardBean.postition} + \"/\" + ${data.size}")
                activity?.let {
                    Glide.with(it)
                        .load(slideCardBean.url)
                        .into(viewHolder.getView(R.id.iv))
                }
            }
        }

        rv.adapter=adapter
        // 初始化数据
        // 初始化数据
        CardConfig.initConfig(activity)
        val slideCallback = SlideCallback(rv, adapter, data)
        val itemTouchHelper = ItemTouchHelper(slideCallback)
        itemTouchHelper.attachToRecyclerView(rv)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}