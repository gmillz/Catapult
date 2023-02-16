plugins {
    id("com.android.library")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(project(":extensions"))

    implementation("androidx.core:core-ktx:1.9.0")
}