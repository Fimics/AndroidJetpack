# Reactive Operators Handbook (Table-Free, Practice-Oriented)
> 面向工程实践的响应式操作符速查与讲解，覆盖 RxJava / RxJS / Reactor / Kotlin Flow 的常见语义。本文以 **RxJava 3** 为主要演示代码载体；少量仅在 Reactor 更常见的操作符（如 `publishOn`、`doOnCancel`）会给出 Reactor 示例。

## How to read / run the demos
- All RxJava snippets assume these imports: `io.reactivex.rxjava3.core.*`, `io.reactivex.rxjava3.schedulers.Schedulers`, `java.util.concurrent.TimeUnit`, and common Java utils (List/Map).
- For time-based operators (`interval`, `timer`, `debounce`, etc.), examples usually end with `take(n)` + `blockingSubscribe(...)` (or `blockingAwait`) so a demo can exit naturally.
- Operator names differ slightly across libraries: RxJS `just`≈`of`, RxJava `flatMap`≈RxJS `mergeMap`, Reactor `publishOn`≈RxJava `observeOn` (概念相近但实现细节不同)。

## 2.1 创建类操作符 (Creation Operators)
- 用于“生成数据流本身”：数据从哪里来、何时开始、何时结束/报错。
- 通常位于链路最前端；掌握它们可以更自然地把回调、集合、定时任务接入响应式链路。

### just
**做什么：** 把一个或多个已存在的值直接转换成数据流，按顺序立即发射并完成。
**典型场景：** 把普通变量/常量快速接入链路（示例、测试、默认值）。
**关键要点 / 记忆法：**
- 在 RxJava 里通常是 `Observable.just(...)` / `Single.just(...)`。
- 对比 `fromIterable`：`just(list)` 会发射“一个 List”，`fromIterable(list)` 会发射“List 里的每个元素”。
**注意点 / 常见坑：**
- RxJava / Reactor 通常不允许发射 null（会直接 NPE）；需要表示“无值”时用 Optional/包装对象或 empty/switchIfEmpty。
**跨库对照：** RxJS: `of(...)`  | Reactor: `Flux.just(...)`  | Kotlin Flow: `flowOf(...)`
**演示代码：**

```java
Observable.just("A", "B", "C")
    .subscribe(
        v -> System.out.println("onNext: " + v),
        e -> System.out.println("onError: " + e),
        () -> System.out.println("onComplete")
    );
```

### from / fromIterable
**做什么：** 把数组、集合、可迭代对象拆分为元素序列并依次发射。
**典型场景：** 对列表数据做过滤、映射、聚合等流式处理。
**关键要点 / 记忆法：**
- 常见坑：不要把“集合本身”当作元素发射——用 `fromIterable(list)`。
- 对比：`just(list)` 发射的是 1 个元素（List）；`fromIterable(list)` 发射的是 List 内多个元素。
**注意点 / 常见坑：**
- 如果迭代期间集合被并发修改，可能触发 ConcurrentModificationException；尽量用不可变集合或拷贝一份再发射。
**跨库对照：** RxJS: `from(iterable)`  | Reactor: `Flux.fromIterable(...)`  | Kotlin Flow: `nums.asFlow()`
**演示代码：**

```java
List<Integer> nums = Arrays.asList(1, 2, 3);

Observable.fromIterable(nums)
    .map(n -> n * 10)
    .blockingSubscribe(v -> System.out.println("onNext: " + v));
```

### create
**做什么：** 通过手动方式创建数据流，自行控制何时发射数据、完成或报错。
**典型场景：** 封装回调型 API、桥接非响应式接口。
**关键要点 / 记忆法：**
- 务必处理取消订阅：`emitter.isDisposed()` 为 true 时停止生产数据。
- 发射规则：完成/报错后不要再 `onNext`；不要重复 `onComplete/onError`。
**注意点 / 常见坑：**
- Observable.create 不具备背压语义；高频/大流量场景优先用 Flowable.create(+BackpressureStrategy) 或自行限速。
**跨库对照：** RxJS: `new Observable(sub => ...)`  | Reactor: `Flux.create(...)`  | Kotlin Flow: `callbackFlow { ... }`
**演示代码：**

```java
Observable<String> obs = Observable.create(emitter -> {
    try {
        emitter.onNext("callback-1");
        emitter.onNext("callback-2");
        emitter.onComplete();
    } catch (Throwable t) {
        emitter.onError(t);
    }
});

obs.subscribe(System.out::println, Throwable::printStackTrace);
```

### defer
**做什么：** 延迟创建数据流：每次订阅时才真正生成（订阅时取最新值/状态）。
**典型场景：** 需要在订阅时获取最新时间、配置或状态。
**关键要点 / 记忆法：**
- 对比 `just(System.currentTimeMillis())`：后者在声明时取值；`defer` 在订阅时取值。
- 常用于避免共享旧状态（例如缓存了过期时间戳/配置）。
**注意点 / 常见坑：**
- 创建类操作符常见坑是“生命周期与资源释放”：冷流每次订阅会重跑；定时/占位流要确保可取消并在合适时机结束。
**跨库对照：** RxJS: `defer(() => ...)`  | Reactor: `Flux.defer(...)`  | Flow: `flow { ... }`（每次 collect 重新执行）
**演示代码：**

```java
Observable<Long> now = Observable.defer(() -> Observable.just(System.currentTimeMillis()));

now.subscribe(v -> System.out.println("t1=" + v));
Thread.sleep(50);
now.subscribe(v -> System.out.println("t2=" + v));
```

### range
**做什么：** 按指定起点和数量生成连续整数序列并发射。
**典型场景：** 生成索引、模拟数据、驱动循环任务。
**关键要点 / 记忆法：**
- range 是最轻量的“生成器”之一；常用来驱动批处理或生成分页页码。
**注意点 / 常见坑：**
- 创建类操作符常见坑是“生命周期与资源释放”：冷流每次订阅会重跑；定时/占位流要确保可取消并在合适时机结束。
**跨库对照：** RxJS: `range(start, count)`  | Reactor: `Flux.range(start, count)`
**演示代码：**

```java
Observable.range(3, 5) // 3..7
    .blockingSubscribe(System.out::println);
```

### interval
**做什么：** 按固定时间间隔不断发射递增数字（0,1,2...）。
**典型场景：** 心跳检测、定时轮询、周期刷新。
**关键要点 / 记忆法：**
- interval 通常是无限流：务必用 `take(n)` / `takeUntil(...)` / 生命周期绑定来结束。
- 时间流对测试不友好：工程里常配 TestScheduler/虚拟时间（不同库方案不同）。
**注意点 / 常见坑：**
- 创建类操作符常见坑是“生命周期与资源释放”：冷流每次订阅会重跑；定时/占位流要确保可取消并在合适时机结束。
**跨库对照：** RxJS: `interval(ms)`  | Reactor: `Flux.interval(Duration...)`
**演示代码：**

```java
Observable.interval(300, TimeUnit.MILLISECONDS)
    .take(5)
    .blockingSubscribe(v -> System.out.println("tick=" + v));
```

### timer
**做什么：** 在延迟一段时间后发射一次数据（单次触发）。
**典型场景：** 延迟执行、超时触发、首帧延迟加载。
**关键要点 / 记忆法：**
- 对比 `interval`：timer 更像“一次性闹钟”。
**注意点 / 常见坑：**
- 创建类操作符常见坑是“生命周期与资源释放”：冷流每次订阅会重跑；定时/占位流要确保可取消并在合适时机结束。
**跨库对照：** RxJS: `timer(dueTime)`  | Reactor: `Mono.delay(...)`
**演示代码：**

