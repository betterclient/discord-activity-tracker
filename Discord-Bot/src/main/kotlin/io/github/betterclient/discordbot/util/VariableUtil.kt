package io.github.betterclient.discordbot.util

import java.io.File

fun getVar(name: String): String {
    return File(".env").readLines().first { it.startsWith(name) }.substring("$name=".length).trim()
}

fun String.ineedhelp(): String {
    return this[0].uppercase() + this.substring(1).lowercase()
}

fun File.createIfFake(): File {
    return also { if (!it.exists()) it.createNewFile() }
}