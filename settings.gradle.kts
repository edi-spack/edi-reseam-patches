pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://git.reseam.app/api/packages/reseam/maven") {
            mavenContent { includeGroupAndSubgroups("app.reseam") }
        }
    }
    (System.getenv("RESEAM_WORKSPACE") ?: providers.gradleProperty("reseam.workspace").orNull)
        ?.takeIf { it.isNotBlank() }
        ?.let { includeBuild(it) }
}

plugins {
    id("app.reseam.workspace") version "0.36.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "edi-reseam-patches"
