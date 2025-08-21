package uz.shavkat.opstatbot.service

import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uz.shavkat.opstatbot.*
import uz.shavkat.opstatbot.entity.ChartInfo
import uz.shavkat.opstatbot.repository.ChartInfoRepository
import uz.shavkat.opstatbot.repository.ReportRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.logging.Logger

@Service
class ReportService(
    private val repository: ReportRepository,
    private val feign: OBFeignClient,
    private val chartRepository: ChartInfoRepository,
    private val jdbcTemplate: JdbcTemplate,
) {
    val logger: Logger = Logger.getAnonymousLogger()

    fun getLastDayChartData(): List<LastDayChartInfo> {
        val res = chartRepository.findAllByDay(LocalDate.now().minusDays(1)).map {
            LastDayChartInfo(it.description!!.substring(0, 45), it.dailyCount!!)
        }
        return res
    }

    @Scheduled(fixedRate = 1000 * 60 * 30)
//    @Scheduled(fixedRate = 1000 * 5)
    fun storeReport() {
        repository.saveAll(
            feign.getReportStore().content!!.map { Project.toEntity(it) }
        )
    }

    //    @PostConstruct
    @Scheduled(cron = "0 10 0 * * *") // 00:10 AM
    fun analyzeDay() {
        val now = LocalDateTime.now().minusDays(1) // yesterday
        logger.info("DailyReport started: $now")
        val startDay = now.with(LocalTime.MIN)
        val endDay = now.with(LocalTime.MAX)

        val publicIds = jdbcTemplate.query(
            "select public_id from report r group by (public_id)",
            BeanPropertyRowMapper(PublicId::class.java)
        )
        publicIds.forEach { result ->

            val firstRowRes = jdbcTemplate.query(
                "select * from report r where r.date between '$startDay' and '$endDay'" +
                        " and public_id = '${result.publicId}' order by id asc limit 1",
                BeanPropertyRowMapper(ReportSql::class.java)
            )

            if (firstRowRes.isEmpty()) {
                val sql = "DELETE FROM report WHERE public_id = ?"
                jdbcTemplate.update(sql, result.publicId)
                analyzeDay()
            }
            val firstRow = firstRowRes.first()
            val lastRow = jdbcTemplate.query(
                "select * from report r where r.date between '$startDay' and '$endDay'" +
                        " and public_id = '${result.publicId}' order by id desc limit 1",
                BeanPropertyRowMapper(ReportSql::class.java)
            ).first()

            chartRepository.save(
                ChartInfo(
                    firstRow.publicId!!,
                    firstRow.description, lastRow.voteCount!! - firstRow.voteCount!!,
                    now.toLocalDate()
                )
            )
        }

    }

}
