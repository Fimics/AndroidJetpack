package com.mic.nav.player;

import java.util.HashMap;
import java.util.Map;

/**
 * 脖子运动平滑处理器
 * 使用指数移动平均滤波 + 速度限制来消除抖动
 */
public class NeckMotionSmoother {
    private static final String TAG = "NeckMotionSmoother";
    
    // 平滑因子 (0-1)，值越小越平滑但响应越慢，建议 0.2-0.4
    private static final float DEFAULT_SMOOTHING_FACTOR = 0.3f;
    
    // 每帧最大允许变化量（防止突变），单位：blendshape值
    private static final float DEFAULT_MAX_DELTA_PER_FRAME = 0.05f;
    
    private final float smoothingFactor;
    private final float maxDeltaPerFrame;
    
    // 上一帧滤波后的值
    private float lastSmoothedPitch = 0f;
    private float lastSmoothedRoll = 0f;
    private float lastSmoothedYaw = 0f;
    
    // 上一帧原始值（用于速度计算）
    private float lastRawPitch = 0f;
    private float lastRawRoll = 0f;
    private float lastRawYaw = 0f;
    
    public NeckMotionSmoother() {
        this(DEFAULT_SMOOTHING_FACTOR, DEFAULT_MAX_DELTA_PER_FRAME);
    }
    
    /**
     * @param smoothingFactor 平滑因子 (0-1)
     * @param maxDeltaPerFrame 每帧最大变化量
     */
    public NeckMotionSmoother(float smoothingFactor, float maxDeltaPerFrame) {
        this.smoothingFactor = Math.max(0.01f, Math.min(1.0f, smoothingFactor));
        this.maxDeltaPerFrame = maxDeltaPerFrame;
    }
    
    /**
     * 对脖子BlendShapes进行平滑处理
     * @param rawBlendShapes 原始blendshapes数据
     * @return 平滑后的数据
     */
    public Map<String, Float> smooth(Map<String, Float> rawBlendShapes) {
        if (rawBlendShapes == null || rawBlendShapes.isEmpty()) {
            return rawBlendShapes;
        }
        
        Map<String, Float> smoothed = new HashMap<>(rawBlendShapes);
        
        // 提取原始值
        float rawPitch = getSafeValue(rawBlendShapes, "HeadPitch");
        float rawRoll = getSafeValue(rawBlendShapes, "HeadRoll");
        float rawYaw = getSafeValue(rawBlendShapes, "HeadYaw");
        
        // 1. 速度限制（防止突变）
        float clampedPitch = clampDelta(rawPitch, lastRawPitch);
        float clampedRoll = clampDelta(rawRoll, lastRawRoll);
        float clampedYaw = clampDelta(rawYaw, lastRawYaw);
        
        // 2. 指数移动平均滤波
        float smoothedPitch = emaFilter(clampedPitch, lastSmoothedPitch);
        float smoothedRoll = emaFilter(clampedRoll, lastSmoothedRoll);
        float smoothedYaw = emaFilter(clampedYaw, lastSmoothedYaw);
        
        // 更新状态
        updateState(smoothedPitch, smoothedRoll, smoothedYaw, rawPitch, rawRoll, rawYaw);
        
        // 写回map
        smoothed.put("HeadPitch", smoothedPitch);
        smoothed.put("HeadRoll", smoothedRoll);
        smoothed.put("HeadYaw", smoothedYaw);
        
        return smoothed;
    }
    
    /**
     * 指数移动平均滤波
     */
    private float emaFilter(float current, float lastSmoothed) {
        return smoothingFactor * current + (1 - smoothingFactor) * lastSmoothed;
    }
    
    /**
     * 限制每帧变化量
     */
    private float clampDelta(float current, float lastRaw) {
        float delta = current - lastRaw;
        if (Math.abs(delta) > maxDeltaPerFrame) {
            return lastRaw + Math.signum(delta) * maxDeltaPerFrame;
        }
        return current;
    }
    
    /**
     * 安全获取map中的值
     */
    private float getSafeValue(Map<String, Float> map, String key) {
        Float value = map.get(key);
        return value != null ? value : 0.0f;
    }
    
    /**
     * 更新内部状态
     */
    private void updateState(float smoothedPitch, float smoothedRoll, float smoothedYaw,
                           float rawPitch, float rawRoll, float rawYaw) {
        this.lastSmoothedPitch = smoothedPitch;
        this.lastSmoothedRoll = smoothedRoll;
        this.lastSmoothedYaw = smoothedYaw;
        
        this.lastRawPitch = rawPitch;
        this.lastRawRoll = rawRoll;
        this.lastRawYaw = rawYaw;
    }
    
    /**
     * 重置状态（用于场景切换或重新初始化）
     */
    public void reset() {
        lastSmoothedPitch = 0f;
        lastSmoothedRoll = 0f;
        lastSmoothedYaw = 0f;
        lastRawPitch = 0f;
        lastRawRoll = 0f;
        lastRawYaw = 0f;
    }
}
