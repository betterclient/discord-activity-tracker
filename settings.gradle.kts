plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "ActivityTracker"
include("Discord-Bot")
include("Slack-Bot")
