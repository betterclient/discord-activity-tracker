package io.github.betterclient.acttrack

import io.github.betterclient.discordbot.DiscordBot
import io.github.betterclient.slackbot.SlackBot
import java.lang.Thread

fun main() {
    println("Started!")
    println()

    ThreadWrapper("DiscordBot") {
        DiscordBot.start()
    }.start()

    ThreadWrapper("SlackBot") {
        SlackBot.start()
    }.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        println()
        println("Shutting down!")
        DiscordBot.shutdown()
        SlackBot.shutdown()
    })
}

class ThreadWrapper(name: String, private val runnable: Runnable) : Thread(name) {
    override fun run() {
        runnable.run()
    }
}