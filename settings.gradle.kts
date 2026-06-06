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
include(":api:api-home")
include(":api:api-video")

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
include(":support:support-media")
include(":support:support-ai")
include(":support:support-camera")
include(":support:support-ble")
include(":support:support-serial")
include(":support:support-python")
include(":support:support-push")
include(":support:support-update")

// ============================================================
// 工具层（libs）：通用工具、UI、日志、图片、扩展
// ============================================================
include(":libs:lib-common")
include(":libs:lib-ui")
include(":libs:lib-log")
include(":libs:lib-image")
include(":libs:lib-extension")
include(":libs:lib-widget")
include(":libs:lib-test")

// ============================================================
// 公共资源 + 性能基线
// ============================================================
include(":common")
include(":baseline-profile")

