# 参考文档
- [视频教程1](https://www.bilibili.com/video/BV1H54y1j7uN/?spm_id_from=333.337.search-card.all.click&vd_source=980e2e83751098334afa972781a5d387)
- [视频教程2](https://www.bilibili.com/video/BV1Wt411d7N2/?spm_id_from=333.337.search-card.all.click)


# 1.使用文档

## 1. 使用
### 1.1 rxjava 的选择
### 1.2 rxjava 的简介与重要概念
- 观察者 (Observer): 观察事件变化并处理的主要角色。消费者(Consumer)也可以理解成一种特殊的观察者。
- 被观察者 (Observable types): 触发事件并决定什么时候发送事件的主要角色。(异常和完成也是一种事件)
- Observable、Flowable、Single、Completable、Maybe都是被观察者。
- Flowable是支持背压的一种被观察者。
- Single、Completable、Maybe是简化版的Observable。
- 几种被观察者通过toObservable/toFlowable/toSingle/toCompletable/toMaybe相互转换。
- 订阅 (subscribe): 观察者和被观察者建立关联的操作。

### 1.3 Rxjava 优势与实用场景
### 1.4 三个基本概念
### 1.5 五种被观察者


## 2. 操作符群  [操作符群](./Reactive_Operators_Table)
### 2.1 创建类操作符（Creation Operators）
### 2.2 转换类操作符（Transforming Operators）
### 2.3 过滤类操作符（Filtering Operators）
### 2.4 组合类操作符（Combining Operators）
### 2.5 条件与布尔类操作符（Conditional / Boolean）
### 2.6 聚合类操作符（Aggregating Operators）
### 2.7 错误处理类操作符（Error Handling）
### 2.8 线程与调度类操作符（Schedulers / 
### 2.9 辅助与调试类操作符（Utility / Debug）

## 3. 核心实现

### 3.1 观察者模式
### 3.2 装饰者模式
### 3.3 手写Rx 部分实现
### 3.4 核心源码之 创建操作符与转换操作符

### 3.5 Scheduler
1. subscribeOn
2. observeOn
3. Scheduler种类
4. 线程调度原理分析
   - 4.1 subscribeOn ,observeOn 分析
   - 4.2 线程切换原理 手写线程切换原理

## 4. 拓展

### 4.1 Flowable背压
### 4.2 生命周期以及内存泄漏问题
### 4.3 RXjava 衍生框架 rxBus, RxPermisson
### 4.4 RXjava 组合框架 Retorfit, RxAndroid


# 2.原理文档

# 3.面试文档