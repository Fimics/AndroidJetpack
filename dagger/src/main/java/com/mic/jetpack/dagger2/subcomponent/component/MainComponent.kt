package com.mic.jetpack.dagger2.subcomponent.component

import com.mic.jetpack.dagger2.subcomponent.module.MainModule
import dagger.Component

@Component(modules = [MainModule::class])
interface MainComponent {
    //只要返回子组件就可以
    fun getSubComponent():SubComponent
}