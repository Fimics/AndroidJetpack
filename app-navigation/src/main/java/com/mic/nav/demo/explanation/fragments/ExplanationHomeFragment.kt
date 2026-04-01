package com.noetix.robotics.demo.explanation.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.noetix.robotics.R
import com.noetix.robotics.common.speech.SpeechHelper

class ExplanationHomeFragment : Fragment(R.layout.fragment_explain_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SpeechHelper.init()

        view.findViewById<AppCompatButton>(R.id.btn_single_point).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_singlePoint)
        }
    }
}
