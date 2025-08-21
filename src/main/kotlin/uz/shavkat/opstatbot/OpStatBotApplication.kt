package uz.shavkat.opstatbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableFeignClients
@SpringBootApplication
class OpStatBotApplication

fun main(args: Array<String>) {
    runApplication<OpStatBotApplication>(*args)
}
