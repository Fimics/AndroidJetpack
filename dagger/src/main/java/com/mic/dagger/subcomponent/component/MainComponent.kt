package com.mic.dagger.subcomponent.component

import com.mic.dagger.subcomponent.module.MainModule
import dagger.Component

@Component(modules = [MainModule::class])
interface MainComponent {
    //只要返回子组件就可以
    fun getSubComponent(): SubComponent
}