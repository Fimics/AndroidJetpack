package com.noetix.robotics.demo.interaction.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.noetix.robotics.R

class InteractionDetailFragment : Fragment(R.layout.fragment_demo) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val tvModuleName = view.findViewById<TextView>(R.id.tv_module_name)
        val tvFragmentName = view.findViewById<TextView>(R.id.tv_fragment_name)
        val btnAction = view.findViewById<Button>(R.id.btn_action)

        tvModuleName.text = "实时互动 (Interaction)"
        tvFragmentName.text = "Detail Fragment (详情页)"
        
        btnAction.text = "返回首页"
        btnAction.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
