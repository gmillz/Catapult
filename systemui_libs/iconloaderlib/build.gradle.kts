plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "app.catapult.launcher.icons"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }

    sourceSets {
        named("main") {
            java.srcDirs(listOf("src", "src_full_lib"))
            manifest.srcFile("AndroidManifest.xml")
            res.srcDirs(listOf("res"))
        }
    }

    lint {
        abortOnError = false
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":extensions"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
}