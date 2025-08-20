plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.github.arundivakaruni.minestompiserver"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:2025.07.17-1.21.8")
    implementation("org.slf4j:slf4j-simple:2.0.17")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "me.arundivakaruni.Main"
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")
    }
}