```java
Observable.timer(500, TimeUnit.MILLISECONDS)
    .map(x -> "boom")
    .blockingSubscribe(System.out::println);
```

### empty
**做什么：** 不发射任何数据，直接结束（完成）。
**典型场景：** 条件分支中表示“什么都不做但正常结束”。
**关键要点 / 记忆法：**
- 常配 `defaultIfEmpty/switchIfEmpty` 做兜底。
**注意点 / 常见坑：**
- 创建类操作符常见坑是“生命周期与资源释放”：冷流每次订阅会重跑；定时/占位流要确保可取消并在合适时机结束。
**跨库对照：** RxJS: `EMPTY`  | Reactor: `Mono.empty()` / `Flux.empty()`  | Flow: `emptyFlow()`
**演示代码：**

```java
Observable.<String>empty()
    .doOnComplete(() -> System.out.println("completed"))
    .subscribe(System.out::println);
```

### never
**做什么：** 不发射数据，也永不结束（除非被取消/超时）。
**典型场景：** 测试、占位，配合取消或超时操作符使用。
**关键要点 / 记忆法：**
- 不要直接 `blockingSubscribe()`；要配 `timeout/takeUntil` 等让它可结束。
**注意点 / 常见坑：**
- 创建类操作符常见坑是“生命周期与资源释放”：冷流每次订阅会重跑；定时/占位流要确保可取消并在合适时机结束。
**跨库对照：** RxJS: `NEVER`  | Reactor: `Flux.never()`
**演示代码：**

```java
Observable.never()
    .timeout(300, TimeUnit.MILLISECONDS)
    .onErrorReturnItem("timeout -> fallback")
    .blockingSubscribe(System.out::println);
```

### error
**做什么：** 不发射数据，直接以错误结束。
**典型场景：** 参数校验失败、前置条件不满足时快速失败。
**关键要点 / 记忆法：**
- 错误会短路：下游只会收到 `onError`，不会再收到 `onComplete`。
**注意点 / 常见坑：**
- 创建类操作符常见坑是“生命周期与资源释放”：冷流每次订阅会重跑；定时/占位流要确保可取消并在合适时机结束。
**跨库对照：** RxJS: `throwError(() => err)`  | Reactor: `Mono.error(err)` / `Flux.error(err)`
**演示代码：**

```java
Observable.error(new IllegalArgumentException("bad input"))
    .subscribe(
        v -> System.out.println("onNext: " + v),
        e -> System.out.println("onError: " + e.getMessage())
    );
```

## 2.2 转换类操作符 (Transforming Operators)
- 对数据做加工，把“原始数据”变成“目标数据”，或改变数据结构形态。
- 最常用：`map`、`flatMap/concatMap/switchMap`，其次是 `scan/buffer/window/groupBy`。

### map
**做什么：** 将每一个数据项转换成另一个数据项（一进一出）。
**典型场景：** DTO→VO、字段提取、格式化字符串。
**关键要点 / 记忆法：**
- map 里尽量只做纯转换；副作用请用 `doOnNext`。
**注意点 / 常见坑：**
- 转换类操作符常见坑是“异常与时序”：mapper/inner 流抛错会直接 onError；并发类要关注乱序与取消。
**跨库对照：** RxJS/Reactor/Flow: `map`
**演示代码：**

```java
Observable.just("  alice ", "bob ")
    .map(String::trim)
    .map(String::toUpperCase)
    .blockingSubscribe(System.out::println); // ALICE, BOB
```

### flatMap
**做什么：** 将数据项转换为新的数据流，并把多个子流合并输出（通常并发）。
**典型场景：** 一个事件触发多个异步请求并汇总结果。
**关键要点 / 记忆法：**
- 并发会带来乱序；如果要保序，用 `concatMap`。
- 子任务可在内部 `subscribeOn(IO)`，让并发真正发生在 IO 线程。
**注意点 / 常见坑：**
- 默认并发可能造成请求风暴/乱序与内存上涨；需要时限制并发（maxConcurrency）或改用 concatMap。
**跨库对照：** RxJS: `mergeMap`  | Reactor: `flatMap`  | Flow: `flatMapMerge`
**演示代码：**

```java
Observable.just(1, 2, 3)
    .flatMap(i ->
        Observable.just("task-" + i)
            .delay(4 - i, TimeUnit.MILLISECONDS) // 不同延迟制造乱序
    )
    .blockingSubscribe(System.out::println);
```

### concatMap
**做什么：** 将数据项转换为子流并按顺序依次执行（串行，保证顺序）。
**典型场景：** 必须保证顺序的请求或写入操作。
**关键要点 / 记忆法：**
- 吞吐会下降，但时序确定性最好；对写操作/有依赖的步骤很重要。
**注意点 / 常见坑：**
- 转换类操作符常见坑是“异常与时序”：mapper/inner 流抛错会直接 onError；并发类要关注乱序与取消。
**跨库对照：** RxJS: `concatMap`  | Reactor: `concatMap`  | Flow: `flatMapConcat`
**演示代码：**

```java
Observable.just(1, 2, 3)
    .concatMap(i ->
        Observable.just("task-" + i)
            .delay(4 - i, TimeUnit.MILLISECONDS)
    )
    .blockingSubscribe(System.out::println); // 一定 task-1,2,3
```

### switchMap
**做什么：** 只处理最新的子流：新的到来会取消旧的。
**典型场景：** 搜索联想、输入变化触发请求（只要最后一次结果）。
**关键要点 / 记忆法：**
- 关键价值在“取消旧请求”，避免旧结果回写新 UI。
- 上游频繁变化会频繁取消；注意资源释放与副作用。
**注意点 / 常见坑：**
- 旧流会被取消，但底层任务未必真正可取消（取决于你的异步实现）；确保取消能终止工作，避免旧结果回写。
**跨库对照：** RxJS: `switchMap`  | Reactor: `switchMap`  | Flow: `flatMapLatest`
**演示代码：**

```java
Observable.interval(0, 200, TimeUnit.MILLISECONDS)
    .take(4) // 0,1,2,3
    .switchMap(i ->
        Observable.interval(0, 80, TimeUnit.MILLISECONDS)
            .map(x -> "req" + i + "-chunk" + x)
            .take(5)
    )
    .blockingSubscribe(System.out::println);
```

### scan
**做什么：** 按顺序累积数据，并在每一步输出当前结果（增量 reduce）。
**典型场景：** 实时计数、状态累加。
**关键要点 / 记忆法：**
- reduce 只输出最终值；scan 每步都输出，适合实时 UI。
**注意点 / 常见坑：**
- 转换类操作符常见坑是“异常与时序”：mapper/inner 流抛错会直接 onError；并发类要关注乱序与取消。
**跨库对照：** RxJS/Reactor/Flow: `scan`
**演示代码：**

```java
Observable.just(1, 2, 3, 4)
    .scan(0, Integer::sum)
    .blockingSubscribe(System.out::println); // 0,1,3,6,10
```

