import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("maven-publish")
    id("com.android.library") version "8.2.2"
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.20"
}

group = "com.tecknobit.glider"
version = "2.0.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            this@jvm.compilerOptions {
                jvmTarget.set(JvmTarget.JVM_18)
            }
        }
    }
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_18)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Glider-Core"
            isStatic = true
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        binaries.executable()
        browser {
            webpackTask {
                dependencies {
                }
            }
        }
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation("io.github.n7ghtm4r3:equinox-core:1.0.9")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
            }
        }

    }

    jvmToolchain(18)
}

android {
    namespace = "com.tecknobit.glidercore"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "com.tecknobit.glidercore"
                artifactId = "glidercore"
                version = "2.0.0"
                from(components["kotlin"])
            }
        }
    }
}