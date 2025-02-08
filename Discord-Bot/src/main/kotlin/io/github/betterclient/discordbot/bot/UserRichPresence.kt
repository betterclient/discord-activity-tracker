package io.github.betterclient.discordbot.bot

import io.github.betterclient.discordbot.DiscordBot
import io.github.betterclient.discordbot.util.ineedhelp
import net.dv8tion.jda.api.entities.RichPresence
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO


data class UserRichPresence(val id: String, val activity: RichPresence) {
    fun toFile(): File {
        val img = BufferedImage(500, 250, BufferedImage.TYPE_INT_ARGB)
        val g2d = img.createGraphics()

        g2d.color = Color(54, 57, 63)
        g2d.fillRect(0, 0, 500, 250)

        g2d.color = Color.WHITE
        g2d.font = Font("Arial", Font.BOLD, 30)
        g2d.drawString(DiscordBot.jda.getUserById(id)!!.name, 20, 40)
        g2d.font = Font("Arial", Font.PLAIN, 20)

        g2d.drawString("${activity.type.name.ineedhelp()} ${activity.name}", 20, 100)
        g2d.drawString(activity.details?: "N/A", 20, 135)
        g2d.drawString(activity.state?: "N/A", 20, 170)

        val largeImage = activity.largeImage
        if (largeImage != null) {
            largeImage.text?.let { g2d.drawString(it, 20, 205) }
            val image = URI(largeImage.url)
            val url = if (largeImage.key.startsWith("mp:external/")) {
                URI(
                    URLDecoder.decode(
                        "https://${
                            largeImage.key.substring("mp:external/".length)
                                .split("/https/")[1]
                        }", StandardCharsets.UTF_8)
                ).toURL()
            } else {
                image.toURL()
            }
            g2d.drawImage(ImageIO.read(url), 350, 0, 150, 150, null)
        }

        g2d.font = Font("Comic Sans", Font.PLAIN, 20)
        g2d.color = Color.GREEN
        val timestamps = activity.timestamps
        if (timestamps != null) {
            val startInstant = Instant.ofEpochSecond(
                if (timestamps.end == 0L) {
                    timestamps.start
                } else {
                    timestamps.end - timestamps.start
                }
            )
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            val startReadable = formatter.format(startInstant)

            g2d.drawString("Playing for $startReadable (probably wrong)", 20, 240)
        }


        g2d.dispose()
        return File.createTempFile("userstatus", ".png").also {
            it.deleteOnExit()
            ImageIO.write(img, "png", it)
        }
    }
}