### buffer
**做什么：** 将一段时间或数量内的数据收集成集合后再发射。
**典型场景：** 批量提交、合并上报、削峰。
**关键要点 / 记忆法：**
- buffer 会暂存数据：窗口太大可能带来内存压力。
**注意点 / 常见坑：**
- buffer 会暂存数据：窗口过大或上游突发会导致内存压力；大对象/长窗口要谨慎，必要时分批处理。
**跨库对照：** RxJS: `bufferCount/bufferTime`  | Reactor: `buffer`
**演示代码：**

```java
Observable.range(1, 10)
    .buffer(3)
    .blockingSubscribe(batch -> System.out.println("batch=" + batch));
```

### window
**做什么：** 将数据按规则切分成多个子数据流（每个窗口是一个流）。
**典型场景：** 按时间窗口分别统计、处理。
**关键要点 / 记忆法：**
- window 更适合“窗口内继续链式处理”；想直接得到 List 用 buffer。
**注意点 / 常见坑：**
- window 产生“流的流”；务必消费每个窗口（flatMap/subscribe），否则窗口内数据可能堆积或被丢弃。
**跨库对照：** RxJS: `windowCount/windowTime`  | Reactor: `window`
**演示代码：**

```java
Observable.range(1, 10)
    .window(4)
    .flatMapSingle(w -> w.toList())
    .blockingSubscribe(win -> System.out.println("window=" + win));
```

### groupBy
**做什么：** 按 key 对数据分组，每组形成独立的数据流。
**典型场景：** 按用户/类型/分类分流处理。
**关键要点 / 记忆法：**
- 组数可能很大：无限流 + 高基数 key 会导致资源增长。
- 常见模式：对每个 group 做 `toList/reduce` 再合并输出。
**注意点 / 常见坑：**
- 高基数 key 会生成大量 group，容易内存/句柄增长；建议限制 key 数量、对 group 做回收/超时或先做预聚合。
**跨库对照：** RxJS/Reactor: `groupBy`（RxJS 需配合 mergeMap 等）
**演示代码：**

```java
Observable.just("u1:pay", "u2:buy", "u1:refund", "u2:pay")
    .groupBy(s -> s.substring(0, 2)) // u1 / u2
    .flatMapSingle(g ->
        g.toList().map(list -> g.getKey() + " -> " + list)
    )
    .blockingSubscribe(System.out::println);
```

### cast / ofType
**做什么：** 将数据转换或筛选为指定类型：`cast` 强转；`ofType` 过滤并强转。
**典型场景：** 上游是泛型或混合类型流，需要只处理某种类型。
**关键要点 / 记忆法：**
- `cast` 不匹配会报错；更安全用 `ofType`。
**注意点 / 常见坑：**
- 转换类操作符常见坑是“异常与时序”：mapper/inner 流抛错会直接 onError；并发类要关注乱序与取消。
**跨库对照：** Flow: `filterIsInstance<T>()`  | Reactor: `cast/ofType`
**演示代码：**

```java
Observable<Object> mixed = Observable.just("a", 1, "b", 2L);

mixed.ofType(String.class)
    .blockingSubscribe(v -> System.out.println("str=" + v));

mixed.cast(String.class)
    .subscribe(
        v -> System.out.println("cast=" + v),
        e -> System.out.println("cast error: " + e)
    );
```

## 2.3 过滤类操作符 (Filtering Operators)
- 控制哪些数据继续向下游流动，以及控制数据数量和频率。
- 典型用途：去噪、限量、去重、防抖、限频。

### filter
**做什么：** 按条件判断，满足条件的数据才会被发射。
**典型场景：** 剔除空值、无效值。
**关键要点 / 记忆法：**
- 复杂条件可抽 predicate 方法，增强可读性。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS/Reactor/Flow: `filter`
**演示代码：**

```java
Observable.just(0, 1, 2, 3, 4, 5)
    .filter(x -> x % 2 == 1)
    .blockingSubscribe(System.out::println); // 1,3,5
```

### take
**做什么：** 只取前 N 个数据，其余忽略并结束。
**典型场景：** 只关心第一个结果/前几个结果；让无限流可结束。
**关键要点 / 记忆法：**
- take 达到 N 后会取消上游（需要上游正确响应取消）。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS/Reactor/Flow: `take`
**演示代码：**

```java
Observable.interval(100, TimeUnit.MILLISECONDS)
    .take(3)
    .blockingSubscribe(v -> System.out.println("take=" + v)); // 0,1,2
```

### takeLast
**做什么：** 只保留最后 N 个数据（通常等待上游完成）。
**典型场景：** 只关心最终状态/最后几条记录。
**关键要点 / 记忆法：**
- 对无限流不适用（永远等不到完成）。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS/Reactor: `takeLast`
**演示代码：**

```java
Observable.range(1, 10)
    .takeLast(3)
    .blockingSubscribe(System.out::println); // 8,9,10
```

### skip
**做什么：** 跳过前 N 个数据。
**典型场景：** 忽略初始化阶段噪声。
**关键要点 / 记忆法：**
- 对无限流很常用：例如跳过首个默认状态。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS/Reactor/Flow: `skip`
**演示代码：**

```java
Observable.range(1, 5)
    .skip(2)
    .blockingSubscribe(System.out::println); // 3,4,5
```

### skipLast
**做什么：** 跳过最后 N 个数据（通常等待上游完成）。
**典型场景：** 忽略收尾标记数据。
**关键要点 / 记忆法：**
- 对无限流不适用。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS/Reactor: `skipLast`
**演示代码：**

```java
Observable.range(1, 5)
    .skipLast(2)
    .blockingSubscribe(System.out::println); // 1,2,3
```

### distinct
**做什么：** 对全部数据进行去重（已出现过的不再发射）。
**典型场景：** 去重 ID、去重请求。
**关键要点 / 记忆法：**
- 需要记忆历史值：无限流可能导致内存增长；必要时用 distinctUntilChanged 或限制窗口。
**注意点 / 常见坑：**
- distinct 会记住所有历史值；长时间运行的流可能无限增长，必要时改用 distinctUntilChanged 或按窗口去重。
**跨库对照：** RxJS/Reactor/Flow: `distinct`
**演示代码：**

```java
Observable.just(1, 1, 2, 2, 1, 3)
    .distinct()
    .blockingSubscribe(System.out::println); // 1,2,3
```

### distinctUntilChanged
**做什么：** 仅去掉连续重复数据。
**典型场景：** 状态变化监听。
**关键要点 / 记忆法：**
- 常配合 `map` 提取关键字段后再 distinctUntilChanged。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS/Reactor/Flow: `distinctUntilChanged`
**演示代码：**

```java
Observable.just("A", "A", "B", "B", "A")
    .distinctUntilChanged()
    .blockingSubscribe(System.out::println); // A,B,A
```

### debounce
**做什么：** 防抖：一段时间内只发射最后一次数据（期间新数据会重置计时）。
**典型场景：** 输入框防抖、搜索联想。
**关键要点 / 记忆法：**
- debounce 偏“停下来再发”；throttle 偏“按频率发”。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS: `debounceTime`  | Reactor: `debounce`
**演示代码：**

```java
Observable<String> typing = Observable.create(emitter -> {
    emitter.onNext("h");
    Thread.sleep(80);
    emitter.onNext("he");
    Thread.sleep(80);
    emitter.onNext("hel");
    Thread.sleep(250); // 停顿足够久，触发一次发射
    emitter.onNext("hell");
    Thread.sleep(250);
    emitter.onComplete();
});

typing.debounce(200, TimeUnit.MILLISECONDS)
    .blockingSubscribe(System.out::println); // hel, hell
```

