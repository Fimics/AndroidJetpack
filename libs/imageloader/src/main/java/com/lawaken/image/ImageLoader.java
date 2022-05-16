package com.lawaken.image;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import java.io.File;

/**
 * describe
 * 对外开放API
 * @author lipengju
 */
public interface ImageLoader {

    void loadImageWithAppCxt(String url, ImageView imageView);

    void loadImage(Context context, String url, ImageView imageView);

    void preLoadImage(Context context,String url);

    void loadImage(String url, ImageView imageView);

    void loadImageAssets(String assets, ImageView imageView);

    void loadImageResources(int resources, ImageView imageView);

    void loadImageFile(File file, ImageView imageView);

    void loadImageUri(Uri uri, ImageView imageView);

    void loadImageByte(byte[] byteArray, ImageView imageView);

    void loadRoundImage(int roundingRadius,String url, ImageView imageView);

    void loadCircleImage(String url, ImageView imageView);

    void loadCircleBorderImage(String url, ImageView imageView, float borderWidth, int borderColor, int heightPx, int widthPx);

    void loadGifImage(String url, ImageView imageView);

    void loadGif2CommonImage(String url, ImageView imageView);

    void loadImageWithProgress(String url, ImageView imageView, ProgressLoadListener listener);

    void asyncDownloadImage(Context context, String url, String savePath, String saveFileName, DownloadListener listener);

    void clearImageDiskCache(final Context context);

    void clearImageMemoryCache(Context context);

    String getCacheSize(Context context);

    void trimMemory(Context context, int level);

    public void clearImageAllCache(Context context);

    public static ImageLoader get(Builder builder){
        return ImageLoaderImpl.get(builder);
    }

}