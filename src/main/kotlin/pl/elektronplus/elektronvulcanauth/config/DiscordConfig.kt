package pl.elektronplus.elektronvulcanauth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "discord")
@ConstructorBinding
data class DiscordConfig(
    val redirectUrl: String,
    val clientId: String,
    val secret: String
)