package pl.elektronplus.elektronvulcanauth.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import pl.elektronplus.elektronvulcanauth.model.LoginForm
import pl.elektronplus.elektronvulcanauth.service.VulcanAuthService


@Controller
@RequestMapping(value = ["/vulcan"])
class VulcanAuthController {

    @Autowired
    private lateinit var service: VulcanAuthService
    @PostMapping
    fun getStudent(body: LoginForm, model: Model): String {
        if(service.login(body, model) == null)
            return "login"
        return "success"
    }

    @GetMapping("/{accessToken}")
    fun loginForm(@PathVariable("accessToken") accessToken: String, model: Model): String {
        val loginForm = LoginForm()
        loginForm.accessToken = accessToken
        model.addAttribute("loginForm", loginForm)
        return "login"
    }

    @GetMapping
    fun redirect(): String {
        return "redirect:/"
    }
}