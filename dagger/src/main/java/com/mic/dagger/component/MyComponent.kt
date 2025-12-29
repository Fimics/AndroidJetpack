package com.mic.dagger.component

/**
 * 存放module的组件
 */
//@AppScope
//@Component(modules = [HttpModule::class,DatabaseModule::class], dependencies = [PresenterComponent::class])
interface MyComponent {
    //注入的位置就写在参数上 ，不能用多态
//    fun injectDagger2Fragment(dagger2Fragment: Dagger2Fragment)
}