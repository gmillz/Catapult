import com.google.protobuf.gradle.*

plugins {
    id("com.android.application").version("7.4.0")
    kotlin("android").version("1.7.20")
    id("com.google.protobuf").version("0.8.19")
    id("com.android.library") version "7.3.1" apply false
}

android {
    namespace = "com.android.launcher3"
    compileSdk = 33

    defaultConfig {
        minSdk = 28
        targetSdk = 32
        applicationId = "app.catapult.launcher"

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
        }
    }

    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    flavorDimensions += "app"

    productFlavors.create("catapult") {
        dimension = "app"
    }

    sourceSets.getByName("main") {
        res.setSrcDirs(listOf("res"))
        java.setSrcDirs(listOf("src", "src_plugins", "src_flags", "src_shortcuts_overrides", "src_ui_overrides"))
        manifest.srcFile("AndroidManifest-common.xml")
    }

    sourceSets.getByName("androidTest") {
        res.setSrcDirs(listOf("tests/res"))
        java.setSrcDirs(listOf("tests/src", "tests/tapl"))
        manifest.srcFile("tests/AndroidManifest-common.xml")
    }

    sourceSets.getByName("androidTestDebug") {
        manifest.srcFile("tests/AndroidManifest.xml")
    }

    sourceSets.getByName("catapult") {
        manifest.srcFile("catapult/AndroidManifest.xml")
        res.setSrcDirs(listOf("catapult/res"))
        java.setSrcDirs(listOf("catapult/src"))
    }
}

dependencies {
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.slice:slice-core:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation(project(":iconloaderlib"))
    implementation(project(":SystemUIPluginCore"))
    implementation(project(":extensions"))

    implementation("com.github.gmillz:ComposeSettings:1.1.2")

    implementation("com.google.protobuf:protobuf-javalite:3.21.12")

    // Compose
    implementation("androidx.compose.ui:ui:1.3.3")
    implementation("androidx.compose.ui:ui-tooling:1.3.3")
    implementation("androidx.compose.material3:material3:1.0.1")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.compose.runtime:runtime-livedata:1.3.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.28.0")
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.28.0")
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.mockito:mockito-core:4.11.0")
    androidTestImplementation("com.google.dexmaker:dexmaker:1.2")
    androidTestImplementation("com.google.dexmaker:dexmaker-mockito:1.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.annotation:annotation:1.5.0")

    api("com.airbnb.android:lottie:5.2.0")

    protobuf(files("protos/"))
    protobuf(files("quickstep/protos_overrides/"))
}

protobuf {
    // Configure the protoc executable
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}