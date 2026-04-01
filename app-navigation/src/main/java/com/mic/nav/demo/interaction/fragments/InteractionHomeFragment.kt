package com.noetix.robotics.demo.interaction.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.noetix.robotics.R

class InteractionHomeFragment : Fragment(R.layout.fragment_demo) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 绑定 fragment_demo.xml 里的占位视图
        val tvModuleName = view.findViewById<TextView>(R.id.tv_module_name)
        val tvFragmentName = view.findViewById<TextView>(R.id.tv_fragment_name)
        val btnAction = view.findViewById<Button>(R.id.btn_action)

        tvModuleName.text = "实时互动 (Interaction)"
        tvFragmentName.text = "Home Fragment (首页)"
        
        btnAction.text = "跳转到详情页"
        btnAction.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_detail)
        }
    }
}
