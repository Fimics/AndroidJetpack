package com.mic.image;

/**
 * 图片下载结果回调
 */
public interface DownloadListener {

    void onDownloadSuccess();

    void onDownloadFail();
}
