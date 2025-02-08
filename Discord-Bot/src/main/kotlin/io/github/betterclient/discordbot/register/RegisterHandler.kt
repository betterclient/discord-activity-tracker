package io.github.betterclient.discordbot.register

import io.github.betterclient.discordbot.DiscordBot
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object RegisterListener {
    fun register(content: String, event: MessageReceivedEvent) {
        val split = content.replace("  ", " ").split(" ")

        val channel = event.channel
        if (split.size != 2) {
            channel.sendMessage("Wrong usage!\nCorrect usage: `!register_me @<slack_id>`").queue()
        } else {
            var id = split[1]
            if (id.startsWith("@")) {
                id = id.substring(1)
            } else if (id.startsWith("<@")) {
                //nuh uh lol
                channel.sendMessage("Wrong usage!\nCorrect usage: `!register_me @<slack_id>`").queue()
                return
            }

            if (DiscordBot.info.registered_users.containsKey(id)) {
                channel.sendMessage("Already registered").queue()
                return
            }

            if (!DiscordBot.SLACK_ID_VERIFIER.invoke(id)) {
                //fake id
                channel.sendMessage("Wrong usage!\nCorrect usage: `!register_me @<slack_id>`").queue()
                return
            }

            id = id.uppercase()

            channel.sendMessage("Confirm from slack.").queue()
            //Send confirm from Slack message before checking blocked
            //(try to lie to them)
            if (DiscordBot.info.blocked_users.containsKey(id) &&
                DiscordBot.info.blocked_users[id]!!.contains(event.author.id.uppercase())
            ) {
                return //Blocked
            }

            val id1 = channel.id
            DiscordBot.lastChannelID = id1
            DiscordBot.REGISTRATION_LISTENER.apply(
                RegistrationInfo(id, event.author.id, event.author.effectiveName),
                {
                    println("Registering...")
                    DiscordBot.info.registered_users[event.author.id] = id
                    DiscordBot.jda
                        .getTextChannelById(id1)!!
                        .sendMessage("Registered Successfully!")
                        .queue()
                },
                {
                    println("Rejection...")
                    DiscordBot.jda
                        .getTextChannelById(id1)!!
                        .sendMessage(":x: Rejected")
                        .queue()
                },
                {
                    println("Blocked...")
                    DiscordBot.jda
                        .getTextChannelById(id1)!!
                        .sendMessage(":x: Rejected") //well you shouldn't tell them
                        .queue()
                })
        }
    }
}

data class RegistrationInfo(val id: String, val discordID: String, val discordName: String) {
    override fun toString(): String {
        return "SlackID: $id | DiscordID: $discordID | DiscordName: $discordName"
    }
}

interface Listener {
    fun apply(info: RegistrationInfo, accept: Runnable, reject: Runnable, rejectBlock: Runnable)
}