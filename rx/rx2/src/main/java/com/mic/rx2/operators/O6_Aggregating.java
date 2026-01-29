package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import java.util.AbstractMap;
import java.util.LinkedHashMap;

import io.reactivex.Observable;

/**
 * ===============================
 * 聚合类操作符（Aggregating Operators）
 *
 * 作用：
 *  - 将多个数据汇总成一个结果（一个值 / 一个集合 / 一个结构）
 *
 * 核心认知：
 *  - 多数聚合操作符需要等待上游 complete 才能输出最终结果
 * ===============================
 */
public class O6_Aggregating {

    /**
     * 1) reduce
     *
     * 功能：
     *  - 将数据逐步累积成一个最终值（只输出一次）
     *
     * 典型场景：
     *  - 求和、汇总
     *
     * 常见坑（非常重要）：
     *  1. 上游必须 complete 才会发射最终值
     *  2. 空流在没有初始值时不会发射任何数据（只会 complete）
     *
     * 对比：
     *  - scan：每一步都发射中间结果
     *  - reduce：只发射最终结果
     */
    public void demoReduce(Output out) {
        Observable.just(1, 2, 3, 4)
                .reduce((a, b) -> a + b)
                .subscribe(
                        v -> out.print("result: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 2) collect
     *
     * 功能：
     *  - 将数据收集到一个自定义容器中
     *
     * 典型场景：
     *  - 批量处理
     *  - 构建复杂结构（StringBuilder / 自定义对象）
     *
     * 常见坑（非常重要）：
     *  1. supplier 必须返回“新容器”，不能复用共享对象
     *  2. 同样需要 complete 才会输出结果
     *
     * 对比：
     *  - toList / toMap：常用快捷方式
     *  - collect：更通用、更灵活
     */
    public void demoCollect(Output out) {
        Observable.just("A", "B", "C")
                .collect(StringBuilder::new, (sb, s) -> sb.append(s))
                .subscribe(v -> out.print("result: " + v.toString()));
    }

    /**
     * 3) count
     *
     * 功能：
     *  - 统计数据条数，输出 Long
     *
     * 典型场景：
     *  - 统计数量
     *  - 埋点计数
     *
     * 常见坑：
     *  - 无限流不会 complete，count 永远不会输出
     */
    public void demoCount(Output out) {
        Observable.just("A", "B", "C")
                .count()
                .subscribe(v -> out.print("count: " + v));
    }

    /**
     * 4) toList
     *
     * 功能：
     *  - 汇总为 List（保持原顺序）
     *
     * 典型场景：
     *  - 需要完整数据集再处理
     *
     * 常见坑（非常重要）：
     *  - 数据量大可能占用大量内存
     *  - 不适合无限流
     */
    public void demoToList(Output out) {
        Observable.range(1, 5)
                .toList()
                .subscribe(v -> out.print("list: " + v));
    }

    /**
     * 5) toMap
     *
     * 功能：
     *  - 汇总为 Map 结构（key -> value）
     *
     * 典型场景：
     *  - 构建索引（例如 id -> item）
     *
     * 常见坑（非常重要）：
     *  1. key 冲突时默认会覆盖旧值
     *  2. 如果需要保留全部冲突值，应使用 toMultimap
     *
     * 对比：
     *  - toMap：key 唯一 / 覆盖
     *  - groupBy：实时分流处理
     */
    public void demoToMap(Output out) {
        Observable.just(
                        new AbstractMap.SimpleEntry<>(1, "A"),
                        new AbstractMap.SimpleEntry<>(2, "B"),
                        new AbstractMap.SimpleEntry<>(1, "A2") // key 冲突示例
                )
                .toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue,
                        LinkedHashMap::new
                )
                .subscribe(map -> out.print("map: " + map));
    }

    /**
     * 6) first
     *
     * 功能：
     *  - 取第一个数据
     *
     * 典型场景：
     *  - 首次命中
     *
     * 常见坑（非常重要）：
     *  - 空流使用 firstOrError 会抛异常
     *  - 推荐使用 first(default) 或 firstElement 做兜底
     *
     * 对比：
     *  - take(1)：仍是 Observable
     *  - first：Single / Maybe，语义更明确
     */
    public void demoFirst(Output out) {
        Observable.just(10, 20, 30)
                .first(999)
                .subscribe(v -> out.print("first: " + v));
    }

    /**
     * 7) last
     *
     * 功能：
     *  - 取最后一个数据
     *
     * 典型场景：
     *  - 最终结果、最终状态
     *
     * 常见坑（非常重要）：
     *  1. 必须等待 complete
     *  2. 无限流 last 永远不会输出
     */
    public void demoLast(Output out) {
        Observable.range(1, 5)
                .last(999)
                .subscribe(v -> out.print("last: " + v));
    }

    /**
     * 8) single
     *
     * 功能：
     *  - 要求上游“恰好只有一个数据”
     *
     * 典型场景：
     *  - 唯一结果校验
     *
     * 常见坑（非常重要）：
     *  1. 0 个数据：singleOrError 会抛异常
     *  2. 多于 1 个数据：直接抛异常
     *
     * 对比：
     *  - first / last：允许多个，只取边界
     *  - single：强约束唯一性
     */
    public void demoSingle(Output out) {
        Observable.just("ONLY_ONE")
                .singleOrError()
                .subscribe(
                        v -> out.print("single: " + v),
                        e -> out.print("onError: " + e)
                );
    }
}
