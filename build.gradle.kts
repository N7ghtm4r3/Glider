import org.jetbrains.dokka.DokkaConfiguration.Visibility.*
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.android.library") version "8.2.2" apply false
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            includeNonPublic.set(true)
            documentedVisibilities.set(setOf(PUBLIC, PROTECTED, PRIVATE))
        }
    }
}

repositories {
    mavenCentral()
}

tasks.withType<DokkaMultiModuleTask> {
    outputDirectory.set(layout.projectDirectory.dir("docs"))
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("docs/logo-icon.svg"))
        footerMessage = "(c) 2025 Tecknobit"
    }
}