package com.justai.jaicf.channel.telegram.activators

import com.justai.jaicf.activator.ActivatorFactory
import com.justai.jaicf.activator.BaseActivator
import com.justai.jaicf.activator.event.EventActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.channel.telegram.callbackQuery
import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.model.scenario.ScenarioModel

class WebAppCallbackActivator(model: ScenarioModel) : BaseActivator(model) {
    override val name = "webAppCallbackActivator"

    companion object : ActivatorFactory {
        override fun create(model: ScenarioModel) = WebAppCallbackActivator(model)
    }

    override fun provideRuleMatcher(botContext: BotContext, request: BotRequest) = ruleMatcher<TelegramRule> {
        EventActivatorContext(request.input).takeIf { request.telegram?.update?.message?.webAppData != null }
    }

    override fun canHandle(request: BotRequest): Boolean {
        return request.telegram?.callbackQuery?.update?.message?.webAppData != null
    }
}