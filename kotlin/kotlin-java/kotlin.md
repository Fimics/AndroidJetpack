## Kotlin

### 1.kotlin 基础

**1. Any,Any?**
>Any 非空类型的根类
Any? 所有类型的根类

>Nothing Nothing处于Kotlin类型层级结构的最底层
*  顾名思义，Nothing是没有实例的类型。Nothing类型的表达式不会产生任何值。需要注意的是，
*  任何返回值为Nothing的表达式之后的语句都是无法执行的。你是不是感觉这有点像return或者break的作用？
*  没错，Kotlin中return、throw等（流程控制中与跳转相关的表达式）返回值都为Nothing
*
*  有趣的是，与Nothing对应的Nothing？，我们从字面上翻译可能会解释为：可空的空。与Any、Any？类似，Nothing？
*  是Nothing的父类型，所以Nothing处于Kotlin类型层级结构的最底层。
*
*  其实，它只能包含一个值：null，本质上与null没有区别。所以我们可以使用null作为任何可空类型的值。
   */

**2. array**

```kotlin
var array: IntArray = intArrayOf(1, 2, 3, 4, 5)

   for (item: Int in array) {
   println(item)
   }

   println("-----")

   for (i: Int in array.indices) {
   println("array[$i] = ${array[i]}")
   }

   println("-----")

   for ((index, value) in array.withIndex()) {
   println("array[$index] = $value")
   }
```

**3. Stting**
```  kotlin
var a: String = "hello \n world"
   println(a)

   var b: String = """hello
   \n world
   welcome
   """
   ```
4. 可变参数

```  klotin
fun hello1(vararg  a:Int){
     for (i in a ){
     println(i)
  }
  ```

### 2.函数
1. 匿名函数
2. 函数类型与隐式返回
3. it关键字
4. 参数是函数的函数
5. 内联函数
6. 函数引用
7. 函数类型作为返回 类型
8. 闭包
9. lambda与匿名内部类
10. copy 函数


### 2.1 操作符
1. apply
2. let
3. run
4. with
5. also
6. takeif
7. takeUnless
8. infix

### 3.面向对象

### 4.adt

### 5.泛型

### 6.异常

### 7.delegate

### 8.lambda

### 9.集合
1. mutator

### 10.多态

### 11.注解

### 12.反射

### 13.协程
