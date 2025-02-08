package io.github.betterclient.slackbot

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.socket_mode.SocketModeApp
import io.github.betterclient.discordbot.DiscordBot
import io.github.betterclient.discordbot.util.getVar
import java.io.File

const val CHANNEL_ID = "C08CQUK0U3T"

object SlackBot {
    lateinit var app: App

    fun start() {
        app = App(AppConfig().also {
            it.singleTeamBotToken = getVar("SLACK_BOT_TOKEN")
            it.signingSecret = getVar("SLACK_SIGNING_SECRET")
        })

        SocketModeApp(getVar("SLACK_APP_TOKEN"), app).startAsync()

        println("Started slack bot.")

        DiscordBot.REGISTRATION_LISTENER = RegistrationListener
        DiscordBot.MESSAGE_LISTENER = this::sendMessage
        DiscordBot.SLACK_ID_VERIFIER = { it0 ->
            val usersProfileGet = app.client.usersProfileGet {
                it.token(getVar("SLACK_BOT_TOKEN"))
                it.user(it0)
            }

            usersProfileGet.isOk
        }
        DiscordBot.DELETE_ALL = Runnable { RegistrationListener.deleteAllMessages() }

        RegistrationListener.deleteAllMessages() //delete as many messages as you can in start
    }

    private fun sendMessage(message: String, file: List<File>) {
        var compiled = ""
        for (file1 in file) {
            compiled += "<${app.client.filesUploadV2 {
                it.token(getVar("SLACK_BOT_TOKEN"))
                it.file(file1)
                it.filename(file1.name)
            }.file.permalink}| >"
        }

        app.client.chatPostMessage {
            it.token(getVar("SLACK_BOT_TOKEN"))
            it.linkNames(true)
            it.channel(CHANNEL_ID)
            it.mrkdwn(true)
            it.text("$message $compiled")
        }

        file.forEach { it.delete() }
    }

    fun shutdown() {
        println("Stopped slack bot") //this doesn't do anything lol
    }
}