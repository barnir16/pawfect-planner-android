buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.0-1.0.29")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
