package com.lawaken.image.strategy.fresco;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.lawaken.image.Builder;
import com.lawaken.image.DownloadListener;
import com.lawaken.image.ImageLoader;
import com.lawaken.image.ProgressLoadListener;
import java.io.File;

/**
 * describe
 * @author lipnegju
 */
public class FrescoStrategy implements ImageLoader {

    private Builder builder;

    public FrescoStrategy(Builder builder) {
        this.builder = builder;
    }

    @Override
    public void loadImageWithAppCxt(String url, ImageView imageView) {

    }

    @Override
    public void loadImage(Context context, String url, ImageView imageView) {

    }

    @Override
    public void preLoadImage(Context context, String url) {

    }

    @Override
    public void loadImage(String url, ImageView imageView) {

    }

    @Override
    public void loadImageAssets(String asstes, ImageView imageView) {

    }

    @Override
    public void loadImageResources(int resources, ImageView imageView) {

    }

    @Override
    public void loadImageFile(File file, ImageView imageView) {

    }

    @Override
    public void loadImageUri(Uri uri, ImageView imageView) {

    }

    @Override
    public void loadImageByte(byte[] byteArray, ImageView imageView) {

    }

    @Override
    public void loadRoundImage(int roundingRadius, String url, ImageView imageView) {

    }

    @Override
    public void loadCircleImage(String url, ImageView imageView) {

    }

    @Override
    public void loadCircleBorderImage(String url, ImageView imageView, float borderWidth, int borderColor, int heightPx, int widthPx) {

    }

    @Override
    public void loadGifImage(String url, ImageView imageView) {

    }

    @Override
    public void loadGif2CommonImage(String url, ImageView imageView) {

    }

    @Override
    public void loadImageWithProgress(String url, ImageView imageView, ProgressLoadListener listener) {

    }

    @Override
    public void asyncDownloadImage(Context context, String url, String savePath, String saveFileName, DownloadListener listener) {

    }

    @Override
    public void clearImageDiskCache(Context context) {

    }

    @Override
    public void clearImageMemoryCache(Context context) {

    }

    @Override
    public String getCacheSize(Context context) {
        return null;
    }

    @Override
    public void trimMemory(Context context, int level) {

    }

    @Override
    public void clearImageAllCache(Context context) {

    }
}
