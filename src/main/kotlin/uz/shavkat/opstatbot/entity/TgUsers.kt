package uz.shavkat.opstatbot.entity

import jakarta.persistence.*
import java.util.*

@Entity
class TgUsers(
    var name: String? = null,
    var username: String? = null,
    var tgUserId: Long? = null,
    @Column(unique = true) var chatId: Long? = null,
    var enabled: Boolean? = null,
    var lastAction: String? = null,
    var admin: Boolean? = false,
    var lastActionDate: Date = Date(),
    var createdAt: Date = Date(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null
)