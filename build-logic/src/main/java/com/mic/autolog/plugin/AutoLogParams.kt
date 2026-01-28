package com.mic.autolog.plugin

import com.android.build.api.instrumentation.InstrumentationParameters
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface AutoLogParams : InstrumentationParameters {
    @get:Input
    val enabled: Property<Boolean>

    @get:Input
    val includePackages: ListProperty<String>

    @get:Input
    val excludePackages: ListProperty<String>

    @get:Input
    val logStack: Property<Boolean>

    @get:Input
    val logEnter: Property<Boolean>

    @get:Input
    val logExit: Property<Boolean>

    @get:Input
    val logError: Property<Boolean>
}