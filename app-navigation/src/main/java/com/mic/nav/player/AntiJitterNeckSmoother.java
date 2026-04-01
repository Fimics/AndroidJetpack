package com.mic.nav.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 高效抗抖动脖子平滑器
 * 核心策略：自适应EMA + 动态死区 + 智能限幅
 * 特点：响应快、延迟低、计算量小
 */
public class AntiJitterNeckSmoother {
    private static final String TAG = "NeckSmoother";

    // ===== 核心参数（激进调优）=====
    // 平滑因子：0.3=灵敏，0.15=平衡，0.05=极平滑
    private static final float SMOOTHING_FACTOR = 0.25f;

    // 死区阈值：消除微抖动（0.005~0.01）
    private static final float DEAD_ZONE = 0.006f;

    // 最大速度：防止突变（0.05~0.1）
    private static final float MAX_VELOCITY = 0.06f;

    // 最大加速度：抑制抖动核心参数（0.01~0.02）
    private static final float MAX_ACCELERATION = 0.012f;

    // 抖动检测窗口
    private static final int JITTER_WINDOW = 5;

    // ===== 状态变量 =====
    private final LinkedList<float[]> jitterQueue = new LinkedList<>();
    private float[] lastValue = new float[3];
    private float[] lastVelocity = new float[3];
    private float[] lastRaw = new float[3];

    public AntiJitterNeckSmoother() {
        reset();
    }

    /**
     * 主平滑方法（极简流程）
     */
    public Map<String, Float> smooth(Map<String, Float> rawBlendShapes) {
        if (rawBlendShapes == null || rawBlendShapes.isEmpty()) {
            return rawBlendShapes;
        }

        // 1. 提取值
        float[] raw = extractValues(rawBlendShapes);

        // 2. 动态死区（只在静止时生效）
        raw = applySmartDeadZone(raw);

        // 3. 计算自适应平滑因子
        float smoothing = calculateAdaptiveSmoothing(raw);

        // 4. 速度限制
        float[] velocityClamped = clampVelocity(raw);

        // 5. EMA平滑
        float[] smoothed = applyEMA(velocityClamped, smoothing);

        // 6. 加速度限制（最终输出）
        float[] finalValue = clampAcceleration(smoothed);

        // 7. 更新状态
        updateState(finalValue, raw);

        return createResultMap(finalValue);
    }

    /**
     * 提取值
     */
    private float[] extractValues(Map<String, Float> map) {
        return new float[] {
                getSafeValue(map, "HeadPitch"),
                getSafeValue(map, "HeadRoll"),
                getSafeValue(map, "HeadYaw")
        };
    }

    /**
     * 智能死区：只在速度接近0时生效，避免运动卡顿
     */
    private float[] applySmartDeadZone(float[] raw) {
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            float delta = raw[i] - lastRaw[i];

            // 如果速度很慢（接近静止），应用死区
            if (Math.abs(lastVelocity[i]) < DEAD_ZONE * 2 && Math.abs(delta) < DEAD_ZONE) {
                result[i] = lastRaw[i]; // 保持不动
            } else {
                result[i] = raw[i]; // 正常通过
            }
        }
        return result;
    }

    /**
     * 自适应平滑：抖动大时更平滑，运动时更灵敏
     */
    private float calculateAdaptiveSmoothing(float[] raw) {
        // 维护抖动队列
        jitterQueue.add(raw.clone());
        if (jitterQueue.size() > JITTER_WINDOW) {
            jitterQueue.removeFirst();
        }

        // 计算抖动强度（方差）
        float jitterIntensity = calculateJitterIntensity();

        // 计算运动速度
        float speed = 0;
        for (int i = 0; i < 3; i++) {
            speed += Math.abs(raw[i] - lastRaw[i]);
        }

        // 运动快时降低平滑，抖动大时增加平滑
        float speedFactor = Math.min(1.0f, speed * 10); // 速度权重
        float jitterFactor = Math.min(1.0f, jitterIntensity / 0.02f); // 抖动权重

        // 最终平滑因子：运动时更灵敏，抖动时更平滑
        return SMOOTHING_FACTOR * (1 + speedFactor * 0.5f - jitterFactor * 0.3f);
    }

    /**
     * 计算抖动强度
     */
    private float calculateJitterIntensity() {
        if (jitterQueue.size() < 2) return 0f;

        float variance = 0;
        for (int i = 0; i < 3; i++) {
            float sum = 0, sumSq = 0;
            for (float[] data : jitterQueue) {
                sum += data[i];
                sumSq += data[i] * data[i];
            }
            float mean = sum / jitterQueue.size();
            variance += (sumSq / jitterQueue.size() - mean * mean);
        }
        return variance / 3;
    }

    /**
     * 速度限制
     */
    private float[] clampVelocity(float[] raw) {
        float[] clamped = new float[3];
        for (int i = 0; i < 3; i++) {
            float delta = raw[i] - lastValue[i];
            if (Math.abs(delta) > MAX_VELOCITY) {
                clamped[i] = lastValue[i] + Math.signum(delta) * MAX_VELOCITY;
            } else {
                clamped[i] = raw[i];
            }
        }
        return clamped;
    }

    /**
     * EMA平滑
     */
    private float[] applyEMA(float[] input, float factor) {
        float[] output = new float[3];
        for (int i = 0; i < 3; i++) {
            output[i] = factor * input[i] + (1 - factor) * lastValue[i];
        }
        return output;
    }

    /**
     * 加速度限制（核心防抖）
     */
    private float[] clampAcceleration(float[] smoothed) {
        float[] finalValue = new float[3];
        for (int i = 0; i < 3; i++) {
            float velocity = smoothed[i] - lastValue[i];
            float accel = velocity - lastVelocity[i];

            if (Math.abs(accel) > MAX_ACCELERATION) {
                // 限制加速度
                velocity = lastVelocity[i] + Math.signum(accel) * MAX_ACCELERATION;
                finalValue[i] = lastValue[i] + velocity;
            } else {
                finalValue[i] = smoothed[i];
            }
        }
        return finalValue;
    }

    /**
     * 更新状态
     */
    private void updateState(float[] finalValue, float[] raw) {
        // 更新速度
        for (int i = 0; i < 3; i++) {
            lastVelocity[i] = finalValue[i] - lastValue[i];
        }

        // 更新值
        System.arraycopy(finalValue, 0, lastValue, 0, 3);
        System.arraycopy(raw, 0, lastRaw, 0, 3);
    }

    /**
     * 创建结果Map
     */
    private Map<String, Float> createResultMap(float[] values) {
        Map<String, Float> result = new HashMap<>();
        result.put("HeadPitch", values[0]);
        result.put("HeadRoll", values[1]);
        result.put("HeadYaw", values[2]);
        return result;
    }

    private float getSafeValue(Map<String, Float> map, String key) {
        Float value = map.get(key);
        return value != null ? value : 0.0f;
    }

    /**
     * 重置状态
     */
    public void reset() {
        jitterQueue.clear();
        lastValue = new float[3];
        lastVelocity = new float[3];
        lastRaw = new float[3];
    }
}
