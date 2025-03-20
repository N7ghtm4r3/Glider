rootProject.name = "Glider"

pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.0"
        kotlin("multiplatform") version "2.1.0"
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include("core")
include("backend")
