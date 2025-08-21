package uz.shavkat.opstatbot

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import uz.shavkat.opstatbot.service.ReportService

@Controller
class IndexController(private val service: ReportService) {

    @GetMapping
    fun index(model: Model): String {
        val jsonMapper = JsonMapper()
        jsonMapper.registerModule(JavaTimeModule())

        model.addAttribute("chartData", service.getLastDayChartData().map { listOf(it.description, it.count) })
        return "index"
    }
}