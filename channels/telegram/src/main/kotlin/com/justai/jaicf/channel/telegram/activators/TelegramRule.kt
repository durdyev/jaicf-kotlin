package com.justai.jaicf.channel.telegram.activators

import com.github.kotlintelegrambot.entities.Message
import com.justai.jaicf.builder.ActivationRulesBuilder
import com.justai.jaicf.channel.telegram.callbackQuery
import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.model.activation.ActivationRuleAdapter

open class TelegramRule(val telegramMessage: (Message) -> Boolean) : ActivationRuleAdapter()
open class TelegramWebAppCallbackRule() : TelegramRule({ it.webAppData != null })

fun ActivationRulesBuilder.webAppCallback() = rule(TelegramWebAppCallbackRule())