pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
    }
}

include(":iconloaderlib")
project(":iconloaderlib").projectDir = File(rootDir, "systemui_libs/iconloaderlib")

include(":searchuilib")
project(":searchuilib").projectDir = File(rootDir, "systemui_libs/searchuilib")

include(":SystemUIPluginCore")

rootProject.name = "Catapult"

include(":extensions")
