package uz.shavkat.opstatbot

import uz.shavkat.opstatbot.entity.Report


data class ProjectResponse(
    var content: List<Project>? = null
)


data class Project(
    var id: String? = null,
    var quarterName: String? = null,
    var publicId: String? = null,
    var title: String? = null,
    var categoryName: String? = null,
    var regionName: String? = null,
    var description: String? = null,
    var voteCount: Int? = null,
) {
    companion object {
        fun toEntity(dto: Project) = Report(
            dto.publicId!!,
            dto.voteCount!!,
            dto.quarterName,
            dto.title,
            dto.categoryName,
            dto.regionName,
            if (dto.description!!.length > 250) dto.description!!.substring(0, 250) else dto.description!!
        )
    }
}

data class PublicId(
    var publicId: String? = null
)

data class ReportSql(
    var publicId: String? = null,
    var voteCount: Int? = null,
    var quarterName: String? = null,
    var title: String? = null,
    var categoryName: String? = null,
    var regionName: String? = null,
    var description: String? = null
)

data class LastDayChartInfo(
    val description: String,
    val count: Int
)