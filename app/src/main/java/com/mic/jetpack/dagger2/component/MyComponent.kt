package com.mic.jetpack.dagger2.component

import com.mic.jetpack.dagger2.Dagger2Fragment
import com.mic.jetpack.dagger2.module.DatabaseModule
import com.mic.jetpack.dagger2.module.HttpModule
import dagger.Component

/**
 * 存放module的组件
 */
@Component(modules = [HttpModule::class,DatabaseModule::class])
interface MyComponent {
    //注入的位置就写在参数上 ，不能用多态
    fun injectDagger2Fragment(dagger2Fragment: Dagger2Fragment)
}