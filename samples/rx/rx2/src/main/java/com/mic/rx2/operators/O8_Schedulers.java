package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * ===============================
 * 线程与调度类操作符（Schedulers / Threading）
 *
 * 目标：
 *  - 控制“生产在哪个线程”
 *  - 控制“消费在哪个线程”
 *  - 控制“取消在哪个线程”
 *  - 控制“延迟/超时”
 *
 * 重要认知：
 *  - subscribeOn 影响上游（数据产生）
 *  - observeOn 影响下游（数据消费）
 * ===============================
 */
public class O8_Schedulers {

    /**
     * 1) subscribeOn
     *
     * 功能：
     *  - 指定数据产生所在线程（影响上游）
     *
     * 典型场景：
     *  - IO、网络请求、文件读写、数据库
     *
     * 常见坑（非常重要）：
     *  1. 多次 subscribeOn 只有“第一次生效”（最靠近源头的那次）
     *  2. subscribeOn 不会把下游切回主线程（那是 observeOn 的职责）
     *
     * 对比：
     *  - subscribeOn：上游线程（生产）
     *  - observeOn：下游线程（消费）
     */
    public void demoSubscribeOn(Output out) {
        Observable.just("work")
                .subscribeOn(Schedulers.io())
                .doOnNext(v -> out.print("doOnNext thread: " + Thread.currentThread().getName()))
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 2) observeOn
     *
     * 功能：
     *  - 指定数据消费所在线程（从 observeOn 这一行开始往下游生效）
     *
     * 典型场景：
     *  - UI 更新（Android 中通常用 AndroidSchedulers.mainThread()）
     *
     * 常见坑（非常重要）：
     *  1. observeOn 会引入队列与线程切换成本，频繁切换会影响性能
     *  2. observeOn 之后的所有操作符默认都在该线程执行，除非再次 observeOn
     *
     * 对比：
     *  - subscribeOn：控制源头生产线程
     *  - observeOn：控制后续链路消费线程
     */
    public void demoObserveOn(Output out) {
        Observable.just(1, 2, 3)
                .doOnNext(v -> out.print("before observeOn: " + Thread.currentThread().getName()))
                .observeOn(Schedulers.computation())
                .doOnNext(v -> out.print("after observeOn: " + Thread.currentThread().getName()))
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 3) publishOn（Reactor 术语）
     *
     * 功能：
     *  - 在 Reactor 中：指定下游调度线程（切换下游执行上下文）
     *  - 在 RxJava 中：等价概念是 observeOn（RxJava 没有 publishOn 操作符）
     *
     * 典型场景：
     *  - Reactor 使用（服务端/响应式流体系里常见）
     *
     * 常见坑：
     *  - 不要误以为 RxJava 也有 publishOn（没有）
     *
     * 对比：
     *  - Reactor：publishOn
     *  - RxJava：observeOn
     */
    public void demoPublishOn(Output out) {
        out.print("RxJava 没有 publishOn，使用 observeOn 等价实现。");
        Observable.just("A")
                .observeOn(Schedulers.single())
                .doOnNext(v -> out.print("thread: " + Thread.currentThread().getName()))
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 4) unsubscribeOn
     *
     * 功能：
     *  - 指定取消订阅线程（dispose 时的清理工作在哪个线程执行）
     *
     * 典型场景：
     *  - 后台释放资源：IO/锁/文件句柄等，避免在 UI 线程释放导致卡顿
     *
     * 常见坑（非常重要）：
     *  1. 只有当 dispose 触发了“实际清理工作”时才有意义
     *  2. 很多人以为它能改变 subscribeOn/observeOn（不能）
     *
     * 对比：
     *  - subscribeOn：订阅与上游生产
     *  - observeOn：下游消费
     *  - unsubscribeOn：取消订阅清理
     */
    public void demoUnsubscribeOn(Output out) {
        Observable<Long> source = Observable.interval(100, TimeUnit.MILLISECONDS)
                .doOnDispose(() -> out.print("disposed on: " + Thread.currentThread().getName()))
                .unsubscribeOn(Schedulers.io());

        source.take(3)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 5) delay
     *
     * 功能：
     *  - 延迟发射数据（整体向后推迟）
     *
     * 典型场景：
     *  - 节奏控制（例如延迟显示/延迟请求）
     *  - 模拟慢网
     *
     * 常见坑（非常重要）：
     *  1. delay 不会阻塞当前线程，它是“调度延迟”
     *  2. delay 会改变时序，测试时建议用 blocking 或 TestScheduler
     *
     * 对比：
     *  - timer：延迟后只发射一次
     *  - delay：对现有流整体延迟
     */
    public void demoDelay(Output out) {
        Observable.just("A", "B")
                .delay(200, TimeUnit.MILLISECONDS)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 6) timeout
     *
     * 功能：
     *  - 超时终止流（抛 TimeoutException 或切换到 fallback）
     *
     * 典型场景：
     *  - 防止卡死（网络请求/等待回调）
     *  - 防止无限等待
     *
     * 常见坑（非常重要）：
     *  1. timeout 是“下一次事件”超时（不是总耗时）
     *  2. 超时后会 dispose 上游（上游后续事件不会再传到下游）
     *
     * 对比：
     *  - timeout：超时失败或 fallback
     *  - takeUntil(timer)：到点就停，但不一定报错（语义不同）
     */
    public void demoTimeout(Output out) {
        Observable.never()
                .timeout(300, TimeUnit.MILLISECONDS)
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e.getClass().getSimpleName() + " / " + e.getMessage()),
                        () -> out.print("onComplete")
                );
    }
}
