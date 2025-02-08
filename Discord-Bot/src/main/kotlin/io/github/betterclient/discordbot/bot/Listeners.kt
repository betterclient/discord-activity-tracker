package io.github.betterclient.discordbot.bot

import io.github.betterclient.discordbot.DiscordBot
import io.github.betterclient.discordbot.register.RegisterListener.register
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File

object ForceCheckListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return

        val content = event.message.contentRaw
        if (content.startsWith("!register_me")) {
            register(content, event)
        } else if (content.startsWith("!DEBUG_TEST") && event.member!!.isOwner) {
            event.channel.sendMessage("Force reloading").queue()
            dosomething()
        }
    }
}

fun dosomething() {
    DiscordBot.jda.guilds[0].members.forEach {
        if (it.user.isBot) return@forEach

        updateUser(it)
    }
}

fun updateUser(member: Member) {
    if (!DiscordBot.info.registered_users.containsKey(member.id)) return
    //user isn't registered ^^

    val s = DiscordBot.info.registered_users[member.id]!!
    val activity = if (member.activities.isEmpty()) {
        return
    } else {
        member.activities
    }

    val files: MutableList<File> = mutableListOf()
    for (activity1 in activity) {
        files += updateActivity(member, activity1)?: continue
    }
    if (files.isEmpty()) return

    println("${member.effectiveName} changed status!")
    DiscordBot.MESSAGE_LISTENER.invoke(
        "<@$s> just changed their rich presence!",
        files
    )
}

val beforeActivities = mutableMapOf<String, MutableList<RichPresence>>()

fun updateActivity(member: Member, activity: Activity): File? {
    val presence = activity.asRichPresence()?.let {
        UserRichPresence(member.id, it)
    }

    if (presence != null) {
        val list = beforeActivities.computeIfAbsent(member.id) { mutableListOf() }
        val richPresence = RichPresence(activity.name, activity.state, activity.asRichPresence()?.details)

        if (!list.contains(richPresence)) {
            list += richPresence
            return presence.toFile()
        }

        list.find { a -> a.name == activity.name }?.let {
            list -= it
            list += richPresence
        }
    }
    return null
}

data class RichPresence(val name: String, val state: String?, val details: String?)