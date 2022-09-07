package pl.elektronplus.elektronvulcanauth.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import pl.elektronplus.elektronvulcanauth.model.LoginRequest
import pl.elektronplus.elektronvulcanauth.model.StudentResponse
import pl.elektronplus.elektronvulcanauth.service.DiscordOAuthService
import pl.elektronplus.elektronvulcanauth.service.VulcanAuthService


@RestController
@RequestMapping(value = ["/vulcan"])
class VulcanAuthController {

    @Autowired
    private lateinit var service: VulcanAuthService
    @PostMapping
    fun getStudent(@RequestBody body: LoginRequest): StudentResponse?{
        return service.login(body)
    }
}