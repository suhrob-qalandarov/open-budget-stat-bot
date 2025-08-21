package uz.shavkat.opstatbot.configData

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.properties.Delegates

@Component
@ConfigurationProperties(prefix = "telegram")
class TelegramProperties {
    lateinit var publicId: List<String>
    lateinit var token: String
    lateinit var username: String
    var winnerCount by Delegates.notNull<Int>()
}