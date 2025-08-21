package uz.shavkat.opstatbot.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uz.shavkat.opstatbot.entity.Report

@Repository
interface ReportRepository : JpaRepository<Report, Long> {
}