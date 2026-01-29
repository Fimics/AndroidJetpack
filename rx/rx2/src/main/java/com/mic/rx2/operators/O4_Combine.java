package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import io.reactivex.Observable;

/**
 * ===============================
 * 组合类操作符（Combining Operators）
 *
 * 作用：
 *  - 将多条数据流组合在一起
 *  - 解决“多源事件 / 多请求结果 / 多状态联动”
 * ===============================
 */
public class O4_Combine {

    /**
     * 1) merge
     *
     * 功能：
     *  - 并发合并多个数据流，先到先发
     *  - 所有上游都 complete 后才 complete
     *
     * 典型场景：
     *  - 多种事件源统一处理（例如：按钮点击流 + 网络回调流 + 传感器流）
     *
     * 常见坑（非常重要）：
     *  1. 不保证顺序（哪个先到先发）
     *  2. 其中任意一个流 onError，会导致整体立即 onError（除非用 delayError 版本）
     *
     * 对比：
     *  - merge：并发、无序
     *  - concat：串行、有序
     */
    public void demoMerge(Output out) {
        Observable.merge(
                Observable.just("A", "B"),
                Observable.just("1", "2")
        ).subscribe(
                v -> out.print("onNext: " + v),
                e -> out.print("onError: " + e),
                () -> out.print("onComplete")
        );
    }

    /**
     * 2) concat
     *
     * 功能：
     *  - 顺序拼接多个数据流（前一个 complete 后才订阅下一个）
     *  - 保证输出顺序
     *
     * 典型场景：
     *  - 按阶段执行任务（先读缓存 -> 再请求网络；先登录 -> 再拉配置）
     *
     * 常见坑：
     *  1. 只要前一个不 complete（比如 never / interval 无限流），后面的永远不会执行
     *  2. 任何一个流 onError，整体立即 onError
     *
     * 对比：
     *  - concat：串行、有序（像“任务队列”）
     *  - merge：并发、无序（像“多路合流”）
     */
    public void demoConcat(Output out) {
        Observable.concat(
                Observable.just("A", "B"),
                Observable.just("1", "2")
        ).subscribe(
                v -> out.print("onNext: " + v),
                e -> out.print("onError: " + e),
                () -> out.print("onComplete")
        );
    }

    /**
     * 3) zip
     *
     * 功能：
     *  - 多个流按顺序一一配对后发射（“拉链式”组合）
     *  - 每次取各流的第 n 个元素组合成一个结果
     *
     * 典型场景：
     *  - 并行请求结果合并（例如：用户信息 + 配置信息 -> 页面模型）
     *
     * 常见坑（非常重要）：
     *  1. “最短流”结束即结束：只要有一个流先 complete，整体就无法再配对
     *  2. 任意一个流 onError，整体立即 onError
     *
     * 对比：
     *  - zip：严格按“次数/序号”配对
     *  - combineLatest：按“最新值”配对（任一变化就组合）
     */
    public void demoZip(Output out) {
        Observable.zip(
                Observable.just(1, 2),
                Observable.just("A", "B"),
                (i, s) -> i + s
        ).subscribe(
                v -> out.print("onNext: " + v),
                e -> out.print("onError: " + e),
                () -> out.print("onComplete")
        );
    }

    /**
     * 4) combineLatest
     *
     * 功能：
     *  - 任一流变化就组合最新值
     *  - 前提：每个参与组合的流至少都发射过 1 次（否则没有“最新值”）
     *
     * 典型场景：
     *  - 表单输入联动（用户名变化/密码变化任意一个变化就更新按钮可用状态）
     *
     * 常见坑（非常重要）：
     *  1. 如果某个流从未发射过值，combineLatest 也不会发射（因为凑不齐最新值）
     *  2. 变化频率高时会产生大量组合事件（注意性能/去抖节流）
     *
     * 对比：
     *  - combineLatest：任一变化就触发，使用“最新值”
     *  - zip：必须一一配对（次数对齐）
     */
    public void demoCombineLatest(Output out) {
        Observable.combineLatest(
                Observable.just(1, 2),
                Observable.just("A", "B"),
                (i, s) -> i + s
        ).subscribe(
                v -> out.print("onNext: " + v),
                e -> out.print("onError: " + e),
                () -> out.print("onComplete")
        );
    }

    /**
     * 5) withLatestFrom
     *
     * 功能：
     *  - 主流触发时带上其他流最新值
     *  - 只有“主流 onNext”才会触发输出（其他流变化不会触发）
     *
     * 典型场景：
     *  - 点击事件带当前状态（点击提交按钮时，带上最新输入框内容/开关状态）
     *
     * 常见坑（非常重要）：
     *  1. 被“带上”的流如果从未发射过值，主流触发时也无法组合（拿不到 latest）
     *  2. 很多人误以为“其他流变化也会触发”——不会，只有主流触发
     *
     * 对比：
     *  - withLatestFrom：主流驱动（点击->带状态）
     *  - combineLatest：任一变化都驱动（输入联动）
     */
    public void demoWithLatestFrom(Output out) {
        Observable.just(1, 2)
                .withLatestFrom(
                        Observable.just("X"),
                        (i, s) -> i + s
                )
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 6) startWith
     *
     * 功能：
     *  - 在正式数据前插入初始值（或初始流）
     *
     * 典型场景：
     *  - 默认 UI 状态（先发射一个“默认展示模型”，再发射真实数据）
     *
     * 常见坑：
     *  1. startWith 是“插入在最前面”，并不会影响后续流的发射节奏
     *  2. 插入的值同样不能为 null
     *
     * 对比：
     *  - startWith：在前面补一个“初始值/初始流”
     *  - defaultIfEmpty：当上游为空时才补默认值（触发条件不同）
     */
    public void demoStartWith(Output out) {
        Observable.just(2, 3)
                .startWith(1)
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 7) amb
     *
     * 功能：
     *  - 多个流竞争，选择最快响应的那个（第一个发射事件的流获胜）
     *  - 其他流会被取消订阅
     *
     * 典型场景：
     *  - 主备接口策略（谁先返回用谁，另一个取消）
     *
     * 常见坑（非常重要）：
     *  1. “最快”是指最先发射任何事件（包括 onNext/onError/onComplete）
     *  2. 一旦胜出，其他流会被 dispose，注意副作用是否可安全取消
     *
     * 对比：
     *  - amb：竞争择优（只用最快的）
     *  - merge：全部合并（都要处理）
     */
    public void demoAmb(Output out) {
        Observable.ambArray(
                Observable.just("fast"),
                Observable.just("slow")
        ).subscribe(
                v -> out.print("onNext: " + v),
                e -> out.print("onError: " + e),
                () -> out.print("onComplete")
        );
    }
}
