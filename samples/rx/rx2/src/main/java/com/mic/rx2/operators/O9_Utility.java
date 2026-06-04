package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * ===============================
 * 辅助与调试类操作符（Utility / Debug）
 *
 * 作用：
 *  - 做副作用（日志、埋点、loading、资源释放）
 *  - 生命周期收口（finally / cancel）
 *  - 共享/缓存（cache/share）
 * ===============================
 */
public class O9_Utility {

    /**
     * 1) doOnNext
     *
     * 功能：
     *  - 数据到来时执行额外逻辑（副作用）
     *  - 不改变数据内容、不改变事件流
     *
     * 典型场景：
     *  - 日志、埋点
     *
     * 常见坑（非常重要）：
     *  1. doOnNext 内不要写耗时逻辑，否则会拖慢链路
     *  2. doOnNext 抛异常会导致整个流出错（尽量保证它不抛）
     *
     * 对比：
     *  - doOnNext：观察数据/做副作用
     *  - map：改变数据
     */
    public void demoDoOnNext(Output out) {
        Observable.just(1, 2, 3)
                .doOnNext(v -> out.print("side-effect: " + v))
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 2) doOnSubscribe
     *
     * 功能：
     *  - 订阅时执行逻辑（能拿到 Disposable）
     *
     * 典型场景：
     *  - 显示 loading
     *
     * 常见坑（非常重要）：
     *  1. doOnSubscribe 执行线程取决于 subscribeOn（可能不在主线程）
     *  2. 如果要确保 UI 线程显示 loading，需要配合 observeOn(AndroidSchedulers.mainThread())
     *
     * 对比：
     *  - doOnSubscribe：订阅时机
     *  - doFinally：结束时机（无论成功失败取消）
     */
    public void demoDoOnSubscribe(Output out) {
        Observable.just("X")
                .doOnSubscribe(d -> out.print("onSubscribe (thread=" + Thread.currentThread().getName() + ")"))
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 3) doOnComplete
     *
     * 功能：
     *  - 完成时执行逻辑（仅 onComplete）
     *
     * 典型场景：
     *  - 隐藏 loading（仅成功完成时）
     *
     * 常见坑（非常重要）：
     *  1. 出错不会触发 doOnComplete（错误要用 doOnError / doFinally）
     *  2. dispose 取消也不会触发 doOnComplete（取消要用 doOnDispose / doFinally）
     *
     * 对比：
     *  - doOnComplete：只在成功完成时触发
     *  - doFinally：成功/失败/取消都会触发（更“收口”）
     */
    public void demoDoOnComplete(Output out) {
        Observable.just(1, 2)
                .doOnComplete(() -> out.print("doOnComplete"))
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 4) doFinally
     *
     * 功能：
     *  - 流结束时统一处理（成功/失败/取消都会触发一次）
     *
     * 典型场景：
     *  - 资源释放（关闭文件/关闭连接/回收句柄）
     *
     * 常见坑（非常重要）：
     *  1. doFinally 触发时机在终止事件之后（onComplete/onError 之后，或 dispose 之后）
     *  2. doFinally 内不要抛异常，否则会让问题更难排查
     *
     * 对比：
     *  - doOnComplete/doOnError：分开处理
     *  - doFinally：统一收口（工程里最常用）
     */
    public void demoDoFinally(Output out) {
        Observable.<Integer>error(new RuntimeException("boom"))
                .doFinally(() -> out.print("doFinally (always)"))
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e.getMessage())
                );
    }

    /**
     * 5) doOnCancel（RxJava 中等价为 doOnDispose）
     *
     * 功能：
     *  - 取消订阅时处理（dispose 时执行）
     *
     * 典型场景：
     *  - 页面销毁：取消请求/取消轮询后的清理动作
     *
     * 常见坑（非常重要）：
     *  1. doOnDispose 只在“主动取消”触发；正常完成/错误不会触发（那是 doFinally）
     *  2. 很多人误以为“complete 也会触发 doOnDispose”——不会
     *
     * 对比：
     *  - doOnDispose：仅取消
     *  - doFinally：任何结束都会触发（更稳的收口）
     */
    public void demoDoOnCancel(Output out) {
        Observable<Long> source = Observable.interval(100, TimeUnit.MILLISECONDS)
                .doOnDispose(() -> out.print("doOnDispose (cancel)"));

        source.take(3)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 6) delay
     *
     * 功能：
     *  - 延迟数据发射（整体往后推迟）
     *
     * 典型场景：
     *  - 模拟慢网（方便观察 loading、骨架屏）
     *
     * 常见坑：
     *  1. delay 不会阻塞当前线程，它是“调度延迟”
     *  2. 会改变时序，测试时要用 blocking 或 TestScheduler
     *
     * 对比：
     *  - delay：对现有流整体延迟
     *  - timer：延迟后只发射一次
     */
    public void demoDelay(Output out) {
        Observable.just("A", "B")
                .delay(150, TimeUnit.MILLISECONDS)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 7) timeout
     *
     * 功能：
     *  - 调试超时：在指定时间内没有等到“下一次事件”就触发超时错误
     *
     * 典型场景：
     *  - 定位问题（卡住/不回调/链路中断）
     *
     * 常见坑（非常重要）：
     *  1. timeout 是“下一次事件”超时，不是“总耗时”
     *  2. 超时后会 dispose 上游（上游后续不会再发射给你）
     *
     * 对比：
     *  - timeout：超时抛错（或可切 fallback）
     *  - takeUntil(timer)：到点停止但不一定报错（语义不同）
     */
    public void demoTimeout(Output out) {
        Observable.never()
                .timeout(200, TimeUnit.MILLISECONDS)
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e.getClass().getSimpleName()),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 8) repeat
     *
     * 功能：
     *  - 完成后重新执行（重复订阅）
     *
     * 典型场景：
     *  - 轮询（配合 delay / repeatWhen / takeUntil）
     *
     * 常见坑（非常重要）：
     *  1. repeat 只在“完成”后重复；出错不会重复（要用 retry）
     *  2. repeat 无限循环很危险，必须加次数或终止条件
     *
     * 对比：
     *  - repeat：完成后重来
     *  - retry：错误后重来
     */
    public void demoRepeat(Output out) {
        Observable.just("PING")
                .repeat(3)
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 9) cache / share
     *
     * 功能：
     *  - cache：缓存上游事件（onNext/onComplete/onError），后续订阅者会“重放”缓存结果
     *  - share：共享订阅（publish().refCount()），多个订阅者共享同一个上游订阅（不重放历史）
     *
     * 典型场景：
     *  - 多处复用（避免重复请求/重复计算）
     *
     * 常见坑（非常重要）：
     *  1. cache 会缓存“全部数据”，数据量大可能爆内存；并且会缓存错误（第一次失败后以后订阅都立刻失败）
     *  2. share 不会重放历史：后订阅者可能错过之前的数据；订阅数归零后可能断开
     *
     * 对比：
     *  - cache：缓存 + 回放（更像“记住结果”）
     *  - share：共享 + 不回放（更像“同看直播”）
     */
    public void demoCacheShare(Output out) {
        out.print("---- cache 示例（缓存并回放） ----");
        Observable<Integer> cached = Observable.range(1, 3)
                .doOnSubscribe(d -> out.print("cache upstream subscribed"))
                .cache();

        cached.subscribe(v -> out.print("cache first sub: " + v));
        cached.subscribe(v -> out.print("cache second sub: " + v));

        out.print("---- share 示例（共享但不回放） ----");
        Observable<Long> shared = Observable.interval(100, TimeUnit.MILLISECONDS)
                .take(5)
                .doOnSubscribe(d -> out.print("share upstream subscribed"))
                .share();

        shared.blockingSubscribe(v -> out.print("share sub1: " + v));
        // 注意：第二个订阅者开始时，上游已结束，因此可能收不到数据（这就是 share 的关键特性）
        shared.subscribe(v -> out.print("share sub2: " + v));
    }
}
