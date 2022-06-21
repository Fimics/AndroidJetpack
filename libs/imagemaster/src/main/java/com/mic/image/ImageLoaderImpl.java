package com.mic.image;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import com.mic.image.strategy.fresco.FrescoStrategy;
import com.mic.image.strategy.picasso.PicassoStrategy;
import com.mic.image.strategy.glide.GlideStrategy;
import java.io.File;

/**
 * @author lipengju
 */
class ImageLoaderImpl implements ImageLoader {

    private Strategy strategy = Strategy.GLIDE;
    private ImageLoader mStrategy;
    private static Builder mBuilder;
    private static volatile  ImageLoaderImpl instance=null;

    private ImageLoaderImpl(Strategy strategy) {
        this.strategy=strategy;
        switch (strategy) {
            case FRESCO:
                mStrategy = new FrescoStrategy(mBuilder);
                break;
            case PICASSO:
                mStrategy = new PicassoStrategy(mBuilder);
                break;
            default:
                mStrategy = new GlideStrategy(mBuilder);
                break;
        }
    }

    public static ImageLoader get(Builder builder){
        mBuilder = builder;
        if(builder!=null && builder.getStrategy()==null){
           throw new IllegalArgumentException("Builder 的 Strategy参数不能为空");
        }
        if(null==instance){
            synchronized (ImageLoaderImpl.class){
                if(null==instance){
                    instance = new ImageLoaderImpl(mBuilder.getStrategy());
                }
            }
        }
        return instance;
    }

    @Override
    public void loadImageWithAppCxt(String url, ImageView imageView) {
        mStrategy.loadImageWithAppCxt(url, imageView);
    }

    @Override
    public void loadImage(Context context, String url, ImageView imageView) {
        mStrategy.loadImage(context, url, imageView);
    }

    /**
     * 预加载图片
     */
    @Override
    public void preLoadImage(Context context, String url) {
        mStrategy.preLoadImage(context, url);
    }

    /**
     * 加载网络url图片、包含Gif图片
     */
    @Override
    public void loadImage(String url, ImageView imageView) {
        mStrategy.loadImage(url, imageView);
    }

    @Override
    public void loadImageAssets(String assets, ImageView imageView) {
        mStrategy.loadImageAssets(assets, imageView);
    }

    @Override
    public void loadImageResources(int resources, ImageView imageView) {
        mStrategy.loadImageResources(resources, imageView);
    }

    @Override
    public void loadImageFile(File file, ImageView imageView) {
        mStrategy.loadImageFile(file, imageView);
    }

    @Override
    public void loadImageUri(Uri uri, ImageView imageView) {
        mStrategy.loadImageUri(uri, imageView);
    }

    /**
     * 加载byteArray图片
     */
    @Override
    public void loadImageByte(byte[] byteArray, ImageView imageView) {
        mStrategy.loadImageByte(byteArray, imageView);
    }

    @Override
    public void loadGifImage(String url, ImageView imageView) {
        mStrategy.loadGifImage(url, imageView);
    }

    @Override
    public void loadGif2CommonImage(String url, ImageView imageView) {
        mStrategy.loadGif2CommonImage(url, imageView);
    }

    /**
     * 加载url图片，并设置圆角
     */
    @Override
    public void loadRoundImage(int roundingRadius, String url, ImageView imageView) {
        mStrategy.loadRoundImage(roundingRadius, url, imageView);
    }

    /**
     * 加载url图片，设置圆形
     */
    @Override
    public void loadCircleImage(String url, ImageView imageView) {
        mStrategy.loadCircleImage(url, imageView);
    }

    /**
     * 加载url图片，自定义一些属性
     */
    @Override
    public void loadCircleBorderImage(String url, ImageView imageView, float borderWidth, int borderColor, int heightPX, int widthPX) {
        mStrategy.loadCircleBorderImage(url, imageView, borderWidth, borderColor, heightPX, widthPX);
    }

    @Override
    public void loadImageWithProgress(String url, ImageView imageView, ProgressLoadListener listener) {
        mStrategy.loadImageWithProgress(url, imageView, listener);
    }

    @Override
    public void asyncDownloadImage(Context context, String url, String savePath, String saveFileName, DownloadListener listener) {
        mStrategy.asyncDownloadImage(context, url, savePath, saveFileName, listener);
    }

    @Override
    public void clearImageDiskCache(final Context context) {
        mStrategy.clearImageDiskCache(context);
    }

    @Override
    public void clearImageMemoryCache(Context context) {
        mStrategy.clearImageMemoryCache(context);
    }

    @Override
    public void clearImageAllCache(Context context) {
        clearImageDiskCache(context.getApplicationContext());
        clearImageMemoryCache(context.getApplicationContext());
    }

    @Override
    public String getCacheSize(Context context) {
        return mStrategy.getCacheSize(context);
    }

    @Override
    public void trimMemory(Context context, int level) {
        mStrategy.trimMemory(context, level);
    }

}
