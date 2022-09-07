package pl.elektronplus.elektronvulcanauth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "vulcan")
@ConstructorBinding
data class VulcanConfig(
    val url: String,
    val symbol: String,
    val schoolId: String
)