### throttle / sample
**做什么：** 限频：按时间间隔限制发射频率（throttleFirst/throttleLatest）或定期采样最新值（sample）。
**典型场景：** 按钮点击限频、滚动事件降采样。
**关键要点 / 记忆法：**
- RxJava 常用 `throttleFirst(window)` 或 `sample(period)`。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS: `throttleTime` / `sampleTime`  | Reactor: `sample`
**演示代码：**

```java
Observable<Long> src = Observable.interval(50, TimeUnit.MILLISECONDS).take(20);

// 窗口内取第一个
src.throttleFirst(200, TimeUnit.MILLISECONDS)
   .blockingSubscribe(v -> System.out.println("throttleFirst=" + v));

// 周期采样最新
Observable.interval(50, TimeUnit.MILLISECONDS).take(20)
   .sample(200, TimeUnit.MILLISECONDS)
   .blockingSubscribe(v -> System.out.println("sample=" + v));
```

### ignoreElements
**做什么：** 忽略所有数据，只关心完成或错误（把流当作“任务”）。
**典型场景：** 写入/上报等只关心任务结束的场景。
**关键要点 / 记忆法：**
- RxJava 中会把 Observable 转为 Completable。
**注意点 / 常见坑：**
- 过滤类操作符常见坑是“丢数据与缓存”：take/skip 会取消/丢弃；distinct/takeLast/skipLast 可能缓存导致内存压力。
**跨库对照：** RxJS: `ignoreElements`  | Reactor: `then()`
**演示代码：**

```java
Observable.range(1, 3)
    .doOnNext(v -> System.out.println("work=" + v))
    .ignoreElements()
    .blockingAwait();

System.out.println("done");
```

## 2.4 组合类操作符 (Combining Operators)
- 把多条数据流合并、对齐或协作，解决多源数据问题。
- 要点：并发/顺序、对齐策略（zip vs combineLatest）、主次关系（withLatestFrom）。

### merge
**做什么：** 并发合并多个数据流，先到先发（不保证顺序）。
**典型场景：** 多种事件源统一处理。
**关键要点 / 记忆法：**
- 需要顺序时用 `concat`。
**注意点 / 常见坑：**
- 组合类操作符常见坑是“对齐策略”：zip 受最短流限制且会缓冲；combineLatest/withLatestFrom 需要各流至少发射一次才能产出。
**跨库对照：** RxJS: `merge`  | Reactor: `Flux.merge`
**演示代码：**

```java
Observable<String> a = Observable.interval(80, TimeUnit.MILLISECONDS).take(3).map(i -> "A" + i);
Observable<String> b = Observable.interval(50, TimeUnit.MILLISECONDS).take(3).map(i -> "B" + i);

Observable.merge(a, b)
    .blockingSubscribe(System.out::println); // 交错输出
```

### concat
**做什么：** 顺序拼接多个数据流：前一个完成后才订阅下一个。
**典型场景：** 按阶段执行任务（先缓存后网络）。
**关键要点 / 记忆法：**
- concat 不并发，顺序确定；merge 并发但乱序。
**注意点 / 常见坑：**
- 组合类操作符常见坑是“对齐策略”：zip 受最短流限制且会缓冲；combineLatest/withLatestFrom 需要各流至少发射一次才能产出。
**跨库对照：** RxJS: `concat`  | Reactor: `concatWith`
**演示代码：**

```java
Observable.concat(
        Observable.just("cache-1", "cache-2"),
        Observable.just("net-1", "net-2")
    )
    .blockingSubscribe(System.out::println);
```

### zip
**做什么：** 多个流按顺序一一配对后发射（都到齐才输出）。
**典型场景：** 并行请求结果合并。
**关键要点 / 记忆法：**
- zip 受最短流限制：任一流先完成且无法配对时，整体结束。
**注意点 / 常见坑：**
- 组合类操作符常见坑是“对齐策略”：zip 受最短流限制且会缓冲；combineLatest/withLatestFrom 需要各流至少发射一次才能产出。
**跨库对照：** RxJS/Reactor/Flow: `zip`
**演示代码：**

```java
Observable<Integer> ids = Observable.just(1, 2, 3);
Observable<String> names = Observable.just("A", "B", "C");

Observable.zip(ids, names, (id, name) -> id + "-" + name)
    .blockingSubscribe(System.out::println); // 1-A, 2-B, 3-C
```

### combineLatest
**做什么：** 任一流变化就组合各流最新值并发射（需要每条流至少发射过一次）。
**典型场景：** 表单输入联动、依赖多状态的 UI 计算。
**关键要点 / 记忆法：**
- combineLatest 是“状态驱动”，zip 是“步进对齐”。
**注意点 / 常见坑：**
- combineLatest 在每条流至少发射一次前不会输出；若需要初始组合值，给各流 startWith 一个默认值。
**跨库对照：** RxJS/Reactor: `combineLatest`  | Flow: `combine`
**演示代码：**

```java
Observable<String> user = Observable.just("u1").delay(120, TimeUnit.MILLISECONDS);
Observable<Integer> page = Observable.interval(50, TimeUnit.MILLISECONDS).map(i -> (int)(i+1)).take(3);

Observable.combineLatest(user, page, (u, p) -> u + "@p" + p)
    .blockingSubscribe(System.out::println);
```

### withLatestFrom
**做什么：** 主流触发时带上其他流最新值一起输出（其他流变化不触发输出）。
**典型场景：** 点击事件带当前状态。
**关键要点 / 记忆法：**
- 主流是触发器；辅流只是提供最新状态。
**注意点 / 常见坑：**
- 副流在发射过至少一次前，主流触发可能被丢掉；常用做法是给副流 startWith 初始状态。
**跨库对照：** RxJS/Reactor: `withLatestFrom`
**演示代码：**

```java
Observable<Long> clicks = Observable.interval(120, TimeUnit.MILLISECONDS).take(4);
Observable<String> state = Observable.interval(70, TimeUnit.MILLISECONDS).map(i -> "S" + i).take(10);

clicks.withLatestFrom(state, (c, s) -> "click=" + c + " state=" + s)
    .blockingSubscribe(System.out::println);
```

### startWith
**做什么：** 在正式数据前插入初始值。
**典型场景：** 默认 UI 状态、占位数据。
**关键要点 / 记忆法：**
- startWith 无条件插入；defaultIfEmpty 仅在空流时插入。
**注意点 / 常见坑：**
- 组合类操作符常见坑是“对齐策略”：zip 受最短流限制且会缓冲；combineLatest/withLatestFrom 需要各流至少发射一次才能产出。
**跨库对照：** RxJS/Reactor/Flow: `startWith`
**演示代码：**

```java
Observable.just("real-1", "real-2")
    .startWithItem("init")
    .blockingSubscribe(System.out::println); // init, real-1, real-2
```

### amb
**做什么：** 多个流竞争，选择最快响应的那条流作为输出，取消其他流。
**典型场景：** 主备接口策略、抢最快结果。
**关键要点 / 记忆法：**
- 胜者确定后其余会被取消：确保上游能响应取消。
**注意点 / 常见坑：**
- 组合类操作符常见坑是“对齐策略”：zip 受最短流限制且会缓冲；combineLatest/withLatestFrom 需要各流至少发射一次才能产出。
**跨库对照：** RxJS: `race`
**演示代码：**

