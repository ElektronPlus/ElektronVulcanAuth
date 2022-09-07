package pl.elektronplus.elektronvulcanauth.controller;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import pl.elektronplus.elektronvulcanauth.service.DiscordOAuthService

@RestController
@RequestMapping(value = ["/"])
class DiscordOAuthController {

    @Autowired
    private lateinit var service: DiscordOAuthService

    @GetMapping
    fun redirectToAuthorize(): ModelAndView {
        return service.redirectToAuthorize()
    }

    @GetMapping("/authorize")
    fun discordAuthorize(@RequestParam code: String) {
        return service.receiveDiscordAuthorization(code)
    }
}
