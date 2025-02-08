plugins {
    kotlin("jvm")
}

group = "io.github.betterclient"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":Discord-Bot"))

    implementation("com.slack.api:bolt-socket-mode:1.45.2")
    implementation("javax.websocket:javax.websocket-api:1.1")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:1.20")
}

kotlin {
    jvmToolchain(21)
}