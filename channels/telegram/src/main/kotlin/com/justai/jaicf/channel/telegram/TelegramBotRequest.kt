package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.files.*
import com.github.kotlintelegrambot.entities.payments.PreCheckoutQuery
import com.github.kotlintelegrambot.entities.payments.SuccessfulPayment
import com.github.kotlintelegrambot.entities.stickers.Sticker
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.channel.invocationapi.InvocationEventRequest
import com.justai.jaicf.channel.invocationapi.InvocationQueryRequest
import com.justai.jaicf.channel.invocationapi.InvocationRequest
import kotlin.random.Random

val BotRequest.telegram get() = this as? TelegramBotRequest

val TelegramBotRequest.text get() = this as? TelegramTextRequest
val TelegramBotRequest.callback get() = this as? TelegramQueryRequest
val TelegramBotRequest.location get() = this as? TelegramLocationRequest
val TelegramBotRequest.contact get() = this as? TelegramContactRequest
val TelegramBotRequest.audio get() = this as? TelegramAudioRequest
val TelegramBotRequest.document get() = this as? TelegramDocumentRequest
val TelegramBotRequest.animation get() = this as? TelegramAnimationRequest
val TelegramBotRequest.game get() = this as? TelegramGameRequest
val TelegramBotRequest.photos get() = this as? TelegramPhotosRequest
val TelegramBotRequest.sticker get() = this as? TelegramStickerRequest
val TelegramBotRequest.video get() = this as? TelegramVideoRequest
val TelegramBotRequest.videoNote get() = this as? TelegramVideoNoteRequest
val TelegramBotRequest.voice get() = this as? TelegramVoiceRequest
val TelegramBotRequest.preCheckout get() = this as? TelegramPreCheckoutRequest
val TelegramBotRequest.successfulPayment get() = this as? TelegramSuccessfulPaymentRequest
val TelegramBotRequest.callbackQuery get() = this as? TelegramCallbackQueryRequest

internal val Message.clientId get() = chat.id.toString()

interface TelegramBotRequest : BotRequest {
    val update: Update
    val chatId: Long? get() = update.message?.chat?.id
}

data class TelegramTextRequest(
    override val update: Update
) : TelegramBotRequest, QueryBotRequest(clientId = update.message?.clientId!!, input = update.message?.text.toString())

data class TelegramQueryRequest(
    override val update: Update,
    val message: Message,
    val data: String
) : TelegramBotRequest, QueryBotRequest(clientId = update.message?.clientId!!, input = data)

data class TelegramLocationRequest(
    override val update: Update,
    val location: Location
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.LOCATION)

data class TelegramContactRequest(
    override val update: Update,
    val contact: Contact
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.CONTACT)

data class TelegramAudioRequest(
    override val update: Update,
    val audio: Audio
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.AUDIO)

data class TelegramDocumentRequest(
    override val update: Update,
    val document: Document
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.DOCUMENT)

data class TelegramAnimationRequest(
    override val update: Update,
    val animation: Animation
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.ANIMATION)

data class TelegramGameRequest(
    override val update: Update,
    val game: Game
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.GAME)

data class TelegramPhotosRequest(
    override val update: Update,
    val photos: List<PhotoSize>
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.PHOTOS)

data class TelegramStickerRequest(
    override val update: Update,
    val sticker: Sticker
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.STICKER)

data class TelegramVideoRequest(
    override val update: Update,
    val video: Video
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.VIDEO)

data class TelegramVideoNoteRequest(
    override val update: Update,
    val videoNote: VideoNote
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.VIDEO_NOTE)

data class TelegramVoiceRequest(
    override val update: Update,
    val voice: Voice
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!, input = TelegramEvent.VOICE)

data class TelegramPreCheckoutRequest(
    override val update: Update,
    val preCheckoutQuery: PreCheckoutQuery
) : TelegramBotRequest, EventBotRequest(clientId = preCheckoutQuery.from.id.toString(), input = TelegramEvent.PRE_CHECKOUT)

data class TelegramSuccessfulPaymentRequest(
    override val update: Update,
    val successfulPayment: SuccessfulPayment
) : TelegramBotRequest, EventBotRequest(clientId = update.message?.clientId!!!!, input = TelegramEvent.SUCCESSFUL_PAYMENT)

data class TelegramCallbackQueryRequest(
    override val update: Update
) : TelegramBotRequest, QueryBotRequest(clientId = update.callbackQuery?.from?.id.toString(), input = TelegramEvent.CALLBACK_QUERY)


interface TelegramInvocationRequest : TelegramBotRequest, InvocationRequest {
    companion object {
        fun create(r: InvocationRequest, update: Update, message: Message): TelegramInvocationRequest? = when (r) {
            is InvocationEventRequest -> TelegramInvocationEventRequest(update, r.clientId, r.input, r.requestData)
            is InvocationQueryRequest -> TelegramInvocationQueryRequest(update, r.clientId, r.input, r.requestData)
            else -> null
        }
    }
}

data class TelegramInvocationEventRequest(
    override val update: Update,
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : TelegramInvocationRequest, InvocationEventRequest(clientId, input, requestData)

data class TelegramInvocationQueryRequest(
    override val update: Update,
    override val clientId: String,
    override val input: String,
    override val requestData: String
) : TelegramInvocationRequest, InvocationQueryRequest(clientId, input, requestData)
