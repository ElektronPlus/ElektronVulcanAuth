package pl.elektronplus.elektronvulcanauth.service

import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import org.springframework.web.client.RestTemplate
import pl.elektronplus.elektronvulcanauth.config.DiscordConfig
import pl.elektronplus.elektronvulcanauth.model.ServerRole
import pl.elektronplus.elektronvulcanauth.model.StudentResponse
import pl.elektronplus.elektronvulcanauth.model.User
import java.util.*


@Service
class DiscordApiService {

    @Autowired
    private lateinit var config: DiscordConfig

    private val logger = KotlinLogging.logger {}

    fun getServerRoleID(serverID: String, className: String): String? {
        val url = "https://discordapp.com/api/guilds/${serverID}/roles"
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bot ${config.token}")

        val request: HttpEntity<String> = HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            url, HttpMethod.GET, request, Array<ServerRole>::class.java
        )

        val role = response.body?.filter { it.name.lowercase().contains(className.lowercase())}
        return role?.get(0)?.id
    }

    fun getDiscordUser(accessToken: String): User? {
        val url = "https://discordapp.com/api/v6/users/@me"
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")

        val request: HttpEntity<String> = HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            url, HttpMethod.GET, request, User::class.java
        )

        return response.body
    }

    fun joinUserToServer(accessToken: String, student: StudentResponse, model: Model): String {
        val discordUser = getDiscordUser(accessToken)
        val discordUsername = discordUser?.username + "#" + discordUser?.discriminator

        val classNumber: Char = student.className[0]
        var serverID = ""
        when (classNumber) {
            '1' -> serverID = config.server1ID
            '2' -> serverID = config.server2ID
            '3' -> serverID = config.server3ID
            '4' -> serverID = config.server4ID
        }

        val url = "https://discordapp.com/api/guilds/${serverID}/members/${discordUser?.id}"
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bot ${config.token}")

        val roles = JSONArray()
        roles.put(getServerRoleID(serverID, student.className))

        val body = JSONObject()
        body.put("access_token", accessToken)
        body.put("nick", student.nick)
        body.put("roles", roles)

        val request = HttpEntity(body.toString(), headers)
        val response = restTemplate.exchange(
            url, HttpMethod.PUT, request, String::class.java
        )
        if(response.statusCode == HttpStatus.NO_CONTENT) {
            model.addAttribute("info", "Twoje konto $discordUsername jest już na serwerze‼️")
        } else if (response.statusCode == HttpStatus.CREATED) {
            model.addAttribute("info", "✨Twoje konto $discordUsername zostało dodane na serwer✨")
        } else {
            model.addAttribute("info", "Coś poszło nie tak: ${response.body}")
        }

        model.addAttribute("student", student)
        logger.info { "DISCORD: ${student.nick}, dcID: ${discordUser?.id}, dcUsername:$discordUsername, serverID: $serverID" }
        return "success"
    }
}