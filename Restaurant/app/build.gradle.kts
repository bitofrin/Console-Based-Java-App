
plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // JUnit untuk testing
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Dependency aplikasi (opsional, contoh Guava)
    implementation(libs.guava)
}

// Atur Java toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    // Main class
    mainClass.set("org.example.App")
}

// Pastikan Gradle bisa menerima input interaktif dari keyboard
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

