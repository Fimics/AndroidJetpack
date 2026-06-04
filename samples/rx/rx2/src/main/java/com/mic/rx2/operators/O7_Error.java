package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;

/**
 * ===============================
 * 错误处理类操作符（Error Handling）
 *
 * 作用：
 *  - 出错兜底（onErrorReturn）
 *  - 出错切换（onErrorResumeNext）
 *  - 重试（retry / retryWhen）
 *  - 错误副作用（doOnError）
 * ===============================
 */
public class O7_Error {

    /**
     * 1) onErrorReturn
     *
     * 功能：
     *  - 出错时返回兜底数据（用一个值替代错误）
     *  - 下游收到兜底值后正常 complete（不会再走 onError）
     *
     * 典型场景：
     *  - 默认展示：网络失败返回默认数据
     *
     * 常见坑（非常重要）：
     *  1. 只能兜底“错误”，不能处理“空流”（空流用 defaultIfEmpty）
     *  2. 兜底值不能是 null
     *  3. 兜底会“吞掉错误”，如果你还想保留错误信息，记得 doOnError 里打日志
     *
     * 对比：
     *  - onErrorReturn：兜底“一个值”
     *  - onErrorResumeNext：兜底“一条流”
     */
    public void demoOnErrorReturn(Output out) {
        Observable.<Integer>error(new RuntimeException("boom"))
                // 等价写法：onErrorReturn(e -> -1)
                .onErrorReturnItem(-1)
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 2) onErrorResumeNext
     *
     * 功能：
     *  - 出错时切换到备用流（备用 Observable）
     *
     * 典型场景：
     *  - 服务降级：主接口失败切换到备份接口 / 缓存数据
     *
     * 常见坑（非常重要）：
     *  1. 备用流也可能出错，需要继续兜底（否则还是会 onError）
     *  2. 不要把“业务错误”全部当异常吞掉，否则排查困难（该抛还是要抛）
     *
     * 对比：
     *  - onErrorResumeNext：错误切换
     *  - switchIfEmpty：空流切换（语义完全不同）
     */
    public void demoOnErrorResumeNext(Output out) {
        Observable.<String>error(new RuntimeException("primary failed"))
                .onErrorResumeNext(Observable.just("backup-1", "backup-2"))
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 3) retry
     *
     * 功能：
     *  - 出错后重新执行（重新订阅上游）
     *
     * 典型场景：
     *  - 临时网络异常、偶发失败
     *
     * 常见坑（非常重要）：
     *  1. retry 会“吞掉错误并重来”，如果不限制次数可能无限重试
     *  2. 重试会重复执行副作用（重复扣费/重复写入/重复提交），要非常谨慎
     *  3. 仅靠 retry 可能导致“雪崩式重试”，真实业务更建议加退避（用 retryWhen）
     *
     * 对比：
     *  - retry：固定次数/简单重试
     *  - retryWhen：策略化（退避/条件重试）
     */
    public void demoRetry(Output out) {
        AtomicInteger times = new AtomicInteger();

        Observable.<String>create(emitter -> {
                    int t = times.incrementAndGet();
                    out.print("attempt " + t);
                    if (t < 3) {
                        emitter.onError(new RuntimeException("fail " + t));
                    } else {
                        emitter.onNext("success at " + t);
                        emitter.onComplete();
                    }
                })
                .retry(2) // 最多重试2次（总尝试=1+2）
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e.getMessage()),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 4) retryWhen
     *
     * 功能：
     *  - 按策略控制是否重试（指数退避/按错误类型/按次数）
     *  - 错误会被映射成一个“控制流”：
     *      * 控制流 onNext：触发重试
     *      * 控制流 onComplete：停止重试并 complete
     *      * 控制流 onError：停止重试并把该 error 传给下游
     *
     * 典型场景：
     *  - 指数退避（或线性退避）重试，避免瞬间打爆服务
     *
     * 常见坑（非常重要）：
     *  1. retryWhen 写错非常容易变成“无限重试”
     *  2. 退避要避免阻塞线程（用 delay/timer，不要 Thread.sleep）
     *  3. 真实业务要区分“可重试错误”和“不可重试错误”
     *
     * 对比：
     *  - retry：简单粗暴
     *  - retryWhen：强大但更危险（需要明确策略）
     */
    public void demoRetryWhen(Output out) {
        AtomicInteger times = new AtomicInteger();

        Observable.<String>create(emitter -> {
                    int t = times.incrementAndGet();
                    out.print("attempt " + t);
                    emitter.onError(new RuntimeException("fail " + t));
                })
                .retryWhen(errors ->
                        errors
                                // 把错误次数限制在 1~3（最多重试3次）
                                .zipWith(Observable.range(1, 3), (err, retryCount) -> retryCount)
                                .flatMap(retryCount -> {
                                    // 这里用“线性退避”演示：200ms, 400ms, 600ms
                                    // 你也可以改成指数退避：Math.pow(2, retryCount) * base
                                    long delayMs = retryCount * 200L;
                                    out.print("retry in " + delayMs + "ms");
                                    return Observable.timer(delayMs, TimeUnit.MILLISECONDS);
                                })
                )
                // 防止 demo 写错导致一直挂住（真实项目一般不用 blockingSubscribe）
                .timeout(1200, TimeUnit.MILLISECONDS)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 5) doOnError
     *
     * 功能：
     *  - 错误时执行副作用（不改变事件流，不会吃掉错误）
     *
     * 典型场景：
     *  - 日志、告警（监控上报、埋点）
     *
     * 常见坑（非常重要）：
     *  1. doOnError 只是“旁路观察”，不会处理错误；下游仍然会收到 onError
     *  2. doOnError 内部不要再抛异常，否则会让链路更难排查
     *
     * 对比：
     *  - doOnError：观察/记录
     *  - onErrorReturn / onErrorResumeNext：处理/兜底
     */
    public void demoDoOnError(Output out) {
        Observable.<Integer>error(new RuntimeException("boom"))
                .doOnError(e -> out.print("log error: " + e.getMessage()))
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e.getMessage()),
                        () -> out.print("onComplete")
                );
    }
}
