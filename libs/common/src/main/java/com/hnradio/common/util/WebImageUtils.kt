package com.hnradio.common.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements


/**
 * Created by liguangze on 2021/7/27.
 */
object WebImageUtils {

    /**
     * 将html文本内容中包含img标签的图片，宽度变为屏幕宽度，高度根据宽度比例自适应
     */
    fun getNewContent(htmltext: String?): String? {
        return try {
            val doc: Document = Jsoup.parse(htmltext)
            val elements: Elements = doc.getElementsByTag("img")
            for (element in elements) {
                element.attr("width", "100%").attr("height", "auto")
            }
            doc.toString()
        } catch (e: Exception) {
            htmltext
        }
    }
}