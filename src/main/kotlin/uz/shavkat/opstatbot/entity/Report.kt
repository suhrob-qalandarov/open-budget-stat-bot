package uz.shavkat.opstatbot.entity


import jakarta.persistence.*
import java.util.*

@Entity
data class Report(
    var publicId: String,
    var voteCount: Int,
    var quarterName: String? = null,
    var title: String? = null,
    var categoryName: String? = null,
    var regionName: String? = null,
    var description: String? = null,
    var date: Date = Date(),
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
)