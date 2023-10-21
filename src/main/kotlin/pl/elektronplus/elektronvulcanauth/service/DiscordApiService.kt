package pl.elektronplus.elektronvulcanauth.service

import mu.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
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

    private val restTemplate = RestTemplate()

    private val logger = KotlinLogging.logger {}

    fun getServerRoleID(serverID: String, className: String): String? {
        val url = "https://discord.com/api/guilds/${serverID}/roles"
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
        val url = "https://discord.com/api/v6/users/@me"
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")

        val request: HttpEntity<String> = HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            url, HttpMethod.GET, request, User::class.java
        )

        return response.body
    }

    fun joinUserToCommunityServer(accessToken: String, student: StudentResponse, discordUser: User){
        val serverID = config.serverCommunityID
        val url = "https://discord.com/api/guilds/$serverID/members/${discordUser.id}"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bot ${config.token}")

        val roles = JSONArray()
        roles.put(getServerRoleID(serverID, student.className))
        roles.put("1052870689352331334")

        val body = JSONObject()
        body.put("access_token", accessToken)
        body.put("roles", roles)

        val request = HttpEntity(body.toString(), headers)
        val response = restTemplate.exchange(
            url, HttpMethod.PUT, request, String::class.java
        )
        sendLogMessage(student, discordUser)
        if(response.statusCode == HttpStatus.NO_CONTENT) {
            restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()
            restTemplate.exchange(
                url, HttpMethod.PATCH, request, String::class.java
            )
        }
    }

    fun sendLogMessage(student: StudentResponse, discordUser: User) {
        val url = "https://discord.com/api/channels/${config.logChannelID}/messages"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.set("Authorization", "Bot ${config.token}")

        val body = JSONObject()
        body.put("content", "Zweryfikowano ucznia: ${student.nick} ${student.className} <@${discordUser.id}>")

        val request = HttpEntity(body.toString(), headers)
        restTemplate.exchange(
            url, HttpMethod.POST, request, String::class.java
        )
    }

    fun joinUserToServer(accessToken: String, student: StudentResponse, model: Model): String {
        val discordUser = getDiscordUser(accessToken)
        joinUserToCommunityServer(accessToken, student, discordUser!!)

//        val classNumber: Char = student.className[0]
//        var serverID = ""
//        when (classNumber) {
//            '1' -> serverID = config.server1ID
//            '2' -> serverID = config.server2ID
//            '3' -> serverID = config.server3ID
//            '4' -> serverID = config.server4ID
//        }
//
//        val url = "https://discordapp.com/api/guilds/${serverID}/members/${discordUser.id}"
//
//        val headers = HttpHeaders()
//        headers.contentType = MediaType.APPLICATION_JSON
//        headers.set("Authorization", "Bot ${config.token}")
//
//        val roles = JSONArray()
//        roles.put(getServerRoleID(serverID, student.className))
//
//        val body = JSONObject()
//        body.put("access_token", accessToken)
//        body.put("nick", student.nick)
//        body.put("roles", roles)
//
//        val request = HttpEntity(body.toString(), headers)
//        val response = restTemplate.exchange(
//            url, HttpMethod.PUT, request, String::class.java
//        )
//        if(response.statusCode == HttpStatus.NO_CONTENT) {
//            model.addAttribute("info", "Twoje konto ${discordUser.username} było już wcześniej na serwerze, więc zostało ponownie połączone. ❤️")
//            restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()
//            restTemplate.exchange(
//                url, HttpMethod.PATCH, request, String::class.java
//            )
//        } else if (response.statusCode == HttpStatus.CREATED) {
//            model.addAttribute("info", "✨Twoje konto ${discordUser.username} zostało dodane na serwer✨")
//        } else {
//            model.addAttribute("info", "Coś poszło nie tak: ${response.body}")
//        }

        model.addAttribute("info", "✨Twoje konto ${discordUser.username} zostało dodane na serwer✨")
        model.addAttribute("student", student)
        logger.info { "DISCORD: ${student.nick}, dcID: ${discordUser.id}, dcUsername:${discordUser.username} " }
        return "success"
    }
}