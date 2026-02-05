package com.mic.rx2.operators;

import com.mic.rx2.core.Output;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * ===============================
 * 创建类操作符（Creation Operators）
 *
 * 作用：
 *  - 决定「数据从哪里来」
 *  - 决定「什么时候开始 / 什么时候结束」
 *
 * 特点：
 *  - 通常是 Rx 链的起点
 *  - 不做数据加工，只负责“生产数据”
 * ===============================
 */
public class O1_Create {

    public void demoSubscribe(Output out) {
        // 创建被观察者
        Observable  observable = Observable.create(new ObservableOnSubscribe<String>() {

            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {

                emitter.onNext("你好呀");
                emitter.onNext("我爱中国");
                emitter.onNext("祝愿祖国繁荣富强");
                emitter.onComplete();
            }
        });

        // 创建观察者
        Observer observer = new Observer<String>(){

            @Override
            public void onSubscribe(Disposable d) {

                out.print( "准备监听");
            }

            @Override
            public void onNext(String s) {

                out.print(s);
            }

            @Override
            public void onError(Throwable e) {

                out.print( "error");
            }

            @Override
            public void onComplete() {

                out.print("监听完毕");
            }
        };

        observable.subscribe(observer);
    }

    /**
     * 1) just
     *
     * 功能：
     *  - 将已经存在的一个或多个值直接转换成数据流，并按顺序立即发射
     *
     * 典型场景：
     *  - 将普通变量、常量快速接入响应式链路，用于示例、测试或默认值
     *
     * 常见坑：
     *  - just(null) 会直接抛 NPE（RxJava 不允许发射 null）
     *
     * 对比：
     *  - just：值已知、立即发射
     *  - defer：订阅时再生成（每次订阅都可能不同）
     */
    public void demoJust(Output out) {
        Observable.just("A", "B", "C")
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 2) from / fromIterable
     *
     * 功能：
     *  - 将数组、集合、可迭代对象拆分为一个个元素并依次发射
     *
     * 典型场景：
     *  - 对列表数据做过滤、映射、统计等流式处理
     *
     * 常见坑：
     *  - 容器不能为 null，元素也不能为 null
     *  - 注意区分：
     *      * 数组：fromArray(...)
     *      * Iterable：fromIterable(...)
     *
     * 对比：
     *  - from*：已有容器拆分
     *  - range：生成连续整数序列（不依赖容器）
     */
    public void demoFrom(Output out) {
        // 数组：fromArray
        Observable.fromArray(10, 20, 30)
                .subscribe(
                        v -> out.print("fromArray onNext: " + v),
                        e -> out.print("fromArray onError: " + e),
                        () -> out.print("fromArray onComplete")
                );

        // 集合/可迭代：fromIterable
        Observable.fromIterable(Arrays.asList(1, 2, 3))
                .subscribe(
                        v -> out.print("fromIterable onNext: " + v),
                        e -> out.print("fromIterable onError: " + e),
                        () -> out.print("fromIterable onComplete")
                );
    }

    /**
     * 3) create
     *
     * 功能：
     *  - 通过手动方式创建数据流，自行控制何时发射数据、完成或报错
     *
     * 典型场景：
     *  - 封装回调型 API、桥接非响应式接口
     *
     * 常见坑（非常重要）：
     *  1. emitter.onNext 之后不能再发射 null
     *  2. onComplete / onError 之后不能再发射任何事件
     *  3. 不处理取消（isDisposed）容易造成内存泄漏
     *
     * 对比：
     *  - create：最灵活，但最危险
     *  - just / from：更安全，优先使用
     */
    public void demoCreate(Output out) {
        Observable.create(emitter -> {
                    out.print("emit 1");
                    emitter.onNext(1);

                    out.print("emit 2");
                    emitter.onNext(2);

                    out.print("complete");
                    emitter.onComplete();
                })
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 4) defer
     *
     * 功能：
     *  - 延迟创建数据流，每次订阅时才真正生成
     *
     * 典型场景：
     *  - 需要在订阅时获取最新时间、配置或状态
     *
     * 常见坑：
     *  - 很多人以为 defer 只执行一次（错误：每次订阅都会重新创建）
     *
     * 对比：
     *  - just：定义时就确定
     *  - defer：订阅时才确定
     */
    public void demoDefer(Output out) {
        Observable<Integer> observable = Observable.defer(() -> {
            out.print("create observable");
            return Observable.just((int) (Math.random() * 100));
        });

        observable.subscribe(v -> out.print("first: " + v));
        observable.subscribe(v -> out.print("second: " + v));
    }

    /**
     * 5) range
     *
     * 功能：
     *  - 按指定起点和数量生成连续整数序列
     *
     * 典型场景：
     *  - 生成索引、模拟数据、驱动循环任务
     *
     * 常见坑：
     *  - 第二个参数是 count，不是 end
     *    range(1,5) → 1~5 共 5 个
     */
    public void demoRange(Output out) {
        Observable.range(1, 5)
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 6) interval
     *
     * 功能：
     *  - 按固定时间间隔不断发射递增数字
     *
     * 典型场景：
     *  - 心跳检测、定时轮询、周期刷新
     *
     * 常见坑：
     *  - 默认是无限流（不 complete），一定要配合 take / dispose / takeUntil
     *
     * 注意：
     *  - Demo 中使用 blockingSubscribe 方便立即看到输出
     */
    public void demoInterval(Output out) {
        Observable.interval(300, TimeUnit.MILLISECONDS)
                .take(5)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 7) timer
     *
     * 功能：
     *  - 在延迟一段时间后发射一次数据
     *
     * 典型场景：
     *  - 延迟执行、超时触发、首帧延迟加载
     *
     * 对比：
     *  - timer：只发一次
     *  - interval：周期性发射
     */
    public void demoTimer(Output out) {
        Observable.timer(500, TimeUnit.MILLISECONDS)
                .blockingSubscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 8) empty
     *
     * 功能：
     *  - 不发射任何数据，直接结束
     *
     * 典型场景：
     *  - 条件分支中表示“什么都不做但正常结束”
     *
     * 注意：
     *  - 会触发 onComplete
     */
    public void demoEmpty(Output out) {
        Observable.empty()
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 9) never
     *
     * 功能：
     *  - 不发射数据，也永不结束
     *
     * 典型场景：
     *  - 测试、占位，配合取消或超时操作符使用
     *
     * 常见坑：
     *  - 如果不配合 timeout / dispose，会永远挂着
     */
    public void demoNever(Output out) {
        Observable.never()
                .timeout(500, TimeUnit.MILLISECONDS)
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e),
                        () -> out.print("onComplete")
                );
    }

    /**
     * 10) error
     *
     * 功能：
     *  - 不发射数据，直接以错误结束
     *
     * 典型场景：
     *  - 参数校验失败、前置条件不满足时快速失败
     *
     * 注意：
     *  - 不会调用 onNext / onComplete
     */
    public void demoError(Output out) {
        Observable.error(new RuntimeException("something wrong"))
                .subscribe(
                        v -> out.print("onNext: " + v),
                        e -> out.print("onError: " + e.getMessage()),
                        () -> out.print("onComplete")
                );
    }
}
