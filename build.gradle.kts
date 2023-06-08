plugins {
    java
    application

    id("gov.tak.gradle.plugins.checker-processor") version "1.2.7" // for nullability enforcement
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

var mainClassName: String by application.mainClass
mainClassName = "me.cortex.jarscanner.Main"

group = "me.cortex"
version = "2.0.0-SNAPSHOT"

java {
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") // for cafedude
}

dependencies {
    implementation(libs.bundles.asm)

    implementation(libs.cafedude)

    implementation(libs.lljzip)

    implementation(libs.picocli)

    implementation(libs.jsr305)

    implementation(libs.slf4j)
    implementation(libs.logback)
}

tasks {
    test {
        useJUnitPlatform()

        failFast = false
        maxParallelForks = kotlin.math.max(Runtime.getRuntime().availableProcessors() - 1, 1)
    }

    withType<Javadoc>().configureEach {
        options {
            encoding = "UTF-8"
        }
    }

    withType<Jar>().configureEach {
        from(rootProject.file("LICENSE"))
        manifest {
            attributes(
                    "Main-Class" to mainClassName,
            )
        }
    }
}
