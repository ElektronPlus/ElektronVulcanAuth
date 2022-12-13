package pl.elektronplus.elektronvulcanauth.controller;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import pl.elektronplus.elektronvulcanauth.model.DiscordOAuthResponse
import pl.elektronplus.elektronvulcanauth.service.DiscordOAuthService

@Controller
@RequestMapping(value = ["/"])
class DiscordOAuthController {

    @Autowired
    private lateinit var service: DiscordOAuthService

    @GetMapping
    fun redirectToAuthorize(): ModelAndView {
        return service.redirectToAuthorize()
    }

    @GetMapping("/authorize")
    fun discordAuthorize(@RequestParam code: String): String {
        val discordOAuth: DiscordOAuthResponse? = service.receiveDiscordAuthorization(code)
        return "redirect:/vulcan/" + discordOAuth?.access_token
    }
}
