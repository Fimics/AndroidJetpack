package com.hnradio.common.util.upgrade

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.hnradio.common.databinding.FragmentUpdateNoticeDialogBinding
import java.lang.reflect.Field

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-12 11:14
 * @Version: 1.0
 */


class UpdateNoticeDialogFragment : DialogFragment() {

    private val binding by lazy { FragmentUpdateNoticeDialogBinding.inflate(layoutInflater) }
    private var mListener: OnUpdateNoticeClickListener? = null

    companion object {
        fun newInstance(
            title: String?, content: String?,
            isForce: Boolean
        ): UpdateNoticeDialogFragment {
            val args = Bundle()
            args.putString(TAG_UPDATE_TITLE, title)
            args.putString(TAG_UPDATE_CONTENT, content)
            args.putBoolean(TAG_IS_FORCE, isForce)
            val fragment = UpdateNoticeDialogFragment()
            fragment.arguments = args
            return fragment
        }

        const val TAG_UPDATE_TITLE = "TAG_UPDATE_TITLE"
        const val TAG_UPDATE_CONTENT = "TAG_UPDATE_CONTENT"
        const val TAG_IS_FORCE = "TAG_IS_FORCE"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //去除标题
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //将周围设置为透明
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCanceledOnTouchOutside(false)
        initView()
        return binding.root
    }

    private fun initView() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        val title = arguments?.getString(TAG_UPDATE_TITLE)
        val content = arguments?.getString(TAG_UPDATE_CONTENT)
        val force = arguments?.getBoolean(TAG_IS_FORCE)

        binding.tvUpdateTitle.text = "${title}版本现已发布，请下载体验。"
        binding.tvUpdateContent.text = content
        binding.tvUpdateContent.movementMethod = ScrollingMovementMethod.getInstance()
        if (force!!) {
            binding.btnCancel.visibility = View.GONE
        } else {
            binding.btnCancel.visibility = View.VISIBLE
        }
        binding.btnCancel.setOnClickListener {
            mListener?.onUpdateCancel()
            dismiss()
        }
        binding.btnSure.setOnClickListener {
       /*     binding.updateLv.visibility = View.GONE
            binding.downloadView.visibility = View.VISIBLE*/
            mListener?.onUpdateSure()
            if (!force){
                dismiss()
            }
        }
    }

    fun setProgress(progress: Int) {
        binding.numberProgressBar.progress = progress
    }

    override fun onStart() {
        super.onStart()
        val win = dialog!!.window
        val dm = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(dm)
        val params = win!!.attributes
        params.width = (dm.widthPixels * 0.8).toInt()
        params.height = (dm.heightPixels * 0.6).toInt()
        win.attributes = params
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            var mDismissed: Field? = null
            mDismissed = DialogFragment::class.java.getDeclaredField("mDismissed")
            mDismissed.isAccessible = true
            mDismissed[this] = false
            val mShownByMe = DialogFragment::class.java.getDeclaredField("mShownByMe")
            mShownByMe.isAccessible = true
            mShownByMe[this] = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val ft: FragmentTransaction = manager.beginTransaction()
            if (isAdded) {
                ft.show(this)
            } else {
                ft.add(this, tag)
            }
            ft.commitAllowingStateLoss()
        } catch (e: Exception) {
            try {
                super.show(manager, tag)
            } catch (e1: Exception) {
                //nothing
            }
        }
    }

    fun setOnUpdateNoticeClickListener(listener: OnUpdateNoticeClickListener) {
        this.mListener = listener
    }

}

interface OnUpdateNoticeClickListener {
    fun onUpdateCancel()
    fun onUpdateSure()
}