package pl.elektronplus.elektronvulcanauth.controller

import io.github.wulkanowy.sdk.Sdk
import io.github.wulkanowy.sdk.scrapper.login.BadCredentialsException
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pl.elektronplus.elektronvulcanauth.model.LoginRequest
import pl.elektronplus.elektronvulcanauth.model.StudentModel

@RestController
@RequestMapping(value = ["/auth"])
class VulcanAuthController {

    private val logger = KotlinLogging.logger {}

    @Value("\${vulcan.url}")
    private val vulcanUrl: String = ""

    @Value("\${vulcan.symbol}")
    private val vulcanSymbol: String = ""

    @Value("\${vulcan.schoolId}")
    private val vulcanSchoolId: String = ""

    @PostMapping
    fun getStudent(@RequestBody body: LoginRequest): StudentModel?{
        val sdk = Sdk()
        val students = runBlocking {
            try {
                sdk.getStudentsFromScrapper(
                    email = body.email,
                    password = body.password,
                    scrapperBaseUrl = vulcanUrl,
                    symbol = vulcanSymbol)
            } catch (ex: BadCredentialsException) {
                throw IllegalStateException("Student not found")
            }
        }

        var student: StudentModel? = null
        students.forEach {
            if(it.schoolSymbol.equals(vulcanSchoolId)) {
                sdk.schoolSymbol = vulcanSchoolId
                sdk.loginType = it.loginType
                sdk.studentId = it.studentId
                sdk.classId = it.classId

                val semester = runBlocking { sdk.getSemesters()[0] }
                logger.info { "hello ${it.studentName} ${it.studentSurname} ${semester.diaryName}" }
                student = StudentModel(it.studentName, it.studentSurname, semester.diaryName)
            }
        }

        if(student == null)
            throw IllegalStateException("Student not found")

        return student
    }
}