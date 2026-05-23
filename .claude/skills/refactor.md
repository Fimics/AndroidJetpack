# /refactor - 重构代码

对指定的类、方法或模块进行重构，保持功能不变。

## 步骤

1. 读取目标代码，理解当前实现
2. 根据用户指定的重构类型执行：
   - **提取方法**：将重复或过长代码块提取为独立方法
   - **提取类**：将职责过多的类拆分
   - **Java 转 Kotlin**：将 Java 文件转为惯用 Kotlin（data class、扩展函数、作用域函数等）
   - **回调转协程**：将 Callback 模式转为 suspend 函数 + Flow
   - **RxJava 转协程**：将 Observable/Single 转为 Flow/suspend
   - **简化代码**：消除冗余、使用 Kotlin 惯用写法
3. 确保重构后代码可编译
4. 列出所有改动点，方便用户 review