package io.github.betterclient.slackbot

import com.slack.api.methods.MethodsClient
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.model.block.ActionsBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ButtonElement
import io.github.betterclient.discordbot.DiscordBot
import io.github.betterclient.discordbot.register.Listener
import io.github.betterclient.discordbot.register.RegistrationInfo
import io.github.betterclient.discordbot.util.getVar
import io.github.betterclient.slackbot.SlackBot.app
import java.util.regex.Pattern

object RegistrationListener : Listener {
    override fun apply(info: RegistrationInfo, accept: Runnable, reject: Runnable, rejectBlock: Runnable) {
        println("Confirming request: $info")
        val methods = app.slack.methods()

        val array = sendMessage(methods, info)

        awaitResponse(array, methods, info, accept, reject, rejectBlock)
    }

    private fun awaitResponse(
        array0: Pair<Array<String>, ChatPostMessageResponse>,
        methods: MethodsClient,
        info: RegistrationInfo,
        accept: Runnable,
        reject: Runnable,
        rejectBlock: Runnable
    ) {
        val array = array0.first

        var hasResponse = false
        val arrayNew = Array<Pattern>(3) {
            when(it) {
                0 -> Pattern.compile("^" + Pattern.quote(array[0]) + "$")
                1 -> Pattern.compile("^" + Pattern.quote(array[1]) + "$")
                2 -> Pattern.compile("^" + Pattern.quote(array[2]) + "$")
                else -> Pattern.compile("^" + Pattern.quote(array[0]) + "$")
            }
        }

        app.blockAction(arrayNew[0]) { _, ctx ->
            if (hasResponse) return@blockAction ctx.ack()

            if (ctx.requestUserId.uppercase() == info.id.uppercase()) {
                methods.chatUpdate {
                    it.token(getVar("SLACK_BOT_TOKEN"))
                    it.channel(CHANNEL_ID)
                    it.ts(array0.second.ts) //ts(this) is tuff man
                    it.text("Registered.")
                    it.blocks(
                        listOf(SectionBlock.builder().text(PlainTextObject("Registered.", false)).build())
                    )
                }
                println("Registered user $info")
                accept.run()
                hasResponse = true
            }
            ctx.ack()
        }

        app.blockAction(arrayNew[1]) { _, ctx ->
            if (hasResponse) return@blockAction ctx.ack()

            if (ctx.requestUserId.uppercase() == info.id.uppercase()) {
                methods.chatUpdate {
                    it.token(getVar("SLACK_BOT_TOKEN"))
                    it.channel(CHANNEL_ID)
                    it.ts(array0.second.ts)
                    it.text("Rejected.")
                    it.blocks(
                        listOf(SectionBlock.builder().text(PlainTextObject("Rejected.", false)).build())
                    )
                }
                println("Rejected user $info")
                reject.run()
                hasResponse = true
            }
            ctx.ack()
        }

        app.blockAction(arrayNew[2]) { _, ctx ->
            if (hasResponse) return@blockAction ctx.ack()

            if (ctx.requestUserId.uppercase() == info.id.uppercase()) {
                methods.chatUpdate {
                    it.token(getVar("SLACK_BOT_TOKEN"))
                    it.channel(CHANNEL_ID)
                    it.ts(array0.second.ts)
                    it.text("Blocked.")
                    it.blocks(
                        listOf(SectionBlock.builder().text(PlainTextObject("Blocked.", false)).build())
                    )
                }
                rejectBlock.run()
                hasResponse = true
                println("Blocked user $info")

                DiscordBot.info.blocked_users.
                computeIfAbsent(ctx.requestUserId.uppercase())
                { mutableListOf() } += info.discordID.uppercase()
            }
            ctx.ack()
        }
    }

    private fun sendMessage(
        methods: MethodsClient,
        info: RegistrationInfo
    ): Pair<Array<String>, ChatPostMessageResponse> {
        val buttonIDS = Array(3) {
            return@Array when(it) {
                0 -> "thats-me${System.currentTimeMillis()}-for${info.id}-${info.discordID}"
                1 -> "thats-not-me${System.currentTimeMillis()}-for${info.id}-${info.discordID}"
                else -> "thats-not-me-block-them${System.currentTimeMillis()}-for${info.id}-${info.discordID}"
            }
        }

        val chatPostMessage = methods.chatPostMessage {
            it.token(getVar("SLACK_BOT_TOKEN"))
            it.channel(CHANNEL_ID)
            it.linkNames(true)
            it.blocks(
                listOf(
                    SectionBlock.builder().text(
                        MarkdownTextObject(
                            "<@${info.id}>, is this you? -> ${info.discordName} (${info.discordID})",
                            true
                        )
                    ).build(),
                    ActionsBlock.builder().elements(
                        listOf(
                            ButtonElement
                                .builder()
                                .text(PlainTextObject("Yes!", false))
                                .style("primary")
                                .actionId(buttonIDS[0])
                                .build(),
                            ButtonElement
                                .builder()
                                .text(PlainTextObject("No!", false))
                                .style("danger")
                                .actionId(buttonIDS[1])
                                .build(),
                            ButtonElement
                                .builder()
                                .text(PlainTextObject("No & Block Future requests", false))
                                .style("danger")
                                .actionId(buttonIDS[2])
                                .build()
                        )
                    ).build()
                )
            )
        }

        return Pair(buttonIDS, chatPostMessage)
    }

    /**
     * Nuke all bot messages
     */
    fun deleteAllMessages() {
        System.gc()

        app.slack.methods().conversationsHistory {
            it.token(getVar("SLACK_BOT_TOKEN"))
            it.channel(CHANNEL_ID)
        }.messages?.forEach { it0 ->
            if (it0.appId == "A08CCBD83DZ" || it0.user == "U08CR5NNSG1") {
                println("Deleting ${it0.ts}")
                app.slack.methods().chatDelete {
                    it.token(getVar("SLACK_BOT_TOKEN"))
                    it.channel(CHANNEL_ID)
                    it.ts(it0.ts)
                }
            }
        }

        Thread.sleep(2000)
        app.slack.methods().chatPostMessage {
            it.token(getVar("SLACK_BOT_TOKEN"))
            it.channel(CHANNEL_ID)
            it.text("Go to [the discord server](https://discord.gg/Gbg8CSCgy6) to link your account!")
            it.mrkdwn(true)
        }
    }
}