package com.mic.p1_base
//导入其他文件
//import com.mic.test.multiply;

/**
 * 给导入的文件起别名
 */
import com.mic.test.multiply as mu

fun main() {
    //val 定义常量 ，相当于java中final修饰的
    val a = 1
//    a=4

    val b = 5
    val c = a + b
    c.toString()
    println("c=${c}")

    var x = 10
    var y: Byte = 20
    //不允许byte类型赋值给int,(小范围赋值给大范围 可以使用y.toInt)
//    x=y;


    println(mu(2, 4))

    val m = intArrayOf(1, 2, 3)
    //此处m的值不能变了
//    m = intArrayOf(3,4,5)
    m.set(0, 5)

    m.forEach { i ->
        println(i)
    }

}