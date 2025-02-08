plugins {
    kotlin("jvm") version "2.0.0"
}

group = "io.github.betterclient"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Slack-Bot"))
    implementation(project(":Discord-Bot"))
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

kotlin {
    jvmToolchain(21)
}