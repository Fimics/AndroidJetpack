package com.mic.p1_base

/**
Any? 包含一切
Any 包含一切非 null
Nothing 什么都不包含
Nothing? 只包含 null

Any? = { 所有值 + null }
├── Any = { 所有非 null 值 }
│    ├── String
│    ├── Int
│    ├── Foo
│    └── ...
└── Nothing? = { null }
Nothing = { }  （在最底层，什么都没有）

 */

fun main() {
    println(convert2Uppercase("hello world"))
    println(convert2Uppercase(23))
}


fun convert2Uppercase(str: Any): String? {
    if(str is String) {
        return str.uppercase()
    }

    val a = arrayOf(1,2,3,4,5)
    hello(1,2,4)
    return null
}

fun hello(vararg  a:Int){
    for (i in a ){
        println(i)
    }
}