```java
Observable<String> fast = Observable.timer(80, TimeUnit.MILLISECONDS).map(x -> "FAST");
Observable<String> slow = Observable.timer(200, TimeUnit.MILLISECONDS).map(x -> "SLOW");

Observable.ambArray(slow, fast)
    .blockingSubscribe(System.out::println); // FAST
```

## 2.5 条件与布尔类操作符 (Conditional / Boolean)
- 用于条件判断、空数据处理以及流程控制。
- 常见于校验、兜底、生命周期管理。

### all
**做什么：** 判断是否所有数据都满足条件，返回布尔结果。
**典型场景：** 批量校验：例如所有字段都合法才提交。
**关键要点 / 记忆法：**
- RxJava 返回 `Single<Boolean>`。
**注意点 / 常见坑：**
- 条件/布尔类操作符常见坑是“短路与完成依赖”：all/any/contains 会短路并取消上游；defaultIfEmpty/switchIfEmpty 依赖上游完成。
**跨库对照：** RxJS: `every`  | Reactor/Flow: `all`
**演示代码：**

```java
boolean ok = Observable.just(2, 4, 6)
    .all(x -> x % 2 == 0)
    .blockingGet();

System.out.println(ok); // true
```

### any
**做什么：** 判断是否存在满足条件的数据，返回布尔结果。
**典型场景：** 错误检测：是否出现非法值。
**关键要点 / 记忆法：**
- RxJava 返回 `Single<Boolean>`。
**注意点 / 常见坑：**
- 条件/布尔类操作符常见坑是“短路与完成依赖”：all/any/contains 会短路并取消上游；defaultIfEmpty/switchIfEmpty 依赖上游完成。
**跨库对照：** RxJS: `some`  | Reactor/Flow: `any`
**演示代码：**

```java
boolean hasEven = Observable.just(1, 3, 5, 6)
    .any(x -> x % 2 == 0)
    .blockingGet();

System.out.println(hasEven); // true
```

### contains
**做什么：** 判断是否出现过指定值（基于 equals）。
**典型场景：** 事件是否发生（是否出现过某个状态码/值）。
**关键要点 / 记忆法：**
- 自定义对象记得实现 equals/hashCode。
**注意点 / 常见坑：**
- 条件/布尔类操作符常见坑是“短路与完成依赖”：all/any/contains 会短路并取消上游；defaultIfEmpty/switchIfEmpty 依赖上游完成。
**跨库对照：** Reactor: `hasElement`(按值通常要额外处理)
**演示代码：**

```java
boolean seen = Observable.just("A", "B", "C")
    .contains("B")
    .blockingGet();

System.out.println(seen); // true
```

### isEmpty
**做什么：** 判断流是否没有任何数据（空则 true）。
**典型场景：** 搜索结果为空、缓存未命中检测。
**关键要点 / 记忆法：**
- RxJava 返回 `Single<Boolean>`。
**注意点 / 常见坑：**
- 条件/布尔类操作符常见坑是“短路与完成依赖”：all/any/contains 会短路并取消上游；defaultIfEmpty/switchIfEmpty 依赖上游完成。
**跨库对照：** RxJS: `isEmpty`  | Reactor: `hasElements`(取反)
**演示代码：**

```java
boolean empty = Observable.<String>empty()
    .isEmpty()
    .blockingGet();

System.out.println(empty); // true
```

### defaultIfEmpty
**做什么：** 空流时返回一个默认值。
**典型场景：** 空页面兜底。
**关键要点 / 记忆法：**
- 仅在“完全无数据”时生效。
**注意点 / 常见坑：**
- 条件/布尔类操作符常见坑是“短路与完成依赖”：all/any/contains 会短路并取消上游；defaultIfEmpty/switchIfEmpty 依赖上游完成。
**跨库对照：** RxJS/Reactor: `defaultIfEmpty`  | Flow: `ifEmpty { emit(...) }`
**演示代码：**

```java
Observable.<String>empty()
    .defaultIfEmpty("DEFAULT")
    .blockingSubscribe(System.out::println); // DEFAULT
```

### switchIfEmpty
**做什么：** 空流时切换到另一条流。
**典型场景：** 缓存失败走网络。
**关键要点 / 记忆法：**
- 区别于 defaultIfEmpty：switchIfEmpty 能执行复杂备用逻辑/异步流。
**注意点 / 常见坑：**
- 条件/布尔类操作符常见坑是“短路与完成依赖”：all/any/contains 会短路并取消上游；defaultIfEmpty/switchIfEmpty 依赖上游完成。
**跨库对照：** RxJS/Reactor: `switchIfEmpty`  | Flow: `ifEmpty { emitAll(...) }`
**演示代码：**

```java
Observable.<String>empty()
    .switchIfEmpty(Observable.just("fallback-1", "fallback-2"))
    .blockingSubscribe(System.out::println);
```

### takeUntil
**做什么：** 接收到停止信号时终止流。
**典型场景：** 页面销毁、生命周期结束时停止订阅。
**关键要点 / 记忆法：**
- 停止信号可以是定时器/生命周期 Subject/外部事件流。
**注意点 / 常见坑：**
- 条件/布尔类操作符常见坑是“短路与完成依赖”：all/any/contains 会短路并取消上游；defaultIfEmpty/switchIfEmpty 依赖上游完成。
**跨库对照：** RxJS/Reactor/Flow: `takeUntil`
**演示代码：**

```java
Observable<Long> source = Observable.interval(100, TimeUnit.MILLISECONDS);
Observable<Long> stop = Observable.timer(350, TimeUnit.MILLISECONDS);

source.takeUntil(stop)
    .blockingSubscribe(v -> System.out.println("v=" + v));
```

### skipUntil
**做什么：** 接收到开始信号后才放行数据（之前的数据全部丢弃）。
**典型场景：** 初始化完成后才处理事件。
**关键要点 / 记忆法：**
- 常见：等待配置/权限/首屏数据 ready。
**注意点 / 常见坑：**
- 条件/布尔类操作符常见坑是“短路与完成依赖”：all/any/contains 会短路并取消上游；defaultIfEmpty/switchIfEmpty 依赖上游完成。
**跨库对照：** RxJS/Reactor/Flow: `skipUntil`
**演示代码：**

```java
Observable<Long> source = Observable.interval(100, TimeUnit.MILLISECONDS).take(8);
Observable<Long> start = Observable.timer(320, TimeUnit.MILLISECONDS);

source.skipUntil(start)
    .blockingSubscribe(v -> System.out.println("pass=" + v));
```

## 2.6 聚合类操作符 (Aggregating Operators)
- 用于将多个数据汇总成一个结果或一个集合。
- 多数聚合依赖“上游完成”才能给出结果：对无限流谨慎使用。

### reduce
**做什么：** 将数据逐步累积成一个最终值（只输出最终结果）。
**典型场景：** 求和、汇总。
**关键要点 / 记忆法：**
- 想看每一步累积过程，用 `scan`。
**注意点 / 常见坑：**
- 聚合类操作符常见坑是“必须等完成 & 内存占用”：reduce/toList/toMap/count 等通常要等上游完成；大流要谨慎一次性聚合。
**跨库对照：** RxJS/Reactor/Flow: `reduce`
**演示代码：**

```java
int sum = Observable.range(1, 5)
    .reduce(0, Integer::sum)
    .blockingGet();

System.out.println(sum); // 15
```

