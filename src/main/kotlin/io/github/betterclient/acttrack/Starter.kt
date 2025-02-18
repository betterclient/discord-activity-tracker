package io.github.betterclient.acttrack

import io.github.betterclient.discordbot.DiscordBot
import io.github.betterclient.discordbot.util.createIfFake
import io.github.betterclient.slackbot.SlackBot
import java.io.File
import java.io.PrintStream
import java.lang.Thread

var writer = StringBuilder()

fun main() {
    System.setOut(object : PrintStream(System.out) {
        override fun print(obj: String?) {
            super.print(obj)
            writer.append("$obj\n")
        }
    })

    println("Started!")
    println()

    ThreadWrapper("DiscordBot") {
        DiscordBot.start()
    }.start()

    ThreadWrapper("SlackBot") {
        SlackBot.start()
    }.start()

    ThreadWrapper("Log file re-setter") {
        while(true) {
            Thread.sleep(30 * 60 * 1000)

            File("log@${System.currentTimeMillis()}.txt").createIfFake().writeBytes(writer.toString().toByteArray())
            writer = StringBuilder()
            System.gc()
        }
    }.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        println()
        println("Shutting down!")
        DiscordBot.shutdown()
        SlackBot.shutdown()
        File("log@${System.currentTimeMillis()}.txt").createIfFake().writeBytes(writer.toString().toByteArray())
    })
}

class ThreadWrapper(name: String, private val runnable: Runnable) : Thread(name) {
    override fun run() {
        runnable.run()
    }
}