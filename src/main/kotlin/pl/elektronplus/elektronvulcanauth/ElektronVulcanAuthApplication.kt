package pl.elektronplus.elektronvulcanauth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import pl.elektronplus.elektronvulcanauth.config.DiscordConfig
import pl.elektronplus.elektronvulcanauth.config.VulcanConfig

@SpringBootApplication
@EnableConfigurationProperties(DiscordConfig::class, VulcanConfig::class)
class ElektronVulcanAuthApplication

fun main(args: Array<String>) {
    runApplication<ElektronVulcanAuthApplication>(*args)
}
