plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(com.idea.buildsrc.Libs.Kotlin.stdlib)
    implementation(com.idea.buildsrc.Libs.Coroutines.android)
    implementation(com.idea.buildsrc.Libs.AndroidX.Lifecycle.lifecycle_common)
    implementation(com.idea.buildsrc.Libs.AndroidX.Lifecycle.lifecycle_runtime)
    implementation(com.idea.buildsrc.Libs.AndroidX.Lifecycle.lifecycle_runtime_ktx)
    implementation(com.idea.buildsrc.Libs.AndroidX.Lifecycle.lifecycle_livedata)
    implementation(com.idea.buildsrc.Libs.AndroidX.Lifecycle.lifecycle_livedata_ktx)
    implementation(com.idea.buildsrc.Libs.AndroidX.Lifecycle.lifecycle_viewmodel_ktx)
    implementation(com.idea.buildsrc.Libs.AndroidX.Lifecycle.lifecycle_viewmodel)
}