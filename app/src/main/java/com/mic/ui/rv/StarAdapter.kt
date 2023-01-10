package com.mic.ui.rv

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mic.R

open class StarAdapter constructor(val context: Context, val list: MutableList<Star>) :
    RecyclerView.Adapter<StarAdapter.StarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StarViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_item_star, null)
        return StarViewHolder(view)
    }

    override fun onBindViewHolder(holder: StarViewHolder, position: Int) {
        holder.tv.text = list[position].name
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * 是否是组的第一个item
     *
     * @param position
     * @return
     */
    open fun isGroupHeader(position: Int): Boolean {
        return if (position == 0) {
            true
        } else {
            val currentGroupName: String = getGroupName(position)
            val preGroupName: String = getGroupName(position - 1)
            preGroupName != currentGroupName
        }
    }

    fun getGroupName(position: Int): String{
        return list[position].groupName
    }

    class StarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var tv: TextView

        init {
            tv = itemView.findViewById(R.id.tv_star)
        }

    }
}