### collect
**做什么：** 将数据收集到容器中（自定义容器与累加逻辑）。
**典型场景：** 批量处理、构建复杂聚合结构。
**关键要点 / 记忆法：**
- 当 toList/toMap 不够用时，用 collect 自定义容器。
**注意点 / 常见坑：**
- 聚合类操作符常见坑是“必须等完成 & 内存占用”：reduce/toList/toMap/count 等通常要等上游完成；大流要谨慎一次性聚合。
**跨库对照：** Reactor: `collectList/collectMap`  | RxJS: 常用 `reduce` 自建容器
**演示代码：**

```java
List<Integer> list = Observable.range(1, 5)
    .collect(ArrayList::new, List::add)
    .blockingGet();

System.out.println(list);
```

### count
**做什么：** 统计数据条数。
**典型场景：** 统计数量、计数校验。
**关键要点 / 记忆法：**
- RxJava 返回 `Single<Long>`。
**注意点 / 常见坑：**
- 聚合类操作符常见坑是“必须等完成 & 内存占用”：reduce/toList/toMap/count 等通常要等上游完成；大流要谨慎一次性聚合。
**跨库对照：** RxJS/Reactor/Flow: `count`
**演示代码：**

```java
long n = Observable.just("a", "b", "c")
    .count()
    .blockingGet();

System.out.println(n); // 3
```

### toList
**做什么：** 汇总为列表（一次性发射）。
**典型场景：** 需要完整数据集再处理。
**关键要点 / 记忆法：**
- 对无限流会一直等不到完成；对大流可能占用内存。
**注意点 / 常见坑：**
- 聚合类操作符常见坑是“必须等完成 & 内存占用”：reduce/toList/toMap/count 等通常要等上游完成；大流要谨慎一次性聚合。
**跨库对照：** RxJS: `toArray`  | Reactor: `collectList`  | Flow: `toList`
**演示代码：**

```java
List<Integer> all = Observable.range(1, 5)
    .toList()
    .blockingGet();

System.out.println(all);
```

### toMap
**做什么：** 汇总为 Map 结构（按 key 建索引）。
**典型场景：** 构建索引、按 id 快速查找。
**关键要点 / 记忆法：**
- key 冲突时可能覆盖：必要时先 groupBy 或自定义合并策略。
**注意点 / 常见坑：**
- 聚合类操作符常见坑是“必须等完成 & 内存占用”：reduce/toList/toMap/count 等通常要等上游完成；大流要谨慎一次性聚合。
**跨库对照：** Reactor: `collectMap`  | Flow: `associateBy`
**演示代码：**

```java
Map<Integer, String> byLen = Observable.just("a", "bb", "ccc")
    .toMap(String::length)
    .blockingGet();

System.out.println(byLen);
```

### first
**做什么：** 取第一个数据（可指定默认值）。
**典型场景：** 首次命中：取第一个可用缓存/首条记录。
**关键要点 / 记忆法：**
- RxJava 常用 `first(default)` 或 `firstOrError()`。
**注意点 / 常见坑：**
- 聚合类操作符常见坑是“必须等完成 & 内存占用”：reduce/toList/toMap/count 等通常要等上游完成；大流要谨慎一次性聚合。
**跨库对照：** RxJS/Reactor/Flow: `first`
**演示代码：**

```java
int first = Observable.just(10, 20, 30)
    .first(0)
    .blockingGet();

System.out.println(first); // 10
```

### last
**做什么：** 取最后一个数据（可指定默认值）。
**典型场景：** 最终结果、最终状态。
**关键要点 / 记忆法：**
- 需要等待完成；对无限流不适用。
**注意点 / 常见坑：**
- 聚合类操作符常见坑是“必须等完成 & 内存占用”：reduce/toList/toMap/count 等通常要等上游完成；大流要谨慎一次性聚合。
**跨库对照：** RxJS/Reactor/Flow: `last`
**演示代码：**

```java
int last = Observable.just(10, 20, 30)
    .last(0)
    .blockingGet();

System.out.println(last); // 30
```

### single
**做什么：** 要求只有一个数据；否则报错（或用 default 兜底）。
**典型场景：** 唯一结果校验。
**关键要点 / 记忆法：**
- 可能为空用 `singleElement()` 或提供默认值。
**注意点 / 常见坑：**
- 聚合类操作符常见坑是“必须等完成 & 内存占用”：reduce/toList/toMap/count 等通常要等上游完成；大流要谨慎一次性聚合。
**跨库对照：** RxJS/Reactor/Flow: `single`(或组合实现)
**演示代码：**

```java
int v = Observable.just(42).singleOrError().blockingGet();
System.out.println(v);

// 多于一个会报错
Observable.just(1, 2)
    .singleOrError()
    .subscribe(
        x -> System.out.println("single=" + x),
        e -> System.out.println("error=" + e)
    );
```

## 2.7 错误处理类操作符 (Error Handling)
- 用于处理异常、重试和降级。
- 建议：对可预期错误做降级/兜底；对不可预期错误保留并记录。

### onErrorReturn
**做什么：** 出错时返回兜底数据并结束。
**典型场景：** 默认展示。
**关键要点 / 记忆法：**
- 如果要切换到备用流而不是单个值，用 onErrorResumeNext。
**注意点 / 常见坑：**
- 错误处理类操作符常见坑是“吞错与副作用重放”：兜底会隐藏真实错误；重试会重放上游副作用，务必限制次数并保证幂等。
**跨库对照：** RxJS: `catchError(() => of(...))`  | Reactor: `onErrorReturn`
**演示代码：**

```java
Observable.<String>error(new RuntimeException("boom"))
    .onErrorReturnItem("FALLBACK")
    .blockingSubscribe(System.out::println);
```

### onErrorResumeNext
**做什么：** 出错时切换到备用流继续执行。
**典型场景：** 服务降级（主接口失败切备接口/缓存）。
**关键要点 / 记忆法：**
- 备用流也可能失败：可再套一层兜底。
**注意点 / 常见坑：**
- 错误处理类操作符常见坑是“吞错与副作用重放”：兜底会隐藏真实错误；重试会重放上游副作用，务必限制次数并保证幂等。
**跨库对照：** RxJS: `catchError(() => other$)`  | Reactor: `onErrorResume`
**演示代码：**

```java
Observable.<String>error(new RuntimeException("primary fail"))
    .onErrorResumeNext(Observable.just("backup-1", "backup-2"))
    .blockingSubscribe(System.out::println);
```

### retry
**做什么：** 出错后重新执行（重新订阅上游）。
**典型场景：** 临时网络异常。
**关键要点 / 记忆法：**
- 一定要限制次数/条件，避免无限重试。
**注意点 / 常见坑：**
- retry 会重新订阅上游：若上游有副作用（下单/写入），必须保证幂等，否则会重复执行。
**跨库对照：** RxJS/Reactor: `retry(n)`
**演示代码：**

```java
AtomicInteger attempts = new AtomicInteger();

Observable.fromCallable(() -> {
        int n = attempts.incrementAndGet();
        if (n < 3) throw new RuntimeException("fail " + n);
        return "ok at " + n;
    })
    .retry(5)
    .blockingSubscribe(System.out::println);
```

### retryWhen
**做什么：** 按策略控制是否重试（可做指数退避、按错误类型过滤）。
**典型场景：** 指数退避重试。
**关键要点 / 记忆法：**
- 典型做法：次数上限 + 退避 + 仅对可恢复错误重试。
**注意点 / 常见坑：**
- retryWhen 很容易写成“永远重试”；务必加次数上限/退避，并在不可恢复错误时及时停止。
**跨库对照：** RxJS: `retryWhen`  | Reactor: `retryWhen`(Retry spec)  | Flow: `retryWhen`
**演示代码：**

