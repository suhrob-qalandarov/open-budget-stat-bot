package uz.shavkat.opstatbot

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import uz.shavkat.opstatbot.service.OBReportBot

@Configuration
class BotConfig {
    @Bean
    fun telegramBotsApi(reportBot: OBReportBot): TelegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        .apply { registerBot(reportBot) }
}