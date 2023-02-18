package com.hnradio.common.util

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 *  格式化
 * created by 乔岩 on 2021/7/30
 */
class FormatUtil {

    companion object {

        /**
         * 格式化  视频时长
         */
        fun formatProgramDuration(second: Int): String {
            val decimalFormat = DecimalFormat("00")
            val hh = decimalFormat.format(second / 3600);
            val mm = decimalFormat.format(second % 3600 / 60);
            val ss = decimalFormat.format(second % 60);
            return "$hh:$mm:$ss";
        }

        /**
         * 格式化  音频时长
         */
        fun formatAudioTime(milliSecond: Long): String {
            val decimalFormat = DecimalFormat("00")
            val hh = decimalFormat.format(milliSecond / 1000 / 3600);
            val mm = decimalFormat.format(milliSecond / 1000 % 3600 / 60);
            val ss = decimalFormat.format(milliSecond / 1000 % 60);
            return if (hh.equals("00")) {
                "$mm:$ss"
            } else {
                "$hh:$mm:$ss"
            }
        }


        /**
         * 格式化
         */
        fun formatMpDuration(second: Int): String {
            val decimalFormat = DecimalFormat("00")
            val mm = decimalFormat.format(second % 3600 / 60);
            val ss = decimalFormat.format(second % 60);
            return "$mm:$ss";
        }

        /**
         * 格式化  播放量
         */
        fun formatPlayAmount(amount: Int): String {
            return when {
                amount > 99999999 -> {
                    val bigDecimal = BigDecimal(amount).divide(BigDecimal(100000000))
                    val result = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP)
                    result.toString() + "亿"
                }
                amount > 9999 -> {
                    val bigDecimal = BigDecimal(amount).divide(BigDecimal(10000))
                    val result = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP)
                    result.toString() + "万"
                }
                else -> amount.toString()
            }
        }

        /**
         * 格式化  时间戳
         */
        fun formatTimeStamp(time: Long): String {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return simpleDateFormat.format(time)
        }

        /**
         * 格式化  时间戳
         */
        fun formatTimeStampHm(time: Long): String {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return simpleDateFormat.format(time)
        }

    }

}