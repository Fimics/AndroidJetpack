pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

rootProject.name = "AiGuide"

// ============================================================
// 壳工程
// ============================================================
include(":app")

// ============================================================
// 架构层：MVC / MVP / MVVM / MVI 基础框架与 Base 类
// ============================================================
include(":arch")

// ============================================================
// 接口层（api）：业务对外能力接口，模块间解耦（非 HTTP）
// ============================================================
include(":api:api-player")
include(":api:api-chat")
include(":api:api-music")
include(":api:api-settings")

// ============================================================
// 业务层（business）：各业务模块，只关心自身业务
// ============================================================
include(":business:module-home")
include(":business:module-chat")
include(":business:module-music")
include(":business:module-video")
include(":business:module-settings")

// ============================================================
// 支撑层（support）：网络、存储、数据库、路由等垂直能力
// ============================================================
include(":support:support-network")
include(":support:support-websocket")
include(":support:support-router")
include(":support:support-storage")
include(":support:support-database")
include(":support:support-permission")

// ============================================================
// 工具层（libs）：通用工具、UI、日志、图片、扩展
// ============================================================
include(":libs:lib-common")
include(":libs:lib-ui")
include(":libs:lib-log")
include(":libs:lib-image")
include(":libs:lib-extension")

