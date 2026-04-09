import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp.plugin)
}

kotlin {
    android {
        namespace = "com.ai.adventchallenge.di"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }

    jvm()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                // Room (API/runtime only, compiler ниже в androidMain)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.android)
                // Room runtime для Android
                implementation(libs.androidx.room.gradle.plugin)
            }
        }

        val jvmMain by getting {
            dependencies {
            }
        }
    }
}

dependencies {
    // Room compiler через KSP для Android таргета
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}

