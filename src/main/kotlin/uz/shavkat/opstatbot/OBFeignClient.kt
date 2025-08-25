package uz.shavkat.opstatbot

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(name = "OBFeign", url = "https://openbudget.uz/api/v2/info")
interface OBFeignClient {

    @GetMapping("/board/52?regionId=12&districtId=160&page=0&size=12&stage=PASSED&quality=")
    fun getReport(): ProjectResponse

    @GetMapping("/board/52?regionId=12&districtId=160&page=0&size=12&stage=PASSED&quality=")
    fun getReportStore(): ProjectResponse
}