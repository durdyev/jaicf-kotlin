package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.logging.LogLevel.All
import com.github.kotlintelegrambot.webhook
import com.google.gson.Gson
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.invocationapi.InvocableBotChannel
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import com.justai.jaicf.channel.invocationapi.getRequestTemplateFromResources
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleAsyncChannelFactory
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.helpers.http.withTrailingSlash
import com.justai.jaicf.helpers.kotlin.PropertyWithBackingField
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TestHandler: Handler {
    override fun checkUpdate(update: Update): Boolean {
        print(update)
        return true
    }

    override suspend fun handleUpdate(bot: Bot, update: Update) {
        print(update)
    }

}

class TelegramChannel(
    override val botApi: BotApi,
    private val telegramBotToken: String,
    private val telegramApiUrl: String = "https://api.telegram.org/",
    private val requestExecutor: Executor = Executors.newFixedThreadPool(10)
) : JaicpCompatibleAsyncBotChannel, InvocableBotChannel {

    private val gson: Gson = Gson()
    private var liveChatProvider: JaicpLiveChatProvider? = null

    val bot: Bot = bot {
        val currentBot = this
        apiUrl = telegramApiUrl.withTrailingSlash()
        token = telegramBotToken
        logLevel = All()

        dispatch {
            addHandler(TestHandler())

            fun process(request: TelegramBotRequest) {
                requestExecutor.execute {
                    botApi.process(
                        request,
                        TelegramReactions(currentBot.build(), request, liveChatProvider),
                        RequestContext.fromHttp(request.update.httpBotRequest)
                    )
                }
            }

            text {
                process(TelegramTextRequest(update))
            }

            callbackQuery {
                process(TelegramCallbackQueryRequest(update))
            }
//
//            callbackQuery {
//                val message = callbackQuery.message ?: return@callbackQuery
//                process(TelegramQueryRequest(update, message, callbackQuery.data))
//            }

            location {
                process(TelegramLocationRequest(update, location))
            }

            contact {
                process(TelegramContactRequest(update, contact))
            }

            audio {
                process(TelegramAudioRequest(update, media))
            }

            document {
                process(TelegramDocumentRequest(update, media))
            }

            animation {
                process(TelegramAnimationRequest(update, media))
            }

            game {
                process(TelegramGameRequest(update, media))
            }

            photos {
                process(TelegramPhotosRequest(update, media))
            }

            sticker {
                process(TelegramStickerRequest(update, media))
            }

            video {
                process(TelegramVideoRequest(update, media))
            }

            videoNote {
                process(TelegramVideoNoteRequest(update, media))
            }

            voice {
                process(TelegramVoiceRequest(update, media))
            }

            preCheckoutQuery {
                process(TelegramPreCheckoutRequest(update, preCheckoutQuery))
            }

            successfulPayment {
                process(TelegramSuccessfulPaymentRequest(update, successfulPayment))
            }

        }
    }

    override fun process(request: HttpBotRequest): HttpBotResponse {
        val update: Update = gson.fromJson(request.receiveText(), Update::class.java)
        update.httpBotRequest = request
        return HttpBotResponse.accepted()
    }

    private fun generateRequestFromTemplate(request: InvocationRequest): String =
        getRequestTemplateFromResources(request, REQUEST_TEMPLATE_PATH)
            .replace("\"{{ timestamp }}\"", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
            .replace("{{ messageId }}", UUID.randomUUID().toString())

    override fun processInvocation(request: InvocationRequest, requestContext: RequestContext) {
        val generatedRequest: String = generateRequestFromTemplate(request)
        val update: Update = gson.fromJson(generatedRequest, Update::class.java) ?: return
        val message = update.message ?: return
        val telegramRequest = TelegramInvocationRequest.create(request, update, message) ?: return
        botApi.process(telegramRequest, TelegramReactions(bot, telegramRequest, liveChatProvider), requestContext)
    }

    fun run() {
        bot.startPolling()
    }

    companion object : JaicpCompatibleAsyncChannelFactory {
        override val channelType = "telegram"
        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider,
        ) = TelegramChannel(botApi, telegramBotToken = "", apiUrl).apply {
            this.liveChatProvider = liveChatProvider
        }

        private const val REQUEST_TEMPLATE_PATH = "/TelegramRequestTemplate.json"
    }

    class Jaicp(
        private val executor: Executor
    ) : JaicpCompatibleAsyncChannelFactory {

        override fun create(
            botApi: BotApi,
            apiUrl: String,
            liveChatProvider: JaicpLiveChatProvider
        ): JaicpCompatibleAsyncBotChannel =
            TelegramChannel(botApi, telegramBotToken = "", apiUrl, executor).apply {
                this.liveChatProvider = liveChatProvider
            }

        override val channelType: String = "telegram"
    }
}

internal var Update.httpBotRequest: HttpBotRequest by PropertyWithBackingField {
    HttpBotRequest("".byteInputStream())
}
