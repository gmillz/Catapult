plugins {
    id("com.android.library")
}

android {
    namespace = "com.android.app.search"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }

    sourceSets {
        named("main") {
            java.srcDirs(listOf("src"))
            manifest.srcFile("AndroidManifest.xml")
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
    implementation("androidx.core:core:1.12.0")
}