package com.hnradio.common.adapter

import android.graphics.Color
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.hnradio.common.adapter.decoration.GridLayoutItemDecoration
import com.hnradio.common.adapter.decoration.LinearItemDecoration

/**
 * Created by ytf on 2020/10/27 027.
 * Description:
 */
object AdapterHelper {

    fun configGridAdapter(recycle : RecyclerView, adapter : BaseQuickAdapter<*, BaseViewHolder>?,
                          spanCount : Int,
                          hspace : Int,
                          vspace : Int,
                          color : Int,
                          oritention: Int = RecyclerView.VERTICAL){

        val m = GridLayoutManager(recycle.context, spanCount, oritention, false)
        recycle.layoutManager = m
        recycle.adapter = adapter
        val d = GridLayoutItemDecoration(spanCount, oritention, hspace, vspace )
        val dc = recycle.itemDecorationCount
        if(dc > 0){
            val de = recycle.getItemDecorationAt(0)
            recycle.removeItemDecoration(de)
        }
        recycle.addItemDecoration(d)
    }

    fun configListAdapter(recycle : RecyclerView, adapter : BaseQuickAdapter<*, BaseViewHolder>?,
                          oritention : Int = RecyclerView.VERTICAL,
                          space : Int = 1,
                          color : Int = Color.TRANSPARENT, offset : Int = 0){

        val m = LinearLayoutManager(recycle.context, oritention, false)
        recycle.layoutManager = m
        recycle.adapter = adapter
        val d = LinearItemDecoration(space, oritention)
        val dc = recycle.itemDecorationCount
        if(dc > 0){
            val de = recycle.getItemDecorationAt(0)
            recycle.removeItemDecoration(de)
        }
        recycle.addItemDecoration(d)
    }
}