```java
AtomicInteger attempts = new AtomicInteger();

Observable.fromCallable(() -> {
        int n = attempts.incrementAndGet();
        if (n < 4) throw new RuntimeException("fail " + n);
        return "ok at " + n;
    })
    .retryWhen(errors ->
        errors.zipWith(Observable.range(1, 3), (err, retryCount) -> retryCount)
              .flatMap(retryCount ->
                  Observable.timer((long)Math.pow(2, retryCount) * 100, TimeUnit.MILLISECONDS)
              )
    )
    .blockingSubscribe(System.out::println, e -> System.out.println("final error=" + e));
```

### doOnError
**做什么：** 错误时执行副作用（记录日志/告警），不改变错误传播。
**典型场景：** 日志、告警、埋点。
**关键要点 / 记忆法：**
- 吞错/降级要用 onErrorReturn/onErrorResume，而不是 doOnError。
**注意点 / 常见坑：**
- 错误处理类操作符常见坑是“吞错与副作用重放”：兜底会隐藏真实错误；重试会重放上游副作用，务必限制次数并保证幂等。
**跨库对照：** RxJS: `tap({ error: ... })`  | Reactor: `doOnError`
**演示代码：**

```java
Observable.<String>error(new RuntimeException("boom"))
    .doOnError(e -> System.out.println("log: " + e.getMessage()))
    .onErrorReturnItem("fallback")
    .blockingSubscribe(System.out::println);
```

## 2.8 线程与调度类操作符 (Schedulers / Threading)
- 用于控制代码在哪个线程执行，避免阻塞和卡顿。
- 核心：`subscribeOn` 影响上游/生产；`observeOn` 影响下游/消费。

### subscribeOn
**做什么：** 指定数据产生所在线程（影响上游执行）。
**典型场景：** IO、网络请求。
**关键要点 / 记忆法：**
- 多个 subscribeOn 一般只有最靠近源头的生效（依库而定）。
**注意点 / 常见坑：**
- 调度类操作符常见坑是“以为切了线程其实没切/切太多”：subscribeOn 影响上游，observeOn/publishOn 影响下游；频繁切线程会引入队列与延迟。
**跨库对照：** Reactor: `subscribeOn`  | Flow: `flowOn`(对上游生效)
**演示代码：**

```java
Observable.fromCallable(() -> {
        System.out.println("work thread = " + Thread.currentThread().getName());
        return "data";
    })
    .subscribeOn(Schedulers.io())
    .blockingSubscribe(v -> System.out.println("recv thread = " + Thread.currentThread().getName()));
```

### observeOn
**做什么：** 指定数据消费所在线程（影响下游回调）。
**典型场景：** UI 更新、下游切线程。
**关键要点 / 记忆法：**
- observeOn 可多次切换：每次只影响其下游。
**注意点 / 常见坑：**
- 调度类操作符常见坑是“以为切了线程其实没切/切太多”：subscribeOn 影响上游，observeOn/publishOn 影响下游；频繁切线程会引入队列与延迟。
**跨库对照：** Reactor: `publishOn`（概念更常用）
**演示代码：**

```java
Observable.just("A", "B")
    .doOnNext(v -> System.out.println("before observeOn = " + Thread.currentThread().getName()))
    .observeOn(Schedulers.single())
    .doOnNext(v -> System.out.println("after observeOn  = " + Thread.currentThread().getName()))
    .blockingSubscribe();
```

### publishOn
**做什么：** 指定下游调度线程（Reactor 常用；概念上类似 RxJava 的 observeOn）。
**典型场景：** Reactor 中切换下游执行上下文。
**关键要点 / 记忆法：**
- Reactor: `publishOn` 影响下游；`subscribeOn` 影响订阅与上游。
**注意点 / 常见坑：**
- 调度类操作符常见坑是“以为切了线程其实没切/切太多”：subscribeOn 影响上游，observeOn/publishOn 影响下游；频繁切线程会引入队列与延迟。
**跨库对照：** RxJava: `observeOn`
**演示代码：**

```java
// Reactor 示例 (Java)
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

Flux.just("A", "B", "C")
    .doOnNext(v -> System.out.println("before publishOn = " + Thread.currentThread().getName()))
    .publishOn(Schedulers.boundedElastic())
    .doOnNext(v -> System.out.println("after  publishOn = " + Thread.currentThread().getName()))
    .blockLast();
```

### unsubscribeOn
**做什么：** 指定取消订阅线程（释放资源在哪个线程发生）。
**典型场景：** 后台释放资源。
**关键要点 / 记忆法：**
- 对涉及 IO 关闭/连接断开等清理很有用。
**注意点 / 常见坑：**
- 调度类操作符常见坑是“以为切了线程其实没切/切太多”：subscribeOn 影响上游，observeOn/publishOn 影响下游；频繁切线程会引入队列与延迟。
**跨库对照：** Reactor: 通常用 `doFinally` 观察取消
**演示代码：**

```java
Disposable d = Observable.interval(50, TimeUnit.MILLISECONDS)
    .doOnDispose(() -> System.out.println("dispose on " + Thread.currentThread().getName()))
    .unsubscribeOn(Schedulers.io())
    .subscribe(v -> {});

Thread.sleep(120);
d.dispose();
Thread.sleep(200);
```

### delay
**做什么：** 延迟发射数据（或延迟订阅，视重载而定）。
**典型场景：** 节奏控制、模拟慢网。
**关键要点 / 记忆法：**
- delay 会引入调度：注意线程与时间单位。
**注意点 / 常见坑：**
- delay 改变时序且可能改变回调线程；用于业务链路要小心超时/取消边界，避免延迟回写旧 UI。
**跨库对照：** RxJS: `delay`  | Reactor: `delayElements/delaySubscription`
**演示代码：**

```java
long start = System.currentTimeMillis();

Observable.just("hello")
    .delay(300, TimeUnit.MILLISECONDS)
    .blockingSubscribe(v ->
        System.out.println(v + " after " + (System.currentTimeMillis() - start) + "ms")
    );
```

### timeout
**做什么：** 超时终止流（超时则报错或切换兜底）。
**典型场景：** 防止卡死。
**关键要点 / 记忆法：**
- 超时会取消上游；上游应能响应取消避免资源泄露。
**注意点 / 常见坑：**
- timeout 依赖调度器计时；若调度线程被阻塞可能误触发超时。高频流上避免在 computation 做重活。
**跨库对照：** RxJS/Reactor/Flow: `timeout`
**演示代码：**

```java
Observable.never()
    .timeout(200, TimeUnit.MILLISECONDS)
    .onErrorReturnItem(-1L)
    .blockingSubscribe(v -> System.out.println("v=" + v));
```

## 2.9 辅助与调试类操作符 (Utility / Debug)
- 用于副作用处理、调试、共享和生命周期管理。
- 经验：副作用用 doOn*；收尾优先用 doFinally；共享用 cache/share（理解生命周期）。

### doOnNext
**做什么：** 数据到来时执行额外逻辑（副作用），不改变数据。
**典型场景：** 日志、埋点、调试。
**关键要点 / 记忆法：**
- 别在 doOnNext 做重阻塞；必要时先切线程。
**注意点 / 常见坑：**
- 辅助/调试类操作符常见坑是“副作用抛错/阻塞”：doOn* 里抛异常会终止流；日志/埋点别做重 IO，收尾优先用 doFinally。
**跨库对照：** RxJS: `tap`  | Reactor: `doOnNext`  | Flow: `onEach`
**演示代码：**

