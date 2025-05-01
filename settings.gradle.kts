pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application")            version "8.9.1"      apply false
        id("org.jetbrains.kotlin.android")       version "2.1.0"      apply false
        id("com.google.devtools.ksp")            version "2.1.0-1.0.20" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "PawfectPlanner"
include(":app")
