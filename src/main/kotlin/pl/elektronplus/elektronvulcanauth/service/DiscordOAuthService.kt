package pl.elektronplus.elektronvulcanauth.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.ModelAndView
import pl.elektronplus.elektronvulcanauth.config.DiscordConfig
import pl.elektronplus.elektronvulcanauth.model.DiscordOAuthResponse

@Service
class DiscordOAuthService {

    @Autowired
    private lateinit var config: DiscordConfig

    private val logger = KotlinLogging.logger {}

    fun redirectToAuthorize(): ModelAndView {
        val url = listOf(
            "https://discordapp.com/oauth2/authorize",
            "?client_id=${config.clientId}",
            "&scope=identify guilds guilds.join",
            "&response_type=code",
            "&redirect_uri=${config.redirectUrl}"
        ).joinToString("")

        return ModelAndView("redirect:${url}")
    }

    fun receiveDiscordAuthorization(code: String) {
        logger.info { code }
        val url = "https://discordapp.com/api/oauth2/token"
        val restTemplate = RestTemplate()

        val headers = org.springframework.http.HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.setBasicAuth(config.clientId, config.secret)

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("redirect_uri", config.redirectUrl)
        map.add("grant_type", "authorization_code")
        map.add("code", code)

        val request: HttpEntity<MultiValueMap<String, String>> = HttpEntity<MultiValueMap<String, String>>(map, headers)

        val response: ResponseEntity<DiscordOAuthResponse> =
            restTemplate.postForEntity(url, request, DiscordOAuthResponse::class.java)

        logger.info { response.body?.access_token }
        if (response.statusCode == HttpStatus.OK) {
            println("Login Successful")
        } else {
            println("Login Failed")
        }
    }


}