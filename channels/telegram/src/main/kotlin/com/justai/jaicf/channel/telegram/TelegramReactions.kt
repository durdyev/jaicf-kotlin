package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.payments.PaymentInvoiceInfo
import com.github.kotlintelegrambot.network.Response
import com.github.kotlintelegrambot.types.TelegramBotResult
import com.justai.jaicf.channel.jaicp.JaicpLiveChatProvider
import com.justai.jaicf.logging.AudioReaction
import com.justai.jaicf.logging.ButtonsReaction
import com.justai.jaicf.logging.ImageReaction
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.jaicp.JaicpCompatibleAsyncReactions
import java.io.File

val Reactions.telegram
    get() = this as? TelegramReactions

@Suppress("MemberVisibilityCanBePrivate")
class TelegramReactions(
    val api: Bot,
    val request: TelegramBotRequest,
    override val liveChatProvider: JaicpLiveChatProvider?
) : Reactions(), JaicpCompatibleAsyncReactions {

    val chatId = ChatId.fromId(request.update.message?.chat?.id?:request.clientId.toLong())
    private val messages = mutableListOf<Message>()

    private fun addResponse(pair: Pair<retrofit2.Response<Response<Message>?>?, Exception?>) {
        pair.first?.body()?.result?.let { message ->
            when (val index = messages.indexOfFirst { it.messageId == message.messageId }) {
                -1 -> messages.add(message)
                else -> messages.set(index, message)
            }
        }
    }

    private fun addResponse(messageResult: TelegramBotResult<Message>) {
        val message = messageResult.get()
        when (val index = messages.indexOfFirst { it.messageId == message.messageId }) {
            -1 -> messages.add(message)
            else -> messages.set(index, message)
        }
    }

    override fun say(text: String): SayReaction {
        return sendMessage(text)
    }

    override fun buttons(vararg buttons: String): ButtonsReaction {
        messages.lastOrNull()?.let { message ->
            val keyboard = message.replyMarkup?.inlineKeyboard?.toMutableList() ?: mutableListOf()
            keyboard.addAll(buttons.map { listOf(InlineKeyboardButton.CallbackData(it, callbackData = it)) })

            api.editMessageReplyMarkup(
                chatId,
                message.messageId,
                replyMarkup = InlineKeyboardMarkup.create(keyboard)
            ).also { addResponse(it) }
        }

        return ButtonsReaction.create(buttons.asList())
    }

    fun say(text: String, inlineButtons: List<String>) = api.sendMessage(
        chatId,
        text,
        replyMarkup = InlineKeyboardMarkup.create(
            listOf(inlineButtons.map { InlineKeyboardButton.CallbackData(it, callbackData = it) })
        ).also {
            SayReaction.create(text)
            ButtonsReaction.create(inlineButtons)
        }
    )

    fun say(
        text: String,
        parseMode: ParseMode? = null,
        disableWebPagePreview: Boolean? = null,
        disableNotification: Boolean? = null,
        replyMarkup: KeyboardReplyMarkup
    ) = sendMessage(text, parseMode, disableWebPagePreview, disableNotification, replyMarkup)

    fun sendMessage(
        text: String,
        parseMode: ParseMode? = null,
        disableWebPagePreview: Boolean? = null,
        disableNotification: Boolean? = null,
        replyMarkup: KeyboardReplyMarkup? = null
    ): SayReaction {
        api.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = parseMode,
            disableWebPagePreview = disableWebPagePreview,
            disableNotification = disableNotification,
            replyMarkup = replyMarkup
        ).also { addResponse(it) }

        return SayReaction.create(text)
    }

    override fun image(url: String): ImageReaction {
        return sendPhoto(url)
    }

    fun image(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = sendPhoto(
        url, caption, parseMode, disableNotification, replyToMessageId,
        replyMarkup
    )

    fun sendPhoto(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ): ImageReaction {
        api.sendPhoto(
            chatId = chatId,
            photo = TelegramFile.ByUrl(url),
            caption = caption,
            parseMode = parseMode,
            disableNotification = disableNotification,
            replyToMessageId = replyToMessageId,
            replyMarkup = replyMarkup
        ).also { addResponse(it) }

        return ImageReaction.create(url)
    }

    fun sendVideo(
        url: String,
        duration: Int? = null,
        width: Int? = null,
        height: Int? = null,
        caption: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null,
        allowSendingWithoutReply: Boolean = false
    ) = api.sendVideo(
        chatId = chatId,
        video = TelegramFile.ByUrl(url),
        duration = duration,
        width = width,
        height = height,
        caption = caption,
        disableNotification = disableNotification,
        replyMarkup = replyMarkup
    ).also { addResponse(it) }

    fun sendVoice(
        url: String,
        duration: Int? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ) = api.sendVoice(
        chatId,
        url,
        duration = duration,
        disableNotification = disableNotification,
        replyToMessageId = replyToMessageId,
        replyMarkup = replyMarkup
    ).also { addResponse(it) }

    override fun audio(url: String): AudioReaction {
        return sendAudio(url)
    }

    fun sendAudio(
        url: String,
        duration: Int? = null,
        performer: String? = null,
        title: String? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null
    ): AudioReaction {
        api.sendAudio(
            chatId = chatId,
            audio = TelegramFile.ByUrl(url),
            duration = duration,
            performer = performer,
            title = title,
            disableNotification = disableNotification,
            replyToMessageId = replyToMessageId,
            replyMarkup = replyMarkup
        ).also { addResponse(it) }

        return AudioReaction.create(url)
    }

    fun sendDocument(
        url: String,
        caption: String? = null,
        parseMode: ParseMode? = null,
        disableContentTypeDetection: Boolean? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null,
        allowSendingWithoutReply: Boolean? = null,
        mimeType: String? = null,
    ) = api.sendDocument(
        chatId = chatId,
        document = TelegramFile.ByUrl(url),
        caption = caption,
        parseMode = parseMode,
        disableContentTypeDetection = disableContentTypeDetection,
        disableNotification = disableNotification,
        replyToMessageId = replyToMessageId,
        allowSendingWithoutReply = allowSendingWithoutReply,
        replyMarkup = replyMarkup,
        mimeType = mimeType
    ).also { addResponse(it) }

    fun sendVenue(
        latitude: Float,
        longitude: Float,
        title: String,
        address: String,
        foursquareId: String? = null,
        foursquareType: String? = null
    ) = api.sendVenue(
        chatId,
        latitude,
        longitude,
        title,
        address,
        foursquareId,
        foursquareType
    ).also { addResponse(it) }

    fun sendContact(
        phoneNumber: String,
        firstName: String,
        lastName: String? = null,
        disableNotification: Boolean? = null,
    ) = api.sendContact(
        chatId,
        phoneNumber,
        firstName,
        lastName,
        disableNotification,
    ).also { addResponse(it) }

    fun sendLocation(
        latitude: Float,
        longitude: Float,
        livePeriod: Int? = null,
        disableNotification: Boolean? = null
    ) = api.sendLocation(
        chatId,
        latitude,
        longitude,
        livePeriod,
        disableNotification,
    ).also { addResponse(it) }

    fun sendMediaGroup(
        mediaGroup: MediaGroup,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null
    ) = api.sendMediaGroup(chatId, mediaGroup, disableNotification)

    fun sendVideoNote(
        file: File,
        duration: Int? = null,
        length: Int? = null,
        disableNotification: Boolean? = null,
        replyToMessageId: Long? = null,
        replyMarkup: ReplyMarkup? = null,
    ) = api.sendVideoNote(
        chatId = chatId,
        videoNote = TelegramFile.ByFile(file),
        duration = duration,
        length = length,
        disableNotification = disableNotification,
        protectContent = false,
        replyToMessageId = replyToMessageId,
        allowSendingWithoutReply = false,
        replyMarkup = replyMarkup,
    ).also { addResponse(it) }

    fun sendInvoice(
        paymentInvoiceInfo: PaymentInvoiceInfo,
        disableNotification: Boolean? = null,
    ) = api.sendInvoice(
        chatId,
        paymentInvoiceInfo,
        disableNotification,
    ).also { addResponse(it) }

    fun answerPreCheckoutQuery(preCheckoutQueryId: String, ok: Boolean, errorMessage: String? = null) =
        api.answerPreCheckoutQuery(preCheckoutQueryId, ok, errorMessage)
}
