package io.github.betterclient.discordbot.register

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationInformations(
    //registered users K: Discord ID, V: Slack ID
    val registered_users: MutableMap<String, String>,
    //blocked users for K: Slack ID, list of Discord IDS
    val blocked_users: MutableMap<String, MutableList<String>>
)