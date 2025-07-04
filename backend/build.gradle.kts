plugins {
    id("java")
    kotlin("jvm")
    id("org.springframework.boot") version "3.2.3"
}

apply(plugin = "io.spring.dependency-management")

group = "com.tecknobit.glider"
version = "2.0.2"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.clojars.org")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.jackson.databind)
    implementation(libs.mysql.connector.java)
    implementation(libs.apimanager)
    implementation(libs.equinox.backend)
    implementation(libs.equinox.core)
    implementation(libs.json)
    implementation(project(":core"))
}

kotlin {
    jvmToolchain(18)
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}