package pl.elektronplus.elektronvulcanauth.service

import io.github.wulkanowy.sdk.Sdk
import io.github.wulkanowy.sdk.pojo.RegisterStudent
import io.github.wulkanowy.sdk.pojo.RegisterUser
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
        val registerUser: RegisterUser = runBlocking {
            try {
                sdk.getUserSubjectsFromScrapper(
                    email = body.email!!,
                    password = body.password!!,
                    scrapperBaseUrl = config.url,
                    symbol = config.symbol)
            } catch (e: BadCredentialsException) {
                model.addAttribute("error", e.message)
                return@runBlocking null
            }
        } ?: return null

        val registerSymbol = try {
            registerUser.symbols
                .filter { it.schools.isNotEmpty() }
                .first { it.schools.any { school -> school.subjects.isNotEmpty() } }
        } catch (e: NoSuchElementException) {
            logger.error { "VULCAN: ${e.message}" }
            model.addAttribute("error", "Brak ucznia Elektrona")
            return null
        }
        logger.info { "VULCAN: $registerSymbol" }
        val registerUnit = registerSymbol.schools.first()
        val registerStudent = registerUnit.subjects.filterIsInstance<RegisterStudent>().first()
        val semester = registerStudent.semesters.first()

        sdk.apply {
            loginType = Sdk.ScrapperLoginType.valueOf(registerUser.loginType?.name!!)
            symbol = registerSymbol.symbol
            schoolSymbol = registerUnit.schoolId
            studentId = registerStudent.studentId
            diaryId = semester.diaryId
        }

        var student: StudentResponse? = null

            if(registerUnit.schoolId == config.schoolId) {
                sdk.studentId = registerStudent.studentId
                sdk.classId = registerStudent.classId

                var studentName = registerStudent.studentName
                val studentSurname = registerStudent.studentSurname
                //anti-second name
                if (studentName.contains(" ")) {
                    studentName = studentName.substring(0, studentName.indexOf(" "))
                }
                val studentNick = "$studentName $studentSurname"

                student = StudentResponse(studentNick, semester.diaryName)
                logger.info { "VULCAN: $studentNick, ${semester.diaryName}" }
            }

        return student!!
    }
}