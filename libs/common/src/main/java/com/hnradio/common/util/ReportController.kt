package com.hnradio.common.util

import com.hnradio.common.widget.bottomDialog.BottomDialogMultiItem

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-12-13 14:56
 * @Version: 1.0
 */
class ReportController {
    private val REPORT_ITEMS = arrayListOf<BottomDialogMultiItem<String>>(
        BottomDialogMultiItem("违法违规"),
        BottomDialogMultiItem("色情低俗"),
        BottomDialogMultiItem("标题党、封面党、骗点击"),
        BottomDialogMultiItem("制售假冒伪劣商品"),
        BottomDialogMultiItem("滥用作品"),
        BottomDialogMultiItem("泄露我的隐私")
    )

    fun getReportItems(): ArrayList<BottomDialogMultiItem<String>> {
        return REPORT_ITEMS
    }

    fun reportUser(reportContent: String) {
        ToastUtils.show("举报成功，我们尽快进行处理")
    }
}