package com.justai.jaicf.channel.telegram.handlers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Update
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.channel.telegram.TelegramCallbackQueryRequest
import com.justai.jaicf.channel.telegram.TelegramReactions
import com.justai.jaicf.channel.telegram.httpBotRequest
import com.justai.jaicf.context.RequestContext

class WebAppDataHandler(val botApi: BotApi) : Handler {
    override fun checkUpdate(update: Update): Boolean {
        return update.message?.webAppData != null
    }

    override suspend fun handleUpdate(bot: Bot, update: Update) {
        var request: TelegramCallbackQueryRequest? = null
        request = TelegramCallbackQueryRequest(update)
        botApi.process(
            request,
            TelegramReactions(bot, request, null),
            RequestContext.fromHttp(request.update.httpBotRequest)
        )
    }
}