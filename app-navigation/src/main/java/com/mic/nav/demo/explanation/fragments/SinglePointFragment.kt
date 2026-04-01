package com.noetix.robotics.demo.explanation.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.noetix.robotics.R
import com.noetix.robotics.demo.explanation.ExplanationViewModel
import com.noetix.robotics.demo.explanation.data.ExplainUiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SinglePointFragment : Fragment(R.layout.fragment_single_point) {

    private lateinit var viewModel: ExplanationViewModel

    private lateinit var llPointsContainer: LinearLayout
    private lateinit var ivContentImage: ImageView
    private lateinit var tvContentText: TextView
    private lateinit var tvStatusInfo: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        llPointsContainer = view.findViewById(R.id.ll_points_container)
        ivContentImage = view.findViewById(R.id.iv_content_image)
        tvContentText = view.findViewById(R.id.tv_content_text)
        tvStatusInfo = view.findViewById(R.id.tv_status_info)

        viewModel = ViewModelProvider(this).get(ExplanationViewModel::class.java)
        viewModel.loadMockData(requireContext())

        setupPointsList()
        setupControls(view)
        observeState()
    }

    private fun setupPointsList() {
        val points = viewModel.getPoints()
        points.forEach { point ->
            val btn = AppCompatButton(requireContext()).apply {
                text = point.slocName
                setOnClickListener {
                    viewModel.startSinglePoint(point.slocId)
                }
                background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.btn_demo_card_bg)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 16 }
            llPointsContainer.addView(btn, params)
        }
    }

    private fun setupControls(view: View) {
        view.findViewById<Button>(R.id.btn_pause).setOnClickListener {
            viewModel.pausePreach()
        }
        view.findViewById<Button>(R.id.btn_resume).setOnClickListener {
            viewModel.resumePreach()
        }
        view.findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.stopPreach()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when (state) {
                    is ExplainUiState.Idle -> {
                        ivContentImage.visibility = View.GONE
                        tvContentText.visibility = View.GONE
                        tvStatusInfo.text = "空闲状态，请点击点位..."
                    }

                    is ExplainUiState.Error -> {
                        tvStatusInfo.text = "错误: ${state.message}"
                    }

                    is ExplainUiState.Finished -> {
                        tvStatusInfo.text = state.message
                        ivContentImage.visibility = View.GONE
                        tvContentText.visibility = View.GONE
                    }

                    is ExplainUiState.ShowContent -> {
                        tvStatusInfo.text =
                            "正在讲解: ${state.pointName} (${state.contentIndex + 1}/${state.totalContents})"

                        if (state.content.isNotEmpty()) {
                            tvContentText.visibility = View.VISIBLE
                            tvContentText.text = state.content
                        } else {
                            tvContentText.visibility = View.GONE
                        }

                        if (state.contentItem.imgUrls.isNotEmpty()) {
                            ivContentImage.visibility = View.VISIBLE
                            // Load image
                            Glide.with(this@SinglePointFragment)
                                .load(state.contentItem.imgUrls.first())
                                .into(ivContentImage)
                        } else {
                            ivContentImage.visibility = View.GONE
                        }
                    }

                    is ExplainUiState.Paused -> {
                        tvStatusInfo.text = "已暂停"
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopPreach()
    }
}
