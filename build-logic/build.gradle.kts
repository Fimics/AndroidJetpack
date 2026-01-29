plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}


dependencies {
    // 让插件能访问 Android Components / Instrumentation API
    compileOnly("com.android.tools.build:gradle:8.3.2")

    // ASM
    implementation("org.ow2.asm:asm:9.6")
    implementation("org.ow2.asm:asm-commons:9.6")
}

/**
 * 这是 Gradle 插件开发专用的 DSL（由你在 build-logic 里应用的 java-gradle-plugin 提供）。
 * 作用：告诉 Gradle——这个 module 不是普通库，而是要“产出 Gradle 插件”，并且在这里声明插件的元信息（ID、入口类等）
 */
gradlePlugin {

    /**
     * 注意：这里的 plugins 不是顶层那个 plugins {}（应用插件的地方），而是：
     * 在 gradlePlugin 块里声明：这个工程将要发布/暴露哪些 Gradle 插件
     * 一个 build-logic 工程里可以注册多个插件（比如一个做 autolog，一个做 lint 规则），都写在这里。
     */
    plugins {

        /**
         * 注册一个插件条目，名字叫 "autoLogPlugin"。
         * 这个名字是内部名字：用于生成/区分多个插件条目
         * 它不是你在 app 里写的 id("...") 那个 id
         */
        register("autoLogPlugin") {
            //定义插件的 Plugin ID
            id = "com.mic.autolog"
            /**
             * Gradle 在执行 id("com.example.autolog") 时，会实例化这个类：
             * Plugin [id: 'com.example.autolog'] was not found
             * 或 Could not load implementation class ...
             */
            implementationClass = "com.mic.autolog.plugin.AutoLogPlugin"
        }
    }
}
