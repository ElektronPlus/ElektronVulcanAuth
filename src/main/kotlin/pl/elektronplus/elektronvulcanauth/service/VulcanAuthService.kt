package pl.elektronplus.elektronvulcanauth.service

import io.github.wulkanowy.sdk.Sdk
import io.github.wulkanowy.sdk.scrapper.login.BadCredentialsException
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pl.elektronplus.elektronvulcanauth.config.VulcanConfig
import pl.elektronplus.elektronvulcanauth.model.LoginRequest
import pl.elektronplus.elektronvulcanauth.model.StudentResponse

@Service
class VulcanAuthService {

    @Autowired
    private lateinit var config: VulcanConfig

    private val logger = KotlinLogging.logger {}

    fun login(body: LoginRequest): StudentResponse? {
        val sdk = Sdk()
        val students = runBlocking {
            try {
                sdk.getStudentsFromScrapper(
                    email = body.email,
                    password = body.password,
                    scrapperBaseUrl = config.url,
                    symbol = config.symbol)
            } catch (ex: BadCredentialsException) {
                throw IllegalStateException("Student not found")
            }
        }

        var student: StudentResponse? = null
        students.forEach {
            if(it.schoolSymbol.equals(config.schoolId)) {
                sdk.schoolSymbol = it.schoolSymbol
                sdk.loginType = it.loginType
                sdk.studentId = it.studentId
                sdk.classId = it.classId

                val semester = runBlocking { sdk.getSemesters()[0] }
                logger.info { "hello ${it.studentName} ${it.studentSurname} ${semester.diaryName}" }
                student = StudentResponse(it.studentName, it.studentSurname, semester.diaryName)
            }
        }

        if(student == null)
            throw IllegalStateException("Student not found")

        return student
    }
}