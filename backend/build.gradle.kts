plugins {
    id("java")
    kotlin("jvm")
    id("org.springframework.boot") version "3.2.3"
}

apply(plugin = "io.spring.dependency-management")

group = "com.tecknobit.glider"
version = "2.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.clojars.org")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.2.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.4")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.github.N7ghtm4r3:APIManager:2.2.4")
    implementation("io.github.n7ghtm4r3:equinox-backend:1.0.9")
    implementation("io.github.n7ghtm4r3:equinox-core:1.0.9")
    implementation("org.json:json:20250107")
    implementation(project(":core"))
}

kotlin {
    jvmToolchain(18)
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}