pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        maven("https://maven.fabricmc.net/") { name = "Fabric"}
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "dataGenerator"
