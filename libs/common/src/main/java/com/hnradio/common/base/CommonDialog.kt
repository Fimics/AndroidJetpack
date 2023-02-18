package com.hnradio.common.base

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.hnradio.common.R
import com.hnradio.common.base.BaseDialog.BaseDialogClickListener.OnActiconListener

class CommonDialog(builder: Builder) : BaseDialog(builder.mContext) {
    private var mNegativeBtn: Button? = null
    private var mPositiveBtn: Button? = null
    private var mContentView: TextView? = null
    private var mTitleView: TextView? = null
    private var mView: View? = null
    override fun initView(): View? {
        mView = mInflater.inflate(R.layout.dialog_common, null)
        mTitleView = mView!!.findViewById<View>(R.id.tv_dialog_title) as TextView
        mPositiveBtn = mView!!.findViewById<View>(R.id.btn_dialog_action) as Button
        mNegativeBtn = mView!!.findViewById<View>(R.id.btn_dialog_cancel) as Button
        mContentView = mView!!.findViewById<View>(R.id.tv_dialog_content) as TextView
        return mView
    }

    class Builder(val mContext: Context) {
        var mTitle: CharSequence? = null
        var mNegativeText: CharSequence? = null
        var mPositiveText: CharSequence? = null
        var mContentText: CharSequence? = null
        var mContentGravity = 0
        var mCancelListener: BaseDialogClickListener.OnCancelListener? = null
        var mActionListener: OnActiconListener? = null
        var isNegativeVisible = false
        var isPositiveVisible = false
        var mDialog: CommonDialog? = null
        private var canceledOnTouchOutside = true
        private var caceledable = true
        var contentColor = -1
        var mTitleColor = -1
        var mPositiveBtnColor = -1
        var isNeverClose = false
        fun setNeverClose(isNeverClose: Boolean): Builder {
            this.isNeverClose = isNeverClose
            return this
        }

        fun setCanceledOntouchOutside(b: Boolean): Builder {
            canceledOnTouchOutside = b
            return this
        }

        fun setContentColor(resId: Int): Builder {
            contentColor = resId
            return this
        }

        fun setCaceledable(b: Boolean): Builder {
            caceledable = b
            return this
        }

        private fun getString(textId: Int): String {
            return mContext.resources.getString(textId)
        }

        fun setTitle(text: CharSequence?): Builder {
            mTitle = text
            return this
        }

        fun setTitleColor(titleColor: Int): Builder {
            mTitleColor = titleColor
            return this
        }

        fun setPositiveBtnColor(positiveBtnColor: Int): Builder {
            mPositiveBtnColor = positiveBtnColor
            return this
        }

        fun setContent(text: CharSequence?): Builder {
            mContentText = text
            return this
        }

        fun setContentGravity(gravity: Int): Builder {
            mContentGravity = gravity
            return this
        }

        fun setNegativeBtn(
            text: CharSequence?,
            listener: BaseDialogClickListener.OnCancelListener?
        ): Builder {
            mNegativeText = text
            mCancelListener = listener
            isNegativeVisible = true
            return this
        }

        fun setPositiveBtn(
            text: CharSequence?,
            listener: OnActiconListener?
        ): Builder {
            mPositiveText = text
            mActionListener = listener
            isPositiveVisible = true
            return this
        }

        fun setPositiveBtn(textId: Int, listener: OnActiconListener?): Builder {
            mPositiveText = getString(textId)
            mActionListener = listener
            isPositiveVisible = true
            return this
        }

        fun setTitle(textId: Int): Builder {
            mTitle = getString(textId)
            return this
        }

        fun setContent(textId: Int): Builder {
            mContentText = getString(textId)
            return this
        }

        fun setNegativeBtn(
            textId: Int,
            listener: BaseDialogClickListener.OnCancelListener?
        ): Builder {
            mNegativeText = getString(textId)
            mCancelListener = listener
            isNegativeVisible = true
            return this
        }

        fun build(): BaseDialog {
            mDialog = CommonDialog(this)
            mDialog!!.setCancelable(false)
            mDialog!!.setCanceledOnTouchOutside(canceledOnTouchOutside)
            mDialog!!.setCancelable(caceledable)
            return mDialog as CommonDialog
        }
    }

    init {
        if (!TextUtils.isEmpty(builder.mTitle)) {
            mTitleView!!.text = builder.mTitle
        } else {
            mTitleView!!.visibility = View.GONE
        }
        if (builder.contentColor != -1) {
            mContentView!!.setTextColor(context.resources.getColor(builder.contentColor))
        }
        if (builder.mTitleColor != -1) {
            mTitleView!!.setTextColor(context.resources.getColor(builder.mTitleColor))
        }
        if (builder.mPositiveBtnColor != -1) {
            mPositiveBtn!!.setTextColor(context.resources.getColor(builder.mPositiveBtnColor))
        }
        if (builder.isPositiveVisible) {
            mPositiveBtn!!.text = builder.mPositiveText
            mPositiveBtn!!.setOnClickListener {
                if (builder.mActionListener != null) {
                    builder.mActionListener!!.onClick()
                }
                if (!builder.isNeverClose) {
                    builder.mDialog!!.dismiss()
                }
            }
        }
        if (builder.isNegativeVisible) {
            mNegativeBtn!!.text = builder.mNegativeText
            mNegativeBtn!!.setOnClickListener {
                if (builder.mCancelListener != null) {
                    builder.mCancelListener!!.onClick()
                }
                if (!builder.isNeverClose) {
                    builder.mDialog!!.dismiss()
                }
            }
        }else{
            mNegativeBtn!!.visibility = View.GONE
            mPositiveBtn!!.setBackgroundResource(R.drawable.shape_common_left_right_raduis_16)
        }

        if (!TextUtils.isEmpty(builder.mContentText)) {
            mContentView!!.text = builder.mContentText
        } else {
            mContentView!!.visibility = View.GONE
        }
        if (builder.mContentGravity > 0) {
            mContentView!!.gravity = builder.mContentGravity
        }
    }
}