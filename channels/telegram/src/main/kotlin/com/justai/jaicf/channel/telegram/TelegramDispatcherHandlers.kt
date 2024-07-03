package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.handlers.Handler
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.payments.SuccessfulPayment

internal fun Dispatcher.successfulPayment(body: SuccessfulPaymentHandler.SuccessfulPaymentHandlerEnvironment.() -> Unit) {
    addHandler(SuccessfulPaymentHandler(body))
}

internal class SuccessfulPaymentHandler(
    private val handleSuccessfulPayment: SuccessfulPaymentHandlerEnvironment.() -> Unit
) : Handler {

    override fun checkUpdate(update: Update): Boolean {
        return update.message?.successfulPayment != null
    }

    override suspend fun handleUpdate(bot: Bot, update: Update) {
        val message = update.message ?: return
        val successfulPayment = message.successfulPayment ?: return

        val environment = SuccessfulPaymentHandlerEnvironment(
            bot = bot,
            update = update,
            message = message,
            successfulPayment = successfulPayment
        )

        environment.handleSuccessfulPayment()
    }

    data class SuccessfulPaymentHandlerEnvironment(
        val bot: Bot,
        val update: Update,
        val message: Message,
        val successfulPayment: SuccessfulPayment
    )
}
