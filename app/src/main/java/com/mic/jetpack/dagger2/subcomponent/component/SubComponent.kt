package com.mic.jetpack.dagger2.subcomponent.component

import com.mic.jetpack.dagger2.Dagger2Fragment
import com.mic.jetpack.dagger2.subcomponent.module.SubModule
import dagger.Subcomponent

@Subcomponent(modules = [SubModule::class])
interface SubComponent {
    fun inject2Fragment(dagger2Fragment: Dagger2Fragment)
}