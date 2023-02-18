package com.hnradio.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/12/20.glide辅助类
 */

public class GlideUtil {

    //默认配置
    private static RequestOptions defaultOption = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(true)
            .optionalCenterCrop();

    /**
     * 加载URL图片
     */
    public static void loadImage(String url, ImageView imageView) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .load(url)
                .into(imageView);
    }

    /**
     * 加载URL图片
     */
    public static void loadImage(String url, ImageView imageView, @DrawableRes int errorSrcId) {
        RequestOptions options = new RequestOptions()
                .error(errorSrcId);
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * 加载URL图片
     */
    public static void loadImage(String url, ImageView imageView, @DrawableRes int errorSrcId, @DrawableRes int placeSrcId) {
        RequestOptions options = new RequestOptions()
                .error(errorSrcId)
                .placeholder(placeSrcId);
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    /**
     * 加载drawable图片
     */
    public static void loadImage(@DrawableRes int drawable_id, ImageView imageView) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .load(drawable_id)
                .into(imageView);
    }

    /**
     * 加载bitmap图片
     */
    public static void loadImage(Bitmap bitmap, ImageView imageView) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .load(bitmap)
                .into(imageView);
    }

    /**
     * 加载圆角图片
     */
    public static void loadImageRound(String url, ImageView imageView, int size) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .load(url)
                .apply(RequestOptions
                        .bitmapTransform(new RoundedCorners(size)))
                .into(imageView);
    }

    //加载GIF图片
    public static void loadGifImage(String url, ImageView imageView) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .asGif()
                .load(url)
                .into(imageView);
    }

    //加载GIF图片
    public static void loadGifImage(int sourceId, ImageView imageView) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .asGif()
                .load(sourceId)
                .into(imageView);
    }

    /**
     * 加载圆图片
     */
    public static void loadImageCircle(String url, ImageView imageView) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)
                .load(url)
                .apply(RequestOptions
                        .bitmapTransform(new CircleCrop()))
                .into(imageView);

    }
    /**
     * 加载圆图片
     */
    public static void loadImageCircle(String url, ImageView imageView,@DrawableRes int errorSrcId) {
        Glide.with(imageView.getContext())
                .applyDefaultRequestOptions(defaultOption)

                .load(url)
                .apply(RequestOptions
                        .bitmapTransform(new CircleCrop()))
                .error(errorSrcId)
                .into(imageView);

    }

    /**
     * 加载圆图片
     */
    public static void loadImageBackground(String url, View view) {
        Glide.with(view.getContext()).load(url).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                view.setBackground(resource);
            }
        });

    }

    /**
     * Gif 加载 可以设置次数，监听播放完成回调
     *
     * @param context     上下文对象
     * @param model       传入的gif地址，可以是网络，也可以是本地，（https://raw.githubusercontent.com/Jay-YaoJie/KotlinDialogs/master/diagram/test.gif）
     * @param imageView   要显示的imageView
     * @param loopCount   播放次数
     * @param gifListener Gif播放完毕回调
     */
    public static void loadOneTimeGif(Context context, Object model, final ImageView imageView, int loopCount, final GifListener gifListener) {
        Glide.with(context).asGif().load(model).listener(new RequestListener<GifDrawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                try {
                    Field gifStateField = GifDrawable.class.getDeclaredField("state");
                    gifStateField.setAccessible(true);
                    Class gifStateClass = Class.forName("com.bumptech.glide.load.resource.gif.GifDrawable$GifState");
                    Field gifFrameLoaderField = gifStateClass.getDeclaredField("frameLoader");
                    gifFrameLoaderField.setAccessible(true);
                    Class gifFrameLoaderClass = Class.forName("com.bumptech.glide.load.resource.gif.GifFrameLoader");
                    Field gifDecoderField = gifFrameLoaderClass.getDeclaredField("gifDecoder");
                    gifDecoderField.setAccessible(true);
                    Class gifDecoderClass = Class.forName("com.bumptech.glide.gifdecoder.GifDecoder");
                    Object gifDecoder = gifDecoderField.get(gifFrameLoaderField.get(gifStateField.get(resource)));
                    Method getDelayMethod = gifDecoderClass.getDeclaredMethod("getDelay", int.class);
                    getDelayMethod.setAccessible(true);
                    //设置播放次数
                    resource.setLoopCount(loopCount);
                    //获得总帧数
                    int count = resource.getFrameCount();
                    int delay = 0;
                    for (int i = 0; i < count; i++) {
                        //计算每一帧所需要的时间进行累加
                        delay += (int) getDelayMethod.invoke(gifDecoder, i);
                    }
                    imageView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (gifListener != null) {
                                gifListener.gifPlayComplete();
                            }
                        }
                    }, delay);
                } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }).into(imageView);
    }

    /**
     * Gif播放完毕回调
     */
    public interface GifListener {
        void gifPlayComplete();
    }

}
