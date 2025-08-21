package uz.shavkat.opstatbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.shavkat.opstatbot.entity.ChartInfo
import uz.shavkat.opstatbot.entity.PositionInfo
import java.time.LocalDate

@Repository
interface ChartInfoRepository : JpaRepository<ChartInfo, Long> {
    fun findAllByDay(day: LocalDate): List<ChartInfo>
    fun findTop6ByDayOrderByDailyCountDesc(day: LocalDate): List<ChartInfo>
}

@Repository
interface PositionInfoRepository : JpaRepository<PositionInfo, Long>