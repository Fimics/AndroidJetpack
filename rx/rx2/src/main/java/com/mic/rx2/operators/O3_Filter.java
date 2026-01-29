package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * ===============================
 * 过滤类操作符（Filtering Operators）
 *
 * 作用：
 *  - 决定「哪些数据可以继续往下游流动」
 *  - 控制数据数量、频率、时机
 * ===============================
 */
public class O3_Filter {

    /**
     * 1) filter
     *
     * 功能：
     *  - 按条件判断，满足条件的数据才会被发射
     *
     * 典型场景：
     *  - 剔除空值、无效值
     *
     * 常见坑：
     *  - 条件里不要写耗时逻辑（会阻塞链路）
     *  - 注意不要返回 null（RxJava 不允许 null）
     *
     * 对比：
     *  - filter：按条件过滤
     *  - distinct：按“是否重复”过滤
     */
    public void demoFilter(Output out) {
        Observable.range(1, 6)
                .filter(i -> i % 2 == 0)
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 2) take
     *
     * 功能：
     *  - 只取前 N 个数据，其余直接忽略并结束
     *
     * 典型场景：
     *  - 只关心第一个结果
     *
     * 常见坑：
     *  - take 会主动触发 onComplete（很多人误以为上游还会继续跑）
     *
     * 对比：
     *  - take(1)：仍是 Observable（语义是“截断”）
     *  - first()/firstElement()：语义是“取第一个”（类型可能变 Maybe/Single）
     */
    public void demoTake(Output out) {
        Observable.range(1, 10)
                .take(3)
                .subscribe(v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete"));
    }

    /**
     * 3) takeLast
     *
     * 功能：
     *  - 只保留最后 N 个数据
     *
     * 典型场景：
     *  - 只关心最终状态
     *
     * 常见坑（非常重要）：
     *  - takeLast 必须等上游 complete 才能知道“最后 N 个是谁”
     *  - 如果上游是无限流（interval/never），takeLast 永远不会发射
     *
     * 对比：
     *  - take：取前 N 个（可提前结束）
     *  - takeLast：取后 N 个（必须等结束）
     */
    public void demoTakeLast(Output out) {
        Observable.range(1, 5)
                .takeLast(2)
                .subscribe(v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete"));
    }

    /**
     * 4) skip
     *
     * 功能：
     *  - 跳过前 N 个数据
     *
     * 典型场景：
     *  - 忽略初始化阶段的噪声
     *
     * 常见坑：
     *  - skip 只是丢弃，不会改变上游是否产生数据（上游仍然在发射）
     *
     * 对比：
     *  - skip：跳过开头
     *  - skipLast：跳过结尾（需要等 complete）
     */
    public void demoSkip(Output out) {
        Observable.range(1, 5)
                .skip(2)
                .subscribe(v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete"));
    }

    /**
     * 5) skipLast
     *
     * 功能：
     *  - 跳过最后 N 个数据
     *
     * 典型场景：
     *  - 忽略收尾标记数据（例如末尾的占位/结束标记）
     *
     * 常见坑（非常重要）：
     *  - skipLast 同样必须等待上游 complete，才能知道“最后 N 个是什么”
     *  - 无限流不会 complete，会导致 skipLast 一直不输出或输出行为不符合预期
     *
     * 对比：
     *  - skipLast：跳过末尾 N 个
     *  - takeLast：只保留末尾 N 个
     */
    public void demoSkipLast(Output out) {
        Observable.range(1, 6)
                .skipLast(2)
                .subscribe(v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete"));
    }

    /**
     * 6) distinct
     *
     * 功能：
     *  - 对全部数据进行去重（全局去重）
     *
     * 典型场景：
     *  - 去重 ID、去重请求
     *
     * 常见坑（非常重要）：
     *  - distinct 需要记住“见过哪些元素”，内部通常用 Set
     *  - 数据量很大或无限流会导致内存增长风险（泄漏式增长）
     *
     * 对比：
     *  - distinct：全局去重（记忆历史）
     *  - distinctUntilChanged：只去连续重复（不需要全局记忆）
     */
    public void demoDistinct(Output out) {
        Observable.just(1, 1, 2, 2, 3, 1)
                .distinct()
                .subscribe(v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete"));
    }

    /**
     * 7) distinctUntilChanged
     *
     * 功能：
     *  - 仅去掉连续重复数据
     *
     * 典型场景：
     *  - 状态变化监听（只关心变化）
     *
     * 常见坑：
     *  - 只会去“连续”重复：1,2,1 不会去掉最后那个 1
     *
     * 对比：
     *  - distinct：全局去重
     *  - distinctUntilChanged：连续去重（更适合状态流）
     */
    public void demoDistinctUntilChanged(Output out) {
        Observable.just(1, 1, 2, 2, 3, 2)
                .distinctUntilChanged()
                .subscribe(v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete"));
    }

    /**
     * 8) debounce
     *
     * 功能：
     *  - 在一段时间内只发射最后一次数据（防抖）
     *
     * 典型场景：
     *  - 输入框防抖（用户停止输入一段时间再请求）
     *
     * 常见坑（非常重要）：
     *  1. debounce 是“最后一次”，不是“第一下”
     *  2. 需要有时间推进（异步/调度器），纯同步 just 你可能看不到效果
     *
     * 对比：
     *  - debounce：等用户“停下来”才发最后一次（防抖）
     *  - throttleFirst/sample：按频率限制（节流）
     */
    public void demoDebounce(Output out) {
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .take(5)
                .debounce(200, TimeUnit.MILLISECONDS)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete"));
    }

    /**
     * 9) throttle / sample
     *
     * 功能：
     *  - throttle（节流）：在时间窗口内限制发射频率
     *  - sample：按固定时间间隔取“最近一次”（等价于 throttleLast）
     *
     * 典型场景：
     *  - 按钮点击限频（用 throttleFirst 最常见）
     *
     * 常见坑（非常重要）：
     *  1. throttleFirst：窗口内只取第一个（适合按钮防连点）
     *  2. sample：每隔一段时间取最后一个（适合按频率刷新 UI）
     *  3. 不同节流策略语义差异很大，别混用
     *
     * 对比：
     *  - throttleFirst：窗口内取第一个（按钮防连点）
     *  - sample(throttleLast)：每个周期取最后一个（周期采样）
     *  - debounce：停下来才发最后一个（防抖）
     */
    public void demoThrottleSample(Output out) {
        out.print("---- throttleFirst 示例（窗口内取第一个） ----");
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .take(8)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .blockingSubscribe(v -> out.print("throttleFirst onNext: " + v));

        out.print("---- sample 示例（每段时间取最后一个） ----");
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .take(8)
                .sample(300, TimeUnit.MILLISECONDS)
                .blockingSubscribe(v -> out.print("sample onNext: " + v));
    }

    /**
     * 10) ignoreElements
     *
     * 功能：
     *  - 忽略所有数据，只关心完成或错误
     *
     * 典型场景：
     *  - 把流当作“任务”使用（只关心成功/失败）
     *
     * 常见坑：
     *  - onNext 全部被丢弃，下游只会收到 onComplete 或 onError
     *
     * 对比：
     *  - ignoreElements：把 Observable “降级”为 Completable（语义：只关心完成/错误）
     *  - take(0)：也能不发射数据，但语义不如 ignoreElements 清晰
     */
    public void demoIgnoreElements(Output out) {
        Observable.just(1, 2, 3)
                .ignoreElements()
                .subscribe(
                        () -> out.print("onComplete"),
                        e -> out.print("onError: " + e)
                );
    }
}
