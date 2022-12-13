package pl.elektronplus.elektronvulcanauth.service

import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import pl.elektronplus.elektronvulcanauth.config.DiscordConfig
import pl.elektronplus.elektronvulcanauth.model.ServerRole
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

        //response.body?.forEach { role -> logger.info {"${role.name} -> ${role.id}"} }
        val role = response.body?.filter { it.name.lowercase().contains(className.lowercase())}
        return role?.get(0)?.id
    }

    fun getUserId(accessToken: String): String? {
        val url = "https://discordapp.com/api/v6/users/@me"
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")

        val request: HttpEntity<String> = HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            url, HttpMethod.GET, request, User::class.java
        )

        return response.body?.id
    }

    fun joinUserToServer(accessToken: String, className: String, nick: String) {
        val userId = getUserId(accessToken)

        val classNumber: Char = className.get(0)
        var serverID = ""
        when (classNumber) {
            '1' -> serverID = config.server1ID
            '2' -> serverID = config.server2ID
            '3' -> serverID = config.server3ID
            '4' -> serverID = config.server4ID
        }

        val url = "https://discordapp.com/api/guilds/${serverID}/members/${userId}"
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bot ${config.token}")

        val roles = JSONArray()
        roles.put(getServerRoleID(serverID, className))

        val body = JSONObject()
        body.put("access_token", accessToken)
        body.put("nick", nick)
        body.put("roles", roles)

        val request = HttpEntity(body.toString(), headers)
        restTemplate.exchange(
            url, HttpMethod.PUT, request, String::class.java
        )
    }
}