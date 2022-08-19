/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idea.buildsrc

object Versions {
    const val ktlint = "0.45.2"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.2.1"

    object Accompanist {
        const val version = "0.24.10-beta"
        const val systemuicontroller = "com.google.accompanist:accompanist-systemuicontroller:$version"
        const val flowlayouts = "com.google.accompanist:accompanist-flowlayout:$version"
    }

    object Kotlin {
        const val version = "1.6.21"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }

    object Coroutines {
        private const val version = "1.6.0"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.7.0"
        const val appcompat = "androidx.appcompat:appcompat:1.4.2"
        const val material = "com.google.android.material:material:1.6.1"


        object Compose {
            const val snapshot = ""
            const val version = "1.2.0"

            const val foundation = "androidx.compose.foundation:foundation:${version}"
            const val layout = "androidx.compose.foundation:foundation-layout:${version}"
            const val ui = "androidx.compose.ui:ui:${version}"
            const val uiUtil = "androidx.compose.ui:ui-util:${version}"
            const val runtime = "androidx.compose.runtime:runtime:${version}"
            const val material = "androidx.compose.material:material:${version}"
            const val animation = "androidx.compose.animation:animation:${version}"
            const val tooling = "androidx.compose.ui:ui-tooling:${version}"
            const val toolingPreview = "androidx.compose.ui:ui-tooling-preview:${version}"
            const val iconsExtended = "androidx.compose.material:material-icons-extended:$version"
            const val uiTest = "androidx.compose.ui:ui-test-junit4:$version"
        }

        object Activity {
            const val activityCompose = "androidx.activity:activity-compose:1.4.0"
        }

        object Lifecycle {
            private const val lifecycle_version = "2.3.1"
            const val lifecycle_viewModel_compose = "androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1"
            const val lifecycle_livedata             = "androidx.lifecycle:lifecycle-livedata:$lifecycle_version"
            const val lifecycle_livedata_ktx         = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
            const val lifecycle_extensions           = "androidx.lifecycle:lifecycle-extensions:2.2.0"
            const val lifecycle_viewmodel            = "androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version"
            const val lifecycle_viewmodel_ktx        = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
            const val lifecycle_viewmodel_savedstate = "androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version"
            const val lifecycle_common               = "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version"
            const val lifecycle_runtime              = "androidx.lifecycle:lifecycle-runtime:$lifecycle_version"
            const val lifecycle_runtime_ktx          = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"
            const val lifecycle_core                 = "androidx.lifecycle:lifecycle-livedata-core-ktx:$lifecycle_version"
        }



        object Navigation {
            const val navigationCompose = "androidx.navigation:navigation-compose:2.4.2"
        }

        object ConstraintLayout {
            const val constraintLayoutCompose =
                "androidx.constraintlayout:constraintlayout-compose:1.0.0"
        }

        object Test {
            private const val version = "1.4.0"
            const val core = "androidx.test:core:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"
            object Ext {
                private const val version = "1.1.2"
                const val junit = "androidx.test.ext:junit-ktx:$version"
            }
            const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
            const val macroBenchmark = "androidx.benchmark:benchmark-macro-junit4:1.1.0-beta04"
            const val uiAutomator = "androidx.test.uiautomator:uiautomator:2.2.0"
        }
    }

    object JUnit {
        private const val version = "4.13"
        const val junit = "junit:junit:$version"
    }

    object Coil {
        const val coilCompose = "io.coil-kt:coil-compose:2.0.0"
    }
}
