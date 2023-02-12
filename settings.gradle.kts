pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
//apply from: 'versions.gradle'
rootProject.name = "AndroidJetpack"
include(":app")
//include(":libs:imagemaster")
include(":libcore")
//include(":xdagger")
//include(":xhilt")
