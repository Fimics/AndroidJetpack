package com.hnradio.common.base

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.hnradio.common.R

/**
 * Created by ytf on 2020/07/29.
 * Description:
 */
abstract class BaseDialogFragment<Binding : ViewDataBinding> : DialogFragment(), View.OnClickListener {

    protected var mBinding : Binding? = null

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    var innerCallback: Callback? = null

    var isShowing = false
        private set

    abstract class Callback {
        open fun onPositive(dialog : BaseDialogFragment<*>, vararg any: Any) {}
        open fun onNegative(dialog : BaseDialogFragment<*>, vararg any: Any) {}
        open fun onEvent(dialog : BaseDialogFragment<*>, vararg any: Any) {}
        open fun onDismiss(){}
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, layout, container, true)
        mBinding?.lifecycleOwner = viewLifecycleOwner
        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wrapSize()
        isShowing = true
    }

    abstract val layout: Int

    private fun wrapSize() {
        // 设置宽度为屏宽、位置在屏幕底部
        val window = dialog?.window
        window?.apply {
            val wlp = window.attributes
            wlp.gravity = gravity
            wlp.width = windowWidth
            wlp.height = windowHeight
            if(windowAnim != 0){
                wlp.windowAnimations = windowAnim
            }
            attributes = wlp
        }
    }

    open val gravity = Gravity.CENTER

    open val windowAnim : Int = 0

    open val windowWidth : Int
        get() = WindowManager.LayoutParams.WRAP_CONTENT

    open val windowHeight : Int
        get() = WindowManager.LayoutParams.WRAP_CONTENT

    override fun onClick(v: View) {}

    override fun onDestroyView() {
        isShowing = false
        super.onDestroyView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        innerCallback?.onDismiss()
        innerCallback = null
    }
}