package pl.elektronplus.elektronvulcanauth.service

import io.github.wulkanowy.sdk.Sdk
import io.github.wulkanowy.sdk.scrapper.login.BadCredentialsException
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import pl.elektronplus.elektronvulcanauth.config.VulcanConfig
import pl.elektronplus.elektronvulcanauth.model.LoginForm
import pl.elektronplus.elektronvulcanauth.model.StudentResponse

@Service
class VulcanAuthService {

    @Autowired
    private lateinit var config: VulcanConfig

    @Autowired
    private lateinit var service: DiscordApiService

    private val logger = KotlinLogging.logger {}

    fun login(body: LoginForm, model: Model): StudentResponse? {
        val sdk = Sdk()
        val students = runBlocking {
            try {
                sdk.getStudentsFromScrapper(
                    email = body.email!!,
                    password = body.password!!,
                    scrapperBaseUrl = config.url,
                    symbol = config.symbol)
            } catch (e: BadCredentialsException) {
                model.addAttribute("error", e.message)
                return@runBlocking null
            }
        } ?: return null

        var student: StudentResponse? = null
        students.forEach {
            if(it.schoolSymbol.equals(config.schoolId)) {
                sdk.schoolSymbol = it.schoolSymbol
                sdk.loginType = it.loginType
                sdk.studentId = it.studentId
                sdk.classId = it.classId

                val semester = runBlocking { sdk.getSemesters()[0] }
                logger.info { "${it.studentName} ${it.studentSurname} ${semester.diaryName}" }
                student = StudentResponse(it.studentName, it.studentSurname, semester.diaryName)
            }
        }

        val studentNick = "${student!!.name} ${student!!.surname}"

        service.joinUserToServer(body.accessToken!!, student!!.className, studentNick)
        model.addAttribute("student", student)
        return student!!
    }
}