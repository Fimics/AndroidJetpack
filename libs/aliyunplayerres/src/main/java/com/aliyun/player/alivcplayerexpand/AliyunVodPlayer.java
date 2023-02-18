package com.aliyun.player.alivcplayerexpand;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.aliyun.player.IPlayer;
import com.aliyun.player.alivcplayerexpand.gesture.GestureDialogManager;
import com.aliyun.player.alivcplayerexpand.gesture.GestureView;
import com.aliyun.player.alivcplayerexpand.listener.LockPortraitListener;
import com.aliyun.player.alivcplayerexpand.listener.OnAutoPlayListener;
import com.aliyun.player.alivcplayerexpand.listener.OnStoppedListener;
import com.aliyun.player.alivcplayerexpand.more.AliyunShowMoreValue;
import com.aliyun.player.alivcplayerexpand.more.ShowMoreView;
import com.aliyun.player.alivcplayerexpand.more.SpeedValue;
import com.aliyun.player.alivcplayerexpand.theme.ITheme;
import com.aliyun.player.alivcplayerexpand.theme.Theme;
import com.aliyun.player.alivcplayerexpand.tipsview.OnTipsViewBackClickListener;
import com.aliyun.player.alivcplayerexpand.tipsview.TipsView;
import com.aliyun.player.alivcplayerexpand.util.FileUtils;
import com.aliyun.player.alivcplayerexpand.util.NetWatchdog;
import com.aliyun.player.alivcplayerexpand.util.OrientationWatchDog;
import com.aliyun.player.alivcplayerexpand.util.ScreenUtils;
import com.aliyun.player.alivcplayerexpand.util.ThreadUtils;
import com.aliyun.player.alivcplayerexpand.util.ToastUtils;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.bean.InfoCode;
import com.aliyun.player.nativeclass.CacheConfig;
import com.aliyun.player.nativeclass.MediaInfo;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.nativeclass.TrackInfo;
import com.aliyun.player.source.UrlSource;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AliyunVodPlayer extends RelativeLayout implements ITheme{

    //视频画面
    private SurfaceView mSurfaceView;
    //手势操作view
    private GestureView mGestureView;
    //手势对话框控制
    private GestureDialogManager mGestureDialogManager;
    //网络状态监听
    private NetWatchdog mNetWatchdog;
    //屏幕方向监听
    private OrientationWatchDog mOrientationWatchDog;
    //Tips view
    private TipsView mTipsView;

    private UrlSource mAliyunLocalSource;
    //seekTo的位置
    private int mSeekToPosition;
    //原视频seekTo的位置
    private int mSourceSeekToPosition;
    //原视频时长
    private long mSourceDuration;
    //皮肤view
    private ControlView mControlView;
    //当前屏幕模式
    private AliyunScreenMode mCurrentScreenMode = AliyunScreenMode.Small;
    /**
     * 判断VodePlayer 是否加载完成
     */
    private Map<MediaInfo, Boolean> hasLoadEnd = new HashMap<>();
    private VodPlayerLoadEndHandler vodPlayerLoadEndHandler = new VodPlayerLoadEndHandler(this);
    /**
     * 原视频MediaInfo
     */
    private MediaInfo mSourceVideoMediaInfo;
    //媒体信息
    private MediaInfo mAliyunMediaInfo;//原视频的buffered
    private long mVideoBufferedPosition = 0;
    //原视频的currentPosition
    private long mCurrentPosition = 0;
    //seekTo时视频的position播放位置
    private int mSeekToCurrentPlayerPosition;

    //第一次初始化网络监听
    private boolean initNetWatch;
    //播放器+渲染的View
    private AliyunRenderView mAliyunRenderView;
    //当前屏幕亮度
    private int mScreenBrightness;
    //音量
    private float currentVolume;
    //运营商是否自动播放
    private boolean mIsOperatorPlay;
    //是不是在seek中
    private boolean inSeek = false;
    //播放是否完成
    private boolean isCompleted = false;

    // 连网断网监听
    private NetConnectedListener mNetConnectedListener = null;
    // 横屏状态点击更多
    private ControlView.OnShowMoreClickListener mOutOnShowMoreClickListener;
    //播放按钮点击监听
    private OnPlayStateBtnClickListener onPlayStateBtnClickListener;

    //ControView隐藏事件
    private ControlView.OnControlViewHideListener mOnControlViewHideListener;
    //字幕、清晰度、音轨、码率点击事件
    private ControlView.OnTrackInfoClickListener mOutOnTrackInfoClickListener;
    //是否锁定全屏
    private boolean mIsFullScreenLocked = false;
    //锁定竖屏
    private LockPortraitListener mLockPortraitListener = null;

    public AliyunVodPlayer(Context context) {
        super(context);
        initVideoView();
    }

    public AliyunVodPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView();
    }

    public AliyunVodPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView();
    }

    //初始化handler
    private VodPlayerHandler mVodPlayerHandler;
    
    /**
     * 初始化view
     */
    private void initVideoView() {
        //初始化handler
        mVodPlayerHandler = new VodPlayerHandler(this);
        //初始化播放器
        initAliVcPlayer();
        //初始化手势view
        initGestureView();
        //初始化控制栏
        initControlView();
        //初始化提示view
        initTipsView();
        //初始化网络监听器
        initNetWatchdog();
        //初始化屏幕方向监听
        initOrientationWatchdog();
        //初始化手势对话框控制
        initGestureDialogManager();
        //先隐藏手势和控制栏，防止在没有prepare的时候做操作。
        hideGestureAndControlViews();
    }

    /**
     * 展示replay
     */
    public void showReplay(){
        if (mTipsView != null) {
            //隐藏其他的动作,防止点击界面去进行其他操作
            mGestureView.hide(ViewAction.HideType.End);
            mControlView.hide(ViewAction.HideType.End);
            mTipsView.showReplayTipView();
        }
    }

    /**
     * 设置播放源
     *
     */
    public void setLocalSource(UrlSource aliyunLocalSource) {
        if (mAliyunRenderView == null) {
            return;
        }

        clearAllSource();
        reset();
        //播放本地资源,需要清空VIDEO_FUNCTION
        mAliyunLocalSource = aliyunLocalSource;

        if (!show4gTips()) {
            prepareLocalSource(aliyunLocalSource);
        }
    }

    /**
     * 清空之前设置的播放源
     */
    private void clearAllSource() {
        mAliyunLocalSource = null;
    }

    /**
     * 重置。包括一些状态值，view的状态等
     */
    private void reset() {
        isCompleted = false;
        inSeek = false;
        mCurrentPosition = 0;
        mVideoBufferedPosition = 0;

        if (mTipsView != null) {
            mTipsView.hideAll();
        }
        if (mControlView != null) {
            mControlView.reset();
        }
        if (mGestureView != null) {
            mGestureView.reset();
        }
        stop();
    }
    /**
     * 判断是否展示4g提示,内部进行了判断
     * 1.不是本地视频
     * 2.当前网络是4G
     * 满足上述条件才会展示4G提示
     *
     * @return true:需要展示,false不需要
     */
    private boolean show4gTips() {
        //播放本地文件不需要提示
        if (isLocalSource()) {
            return false;
        } else {
            //不是本地文件
            if (NetWatchdog.is4GConnected(getContext())) {
                if (mIsOperatorPlay) {
                    //运营商自动播放,则Toast提示后,继续播放
                    ToastUtils.show(getContext(), R.string.alivc_operator_play);
                    return false;
                } else {
                    if (mTipsView != null) {
                        mTipsView.showNetChangeTipView();
                    }
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * 设置自动播放
     *
     * @param auto true 自动播放
     */
    public void setAutoPlay(boolean auto) {
        if (mAliyunRenderView != null) {
            mAliyunRenderView.setAutoPlay(auto);
        }
    }
    /**
     * 更新UI播放器的主题
     *
     * @param theme 支持的主题
     */
    @Override
    public void setTheme(Theme theme) {
        //通过判断子View是否实现了ITheme的接口，去更新主题
        int childCounts = getChildCount();
        for (int i = 0; i < childCounts; i++) {
            View view = getChildAt(i);
            if (view instanceof ITheme) {
                ((ITheme) view).setTheme(theme);
            }
        }
    }

    /**
     * 设置缓存配置
     */
    public void setCacheConfig(CacheConfig cacheConfig){
        if(mAliyunRenderView != null){
            mAliyunRenderView.setCacheConfig(cacheConfig);
        }
    }

    /**
     * 获取Config
     */
    public PlayerConfig getPlayerConfig() {
        if(mAliyunRenderView != null){
            return mAliyunRenderView.getPlayerConfig();
        }
        return null;
    }

    /**
     * 设置Config
     */
    public void setPlayerConfig(PlayerConfig playerConfig){
        if(mAliyunRenderView != null){
            mAliyunRenderView.setPlayerConfig(playerConfig);
        }
    }

    /**
     * 设置硬解码开关
     */
    public void setEnableHardwareDecoder(boolean enableHardwareDecoder) {
        if (mAliyunRenderView != null) {
            mAliyunRenderView.enableHardwareDecoder(enableHardwareDecoder);
        }
    }

    /**
     * 设置播放时的镜像模式
     *
     * @param mode 镜像模式
     */
    public void setRenderMirrorMode(IPlayer.MirrorMode mode) {
        if (mAliyunRenderView != null) {
            mAliyunRenderView.setMirrorMode(mode);
        }
    }

    /**
     * 设置播放时的旋转方向
     *
     * @param rotate 旋转角度
     */
    public void setRenderRotate(IPlayer.RotateMode rotate) {
        if (mAliyunRenderView != null) {
            mAliyunRenderView.setRotateModel(rotate);
        }
    }

    /**
     * 隐藏手势和控制栏
     */
    private void hideGestureAndControlViews() {
        if (mGestureView != null) {
            mGestureView.hide(ViewAction.HideType.Normal);
        }
        if (mControlView != null) {
            mControlView.hide(ViewAction.HideType.Normal);
        }
    }

    /**
     * 初始化网络监听
     */
    private void initNetWatchdog() {
        Context context = getContext();
        mNetWatchdog = new NetWatchdog(context);
        mNetWatchdog.setNetChangeListener(new MyNetChangeListener(this));
        mNetWatchdog.setNetConnectedListener(new MyNetConnectedListener(this));
    }

    /**
     * 初始化控制栏view
     */
    private void initControlView() {
        mControlView = new ControlView(getContext());
        addSubView(mControlView);

        //设置播放按钮点击
        mControlView.setOnPlayStateClickListener(new ControlView.OnPlayStateClickListener() {
            @Override
            public void onPlayStateClick() {
                switchPlayerState();
            }
        });
        //设置进度条的seek监听
        mControlView.setOnSeekListener(new ControlView.OnSeekListener() {
            @Override
            public void onSeekEnd(int position) {
                if (mControlView != null) {
                    mControlView.setVideoPosition(position);
                }
                if (isCompleted) {
                    //播放完成了，不能seek了
                    inSeek = false;
                } else {

                    //拖动结束后，开始seek
                    seekTo(position);

                    if (onSeekStartListener != null) {
                        onSeekStartListener.onSeekStart(position);
                    }
                }
            }

            @Override
            public void onSeekStart(int position) {
                //拖动开始
                inSeek = true;
                mSeekToCurrentPlayerPosition = position;
            }

            @Override
            public void onProgressChanged(int progress) {
            }
        });
        //清晰度按钮点击
        mControlView.setOnQualityBtnClickListener(new ControlView.OnQualityBtnClickListener() {

            @Override
            public void onQualityBtnClick(View v, List<TrackInfo> qualities, String currentQuality) {

            }

            @Override
            public void onHideQualityView() {
            }
        });

        //点击锁屏的按钮
        mControlView.setOnScreenLockClickListener(new ControlView.OnScreenLockClickListener() {
            @Override
            public void onClick() {
                lockScreen(!mIsFullScreenLocked);
            }
        });
        //点击全屏/小屏按钮
        mControlView.setOnScreenModeClickListener(new ControlView.OnScreenModeClickListener() {
            @Override
            public void onClick() {
                if (mCurrentScreenMode == AliyunScreenMode.Small) {
                    changedToLandForwardScape(true);
                } else {
                    changedToPortrait(true);
                }
                if (mCurrentScreenMode == AliyunScreenMode.Full) {
                    mControlView.showMoreButton();
                    mControlView.showBackButton();
                } else if (mCurrentScreenMode == AliyunScreenMode.Small) {
                    mControlView.hideMoreButton();
                    mControlView.hideBackButton();
                }
            }
        });
        //点击了标题栏的返回按钮
        mControlView.setOnBackClickListener(new ControlView.OnBackClickListener() {
            @Override
            public void onClick() {
                if (mCurrentScreenMode == AliyunScreenMode.Full) {
                    if (isLocalSource()) {
                        //如果播放的是本地视频,并且是全屏模式下点击返回按钮,则需要关闭Activity
                        if (orientationChangeListener != null) {
                            orientationChangeListener.orientationChange(false, AliyunScreenMode.Small);
                        }
                    } else {
                        //设置为小屏状态
                        changeScreenMode(AliyunScreenMode.Small, false);
                    }
                } else if (mCurrentScreenMode == AliyunScreenMode.Small) {
                    //小屏状态下，就结束活动
                    Context context = getContext();
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }

                if (mCurrentScreenMode == AliyunScreenMode.Small) {
                    mControlView.hideMoreButton();
                }
            }
        });

        // 横屏下显示更多
        mControlView.setOnShowMoreClickListener(new ControlView.OnShowMoreClickListener() {
            @Override
            public void showMore() {
                showMoreDialog();
                if (mOutOnShowMoreClickListener != null) {
                    mOutOnShowMoreClickListener.showMore();
                }
            }
        });

        // 截屏
        mControlView.setOnScreenShotClickListener(new ControlView.OnScreenShotClickListener() {
            @Override
            public void onScreenShotClick() {
                if (!mIsFullScreenLocked) {
                    snapShot();
                }
            }
        });

        //ControlView隐藏监听
        mControlView.setOnControlViewHideListener(new ControlView.OnControlViewHideListener() {
            @Override
            public void onControlViewHide() {
                if (mOnControlViewHideListener != null) {
                    mOnControlViewHideListener.onControlViewHide();
                }
            }
        });

    }


    private AlivcShowMoreDialog showMoreDialog;
    private void showMoreDialog() {
        showMoreDialog = new AlivcShowMoreDialog(getContext());
        AliyunShowMoreValue moreValue = new AliyunShowMoreValue();
        moreValue.setSpeed(getCurrentSpeed());
        moreValue.setVolume((int) getCurrentVolume());
        moreValue.setScaleMode(getScaleMode());
        moreValue.setLoop(isLoop());

        ShowMoreView showMoreView = new ShowMoreView(getContext(), moreValue);
        showMoreDialog.setContentView(showMoreView);
        showMoreDialog.show();

        showMoreView.setOnSpeedCheckedChangedListener(new ShowMoreView.OnSpeedCheckedChangedListener() {
            @Override
            public void onSpeedChanged(RadioGroup group, int checkedId) {
                // 点击速度切换
                if (checkedId == R.id.rb_speed_normal) {
                    changeSpeed(SpeedValue.One);
                } else if (checkedId == R.id.rb_speed_onequartern) {
                    changeSpeed(SpeedValue.OneQuartern);
                } else if (checkedId == R.id.rb_speed_onehalf) {
                    changeSpeed(SpeedValue.OneHalf);
                } else if (checkedId == R.id.rb_speed_twice) {
                    changeSpeed(SpeedValue.Twice);
                }

            }
        });

        showMoreView.setOnScaleModeCheckedChangedListener(new ShowMoreView.OnScaleModeCheckedChangedListener() {
            @Override
            public void onScaleModeChanged(RadioGroup group, int checkedId) {
                //切换画面比例
                IPlayer.ScaleMode mScaleMode;
                if (checkedId == R.id.rb_scale_aspect_fit) {
                    mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
                } else if (checkedId == R.id.rb_scale_aspect_fill) {
                    mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL;
                } else if (checkedId == R.id.rb_scale_to_fill) {
                    mScaleMode = IPlayer.ScaleMode.SCALE_TO_FILL;
                } else {
                    mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
                }
                setScaleMode(mScaleMode);
            }
        });

        showMoreView.setOnLoopCheckedChangedListener(new ShowMoreView.OnLoopCheckedChangedListener() {
            @Override
            public void onLoopChanged(RadioGroup group, int checkedId) {
                boolean isLoop;
                if (checkedId == R.id.rb_loop_open) {
                    isLoop = true;
                } else {
                    isLoop = false;
                }
                setLoop(isLoop);
            }
        });

        /**
         * 初始化亮度
         */
        showMoreView.setBrightness(getScreenBrightness());

        // 亮度seek
        showMoreView.setOnLightSeekChangeListener(new ShowMoreView.OnLightSeekChangeListener() {
            @Override
            public void onStart(SeekBar seekBar) {

            }

            @Override
            public void onProgress(SeekBar seekBar, int progress, boolean fromUser) {
                setWindowBrightness(progress);
                setScreenBrightness(progress);
            }

            @Override
            public void onStop(SeekBar seekBar) {

            }
        });

        /**
         * 初始化音量
         */
        showMoreView.setVoiceVolume(getCurrentVolume());
        showMoreView.setOnVoiceSeekChangeListener(new ShowMoreView.OnVoiceSeekChangeListener() {
            @Override
            public void onStart(SeekBar seekBar) {

            }

            @Override
            public void onProgress(SeekBar seekBar, int progress, boolean fromUser) {
                setCurrentVolume(progress / 100.00f);
            }

            @Override
            public void onStop(SeekBar seekBar) {

            }
        });

    }

    /**
     * 设置屏幕亮度
     */
    private void setWindowBrightness(int brightness) {
        if(getContext() instanceof  Activity){
            Window window = ((Activity)getContext()).getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.screenBrightness = brightness / 100.00f;
            window.setAttributes(lp);
        }
    }

    /**
     * 设置当前屏幕亮度
     */
    public void setScreenBrightness(int screenBrightness) {
        this.mScreenBrightness = screenBrightness;
    }

    /**
     * 获取当前屏幕亮度
     */
    public int getScreenBrightness() {
        return this.mScreenBrightness;
    }

    /**
     * 获取缩放模式
     * @return      当前缩放模式
     */
    public IPlayer.ScaleMode getScaleMode(){
        IPlayer.ScaleMode scaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
        if (mAliyunRenderView != null) {
            scaleMode = mAliyunRenderView.getScaleModel();
        }
        return scaleMode;
    }

    /**
     * 设置当前速率
     */
    public void setCurrentVolume(float progress) {
        if(progress <= 0){
            progress = 0;
        }
        if(progress >= 1){
            progress = 1;
        }
        this.currentVolume = progress;
        if (mAliyunRenderView != null) {
            mAliyunRenderView.setVolume(progress);
        }
    }

    /**
     * 获取当前音量
     */
    public float getCurrentVolume() {
        if (mAliyunRenderView != null) {
            return mAliyunRenderView.getVolume();
        }
        return 0;
    }

    /**
     * 设置是否循环播放
     */
    public void setLoop(boolean isLoop){
        if(mAliyunRenderView != null){
            mAliyunRenderView.setLoop(isLoop);
        }
    }

    /**
     * 是否开启循环播放
     */
    public boolean isLoop(){
        if(mAliyunRenderView != null){
            return mAliyunRenderView.isLoop();
        }
        return false;
    }

    /**
     * 设置缩放模式
     *
     * @param scaleMode 缩放模式
     */
    public void setScaleMode(IPlayer.ScaleMode scaleMode) {
        if (mAliyunRenderView != null) {
            mAliyunRenderView.setScaleModel(scaleMode);
        }
    }

    private float currentSpeed;
    /**
     * 切换播放速度
     *
     * @param speedValue 播放速度
     */
    public void changeSpeed(SpeedValue speedValue) {
        if (speedValue == SpeedValue.One) {
            currentSpeed = 1.0f;
        } else if (speedValue == SpeedValue.OneQuartern) {
            currentSpeed = 0.5f;
        } else if (speedValue == SpeedValue.OneHalf) {
            currentSpeed = 1.5f;
        } else if (speedValue == SpeedValue.Twice) {
            currentSpeed = 2.0f;
        }
        mAliyunRenderView.setSpeed(currentSpeed);
    }

    /**
     * 获取当前速率
     */
    public float getCurrentSpeed() {
        return currentSpeed;
    }


    /**
     * 截图功能
     *
     * @return 图片
     */
    public void snapShot() {
        if (mAliyunRenderView != null) {
            mAliyunRenderView.snapshot();
        }
    }

    /**
     * 改变屏幕模式：小屏或者全屏。
     *
     * @param targetMode {@link AliyunScreenMode}
     */
    public void changeScreenMode(AliyunScreenMode targetMode, boolean isReverse) {
        AliyunScreenMode finalScreenMode = targetMode;

        if (mIsFullScreenLocked) {
            finalScreenMode = AliyunScreenMode.Full;
        }

        //这里可能会对模式做一些修改
        if (targetMode != mCurrentScreenMode) {
            mCurrentScreenMode = finalScreenMode;
        }

        if(mGestureDialogManager != null){
            mGestureDialogManager.setCurrentScreenMode(mCurrentScreenMode);
        }

        if (mControlView != null) {
            mControlView.setScreenModeStatus(finalScreenMode);
        }

        Context context = getContext();
        if (context instanceof Activity) {
            if (finalScreenMode == AliyunScreenMode.Full) {
                if (getLockPortraitMode() == null) {
                    //不是固定竖屏播放。
//                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    if (isReverse) {
                        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    } else {
                        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }

                    //SCREEN_ORIENTATION_LANDSCAPE只能固定一个横屏方向
                } else {
                    //如果是固定全屏，那么直接设置view的布局，宽高
                    ViewGroup.LayoutParams aliVcVideoViewLayoutParams = getLayoutParams();
                    aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                }
            } else if (finalScreenMode == AliyunScreenMode.Small) {

                if (getLockPortraitMode() == null) {
                    //不是固定竖屏播放。
                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    //如果是固定全屏，那么直接设置view的布局，宽高
                    ViewGroup.LayoutParams aliVcVideoViewLayoutParams = getLayoutParams();
                    aliVcVideoViewLayoutParams.height = (int) (ScreenUtils.getWidth(context) * 9.0f / 16);
                    aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                }
            }
        }
    }

    /**
     * 锁定竖屏
     *
     * @return 竖屏监听器
     */
    public LockPortraitListener getLockPortraitMode() {
        return mLockPortraitListener;
    }

    /**
     * 屏幕方向变为横屏。
     *
     * @param fromPort 是否从竖屏变过来
     */
    private void changedToLandForwardScape(boolean fromPort) {
        //如果不是从竖屏变过来，也就是一直是横屏的时候，就不用操作了
        if (!fromPort) {
            return;
        }
        changeScreenMode(AliyunScreenMode.Full, false);
        if (orientationChangeListener != null) {
            orientationChangeListener.orientationChange(fromPort, mCurrentScreenMode);
        }
    }

    /**
     * 屏幕方向变为横屏。
     *
     * @param fromPort 是否从竖屏变过来
     */
    private void changedToLandReverseScape(boolean fromPort) {
        //如果不是从竖屏变过来，也就是一直是横屏的时候，就不用操作了
        if (!fromPort) {
            return;
        }
        changeScreenMode(AliyunScreenMode.Full, true);
        if (orientationChangeListener != null) {
            orientationChangeListener.orientationChange(fromPort, mCurrentScreenMode);
        }
    }

    /**
     * 屏幕方向变为竖屏
     *
     * @param fromLand 是否从横屏转过来
     */
    private void changedToPortrait(boolean fromLand) {
        //屏幕转为竖屏
        if (mIsFullScreenLocked) {
            return;
        }

        if (mCurrentScreenMode == AliyunScreenMode.Full) {
            //全屏情况转到了竖屏
            if (getLockPortraitMode() == null) {
                //没有固定竖屏，就变化mode
                if (fromLand) {
                    changeScreenMode(AliyunScreenMode.Small, false);
                } else {
                    //如果没有转到过横屏，就不让他转了。防止竖屏的时候点横屏之后，又立即转回来的现象
                }
            } else {
                //固定竖屏了，竖屏还是竖屏，不用动
            }
        } else if (mCurrentScreenMode == AliyunScreenMode.Small) {
            //竖屏的情况转到了竖屏
        }
        if (orientationChangeListener != null) {
            orientationChangeListener.orientationChange(fromLand, mCurrentScreenMode);
        }
    }

    /**
     * 初始化手势的控制类
     */
    private void initGestureDialogManager() {
        Context context = getContext();
        if (context instanceof Activity) {
            mGestureDialogManager = new GestureDialogManager((Activity) context);
        }
    }

    /**
     * 初始化屏幕方向旋转。用来监听屏幕方向。结果通过OrientationListener回调出去。
     */
    private void initOrientationWatchdog() {
        final Context context = getContext();
        mOrientationWatchDog = new OrientationWatchDog(context);
        mOrientationWatchDog.setOnOrientationListener(new InnerOrientationListener(this));
    }

    private static class InnerOrientationListener implements OrientationWatchDog.OnOrientationListener {

        private WeakReference<AliyunVodPlayer> playerViewWeakReference;

        public InnerOrientationListener(AliyunVodPlayer playerView) {
            playerViewWeakReference = new WeakReference<AliyunVodPlayer>(playerView);
        }

        @Override
        public void changedToLandForwardScape(boolean fromPort) {
            AliyunVodPlayer playerView = playerViewWeakReference.get();
            if (playerView != null) {
                playerView.changedToLandForwardScape(fromPort);
            }
        }

        @Override
        public void changedToLandReverseScape(boolean fromPort) {
            AliyunVodPlayer playerView = playerViewWeakReference.get();
            if (playerView != null) {
                playerView.changedToLandReverseScape(fromPort);
            }
        }

        @Override
        public void changedToPortrait(boolean fromLand) {
            AliyunVodPlayer playerView = playerViewWeakReference.get();
            if (playerView != null) {
                playerView.changedToPortrait(fromLand);
            }
        }
    }

    /**
     * seek操作
     *
     * @param position 目标位置
     */
    public void seekTo(int position) {
        mSeekToPosition = position;
        if (mAliyunRenderView == null) {
            return;
        }
        inSeek = true;
        //如果是视频广告跟试看同时存在，第一段广告播放完毕，试看view显示之后不播放广告

        mSourceSeekToPosition = position;
        realySeekToFunction(position);
    }

    private void realySeekToFunction(int position) {
        /** 这里由于如果是视频广告seekEnd返回的progress是包含了视频广告的时间,而这里的seek,需要的是原视频的seek时间,所以需要减去视频广告的时间 */
        if (GlobalPlayerConfig.IS_VIDEO) {
            isAutoAccurate(position);
        } else {
            isAutoAccurate(position);
        }
        mAliyunRenderView.start();
        if (mControlView != null) {
            mControlView.setPlayState(ControlView.PlayState.Playing);
        }
    }

    /**
     * 判断是否开启精准seek
     */
    private void isAutoAccurate(long position) {
//        if (GlobalPlayerConfig.PlayConfig.mEnableAccurateSeekModule) {
//            mAliyunRenderView.seekTo(position, IPlayer.SeekMode.Accurate);
//        } else {
            mAliyunRenderView.seekTo(position, IPlayer.SeekMode.Inaccurate);
//        }
    }

    /**
     * 初始化手势view
     */
    private void initGestureView() {
        mGestureView = new GestureView(getContext());
        addSubView(mGestureView);
        mGestureView.setMultiWindow(false);

        //设置手势监听
        mGestureView.setOnGestureListener(new GestureView.GestureListener() {

            @Override
            public void onHorizontalDistance(float downX, float nowX) {
                //水平滑动调节seek。
                // seek需要在手势结束时操作。
                long duration = mAliyunRenderView.getDuration();
                long position = mCurrentPosition;
                long deltaPosition = 0;
                int targetPosition = 0;

                if (mPlayerState == IPlayer.prepared ||
                        mPlayerState == IPlayer.paused ||
                        mPlayerState == IPlayer.started) {
                    //在播放时才能调整大小
                    deltaPosition = (long) (nowX - downX) * duration / getWidth();
                    targetPosition = getTargetPosition(duration, position, deltaPosition);

                }
                if (mControlView != null) {
                    inSeek = true;
                    mControlView.setVideoPosition(targetPosition);
                    //关闭状态栏自动隐藏的功能
                    mControlView.closeAutoHide();
                }
            }

            @Override
            public void onLeftVerticalDistance(float downY, float nowY) {
                //左侧上下滑动调节亮度
                int changePercent = (int) ((nowY - downY) * 100 / getHeight());

                if (mGestureDialogManager != null) {
                    mGestureDialogManager.showBrightnessDialog(AliyunVodPlayer.this, mScreenBrightness);
                    int brightness = mGestureDialogManager.updateBrightnessDialog(changePercent);
                    if (mOnScreenBrightnessListener != null) {
                        mOnScreenBrightnessListener.onScreenBrightness(brightness);
                    }
                    mScreenBrightness = brightness;
                }
            }

            @Override
            public void onRightVerticalDistance(float downY, float nowY) {
                //右侧上下滑动调节音量
                float volume = mAliyunRenderView.getVolume();
                int changePercent = (int) ((nowY - downY) * 100 / getHeight());
                if (mGestureDialogManager != null) {
                    mGestureDialogManager.showVolumeDialog(AliyunVodPlayer.this, volume * 100);
                    float targetVolume = mGestureDialogManager.updateVolumeDialog(changePercent);
                    currentVolume = targetVolume;
                    //不管是否投屏状态,都设置音量改变给播放器,用于保存当前的音量
                    mAliyunRenderView.setVolume((targetVolume / 100.00f));//通过返回值改变音量
                }
            }

            @Override
            public void onGestureEnd() {
                //手势结束。
                //seek需要在结束时操作。
                if (mGestureDialogManager != null) {
                    int seekPosition = mControlView.getVideoPosition();
                    if (seekPosition >= mAliyunRenderView.getDuration()) {
                        seekPosition = (int) (mAliyunRenderView.getDuration() - 1000);
                    }
                    if (seekPosition <= 0) {
                        seekPosition = 0;
                    }
                    if (inSeek) {
                        seekTo(seekPosition);
                        inSeek = false;
                    }
                    if(mControlView != null){
                        //开启状态栏自动隐藏的功能
                        mControlView.openAutoHide();
                    }

                    mGestureDialogManager.dismissBrightnessDialog();
                    mGestureDialogManager.dismissVolumeDialog();
                }

            }

            @Override
            public void onSingleTap() {
                //单击事件，显示控制栏
                if (mControlView != null) {
                    //播放广告状态下的单击事件
                    if (mControlView.getVisibility() != VISIBLE) {
                        mControlView.show();
                    } else {
                        mControlView.hide(ControlView.HideType.Normal);
                    }
                }
            }

            @Override
            public void onDoubleTap() {
                //双击事件，控制暂停播放
                switchPlayerState();
            }
        });
    }

    /**
     * 切换播放状态。点播播放按钮之后的操作
     */
    private void switchPlayerState() {
        //非投屏状态下的处理
        if (mPlayerState == IPlayer.started) {
            pause();
        } else if (mPlayerState == IPlayer.paused || mPlayerState == IPlayer.prepared || mPlayerState == IPlayer.stopped) {
            start();
        }

        if (onPlayStateBtnClickListener != null) {
            onPlayStateBtnClickListener.onPlayBtnClick(mPlayerState);
        }
    }

    /**
     * 目标位置计算算法
     *
     * @param duration        视频总时长
     * @param currentPosition 当前播放位置
     * @param deltaPosition   与当前位置相差的时长
     * @return
     */
    public int getTargetPosition(long duration, long currentPosition, long deltaPosition) {
        // seek步长
        long finalDeltaPosition;
        // 根据视频时长，决定seek步长
        long totalMinutes = duration / 1000 / 60;
        int hours = (int) (totalMinutes / 60);
        int minutes = (int) (totalMinutes % 60);

        // 视频时长为1小时以上，小屏和全屏的手势滑动最长为视频时长的十分之一
        if (hours >= 1) {
            finalDeltaPosition = deltaPosition / 10;
        }// 视频时长为31分钟－60分钟时，小屏和全屏的手势滑动最长为视频时长五分之一
        else if (minutes > 30) {
            finalDeltaPosition = deltaPosition / 5;
        }// 视频时长为11分钟－30分钟时，小屏和全屏的手势滑动最长为视频时长三分之一
        else if (minutes > 10) {
            finalDeltaPosition = deltaPosition / 3;
        }// 视频时长为4-10分钟时，小屏和全屏的手势滑动最长为视频时长二分之一
        else if (minutes > 3) {
            finalDeltaPosition = deltaPosition / 2;
        }// 视频时长为1秒钟至3分钟时，小屏和全屏的手势滑动最长为视频结束
        else {
            finalDeltaPosition = deltaPosition;
        }

        long targetPosition = finalDeltaPosition + currentPosition;
        if (targetPosition < 0) {
            targetPosition = 0;
        }
        if (targetPosition > duration) {
            targetPosition = duration;
        }
        return (int) targetPosition;
    }

    /**
     * addSubView 添加子view到布局中
     *
     * @param view 子view
     */
    private void addSubView(View view) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //添加到布局中
        addView(view, params);
    }

    /**
     * 初始化播放器
     */
    private void initAliVcPlayer() {
        mAliyunRenderView = new AliyunRenderView(getContext());
        addSubView(mAliyunRenderView);
        mAliyunRenderView.setSurfaceType(AliyunRenderView.SurfaceType.SURFACE_VIEW);
        //设置准备回调
        mAliyunRenderView.setOnPreparedListener(new VideoPlayerPreparedListener(this, false));
        //播放器出错监听
        mAliyunRenderView.setOnErrorListener(new VideoPlayerErrorListener(this, false));
        //播放器加载回调
        mAliyunRenderView.setOnLoadingStatusListener(new VideoPlayerLoadingStatusListener(this, false));
        //播放器状态
        mAliyunRenderView.setOnStateChangedListener(new VideoPlayerStateChangedListener(this, false));
        //播放结束
        mAliyunRenderView.setOnCompletionListener(new VideoPlayerCompletionListener(this, false));
        //播放信息监听
        mAliyunRenderView.setOnInfoListener(new VideoPlayerInfoListener(this, false));
        //第一帧显示
        mAliyunRenderView.setOnRenderingStartListener(new VideoPlayerRenderingStartListener(this, false));
        //trackChange监听
        mAliyunRenderView.setOnTrackChangedListener(new VideoPlayerTrackChangedListener(this));
        //seek结束事件
        mAliyunRenderView.setOnSeekCompleteListener(new VideoPlayerOnSeekCompleteListener(this));
    }

    /**
     * 初始化提示view
     */
    private void initTipsView() {
        mTipsView = new TipsView(getContext());
        //设置tip中的点击监听事件
        mTipsView.setOnTipClickListener(new TipsView.OnTipClickListener() {
            @Override
            public void onContinuePlay() {
                mIsOperatorPlay = true;
                //继续播放。如果没有prepare或者stop了，需要重新prepare
                mTipsView.hideAll();
                if (GlobalPlayerConfig.IS_VIDEO) {
                    if (mAliyunRenderView != null) {
                        mAliyunRenderView.start();
                    }
                    if (mControlView != null) {
                        mControlView.setHideType(ViewAction.HideType.Normal);
                    }
                    if (mGestureView != null) {
                        mGestureView.setVisibility(VISIBLE);
                        mGestureView.setHideType(ViewAction.HideType.Normal);
                    }
                } else {
                    if (mPlayerState == IPlayer.idle
                            || mPlayerState == IPlayer.stopped
                            || mPlayerState == IPlayer.error
                            || mPlayerState == IPlayer.completion)  {
                        mAliyunRenderView.setAutoPlay(true);
                        if (mAliyunLocalSource != null) {
                            prepareLocalSource(mAliyunLocalSource);
                        }
                    }else{
                        start();
                    }
                }
            }

            @Override
            public void onStopPlay() {
                // 结束播放
                mTipsView.hideAll();
                stop();

                Context context = getContext();
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onRetryPlay(int errorCode) {
                if(mOutOnTipClickListener != null){
                    mOutOnTipClickListener.onRetryPlay(errorCode);
                }
            }

            @Override
            public void onReplay() {
                //重播
                rePlay();
            }

            @Override
            public void onRefreshSts() {
                if (mOutTimeExpiredErrorListener != null) {
                    mOutTimeExpiredErrorListener.onTimeExpiredError();
                }
            }

            @Override
            public void onWait() {
                mTipsView.hideAll();
                mTipsView.showNetLoadingTipView();
            }

            @Override
            public void onExit() {
                mTipsView.hideAll();
                if (mOutOnTipClickListener != null) {
                    mOutOnTipClickListener.onExit();
                }
            }
        });

        mTipsView.setOnTipsViewBackClickListener(new OnTipsViewBackClickListener() {
            @Override
            public void onBackClick() {
                if(mOutOnTipsViewBackClickListener != null){
                    mOutOnTipsViewBackClickListener.onBackClick();
                }
            }
        });
        addSubView(mTipsView);
    }

    /**
     * prepare本地播放源
     *
     * @param aliyunLocalSource 本地播放源
     */
    private void prepareLocalSource(UrlSource aliyunLocalSource) {
        if (mTipsView != null) {
            mTipsView.showNetLoadingTipView();
        }
        if (mControlView != null) {
            mControlView.setForceQuality(true);
            mControlView.setIsMtsSource(false);
            mControlView.hideMoreButton();
        }

        if(aliyunLocalSource != null && aliyunLocalSource.getUri().startsWith("artc")){
            PlayerConfig playerConfig = mAliyunRenderView.getPlayerConfig();
            playerConfig.mMaxDelayTime = 1000;
            playerConfig.mHighBufferDuration = 10;
            playerConfig.mStartBufferDuration = 10;
            mAliyunRenderView.setPlayerConfig(playerConfig);
        }

        mAliyunRenderView.setAutoPlay(true);
        mAliyunRenderView.setDataSource(aliyunLocalSource);
        mAliyunRenderView.prepare();
    }

    /**
     * 重播，将会从头开始播放
     */
    public void rePlay() {

        isCompleted = false;
        inSeek = false;

        if (mTipsView != null) {
            mTipsView.hideAll();
        }
        if (mControlView != null) {
            mControlView.reset();
        }
        if (mGestureView != null) {
            mGestureView.reset();
        }

        if (mAliyunRenderView != null) {
            //显示网络加载的loading。。
            if (mTipsView != null) {
                mTipsView.showNetLoadingTipView();
            }
            //重播是从头开始播
            mAliyunRenderView.prepare();
        }
    }

    /**
     * 开启网络监听
     */
    public void startNetWatch(){
        initNetWatch = true;
        if (mNetWatchdog != null) {
            mNetWatchdog.startWatch();
        }
    }

    /**
     * 关闭网络监听
     */
    public void stopNetWatch(){
        if (mNetWatchdog != null) {
            mNetWatchdog.stopWatch();
        }
    }

    /**
     * 开始播放
     */
    public void start() {
        if (mControlView != null) {
            mControlView.setPlayState(ControlView.PlayState.Playing);
        }

        if (mAliyunRenderView == null) {
            return;
        }

        mGestureView.show();
        mControlView.show();
        if(mSourceDuration <= 0 && mPlayerState == IPlayer.stopped) {
            //直播
            mAliyunRenderView.prepare();
        }else{
            mAliyunRenderView.start();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mControlView != null) {
            mControlView.setPlayState(ControlView.PlayState.NotPlaying);
        }
        if (mAliyunRenderView == null) {
            return;
        }
        if (mPlayerState == IPlayer.started || mPlayerState == IPlayer.prepared) {
            if(mSourceDuration <= 0) {
                //直播调用stop
                mPlayerState = IPlayer.stopped;
                mAliyunRenderView.stop();
            }else{
                mAliyunRenderView.pause();
            }
        }
    }

    /**
     * 停止播放
     */
    private void stop() {
        Boolean hasLoadedEnd = null;
        MediaInfo mediaInfo = null;
        if (mAliyunRenderView != null && hasLoadEnd != null) {
            mediaInfo = mAliyunRenderView.getMediaInfo();
            hasLoadedEnd = hasLoadEnd.get(mediaInfo);
        }

        if (mAliyunRenderView != null && hasLoadedEnd != null) {
            mPlayerState = IPlayer.stopped;
            mAliyunRenderView.stop();
        }
        if (mControlView != null) {
            mControlView.setPlayState(ControlView.PlayState.NotPlaying);
        }
        if (hasLoadEnd != null) {
            hasLoadEnd.remove(mediaInfo);
        }
    }

    /**
     * 在activity调用onResume的时候调用。 解决home回来后，画面方向不对的问题
     */
    public void onResume() {
        mInBackground = false;
        if (mIsFullScreenLocked) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                changeScreenMode(AliyunScreenMode.Small, false);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                changeScreenMode(AliyunScreenMode.Full, false);
            }
        }

        if (mOrientationWatchDog != null) {
            mOrientationWatchDog.startWatch();
        }

        //从其他界面过来的话，也要show。
//        if (mControlView != null) {
//            mControlView.show();
//        }


        //onStop中记录下来的状态，在这里恢复使用
        //不是投屏中再回复播放
        resumePlayerState();
    }

    //判断当前是否在后台
    private boolean mInBackground = false;
    /**
     * 暂停播放器的操作
     */
    public void onStop() {
        mInBackground = true;
        if (mOrientationWatchDog != null) {
            mOrientationWatchDog.stopWatch();
        }
        //保存播放器的状态，供resume恢复使用。
        savePlayerState();
    }

    /**
     * 保存当前的状态，供恢复使用
     */
    private void savePlayerState() {
        if (mAliyunRenderView == null) {
            return;
        }
        if(mSourceDuration <= 0){
            //直播调用stop
            mPlayerState = IPlayer.stopped;
            mAliyunRenderView.stop();
        }else{
            pause();
        }
    }

    /**
     * 活动销毁，释放
     */
    public void onDestroy() {
        stop();
        if (mAliyunRenderView != null) {
            mAliyunRenderView.release();
            mAliyunRenderView = null;
        }
        mSurfaceView = null;
        mGestureView = null;
        mControlView = null;
        mGestureDialogManager = null;
        if (mNetWatchdog != null) {
            mNetWatchdog.stopWatch();
        }
        mNetWatchdog = null;
        mTipsView = null;
        mAliyunMediaInfo = null;
        if (mOrientationWatchDog != null) {
            mOrientationWatchDog.destroy();
        }
        mOrientationWatchDog = null;
        if (hasLoadEnd != null) {
            hasLoadEnd.clear();
        }
        stopNetWatch();
    }

    /**
     * 开启屏幕旋转监听
     */
    public void startOrientationWatchDog() {
        if (mOrientationWatchDog != null) {
            mOrientationWatchDog.startWatch();
        }
    }

    /**
     * 关闭屏幕旋转监听
     */
    public void stopOrientationWatchDog() {
        if (mOrientationWatchDog != null) {
            mOrientationWatchDog.stopWatch();
        }
    }

    /**
     * 判断是否是本地资源
     *
     * @return
     */
    private boolean isLocalSource() {
        String scheme = null;
        if (GlobalPlayerConfig.PLAYTYPE.STS.equals(GlobalPlayerConfig.mCurrentPlayType)
                || GlobalPlayerConfig.PLAYTYPE.MPS.equals(GlobalPlayerConfig.mCurrentPlayType)
                || GlobalPlayerConfig.PLAYTYPE.AUTH.equals(GlobalPlayerConfig.mCurrentPlayType)
                || GlobalPlayerConfig.PLAYTYPE.DEFAULT.equals(GlobalPlayerConfig.mCurrentPlayType)) {
            return false;
        }
        if(mAliyunLocalSource == null || TextUtils.isEmpty(mAliyunLocalSource.getUri())){
            return false;
        }
        if (GlobalPlayerConfig.PLAYTYPE.URL.equals(GlobalPlayerConfig.mCurrentPlayType)) {
            Uri parse = Uri.parse(mAliyunLocalSource.getUri());
            scheme = parse.getScheme();
        }
        return scheme == null;
    }

    /**
     * Activity回来后，恢复之前的状态
     */
    private void resumePlayerState() {
        if (mAliyunRenderView == null) {
            return;
        }

        //恢复前台后需要继续播放,包括本地资源也要继续播放
        if(!isLocalSource() && NetWatchdog.is4GConnected(getContext()) && !mIsOperatorPlay && isPlaying()){
            pause();
        }else{
            if(mSourceDuration <= 0 && mPlayerState == IPlayer.stopped){
                //直播
                mAliyunRenderView.prepare();
            }else{
                start();
            }
        }
    }

    /**
     * 原视频准备完成
     */
    private void sourceVideoPlayerPrepared() {
        //需要将mThumbnailPrepareSuccess重置,否则会出现缩略图错乱的问题
        if (mAliyunRenderView == null) {
            return;
        }
        mAliyunMediaInfo = mAliyunRenderView.getMediaInfo();
        if (mAliyunMediaInfo == null) {
            return;
        }

        //防止服务器信息和实际不一致
        mSourceDuration = mAliyunRenderView.getDuration();
        mAliyunMediaInfo.setDuration((int) mSourceDuration);
        //直播流
        if(mSourceDuration <= 0){
            TrackInfo trackVideo = mAliyunRenderView.currentTrack(TrackInfo.Type.TYPE_VIDEO);
            TrackInfo trackAudio = mAliyunRenderView.currentTrack(TrackInfo.Type.TYPE_AUDIO);
            if(trackVideo == null && trackAudio != null){
                Toast.makeText(getContext(), getContext().getString(R.string.alivc_player_audio_stream), Toast.LENGTH_SHORT).show();
            }else if(trackVideo != null && trackAudio == null){
                Toast.makeText(getContext(), getContext().getString(R.string.alivc_player_video_stream), Toast.LENGTH_SHORT).show();
            }
        }

        //使用用户设置的标题
        if (!GlobalPlayerConfig.IS_VIDEO) {
            TrackInfo trackInfo = mAliyunRenderView.currentTrack(TrackInfo.Type.TYPE_VOD.ordinal());
            if (trackInfo != null) {
                mControlView.setMediaInfo(mAliyunMediaInfo, mAliyunRenderView.currentTrack(TrackInfo.Type.TYPE_VOD.ordinal()).getVodDefinition());
            } else {
                mControlView.setMediaInfo(mAliyunMediaInfo, "FD");
            }

            mControlView.setScreenModeStatus(mCurrentScreenMode);
            mControlView.show();
            mGestureView.show();
        }

        mControlView.setHideType(ViewAction.HideType.Normal);
        mGestureView.setHideType(ViewAction.HideType.Normal);
        if (mTipsView != null) {
            mTipsView.hideNetLoadingTipView();
            mTipsView.hideBufferLoadingTipView();
        }
        //如果是视频广告，那么走这里并不会显示试看view,试看功能放在视频广告或者图片广告结束之后
        if (GlobalPlayerConfig.IS_VIDEO) {

            Message msg = Message.obtain();
            msg.what = SOURCE_VIDEO_PREPARED;
            msg.obj = mAliyunMediaInfo;
            mVodPlayerHandler.sendMessage(msg);

            return;
        }else {
            if (mSurfaceView != null) {
                mSurfaceView.setVisibility(View.VISIBLE);
            }
        }
        //准备成功之后可以调用start方法开始播放
        if (mOutPreparedListener != null) {
            mOutPreparedListener.onPrepared();
        }
    }

    private static class VideoPlayerErrorListener implements IPlayer.OnErrorListener {

        private WeakReference<AliyunVodPlayer> weakReference;
        private boolean isAdvPlayer;

        public VideoPlayerErrorListener(AliyunVodPlayer aliyunVodPlayerView, boolean isAdvPlayer) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
            this.isAdvPlayer = isAdvPlayer;
        }

        @Override
        public void onError(ErrorInfo errorInfo) {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerError(errorInfo);
            }
        }
    }

    /**
     * 锁定屏幕。锁定屏幕后，只有锁会显示，其他都不会显示。手势也不可用
     *
     * @param lockScreen 是否锁住
     */
    public void lockScreen(boolean lockScreen) {
        mIsFullScreenLocked = lockScreen;
        if (mControlView != null) {
            mControlView.setScreenLockStatus(mIsFullScreenLocked);
        }
        if (mGestureView != null) {
            mGestureView.setScreenLockStatus(mIsFullScreenLocked);
        }
    }
    /**
     * 显示错误提示
     *
     * @param errorCode  错误码
     * @param errorEvent 错误事件
     * @param errorMsg   错误描述
     */
    public void showErrorTipView(int errorCode, String errorEvent, String errorMsg) {
        stop();

        if (mControlView != null) {
            mControlView.setPlayState(ControlView.PlayState.NotPlaying);
        }

        if (mTipsView != null) {
            //隐藏其他的动作,防止点击界面去进行其他操作
            mGestureView.hide(ViewAction.HideType.End);
            mControlView.hide(ViewAction.HideType.End);
            mTipsView.showErrorTipView(errorCode, errorEvent, errorMsg);
        }
    }

    /**
     * 原视频错误监听
     */
    private void sourceVideoPlayerError(ErrorInfo errorInfo) {
        if (mTipsView != null) {
            mTipsView.hideAll();
        }
        //出错之后解锁屏幕，防止不能做其他操作，比如返回。
        lockScreen(false);

        //errorInfo.getExtra()展示为null,修改为显示errorInfo.getCode的十六进制的值
        showErrorTipView(errorInfo.getCode().getValue(), Integer.toHexString(errorInfo.getCode().getValue()), errorInfo.getMsg());


        if (mOutErrorListener != null) {
            mOutErrorListener.onError(errorInfo);
        }
    }

    /**
     * 播放器加载状态监听
     */
    private static class VideoPlayerLoadingStatusListener implements IPlayer.OnLoadingStatusListener {

        private WeakReference<AliyunVodPlayer> weakReference;
        private boolean isAdvPlayer;

        public VideoPlayerLoadingStatusListener(AliyunVodPlayer aliyunVodPlayerView, boolean isAdvPlayer) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
            this.isAdvPlayer = isAdvPlayer;
        }

        @Override
        public void onLoadingBegin() {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerLoadingBegin();
            }
        }

        @Override
        public void onLoadingProgress(int percent, float v) {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerLoadingProgress(percent);
            }
        }

        @Override
        public void onLoadingEnd() {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerLoadingEnd();
            }
        }
    }

    /**
     * 原视频开始加载
     */
    private void sourceVideoPlayerLoadingBegin() {

        if (mTipsView != null) {
            //视频广告,并且广告视频在播放状态,不要展示loading
            if (GlobalPlayerConfig.IS_VIDEO) {

            } else {
                mTipsView.hideNetLoadingTipView();
                mTipsView.showBufferLoadingTipView();
            }
        }
    }

    /**
     * 原视频开始加载进度
     */
    private void sourceVideoPlayerLoadingProgress(int percent) {

        if (mTipsView != null) {
            //视频广告,并且广告视频在播放状态,不要展示loading
            if (GlobalPlayerConfig.IS_VIDEO) {

            } else {
                mTipsView.updateLoadingPercent(percent);
            }
            if (percent == 100) {
                mTipsView.hideBufferLoadingTipView();
            }
        }
    }

    //当前播放器的状态
    private int mPlayerState = IPlayer.idle;
    /**
     * 是否处于播放状态：start或者pause了
     *
     * @return 是否处于播放状态
     */
    public boolean isPlaying() {
        return mPlayerState == IPlayer.started;
    }

    /**
     * 原视频加载结束
     */
    private void sourceVideoPlayerLoadingEnd() {

        if (mTipsView != null) {
            if (isPlaying()) {
                mTipsView.hideErrorTipView();
            }
            mTipsView.hideBufferLoadingTipView();
        }
        if(mControlView != null){
            mControlView.setHideType(ViewAction.HideType.Normal);
        }
        if(mGestureView != null){
            mGestureView.setHideType(ViewAction.HideType.Normal);
            mGestureView.show();
        }
        hasLoadEnd.put(mAliyunMediaInfo, true);
        vodPlayerLoadEndHandler.sendEmptyMessage(1);
    }

    /**
     * 广告视频播放器准备对外接口监听
     */
    public static class VideoPlayerPreparedListener implements IPlayer.OnPreparedListener {

        private WeakReference<AliyunVodPlayer> weakReference;
        private boolean isAdvPlayer;

        public VideoPlayerPreparedListener(AliyunVodPlayer aliyunVodPlayerView, boolean isAdvPlayer) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
            this.isAdvPlayer = isAdvPlayer;
        }

        @Override
        public void onPrepared() {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                //原视频的播放器
                aliyunVodPlayerView.sourceVideoPlayerPrepared();
            }
        }
    }


    /**
     * 原视频状态改变监听
     */
    private void sourceVideoPlayerStateChanged(int newState) {
        mPlayerState = newState;
        if (newState == IPlayer.stopped) {
            if (mOnStoppedListener != null) {
                mOnStoppedListener.onStop();
            }
        } else if (newState == IPlayer.started) {
            if (mControlView != null) {
                mControlView.setPlayState(ControlView.PlayState.Playing);
            }
        }
    }

    /**
     * 播放器状态改变监听
     */
    private static class VideoPlayerStateChangedListener implements IPlayer.OnStateChangedListener {

        private WeakReference<AliyunVodPlayer> weakReference;
        private boolean isAdvPlayer;

        public VideoPlayerStateChangedListener(AliyunVodPlayer aliyunVodPlayerView, boolean isAdvPlayer) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
            this.isAdvPlayer = isAdvPlayer;
        }


        @Override
        public void onStateChanged(int newState) {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerStateChanged(newState);
            }
        }
    }

    /**
     * 原视频Info
     */
    private void sourceVideoPlayerInfo(InfoBean infoBean) {
        if (infoBean.getCode() == InfoCode.AutoPlayStart) {
            //自动播放开始,需要设置播放状态
            if (mControlView != null) {
                mControlView.setPlayState(ControlView.PlayState.Playing);
            }
            if (mOutAutoPlayListener != null) {
                mOutAutoPlayListener.onAutoPlayStarted();
            }
        } else if (infoBean.getCode() == InfoCode.BufferedPosition) {
            //更新bufferedPosition
            mVideoBufferedPosition = infoBean.getExtraValue();
            mControlView.setVideoBufferPosition((int) mVideoBufferedPosition);
        } else if (infoBean.getCode() == InfoCode.CurrentPosition) {
            //更新currentPosition
            mCurrentPosition = infoBean.getExtraValue();
//            if (mDanmakuView != null) {
//                mDanmakuView.setCurrentPosition((int) mCurrentPosition);
//            }
            if (mControlView != null) {
                //如果是试看视频,并且试看已经结束了,要屏蔽其他按钮的操作
                mControlView.setOtherEnable(true);
            }
            if (GlobalPlayerConfig.IS_VIDEO) {
                if (mControlView != null && !inSeek && mPlayerState == IPlayer.started) {
                    /*
                        由于关键帧的问题,seek到sourceDuration / 2时间点会导致进度条和广告时间对应不上,导致在播放原视频的时候进度条还在广告进度条范围内
                     */
                    mControlView.setAdvVideoPosition((int) (mCurrentPosition), (int) mCurrentPosition);
                }
            } else {
                if (mControlView != null && !inSeek && mPlayerState == IPlayer.started) {
                    mControlView.setVideoPosition((int) mCurrentPosition);
                }
            }
        }
        if (mOutInfoListener != null) {
            mOutInfoListener.onInfo(infoBean);
        }
    }

    /**
     * 原视频播放完成
     */
    private void sourceVideoPlayerCompletion() {
        inSeek = false;
        if(showMoreDialog != null){
            showMoreDialog.dismiss();
        }
        if (mOutCompletionListener != null) {
            mOutCompletionListener.onCompletion();
        }
    }

    /**
     * 原视频onVideoRenderingStart
     */
    private void sourceVideoPlayerOnVideoRenderingStart() {
        if (mOutFirstFrameStartListener != null) {
            mOutFirstFrameStartListener.onRenderingStart();
        }
    }

    /**
     * 播放器Render监听
     */
    private static class VideoPlayerRenderingStartListener implements IPlayer.OnRenderingStartListener {

        private WeakReference<AliyunVodPlayer> weakReference;
        private boolean isAdvPlayer;

        public VideoPlayerRenderingStartListener(AliyunVodPlayer aliyunVodPlayerView, boolean isAdvPlayer) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
            this.isAdvPlayer = isAdvPlayer;
        }

        @Override
        public void onRenderingStart() {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerOnVideoRenderingStart();
            }
        }
    }

    /**
     * 播放器播放完成监听
     */
    private static class VideoPlayerCompletionListener implements IPlayer.OnCompletionListener {

        private WeakReference<AliyunVodPlayer> weakReference;
        private boolean isAdvPlayer;

        public VideoPlayerCompletionListener(AliyunVodPlayer aliyunVodPlayerView, boolean isAdvPlayer) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
            this.isAdvPlayer = isAdvPlayer;
        }

        @Override
        public void onCompletion() {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerCompletion();
            }
        }
    }
    /**
     * 播放器Info监听
     */
    private static class VideoPlayerInfoListener implements IPlayer.OnInfoListener {

        private WeakReference<AliyunVodPlayer> weakReference;
        private boolean isAdvPlayer;

        public VideoPlayerInfoListener(AliyunVodPlayer aliyunVodPlayerView, boolean isAdvPlayer) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
            this.isAdvPlayer = isAdvPlayer;
        }

        @Override
        public void onInfo(InfoBean infoBean) {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerInfo(infoBean);
            }
        }
    }

    /**
     * 播放器TrackChanged监听
     */
    private static class VideoPlayerTrackChangedListener implements IPlayer.OnTrackChangedListener {

        private WeakReference<AliyunVodPlayer> weakReference;

        public VideoPlayerTrackChangedListener(AliyunVodPlayer aliyunVodPlayerView) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
        }

        @Override
        public void onChangedSuccess(TrackInfo trackInfo) {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerTrackInfoChangedSuccess(trackInfo);
            }
        }

        @Override
        public void onChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerTrackInfoChangedFail(trackInfo, errorInfo);
            }
        }
    }

    /**
     * 原视频 trackInfoChangedSuccess
     */
    private void sourceVideoPlayerTrackInfoChangedSuccess(TrackInfo trackInfo) {
        //清晰度切换监听
        if (trackInfo.getType() == TrackInfo.Type.TYPE_VOD) {
            //切换成功后就开始播放
            mControlView.setCurrentQuality(trackInfo.getVodDefinition());
            start();

            if (mTipsView != null) {
                mTipsView.hideNetLoadingTipView();
            }
        }
        if(mOutOnTrackChangedListener != null){
            mOutOnTrackChangedListener.onChangedSuccess(trackInfo);
        }
    }

    /**
     * 原视频 trackInfochangedFail
     */
    private void sourceVideoPlayerTrackInfoChangedFail(TrackInfo trackInfo, ErrorInfo errorInfo) {
        //失败的话，停止播放，通知上层
        if (mTipsView != null) {
            mTipsView.hideNetLoadingTipView();
        }
        stop();
        if(mOutOnTrackChangedListener != null){
            mOutOnTrackChangedListener.onChangedFail(trackInfo,errorInfo);
        }
    }

    /**
     * 播放器seek完成监听
     */
    private static class VideoPlayerOnSeekCompleteListener implements IPlayer.OnSeekCompleteListener {

        private WeakReference<AliyunVodPlayer> weakReference;

        public VideoPlayerOnSeekCompleteListener(AliyunVodPlayer aliyunVodPlayerView) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
        }

        @Override
        public void onSeekComplete() {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.sourceVideoPlayerSeekComplete();
            }
        }
    }

    /**
     * 原视频seek完成
     */
    private void sourceVideoPlayerSeekComplete() {
        inSeek = false;

        if (mOuterSeekCompleteListener != null) {
            mOuterSeekCompleteListener.onSeekComplete();
        }
    }
    /**
     * 播放器截图事件监听
     */
    private static class VideoPlayerOnSnapShotListener implements IPlayer.OnSnapShotListener{

        private WeakReference<AliyunVodPlayer> weakReference;

        public VideoPlayerOnSnapShotListener(AliyunVodPlayer aliyunVodPlayerView){
            weakReference = new WeakReference<>(aliyunVodPlayerView);
        }

        @Override
        public void onSnapShot(Bitmap bitmap, int width, int height) {
            AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
            if(aliyunVodPlayerView != null){
                aliyunVodPlayerView.sourceVideoSnapShot(bitmap,width,height);
            }
        }
    }

    /**
     * 原视频截图完成
     */
    private void sourceVideoSnapShot(final Bitmap bitmap, int width, int height){
        ThreadUtils.runOnSubThread(new Runnable() {

            @Override
            public void run() {
                String videoPath = FileUtils.getDir(getContext()) + GlobalPlayerConfig.SNAP_SHOT_PATH;
                String bitmapPath = FileUtils.saveBitmap(bitmap,videoPath);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    FileUtils.saveImgToMediaStore(getContext().getApplicationContext(),bitmapPath,"image/png");
                }else{
                    MediaScannerConnection.scanFile(getContext().getApplicationContext(),
                            new String[] {bitmapPath},
                            new String[] {"image/png"}, null);
                }
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(getContext(), R.string.alivc_player_snap_shot_save_success);
                    }
                });
            }
        });
    }
    /**
     * 原视频preapred完成
     */
    private static final int SOURCE_VIDEO_PREPARED = 1;

    /**
     * Handler
     */
    private static class VodPlayerHandler extends Handler {

        private WeakReference<AliyunVodPlayer> weakReference;

        public VodPlayerHandler(AliyunVodPlayer aliyunVodPlayerView) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SOURCE_VIDEO_PREPARED:

                    AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
                    if (aliyunVodPlayerView == null) {
                        return;
                    }
                    if (msg.what == SOURCE_VIDEO_PREPARED) {
                        aliyunVodPlayerView.mSourceVideoMediaInfo = (MediaInfo) msg.obj;
                    }

                    //视频广告和原视频都准备完成
                    if (aliyunVodPlayerView.mSourceVideoMediaInfo != null) {
                        MediaInfo mediaInfo = new MediaInfo();
                        //重新创建一个新的MediaInfo,并且重新计算duration
                        mediaInfo.setDuration(aliyunVodPlayerView.mSourceVideoMediaInfo.getDuration());

                        if (aliyunVodPlayerView.mAliyunRenderView != null) {
                            TrackInfo trackInfo = aliyunVodPlayerView.mAliyunRenderView.currentTrack(TrackInfo.Type.TYPE_VOD.ordinal());
                            if (trackInfo != null) {
                                aliyunVodPlayerView.mControlView.setMediaInfo(aliyunVodPlayerView.mSourceVideoMediaInfo,
                                        trackInfo.getVodDefinition());
                            }
                        }

                        aliyunVodPlayerView.mControlView.setHideType(ViewAction.HideType.Normal);
                        aliyunVodPlayerView.mGestureView.setHideType(ViewAction.HideType.Normal);
                        aliyunVodPlayerView.mControlView.setPlayState(ControlView.PlayState.Playing);
                        //如果是投屏状态,则不显示视频广告的seekBar
                        aliyunVodPlayerView.mControlView.setMutiSeekBarInfo(0,
                                aliyunVodPlayerView.mSourceVideoMediaInfo.getDuration(), MutiSeekBarView.AdvPosition.ONLY_START);
                        aliyunVodPlayerView.mGestureView.show();


                        if (aliyunVodPlayerView.mTipsView != null) {
                            aliyunVodPlayerView.mTipsView.hideNetLoadingTipView();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 当VodPlayer 没有加载完成的时候,调用onStop 去暂停视频,
     * 会出现暂停失败的问题。
     */
    private static class VodPlayerLoadEndHandler extends Handler {

        private WeakReference<AliyunVodPlayer> weakReference;

        private boolean intentPause;

        public VodPlayerLoadEndHandler(AliyunVodPlayer aliyunVodPlayerView) {
            weakReference = new WeakReference<>(aliyunVodPlayerView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                intentPause = true;
            }
            if (msg.what == 1) {
                AliyunVodPlayer aliyunVodPlayerView = weakReference.get();
                if (aliyunVodPlayerView != null && intentPause) {
                    aliyunVodPlayerView.onStop();
                    intentPause = false;
                }
            }
        }
    }

    private void onWifiTo4G() {
        //如果已经显示错误了，那么就不用显示网络变化的提示了。
        if (mTipsView.isErrorShow()) {
            return;
        }

        //wifi变成4G，如果不是本地视频先暂停播放
        if (!isLocalSource()) {
            if (mIsOperatorPlay) {
                ToastUtils.show(getContext(), R.string.alivc_operator_play);
            } else {
                pause();
            }
        }

        if(!initNetWatch){
            reload();
        }

        //显示网络变化的提示
        if (!isLocalSource() && mTipsView != null) {
            if (mIsOperatorPlay) {
                ToastUtils.show(getContext(), R.string.alivc_operator_play);
            } else {
                mTipsView.hideAll();
                mTipsView.showNetChangeTipView();
                //隐藏其他的动作,防止点击界面去进行其他操作
                if (mControlView != null) {
                    mControlView.setHideType(ViewAction.HideType.Normal);
                    mControlView.hide(ControlView.HideType.Normal);
                }
                if (mGestureView != null) {
                    mGestureView.setHideType(ViewAction.HideType.Normal);
                    mGestureView.hide(ControlView.HideType.Normal);
                }
            }
        }
        initNetWatch = false;
    }

    private void onNetDisconnected() {
        //网络断开。
        // NOTE： 由于安卓这块网络切换的时候，有时候也会先报断开。所以这个回调是不准确的。
    }

    private static class MyNetChangeListener implements NetWatchdog.NetChangeListener {

        private WeakReference<AliyunVodPlayer> viewWeakReference;

        public MyNetChangeListener(AliyunVodPlayer aliyunVodPlayerView) {
            viewWeakReference = new WeakReference<>(aliyunVodPlayerView);
        }

        @Override
        public void onWifiTo4G() {
            AliyunVodPlayer aliyunVodPlayerView = viewWeakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.onWifiTo4G();
            }
        }

        @Override
        public void on4GToWifi() {
            AliyunVodPlayer aliyunVodPlayerView = viewWeakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.on4GToWifi();
            }
        }

        @Override
        public void onNetDisconnected() {
            AliyunVodPlayer aliyunVodPlayerView = viewWeakReference.get();
            if (aliyunVodPlayerView != null) {
                aliyunVodPlayerView.onNetDisconnected();
            }
        }
    }

    private void on4GToWifi() {
        //如果已经显示错误了，那么就不用显示网络变化的提示了。
        if (mTipsView.isErrorShow()) {
            return;
        }

        //隐藏网络变化的提示
        if (mTipsView != null) {
            mTipsView.hideNetErrorTipView();
        }
        if(!initNetWatch){
            reload();
        }
        initNetWatch = false;
    }

    /**
     * reload
     */
    public void reload(){
        if(mAliyunRenderView != null){
            mAliyunRenderView.reload();
        }
    }

    /**
     * 断网/连网监听
     */
    private class MyNetConnectedListener implements NetWatchdog.NetConnectedListener {
        public MyNetConnectedListener(AliyunVodPlayer aliyunVodPlayerView) {
        }

        @Override
        public void onReNetConnected(boolean isReconnect) {
            if (mNetConnectedListener != null) {
                mNetConnectedListener.onReNetConnected(isReconnect);
            }
        }

        @Override
        public void onNetUnConnected() {
            if (mNetConnectedListener != null) {
                mNetConnectedListener.onNetUnConnected();
            }
        }
    }

    /**
     * 获取当前屏幕模式：小屏、全屏
     *
     * @return 当前屏幕模式
     */
    public AliyunScreenMode getScreenMode() {
        return mCurrentScreenMode;
    }

    //---------------------------------------------------------Interface-----------------------------------------------//

    /**
     * 判断是否有网络的监听
     */
    public interface NetConnectedListener {
        /**
         * 网络已连接
         */
        void onReNetConnected(boolean isReconnect);

        /**
         * 网络未连接
         */
        void onNetUnConnected();
    }

    public interface OnScreenBrightnessListener {
        void onScreenBrightness(int brightness);
    }

    public interface OnFinishListener {
        void onFinishClick();
    }

    public interface OnTimeExpiredErrorListener {
        void onTimeExpiredError();
    }

    /**
     * seek开始监听
     */
    public interface OnSeekStartListener {
        void onSeekStart(int position);
    }

    /**
     * 屏幕方向改变监听接口
     */
    public interface OnOrientationChangeListener {
        /**
         * 屏幕方向改变
         *
         * @param from        从横屏切换为竖屏, 从竖屏切换为横屏
         * @param currentMode 当前屏幕类型
         */
        void orientationChange(boolean from, AliyunScreenMode currentMode);
    }
    /**
     * 播放按钮点击listener
     */
    public interface OnPlayStateBtnClickListener {
        void onPlayBtnClick(int playerState);
    }
//---------------------------------------------------------Interface-----------------------------------------------//

//------------------------------------------------------------事件监听-----------------------------------------------------------//

    //对外的各种事件监听

    private IPlayer.OnSeekCompleteListener mOuterSeekCompleteListener = null;

    /**
     * 设置seek结束监听
     *
     * @param onSeekCompleteListener seek结束监听
     */
    public void setOnSeekCompleteListener(IPlayer.OnSeekCompleteListener onSeekCompleteListener) {
        mOuterSeekCompleteListener = onSeekCompleteListener;
    }


    private IPlayer.OnTrackChangedListener mOutOnTrackChangedListener = null;

    /**
     * 流切换监听事件
     */
    public void setOnTrackChangedListener(IPlayer.OnTrackChangedListener listener){
        this.mOutOnTrackChangedListener = listener;
    }

    private IPlayer.OnRenderingStartListener mOutFirstFrameStartListener = null;
    /**
     * 设置首帧显示事件监听
     *
     * @param onFirstFrameStartListener 首帧显示事件监听
     */
    public void setOnFirstFrameStartListener(IPlayer.OnRenderingStartListener onFirstFrameStartListener) {
        mOutFirstFrameStartListener = onFirstFrameStartListener;
    }

    private OnScreenBrightnessListener mOnScreenBrightnessListener = null;
    public void setOnScreenBrightness(OnScreenBrightnessListener listener) {
        this.mOnScreenBrightnessListener = listener;
    }


    private OnTipsViewBackClickListener mOutOnTipsViewBackClickListener = null;
    public void setOnTipsViewBackClickListener(OnTipsViewBackClickListener listener){
        this.mOutOnTipsViewBackClickListener = listener;
    }

    private TipsView.OnTipClickListener mOutOnTipClickListener = null;
    public void setOnTipClickListener(TipsView.OnTipClickListener listener){
        this.mOutOnTipClickListener = listener;
    }

    private OnAutoPlayListener mOutAutoPlayListener = null;
    /**
     * 设置自动播放事件监听
     *
     * @param l 自动播放事件监听
     */
    public void setOnAutoPlayListener(OnAutoPlayListener l) {
        mOutAutoPlayListener = l;
    }


    private IPlayer.OnPreparedListener mOutPreparedListener = null;
    /**
     * 设置准备事件监听
     *
     * @param onPreparedListener 准备事件
     */
    public void setOnPreparedListener(IPlayer.OnPreparedListener onPreparedListener) {
        mOutPreparedListener = onPreparedListener;
    }

    private IPlayer.OnErrorListener mOutErrorListener = null;
    /**
     * 设置错误事件监听
     *
     * @param onErrorListener 错误事件监听
     */
    public void setOnErrorListener(IPlayer.OnErrorListener onErrorListener) {
        mOutErrorListener = onErrorListener;
    }

    private IPlayer.OnInfoListener mOutInfoListener = null;
    /**
     * 设置信息事件监听
     *
     * @param onInfoListener 信息事件监听
     */
    public void setOnInfoListener(IPlayer.OnInfoListener onInfoListener) {
        mOutInfoListener = onInfoListener;
    }

    private IPlayer.OnCompletionListener mOutCompletionListener = null;
    /**
     * 设置播放完成事件监听
     *
     * @param onCompletionListener 播放完成事件监听
     */
    public void setOnCompletionListener(IPlayer.OnCompletionListener onCompletionListener) {
        mOutCompletionListener = onCompletionListener;
    }

    public void setNetConnectedListener(NetConnectedListener listener) {
        this.mNetConnectedListener = listener;
    }

    /**
     * 设置播放状态点击监听
     */
    public void setOnPlayStateBtnClickListener(OnPlayStateBtnClickListener listener) {
        this.onPlayStateBtnClickListener = listener;
    }

    private OnOrientationChangeListener orientationChangeListener;
    public void setOrientationChangeListener(OnOrientationChangeListener listener) {
        this.orientationChangeListener = listener;
    }

    private OnTimeExpiredErrorListener mOutTimeExpiredErrorListener = null;
    /**
     * 设置源超时监听
     *
     * @param l 源超时监听
     */
    public void setOnTimeExpiredErrorListener(OnTimeExpiredErrorListener l) {
        mOutTimeExpiredErrorListener = l;
    }

    //停止按钮监听
    private OnStoppedListener mOnStoppedListener;
    /**
     * 设置停止播放监听
     *
     * @param onStoppedListener 停止播放监听
     */
    public void setOnStoppedListener(OnStoppedListener onStoppedListener) {
        this.mOnStoppedListener = onStoppedListener;
    }

    private OnFinishListener mOnFinishListener = null;
    /**
     * 设置监听
     */
    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.mOnFinishListener = onFinishListener;
    }

    private OnSeekStartListener onSeekStartListener;
    public void setOnSeekStartListener(OnSeekStartListener listener) {
        this.onSeekStartListener = listener;
    }
}
