package uz.shavkat.opstatbot

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(name = "OBFeign", url = "https://openbudget.uz/api/v2/info")
interface OBFeignClient {

    @GetMapping("/board/52?regionId=12&districtId=160&page=0&size=12&stage=PASSED&quality=")
    fun getReport(): ProjectResponse

    @GetMapping("/board/52?regionId=12&districtId=160&page=0&size=12&stage=PASSED&quality=")
    fun getReportStore(): ProjectResponse

    @GetMapping("/initiative/count/98844e33-4586-4474-bec3-5838c3ebaab3")
    fun getVoteCount(): VoteCount
}