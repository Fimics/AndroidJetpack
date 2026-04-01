package com.noetix.robotics.demo.camera.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.noetix.robotics.R

class CameraHomeFragment : Fragment(R.layout.fragment_camera_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val btnAction = view.findViewById<Button>(R.id.btn_action)

        btnAction.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_preview)
        }
    }
}
