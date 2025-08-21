package uz.shavkat.opstatbot.service

import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import uz.shavkat.opstatbot.OBFeignClient
import uz.shavkat.opstatbot.PublicId
import uz.shavkat.opstatbot.ReportSql
import uz.shavkat.opstatbot.configData.TelegramProperties
import uz.shavkat.opstatbot.entity.PositionInfo
import uz.shavkat.opstatbot.entity.TgUsers
import uz.shavkat.opstatbot.repository.PositionInfoRepository
import uz.shavkat.opstatbot.repository.TgUsersRepository
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

@Service
class OBReportBot(
//    @Value("\${telegram.token}") private val token: String,
//    @Value("\${telegram.username}") private val username: String,
//    @Value("\${telegram.publicId}") private val publicId: List<String>,
//    @Value("\${telegram.winnerCount}") private val winnerCount: Int,
    private val properties: TelegramProperties,
    private val feign: OBFeignClient,
    private val tgUsersRepository: TgUsersRepository,
    private val positionInfoRepository: PositionInfoRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val reportService: ReportService,

    ) : TelegramLongPollingBot(properties.token) {
    val logger: Logger = Logger.getAnonymousLogger()
    override fun getBotUsername() = properties.username

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.chatId != -1001756782480) { // Obodon gruppa
            val message = update.message
            val chatId = message.chatId

            val tgUser = tgUsersRepository.findByChatId(chatId) ?: run {
                tgUsersRepository.save(
                    TgUsers(
                        "${message.from.firstName} ${message.from.lastName ?: ""}",
                        message.from.userName,
                        message.from.id,
                        message.chatId,
                        false,
                        if (message.hasText()) message.text else "no text"
                    )
                )
            }

            tgUser.tgUserId = message.from.id

            tgUser.lastActionDate = Date()
            tgUser.lastAction = if (message.hasText()) message.text else "no text"
            tgUsersRepository.save(tgUser)

            logger.info("${tgUser.name} active: ${tgUser.enabled} xabar oldi: ${message.text}")
            var responseText = "Botdan foydalanish uchun @SuhrobQalandarov ga murojaat qiling!"
            if (tgUsersRepository.existsByChatIdAndEnabledTrue(chatId)) {
                responseText = if (message.hasText()) {
                    val messageText = message.text
                    when (messageText) {
                        "/start" -> "Добро пожаловать!"
                        "\uD83D\uDCC9 Taqqoslama" -> compareRateReport()
                        "\uD83D\uDCC8 Hisobot" -> report()
                        "\uD83D\uDCCA Kunlik" -> daily()
                        "admin:unverified" -> getUnVerified(chatId)
                        "admin:verified" -> getVerified(chatId)
                        "analise_day" -> {
                            reportService.analyzeDay()
                            "analysed"
                        }

                        else -> {
                            val result: String = if (messageText.contains("admin:enable")) {
                                try {
                                    val newChatId = messageText.split("-")[1].toLong()
                                    enableUser(newChatId, chatId)
                                } catch (e: Exception) {
                                    e.stackTraceToString().substring(0, 100)
                                }
                            } else if (messageText.contains("admin:disable")) {
                                try {
                                    val newChatId = messageText.split("-")[1].toLong()
                                    enableUser(newChatId, chatId, false)
                                } catch (e: Exception) {
                                    e.stackTraceToString().substring(0, 100)
                                }
                            } else {
                                "Вы написали: *$messageText*"
                            }
                            result
                        }
                    }
                } else {
                    "Я понимаю только текст"
                }
            } else {
                println("Unregistered chatId: $chatId")
            }
            sendNotification(chatId, responseText)
        }
    }

    private fun report(): String {

        val builder: StringBuilder = java.lang.StringBuilder()
        builder.append("*\uD83D\uDCCA Tuman bo'yicha hisobot: \n\n*")
        val result = feign.getReport()
        result.content!!.forEachIndexed { index, it ->
            val sign = if (index <= properties.winnerCount) {
                if (index == 0) "\uD83E\uDD47" else if (index == 1) "\uD83E\uDD48" else if (index == 2) "\uD83E\uDD49" else "\uD83C\uDFC5"
            } else "\uD83D\uDE15"

            if (properties.publicId.contains(it.publicId)) {
                val maxLength = it.description!!.length
                builder.append("*$sign${it.voteCount} | (${it.quarterName}) ${ it.description!!.substring(
                    0,
                    if (maxLength > 50) 50 else maxLength
                )}*")
            } else {
                val maxLength = it.description!!.length
                builder.append("$sign${it.voteCount} | (${it.quarterName}) ${ it.description!!.substring(
                    0,
                    if (maxLength > 50) 50 else maxLength
                )}")
            }

            builder.appendLine()
            if (index == 8) {
                builder.append("\n__________________\n")
            }
        }
        builder.appendLine()
        return builder.toString()
    }

    private fun daily(): String {
        val now = LocalDateTime.now()

        val startDay = now.with(LocalTime.MIN)
        val endDay = now.with(LocalTime.MAX)

        val publicIds = jdbcTemplate.query(
            "select public_id from report r  where r.date between '$startDay' and '$endDay' group by (public_id)",
            BeanPropertyRowMapper(PublicId::class.java)
        )
        val builder: StringBuilder = java.lang.StringBuilder("\uD83D\uDCCA Kunlik to'plangan ovozlar soni: ")
        builder.appendLine()
        val resultMap = mutableMapOf<String, Int>()
        publicIds.forEach { result ->

            val firstRowRes = jdbcTemplate.query(
                "select * from report r where r.date between '$startDay' and '$endDay'" +
                        " and public_id = '${result.publicId}' order by id asc limit 1",
                BeanPropertyRowMapper(ReportSql::class.java)
            )
            val lastRowRes = jdbcTemplate.query(
                "select * from report r where r.date between '$startDay' and '$endDay'" +
                        " and public_id = '${result.publicId}' order by id desc limit 1",
                BeanPropertyRowMapper(ReportSql::class.java)
            )

            val firstRow = firstRowRes.first()
            val lastRow = lastRowRes.first()

            resultMap[firstRow.description!!.substring(0, 45)] = lastRow.voteCount!! - firstRow.voteCount!!
        }

        resultMap.toList().sortedByDescending { it.second }.map {
            builder.append("\uD83D\uDCCC ${it.second} - ${it.first}")
            builder.appendLine()
        }

        return builder.toString()
    }


    private fun compareRateReport(): String {

        val builder: StringBuilder = java.lang.StringBuilder()
        builder.append("*\uD83D\uDCCB Tuman bo'yicha taqqoslama hisobot: \n\n*")
        val result = feign.getReport()
        val ourVotes = result.content!!.last { properties.publicId.contains(it.publicId) }.voteCount!!
        result.content!!.forEachIndexed { index, it ->
            val sign =
                if (index == 0) "\uD83E\uDD47" else if (index == 1) "\uD83E\uDD48" else if (index == 2) "\uD83E\uDD49" else "\uD83C\uDFC5"
            if (properties.publicId.contains(it.publicId)) {
                val maxLength = it.description!!.length
                builder.append(
                    "*$sign${it.voteCount?.minus(ourVotes)} | ${
                        it.description!!.substring(
                            0,
                            if (maxLength > 50) 50 else maxLength
                        )
                    }*"
                )
            } else {
                val maxLength = it.description!!.length
                builder.append(
                    "$sign${it.voteCount?.minus(ourVotes)} | ${
                        it.description!!.substring(
                            0,
                            if (maxLength > 50) 50 else maxLength
                        )
                    }"
                )
            }

            builder.appendLine()
            if (index + 1 == properties.winnerCount) {
                builder.append("\n__________________\n")
            }
        }
        builder.appendLine()
        return builder.toString()
    }

    private fun enableUser(chatId: Long, currentChatId: Long, enable: Boolean? = true): String {
        return if (tgUsersRepository.existsByChatIdAndEnabledTrueAndAdminTrue(currentChatId)) {
            tgUsersRepository.findByChatId(chatId)?.let {
                it.enabled = enable
                tgUsersRepository.save(it)
                sendNotification(
                    it.chatId!!,
                    if (enable == true) "Botdan foydalanish uchun admin tomonidan ruxsat berildi!\n\n/start ni bosing va foydalanishni boshlang!" else "Siz bloklandingiz!"
                )
            }
            "Done"
        } else "Command not found"
    }

    private fun getVerified(currentChatId: Long): String {
        return if (tgUsersRepository.existsByChatIdAndEnabledTrueAndAdminTrue(currentChatId)) {
            val builder: StringBuilder = java.lang.StringBuilder()
            builder.append("Verified users:")
            builder.appendLine()
            tgUsersRepository.findAllByEnabled(true).forEachIndexed { index, tgUsers ->

                builder.append("${index + 1}. ${tgUsers.name} ${tgUsers.username} ${tgUsers.chatId}")
                builder.appendLine()
            }
            builder.toString()
        } else "Command not found"

    }

    private fun getUnVerified(currentChatId: Long): String {
        return if (tgUsersRepository.existsByChatIdAndEnabledTrueAndAdminTrue(currentChatId)) {
            val builder: StringBuilder = java.lang.StringBuilder()
            builder.append("Unverified users:")
            builder.appendLine()
            tgUsersRepository.findAllByEnabled(false).forEachIndexed { index, tgUsers ->

                builder.append("${index + 1}. ${tgUsers.name} ${tgUsers.username} ${tgUsers.chatId}")
                builder.appendLine()
            }
            builder.toString()
        } else "Command not found"
    }


    var lastPos: AtomicInteger = AtomicInteger(0)

    @Scheduled(initialDelay = 1000, fixedRate = 1000 * 60)
    fun notifyAboutChangeRate() {
        val position = positionInfoRepository.findAll().firstOrNull() ?: run {
            positionInfoRepository.save(
                PositionInfo(
                    currentPosition = -1,
                    currentVotes = -2
                )
            )
        }
        val list = feign.getReport().content!!
        var currentPosition = position.currentPosition
        var currentVotes = position.currentVotes


        list.forEachIndexed { index, project ->
            if (lastPos.get() == 0 && properties.publicId.contains(project.publicId)) {
                lastPos = AtomicInteger(index + 1)
                logger.info("last position set: $lastPos")
                notifyAboutChangeRate()
            }
            if (properties.publicId.contains(project.publicId)) {
                currentPosition = index + 1
                currentVotes = project.voteCount!!
                logger.info("\ncurrent position set: $currentPosition \n last position: $lastPos")

            }
        }

        if (currentPosition != -1 && lastPos.get() != currentPosition) {

            val smile = if ((lastPos.get() - currentPosition > 0)) "\uD83E\uDD73\uD83E\uDD73" else "☹\uFE0F☹\uFE0F"

            val message = """
                $smile -  Pozitsiya o'zgardi!
                🏅 Joriy o'rin: $currentPosition
                🗑 Ovozlar soni: $currentVotes
            """.trimIndent()
            logger.info("o'rin almashdi: $message")


            lastPos = AtomicInteger(currentPosition)

            position.currentPosition = currentPosition
            position.currentVotes = currentVotes
            positionInfoRepository.save(position)

            tgUsersRepository.findAllByEnabled(true).forEach { user ->
                sendNotification(user.chatId!!.toLong(), message)
            }
        }


    }

    private fun sendNotification(chatId: Long, responseText: String) {
        val responseMessage = SendMessage(chatId.toString(), responseText.replace("_", "-"))
        responseMessage.enableMarkdown(true)
        responseMessage.enableHtml(true)
        responseMessage.replyMarkup = null
        responseMessage.replyMarkup = getReplyMarkup(
            chatId,
            mutableListOf(listOf("\uD83D\uDCC8 Hisobot", "\uD83D\uDCC9 Taqqoslama"), listOf("\uD83D\uDCCA Kunlik"))
        )
        execute(responseMessage)
    }

    private fun getReplyMarkup(chatId: Long, allButtons: MutableList<List<String>>): ReplyKeyboardMarkup {
        val markup = ReplyKeyboardMarkup()
        if (tgUsersRepository.existsByChatIdAndEnabledTrueAndAdminTrue(chatId)) {
            allButtons.addAll(listOf(listOf("admin:unverified", "admin:verified")))
        }
        markup.keyboard = allButtons.map { rowButtons ->
            val row = KeyboardRow()
            rowButtons.forEach { rowButton -> row.add(rowButton) }
            row
        }
        return markup
    }
}
