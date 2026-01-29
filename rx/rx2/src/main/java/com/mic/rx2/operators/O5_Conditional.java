package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * ===============================
 * 条件与布尔类操作符（Conditional / Boolean）
 *
 * 作用：
 *  - 条件判断（all / any / contains）
 *  - 空流兜底（isEmpty / defaultIfEmpty / switchIfEmpty）
 *  - 流程控制（takeUntil / skipUntil）
 *
 * 说明（RxJava2 语义提醒）：
 *  - all/any/contains/isEmpty 都是“聚合判断”，一般需要等上游 complete 才能给结论
 *  - takeUntil(other)/skipUntil(other) 里，other 作为“信号流”，通常以 onNext 触发为准
 * ===============================
 */
public class O5_Conditional {

    /**
     * all
     *
     * 功能：
     *  - 判断上游「是否所有数据都满足条件」
     *  - 最终只发射一个 Boolean，然后结束
     *
     * 应用场景：
     *  - 批量校验：是否所有字段都合法
     *  - 权限校验：是否所有项都满足规则
     *
     * 常见坑（非常重要）：
     *  1. all 通常要等到上游 complete 才能给最终结论
     *  2. 如果上游是无限流（interval/never），all 可能永远不会结束
     *  3. 条件里不要写耗时逻辑
     *
     * 对比：
     *  - all：全部满足才 true
     *  - any：只要有一个满足就 true
     */
    public void demoAll(Output out) {
        Observable.just(2, 4, 6)
                .all(i -> i % 2 == 0)
                .toObservable()
                .subscribe(
                        v -> out.print("all even? " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }


    /**
     * any
     *
     * 功能：
     *  - 判断上游「是否存在任意一个数据满足条件」
     *  - 最终只发射一个 Boolean，然后结束
     *
     * 应用场景：
     *  - 错误检测：是否出现过异常标记
     *  - 命中检测：是否存在目标元素
     *
     * 常见坑：
     *  1. any 可能会提前结束：一旦命中就直接 true 并 dispose 上游
     *  2. 无限流如果永远不命中也会一直不结束
     *
     * 对比：
     *  - any：有一个满足即 true（可提前结束）
     *  - contains：判断是否出现过指定值
     */
    public void demoAny(Output out) {
        Observable.just(1, 3, 4)
                .any(i -> i % 2 == 0)
                .subscribe(
                        v -> out.print("any even? " + v),
                        e -> out.print("onError: " + e)
                );
    }

    /**
     * contains
     *
     * 功能：
     *  - 判断上游是否出现过指定值（基于 equals）
     *
     * 应用场景：
     *  - 事件是否发生：是否出现过某个状态码/关键字
     *
     * 常见坑：
     *  1. 比较基于 equals：自定义对象要正确重写 equals/hashCode
     *  2. 也可能提前结束：一旦命中就结束并 dispose 上游
     *
     * 对比：
     *  - contains：判断“出现过某值”
     *  - any：判断“存在满足条件的值”
     */
    public void demoContains(Output out) {
        Observable.just("A", "B", "C")
                .contains("B")
                .subscribe(
                        v -> out.print("contains B? " + v),
                        e -> out.print("onError: " + e)
                );
    }

    /**
     * isEmpty
     *
     * 功能：
     *  - 判断上游是否“没有任何 onNext”
     *  - 发射一个 Boolean 后结束
     *
     * 应用场景：
     *  - 搜索结果是否为空
     *  - 缓存是否命中：缓存流为空则走网络
     *
     * 常见坑（非常重要）：
     *  1. isEmpty 需要等上游 complete 才能确定“到底有没有数据”
     *  2. 无限流永远无法得出结论
     *
     * 对比：
     *  - isEmpty：得出 Boolean
     *  - defaultIfEmpty / switchIfEmpty：直接给兜底值或兜底流
     */
    public void demoIsEmpty(Output out) {
        Observable.<Integer>empty()
                .isEmpty()
                .subscribe(
                        v -> out.print("isEmpty? " + v),
                        e -> out.print("onError: " + e)
                );
    }

    /**
     * defaultIfEmpty
     *
     * 功能：
     *  - 如果上游为空（没有 onNext），就发射一个默认值
     *
     * 应用场景：
     *  - 空页面兜底：返回默认展示数据
     *
     * 常见坑（非常重要）：
     *  1. 只有“完全没有 onNext”才算 empty；发射过哪怕 1 个数据都不会走默认值
     *  2. 默认值不能为 null
     *
     * 对比：
     *  - defaultIfEmpty：兜底“一个值”
     *  - switchIfEmpty：兜底“一条流”
     */
    public void demoDefaultIfEmpty(Output out) {
        Observable.<Integer>empty()
                .defaultIfEmpty(100)
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * switchIfEmpty
     *
     * 功能：
     *  - 如果上游为空，切换到另一条 Observable
     *
     * 应用场景：
     *  - 缓存失败走网络：cache.switchIfEmpty(network)
     *
     * 常见坑（非常重要）：
     *  1. 只对“空流”生效，不对“错误流”生效（错误要用 onErrorResumeNext）
     *  2. 兜底流内部也要注意线程与异常处理
     *
     * 对比：
     *  - switchIfEmpty：空流切换
     *  - onErrorResumeNext：错误切换
     */
    public void demoSwitchIfEmpty(Output out) {
        Observable.<String>empty()
                .switchIfEmpty(Observable.just("fallback-1", "fallback-2"))
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * takeUntil
     *
     * 功能（RxJava2）：
     *  - “信号流 other 一旦发射 onNext”，就终止主流并 complete（dispose 主流）
     *
     * 应用场景：
     *  - 页面销毁/生命周期结束时停止轮询
     *  - 收到取消信号时停止任务
     *
     * 常见坑（非常重要）：
     *  1. other 一触发（onNext），主流立刻 dispose，后续数据不再下发
     *  2. 定时边界存在竞态：例如 450ms 停止，100ms 间隔的主流可能输出 0~3 或 0~4
     *
     * 对比：
     *  - takeUntil：先放行，后停止
     *  - skipUntil：先丢弃，后放行
     */
    public void demoTakeUntil(Output out) {
        Observable<Long> main = Observable.interval(100, TimeUnit.MILLISECONDS);

        // stop 信号：450ms 后发射一个 onNext(0) —— 一旦发射，就终止 main
        Observable<Long> stop = Observable.timer(450, TimeUnit.MILLISECONDS).take(1);

        main.takeUntil(stop)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * skipUntil
     *
     * 功能（RxJava2）：
     *  - 在“开始信号流 other 发射 onNext”之前，主流的数据全部丢弃
     *  - other 一旦 onNext，之后主流数据全部放行
     *
     * 应用场景：
     *  - 初始化完成后再处理事件
     *  - 等权限/配置拉取完成后再消费数据
     *
     * 常见坑（非常重要）：
     *  1. 如果 other 永远不发射 onNext，主流会一直被丢弃（看起来像“没数据”）
     *  2. other 只影响“开始放行的那个时刻”，之后不会再阻塞
     *
     * 对比：
     *  - skipUntil：先丢弃，后放行
     *  - takeUntil：先放行，后终止
     */
    public void demoSkipUntil(Output out) {
        Observable<Long> main = Observable.interval(100, TimeUnit.MILLISECONDS).take(8);

        // start 信号：350ms 后发射一个 onNext(0) —— 一旦发射，就开始放行 main
        Observable<Long> start = Observable.timer(350, TimeUnit.MILLISECONDS).take(1);

        main.skipUntil(start)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }
}