```java
Observable.range(1, 3)
    .doOnNext(v -> System.out.println("log v=" + v))
    .map(v -> v * 10)
    .blockingSubscribe(System.out::println);
```

### doOnSubscribe
**做什么：** 订阅时执行逻辑。
**典型场景：** 显示 loading、初始化资源。
**关键要点 / 记忆法：**
- 冷流每次订阅都会触发。
**注意点 / 常见坑：**
- 辅助/调试类操作符常见坑是“副作用抛错/阻塞”：doOn* 里抛异常会终止流；日志/埋点别做重 IO，收尾优先用 doFinally。
**跨库对照：** Reactor: `doOnSubscribe`
**演示代码：**

```java
Observable.just("data")
    .doOnSubscribe(d -> System.out.println("show loading"))
    .doFinally(() -> System.out.println("hide loading"))
    .blockingSubscribe(System.out::println);
```

### doOnComplete
**做什么：** 完成时执行副作用（仅正常完成）。
**典型场景：** 隐藏 loading、记录完成日志。
**关键要点 / 记忆法：**
- 错误不会触发 doOnComplete；统一收尾用 doFinally。
**注意点 / 常见坑：**
- 辅助/调试类操作符常见坑是“副作用抛错/阻塞”：doOn* 里抛异常会终止流；日志/埋点别做重 IO，收尾优先用 doFinally。
**跨库对照：** RxJS: `tap({ complete: ... })`  | Reactor: `doOnComplete`  | Flow: `onCompletion`
**演示代码：**

```java
Observable.just(1, 2)
    .doOnComplete(() -> System.out.println("complete"))
    .blockingSubscribe(System.out::println);
```

### doFinally
**做什么：** 流结束时统一处理（完成/错误/取消都会触发）。
**典型场景：** 资源释放、统一隐藏 loading。
**关键要点 / 记忆法：**
- 把清理逻辑集中放在 doFinally，避免遗漏取消分支。
**注意点 / 常见坑：**
- 辅助/调试类操作符常见坑是“副作用抛错/阻塞”：doOn* 里抛异常会终止流；日志/埋点别做重 IO，收尾优先用 doFinally。
**跨库对照：** RxJS: `finalize`  | Reactor: `doFinally`  | Flow: `onCompletion`
**演示代码：**

```java
Observable.<String>error(new RuntimeException("boom"))
    .doFinally(() -> System.out.println("cleanup (always)"))
    .onErrorReturnItem("fallback")
    .blockingSubscribe(System.out::println);
```

### doOnCancel
**做什么：** 取消订阅时处理（更常见于 Reactor；RxJava 对应 doOnDispose）。
**典型场景：** 页面销毁时停止后台任务、释放资源。
**关键要点 / 记忆法：**
- cancel/dispose 属于外部终止，不等于 onComplete。
**注意点 / 常见坑：**
- 辅助/调试类操作符常见坑是“副作用抛错/阻塞”：doOn* 里抛异常会终止流；日志/埋点别做重 IO，收尾优先用 doFinally。
**跨库对照：** RxJava: `doOnDispose`
**演示代码：**

```java
// Reactor 示例 (Java)
import reactor.core.publisher.Flux;

var disposable = Flux.interval(java.time.Duration.ofMillis(100))
    .doOnCancel(() -> System.out.println("cancelled"))
    .subscribe();

Thread.sleep(250);
disposable.dispose();
Thread.sleep(200);
```

### delay
**做什么：** 延迟数据发射（调试时常用来制造时序差）。
**典型场景：** 模拟慢网、观察并发/取消行为。
**关键要点 / 记忆法：**
- 语义同 2.8 的 delay；这里只强调调试用途。
**注意点 / 常见坑：**
- delay 改变时序且可能改变回调线程；用于业务链路要小心超时/取消边界，避免延迟回写旧 UI。
**跨库对照：** 同 2.8
**演示代码：**

```java
Observable.just("A", "B")
    .concatMap(v -> Observable.just(v).delay(150, TimeUnit.MILLISECONDS))
    .blockingSubscribe(System.out::println);
```

### timeout
**做什么：** 调试超时：快速暴露卡死/无响应问题。
**典型场景：** 定位问题：某步骤不应超过 N ms。
**关键要点 / 记忆法：**
- 语义同 2.8 的 timeout；这里只强调定位用途。
**注意点 / 常见坑：**
- timeout 依赖调度器计时；若调度线程被阻塞可能误触发超时。高频流上避免在 computation 做重活。
**跨库对照：** 同 2.8
**演示代码：**

```java
Observable.timer(500, TimeUnit.MILLISECONDS)
    .timeout(200, TimeUnit.MILLISECONDS)
    .subscribe(
        v -> System.out.println("ok"),
        e -> System.out.println("timeout -> " + e)
    );

Thread.sleep(600);
```

### repeat
**做什么：** 完成后重新执行（重新订阅上游）。
**典型场景：** 轮询（配合 delay/interval）、重复任务。
**关键要点 / 记忆法：**
- repeat 会立刻重来；要间隔请配 `repeatWhen` + timer/interval。
**注意点 / 常见坑：**
- 辅助/调试类操作符常见坑是“副作用抛错/阻塞”：doOn* 里抛异常会终止流；日志/埋点别做重 IO，收尾优先用 doFinally。
**跨库对照：** RxJS/Reactor: `repeat`
**演示代码：**

```java
Observable.fromCallable(() -> System.currentTimeMillis())
    .repeat(3)
    .blockingSubscribe(v -> System.out.println("t=" + v));
```

### cache / share
**做什么：** 缓存并共享结果：cache 会记住并重放；share 让多个订阅共享同一条上游（热化/引用计数）。
**典型场景：** 多处复用结果，避免重复网络调用。
**关键要点 / 记忆法：**
- cache：一次请求，多次订阅，后订阅也能拿到已完成结果。
- share：多订阅共享过程，但晚订阅可能错过已发射数据；需要重放用 replay/shareReplay。
**注意点 / 常见坑：**
- cache 可能长期持有结果导致内存滞留；share 不重放历史，晚订阅会错过事件——需要重放用 replay/shareReplay。
**跨库对照：** RxJS: `share`, `shareReplay`  | Reactor: `cache`, `share`  | Flow: `shareIn/stateIn`
**演示代码：**

```java
// cache: 后订阅也能拿到已完成的结果
Observable<Long> cached = Observable.fromCallable(() -> {
        System.out.println("do network once");
        return System.currentTimeMillis();
    })
    .cache();

cached.blockingSubscribe(v -> System.out.println("sub1=" + v));
cached.blockingSubscribe(v -> System.out.println("sub2=" + v)); // 不会再次打印 do network once

// share: 多订阅共享同一上游（不重放历史）
Observable<Long> shared = Observable.interval(100, TimeUnit.MILLISECONDS).take(4).share();
shared.subscribe(v -> System.out.println("A:" + v));
Thread.sleep(180);
shared.blockingSubscribe(v -> System.out.println("B:" + v)); // B 可能错过 0
```

---

## Notes
- 本文刻意避开源码细节，聚焦语义差异与工程习惯。
- 跨库迁移时请重点验证：线程/调度、取消语义、背压行为、时间操作符的测试策略。
