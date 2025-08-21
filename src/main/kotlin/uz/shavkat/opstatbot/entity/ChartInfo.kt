package uz.shavkat.opstatbot.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.io.Serializable
import java.time.LocalDate

@Entity
class ChartInfo(
    var publicId: String? = null,
    var description: String? = null,
    var dailyCount: Int? = null,
    var day: LocalDate? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
) : Serializable
