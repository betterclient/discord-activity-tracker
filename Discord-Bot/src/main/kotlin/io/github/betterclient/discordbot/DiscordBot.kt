package io.github.betterclient.discordbot

import io.github.betterclient.discordbot.bot.ForceCheckListener
import io.github.betterclient.discordbot.bot.beforeActivities
import io.github.betterclient.discordbot.bot.dosomething
import io.github.betterclient.discordbot.register.Listener
import io.github.betterclient.discordbot.register.RegistrationInfo
import io.github.betterclient.discordbot.register.RegistrationInformations
import io.github.betterclient.discordbot.util.createIfFake
import io.github.betterclient.discordbot.util.getVar
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.io.File

object DiscordBot {
    private lateinit var TOKEN: String
    lateinit var jda: JDA
    var lastChannelID = "1337509261118279762"

    lateinit var info: RegistrationInformations

    var REGISTRATION_LISTENER: Listener = object : Listener {
        override fun apply(info: RegistrationInfo, accept: Runnable, reject: Runnable, rejectBlock: Runnable) {}
    }

    var MESSAGE_LISTENER: (String, List<File>) -> Unit = object : ((String, List<File>) -> Unit) {
        override fun invoke(p1: String, p2: List<File>) { println(p1) }
    }
    var DELETE_ALL: Runnable = Runnable { }

    var SLACK_ID_VERIFIER: (String) -> Boolean = { false }

    fun start() {
        File(".DISCORD_REGISTERED").apply {
            info = if (!exists()) {
                RegistrationInformations(mutableMapOf(), mutableMapOf())
            } else {
                Json.decodeFromString(this.readText())
            }
        }

        TOKEN = getVar("DISCORD_TOKEN")

        jda = JDABuilder.createDefault(TOKEN).also {
            it.enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MEMBERS
            )
            it.enableCache(
                CacheFlag.CLIENT_STATUS,
                CacheFlag.ACTIVITY
            )
            it.setStatus(OnlineStatus.IDLE)
            it.setMemberCachePolicy(MemberCachePolicy.ALL)
        }.build()
        println("Started discord bot.")

        jda.addEventListener(ForceCheckListener)

        var i = 0
        while (true) {
            //just run forever basically
            Thread.sleep(1 * 60 * 1000)

            try {
                if (i == 6) {
                    DELETE_ALL.run()
                    beforeActivities.clear()
                    i = 0
                }

                dosomething()
                i++

            } catch (e: Throwable) {
                jda.guilds[0].textChannels[0].sendMessageEmbeds(
                    listOf(EmbedBuilder()
                        .setTitle("Ran into an exception!")
                        .setDescription(
                            e.message
                        )
                        .build())
                ).queue()
            }
        }
    }

    fun shutdown() {
        jda.shutdownNow()

        File(".DISCORD_REGISTERED").createIfFake().writeBytes(
            Json.encodeToString(info).toByteArray()
        )

        println("Stopped discord bot")
    }
}