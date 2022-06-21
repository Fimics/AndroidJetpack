package com.mic.image.strategy.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mic.image.Builder;
import com.mic.image.DownloadListener;
import com.mic.image.ImageLoader;
import com.mic.image.ProgressLoadListener;
import com.lawaken.image.R;
import com.mic.image.utils.FileUtils;
import java.io.File;


/**
 * describe
 * @author lipnegju
 */
public class GlideStrategy implements ImageLoader {

    private Builder builder;

    public GlideStrategy(Builder builder) {
        this.builder = builder;
    }

    @Override
    public void loadImageWithAppCxt(String url, ImageView imageView) {
        Glide.with(imageView.getContext().getApplicationContext())
                .load(url)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void preLoadImage(Context context,String url) {
        Glide.with(context)
                .load(url)
                .apply(getRequestOptions())
                .preload();
    }

    @Override
    public void loadImage(String url, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void loadImageAssets(String asstes, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(asstes)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void loadImageResources(int resources, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(resources)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void loadImageFile(File file, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(file)
                .apply(getRequestOptions())
                .into(imageView);
    }


    @Override
    public void loadImageUri(Uri uri, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(uri)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void loadImageByte(byte[] byteArray, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(byteArray)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void loadCircleImage(String url, ImageView imageView) {
        Glide.with(imageView.getContext())
                .load(url)
//                .transform(new GlideCircleTransform())
                .apply(getRequestOptions().circleCrop())
                .into(imageView);
    }

    @Override
    public void loadRoundImage(int roundingRadius,String url, ImageView imageView) {
        RoundedCorners roundedCorners = new RoundedCorners(roundingRadius);
        Glide.with(imageView.getContext())
                .load(url)
                .apply(getRequestOptions())
                .apply(RequestOptions.bitmapTransform(roundedCorners))
                .into(imageView);
    }

    @Override
    public void loadCircleBorderImage(String url, ImageView imageView, float borderWidth, int borderColor, int heightPx, int widthPx) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(getRequestOptions())
                .transform(new CircleTransform(imageView.getContext(),borderWidth,borderColor,heightPx,widthPx))
                .into(imageView);
    }

    @Override
    public void loadGifImage(String url, ImageView imageView) {
        Glide.with(imageView.getContext())
                .asGif()
                .load(url)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void loadGif2CommonImage(String url, ImageView imageView) {
        Glide.with(imageView.getContext())
                .asBitmap()
                .load(url)
                .apply(getRequestOptions())
                .into(imageView);
    }

    @Override
    public void loadImageWithProgress(String url, ImageView imageView, ProgressLoadListener listener) {

    }

    @Override
    public void asyncDownloadImage(final Context context, String url, final String savePath, final String saveFileName, final DownloadListener listener) {

        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>(){
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        File destFile = FileUtils.savaFileUtils(context, true, savePath,saveFileName);
                        FileUtils.saveBitmap2File(context,resource,destFile);
                        listener.onDownloadSuccess();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        listener.onDownloadFail();
                    }
                });
    }

        @Override
    public void clearImageDiskCache(Context context) {
        //清空内存缓存，要求在主线程中执行
        Glide.get(context).clearMemory();
    }

    @Override
    public void clearImageMemoryCache(Context context) {
        //清空磁盘缓存，要求在子线程中执行
        Glide.get(context).clearDiskCache();
    }

    @Override
    public void trimMemory(Context context, int level) {
        //裁剪 Glide 缓存的图片内存空间
        Glide.get(context).trimMemory(level);
    }

    @Override
    public void clearImageAllCache(Context context) {

    }

    @Override
    public String getCacheSize(Context context) {
        try {
            return FileUtils.getFormatSize(FileUtils.getFolderSize(new File(context.getCacheDir() + "/"+ InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public RequestOptions getRequestOptions() {
        DiskCacheStrategy diskCacheStrategy;
        if(builder.isDiskCache()){
            diskCacheStrategy = DiskCacheStrategy.ALL;
        }else{
            diskCacheStrategy = DiskCacheStrategy.NONE;
        }

        RequestOptions options =new RequestOptions();
        //优先级设置
        options.priority(Priority.HIGH)
                //设置占位图
                .placeholder(builder.getPlaceholderId() != 0 ? builder.getPlaceholderId() : R.drawable.default_image)
                //设置错误图片
                .error(builder.getErrorImageId() != 0 ? builder.getErrorImageId()  : R.drawable.default_image)
                //url为null
                .fallback(builder.getFallbackImageId() != 0 ? builder.getFallbackImageId() : R.drawable.default_image)
                //指定图片大小
                .skipMemoryCache(!builder.isMemoryCache())
                .diskCacheStrategy(diskCacheStrategy);
        return options;
    }
}
