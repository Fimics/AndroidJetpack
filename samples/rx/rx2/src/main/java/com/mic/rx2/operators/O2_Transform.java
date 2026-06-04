package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import io.reactivex.Observable;

/**
 * ===============================
 * 转换类操作符（Transforming Operators）
 *
 * 作用：
 *  - 对数据进行加工、变形
 *  - 改变数据内容或结构
 *
 * 特点：
 *  - 不改变数据流的“节奏”
 *  - 只改变“数据本身”
 * ===============================
 */
public class O2_Transform {

    /**
     * 1) map
     *
     * 功能：
     *  - 将每一个数据项转换成另一个数据项，一进一出
     *
     * 典型场景：
     *  - DTO 转 VO
     *  - 字段提取
     *  - 格式化字符串
     *
     * 常见坑：
     *  - 不要在 map 中写耗时操作（否则阻塞上游线程）
     *  - 不要返回 null（RxJava 不允许 null，会直接 NPE）
     *
     * 对比：
     *  - map：一进一出
     *  - flatMap：一进多出 / 一进一流
     */
    public void demoMap(Output out) {
        Observable.just(1, 2, 3)
                .map(i -> i * 10)
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 2) flatMap
     *
     * 功能：
     *  - 将数据项转换为新的数据流，并把多个流合并输出
     *
     * 典型场景：
     *  - 一个事件触发多个异步请求并汇总结果
     *
     * 常见坑（非常重要）：
     *  1. 不保证顺序（合并输出时可能乱序）
     *  2. 并发过高可能造成资源压力（线程/网络/IO）
     *
     * 对比：
     *  - flatMap：并发、无序
     *  - concatMap：串行、有序
     *  - switchMap：只保留最新（会取消旧流）
     */
    public void demoFlatMap(Output out) {
        Observable.just("A", "B")
                .flatMap(s -> Observable.just(s + "1", s + "2"))
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 3) concatMap
     *
     * 功能：
     *  - 将数据项转换为流并按顺序依次执行（串行、有序）
     *
     * 典型场景：
     *  - 必须保证顺序的请求或写入操作（例如按顺序落库/写文件）
     *
     * 常见坑：
     *  - 串行执行，整体耗时可能比 flatMap 更长
     *
     * 对比：
     *  - concatMap：有序（严格串行）
     *  - flatMap：无序（可并发）
     */
    public void demoConcatMap(Output out) {
        Observable.just("A", "B")
                .concatMap(s -> Observable.just(s + "1", s + "2"))
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 4) switchMap
     *
     * 功能：
     *  - 只处理最新的数据流，新的到来会取消旧的
     *
     * 典型场景：
     *  - 搜索联想
     *  - 输入变化触发请求（只要最后一次输入的结果）
     *
     * 常见坑（非常重要）：
     *  1. 旧流会被取消（旧请求/旧任务可能中途被终止）
     *  2. 不适合必须执行完成的任务（例如必须落库、必须上报）
     *
     * 对比：
     *  - switchMap：只保留最新
     *  - flatMap：全部处理（并发合并）
     *  - concatMap：全部处理（顺序执行）
     */
    public void demoSwitchMap(Output out) {
        Observable.just(1, 2, 3)
                .switchMap(i -> Observable.just("inner " + i))
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 5) scan
     *
     * 功能：
     *  - 按顺序累积数据，并在每一步输出当前结果
     *
     * 典型场景：
     *  - 实时计数
     *  - 状态累加（例如累计积分、累计金额）
     *
     * 常见坑：
     *  - scan 会发射“每一步”的结果，如果下游只需要最终值，应该用 reduce
     *
     * 对比：
     *  - scan：每一步都发射（可做“实时过程”）
     *  - reduce：只发射最终结果（更像“最终汇总”）
     */
    public void demoScan(Output out) {
        Observable.just(1, 2, 3, 4)
                .scan((a, b) -> a + b)
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 6) buffer
     *
     * 功能：
     *  - 将一段时间或数量内的数据收集成集合后再发射
     *
     * 典型场景：
     *  - 批量提交
     *  - 合并上报
     *  - 削峰（把频繁的小事件聚合成批量）
     *
     * 常见坑（非常重要）：
     *  1. buffer 会产生集合对象，数据量大时注意内存
     *  2. 如果上游很快、buffer 很大，下游处理慢会有背压/堆积风险（RxJava2 Observable 无背压）
     *
     * 对比：
     *  - buffer：输出 List（集合）
     *  - window：输出 Observable（子流）
     */
    public void demoBuffer(Output out) {
        Observable.range(1, 7)
                .buffer(3)
                .subscribe(v -> out.print("onNext: " + v));
    }

    /**
     * 7) window
     *
     * 功能：
     *  - 将数据按规则切分成多个子数据流（每个窗口是一个 Observable）
     *
     * 典型场景：
     *  - 按时间窗口分别统计、处理（例如每 5 秒统计一次）
     *
     * 常见坑：
     *  - window 产生“子流”，如果子流不订阅/不消费，可能导致上游资源无法释放或行为不符合预期
     *
     * 对比：
     *  - window：子 Observable（适合“每个窗口独立处理”）
     *  - buffer：List（适合“攒一批一次处理”）
     */
    public void demoWindow(Output out) {
        Observable.range(1, 6)
                .window(2)
                .subscribe(win ->
                        win.subscribe(v -> out.print("window item: " + v))
                );
    }

    /**
     * 8) groupBy
     *
     * 功能：
     *  - 按 key 对数据分组，每组形成独立的数据流
     *
     * 典型场景：
     *  - 按用户、类型、分类分流处理（例如不同类型走不同处理器）
     *
     * 常见坑（非常重要）：
     *  1. groupBy 会产生多个分组流，如果分组流不订阅，会导致上游无法正常流动（看起来像“卡住”）
     *  2. 分组数量不可控时要谨慎（key 太多会产生大量 group，内存/资源压力）
     *
     * 对比：
     *  - groupBy：分组后分别处理（多个子流）
     *  - toMap/collect：聚合成一个容器（一次性汇总）
     */
    public void demoGroupBy(Output out) {
        Observable.just(1, 2, 3, 4)
                .groupBy(i -> i % 2 == 0 ? "even" : "odd")
                .subscribe(group ->
                        group.subscribe(v ->
                                out.print(group.getKey() + ": " + v))
                );
    }

    /**
     * 9) cast / ofType
     *
     * 功能：
     *  - cast：将数据强制转换为指定类型
     *  - ofType：先筛选出指定类型的元素，再进行类型转换（更安全）
     *
     * 典型场景：
     *  - 上游是泛型或混合类型流（Object、基类、接口类型）
     *
     * 常见坑（非常重要）：
     *  1. cast：类型不匹配会直接抛 ClassCastException，导致整个链路 onError
     *  2. ofType：不匹配的元素会被过滤掉，可能导致“看起来少数据”（但这是预期行为）
     *
     * 对比：
     *  - cast：强转（不安全，但直接）
     *  - ofType：过滤 + 转型（安全，推荐用于混合类型流）
     */
    public void demoCastOfType(Output out) {
        Observable.just("A", "B")
                .cast(String.class)
                .subscribe(v -> out.print("cast onNext: " + v));

        Observable.just("A", 1, "B", 2.0)
                .ofType(String.class)
                .subscribe(v -> out.print("ofType(String) onNext: " + v));
    }
}
