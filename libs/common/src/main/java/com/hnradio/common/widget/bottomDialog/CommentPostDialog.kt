package com.hnradio.common.widget.bottomDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.hnradio.common.R
import com.hnradio.common.util.ToastUtils
import razerdp.util.KeyboardUtils


/**
 *  发布评论
 * created by qiaoyan on 2021/8/11
 */
class CommentPostDialog(
    context: Context,
    private val onSendClickListener: OnSendClickListener,
    var isDismissOnCallBack: Boolean = true
) :
    Dialog(context, R.style.CustomDialog) {

    var editView: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dialog_comment)
        val etComment = findViewById<EditText>(R.id.et_comment)
        editView = etComment
        etComment.requestFocus()

        window?.setGravity(Gravity.BOTTOM)
        window?.setWindowAnimations(R.style.Comment_Dialog_Anim_Style);
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        findViewById<TextView>(R.id.tv_send).setOnClickListener {
            if (etComment.text.toString().trim().isBlank()) {
                ToastUtils.show("请输入内容")
                return@setOnClickListener
            }
            onSendClickListener.onSendClick(etComment.text.toString().trim())
            if (isDismissOnCallBack)
                dismiss()
        }
    }

    override fun dismiss() {
        editView?.let {
            //关闭键盘
            KeyboardUtils.close(it)
        }
        super.dismiss()
    }

    interface OnSendClickListener {

        fun onSendClick(text: String)
    }


}