plugins {
    kotlin("jvm") version "2.0.0"
    id("application")
}

group = "io.github.betterclient"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Slack-Bot"))
    implementation(project(":Discord-Bot"))
    implementation("org.slf4j:slf4j-nop:2.0.16")
}

application {
    mainClass = "io.github.betterclient.acttrack.StarterKt"
}

kotlin {
    jvmToolchain(21)
}