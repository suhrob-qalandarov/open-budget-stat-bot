package uz.shavkat.opstatbot.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.*

@Entity
class PositionInfo(
    var currentPosition: Int,
    var currentVotes: Int,
    var updatedDate: Date = Date(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)