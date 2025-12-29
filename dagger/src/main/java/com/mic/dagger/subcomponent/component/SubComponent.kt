package com.mic.dagger.subcomponent.component

import com.mic.dagger.Dagger2Fragment
import com.mic.dagger.subcomponent.module.SubModule
import dagger.Subcomponent

@Subcomponent(modules = [SubModule::class])
interface SubComponent {
    fun inject2Fragment(dagger2Fragment: Dagger2Fragment)
}