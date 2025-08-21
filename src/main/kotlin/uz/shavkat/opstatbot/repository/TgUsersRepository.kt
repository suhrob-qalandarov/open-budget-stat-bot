package uz.shavkat.opstatbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.shavkat.opstatbot.entity.TgUsers

@Repository
interface TgUsersRepository : JpaRepository<TgUsers, Long> {
    fun existsByChatIdAndEnabledTrueAndAdminTrue(chatId: Long): Boolean
    fun existsByChatIdAndEnabledTrue(chatId: Long): Boolean
    fun findByChatId(chatId: Long): TgUsers?
    fun findAllByEnabled(enabled: Boolean): List<TgUsers>
}