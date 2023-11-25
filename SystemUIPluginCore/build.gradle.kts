plugins {
    id("com.android.library")
}

android {
    namespace = "com.android.systemui.plugin_core"

    compileSdk = 34

    sourceSets {
        named("main") {
            java.srcDirs(listOf("src"))
            aidl.srcDirs(listOf("src"))
            manifest.srcFile("AndroidManifest.xml")
        }
    }
}

dependencies {
}