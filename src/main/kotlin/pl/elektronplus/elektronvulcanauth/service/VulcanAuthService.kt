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

    private val logger = KotlinLogging.logger {}

    fun login(body: LoginForm, model: Model): StudentResponse? {
        val sdk = Sdk()
        sdk.setSimpleHttpLogger { }
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
            if(it.schoolSymbol == config.schoolId) {
                sdk.schoolSymbol = it.schoolSymbol
                sdk.loginType = it.loginType
                sdk.studentId = it.studentId
                sdk.classId = it.classId

                val semester = runBlocking { sdk.getSemesters()[0] }
                val studentNick = "${it.studentName} ${it.studentSurname}"

                student = StudentResponse(studentNick, semester.diaryName)
                logger.info { "VULCAN: $studentNick, ${semester.diaryName}" }
            }
        }

        return student!!
    }
}