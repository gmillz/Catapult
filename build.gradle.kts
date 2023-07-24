import com.google.protobuf.gradle.*
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application").version("8.0.2")
    kotlin("android").version("1.9.0")
    id("com.google.protobuf").version("0.9.4")
    id("com.android.library") version "8.0.2" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
    kotlin("plugin.parcelize").version("1.9.0")
}

android {
    namespace = "com.android.launcher3"
    testNamespace = "com.android.launcher3.tests"
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

    applicationVariants.configureEach {
        outputs.configureEach {
            val releaseType = name.split("-")[1]
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Catapult_${versionName}_${releaseType}.apk"
        }
    }

    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    var releaseSigning = signingConfigs.getByName("debug")
    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        releaseSigning = signingConfigs.create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            signingConfig = releaseSigning
        }
        getByName("release") {
            signingConfig = releaseSigning
        }
    }

    kotlin {
        jvmToolchain(17)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
        aidl.setSrcDirs(listOf("catapult/aidl"))
        res.setSrcDirs(listOf("catapult/res"))
        java.setSrcDirs(listOf("catapult/src"))
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.slice:slice-core:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation(project(":iconloaderlib"))
    implementation(project(":SystemUIPluginCore"))
    implementation(project(":extensions"))

    implementation("com.github.gmillz:ComposeSettings:1.1.2")

    implementation("com.google.protobuf:protobuf-javalite:3.21.12")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")

    // Persian Date
    implementation("com.github.samanzamani:PersianDate:1.5.4")

    // Compose
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.ui:ui-tooling:1.4.3")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.4.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.28.0")
    implementation("com.google.accompanist:accompanist-permissions:0.28.0")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.28.0")
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.20.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    implementation("com.google.code.gson:gson:2.10")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.mockito:mockito-core:4.11.0")
    androidTestImplementation("com.google.dexmaker:dexmaker:1.2")
    androidTestImplementation("com.google.dexmaker:dexmaker-mockito:1.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.annotation:annotation:1.6.0")

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