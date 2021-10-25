package actions

import Bot
import Database
import MessageTexts.AGREE
import MessageTexts.ASK_FIRST_NAME
import MessageTexts.ASK_LAST_NAME
import MessageTexts.ASK_RULES
import MessageTexts.ASK_STUD_PRO
import MessageTexts.ASK_UNIVERSITY
import MessageTexts.ASK_YEAR_STUDY
import MessageTexts.FINISH_REGISTRATION
import MessageTexts.FORCE_AGREE
import MessageTexts.GREETING
import MessageTexts.RULES
import MessageTexts.WRONG_YEAR_STUDY
import Student
import Student.Status.REGISTRATION
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RegistrationAction(bot: Bot, message: Message, student: Student, database: Database): Action(bot, message, student, database) {

    fun start() {
        if (student.id == System.getenv("feedbackChatId")) return
        if (student.status == REGISTRATION.name || !student.isNew) return

        database.addStudent(student.copy(status = REGISTRATION.name))
        sendMessage(student.id, GREETING, null)
        askFirstName()
    }

    private fun askFirstName() {
        sendMessage(student.id, ASK_FIRST_NAME, null)
    }

    private fun askLastName(firstName: String? = null) {
        if (firstName != null) database.updateColumn(Student::firstName, firstName)
        sendMessage(student.id, ASK_LAST_NAME, null)
    }

    private fun askUniversity(lastName: String? = null) {
        if (lastName != null) database.updateColumn(Student::lastName, lastName)
        sendMessage(student.id, ASK_UNIVERSITY.format(student.firstName), markup = MarkupUtil.getUniversitiesMarkup())
    }

    private fun askYearStudy(university: String? = null) {
        if (university != null) database.updateColumn(Student::university, university)
        sendMessage(student.id, ASK_YEAR_STUDY, markup = MarkupUtil.getYearStudyMarkup())
    }

    private fun askStudPro(yearStudy: String? = null) {
        if (yearStudy?.toIntOrNull() in 1..6) {
            if (yearStudy != null) database.updateColumn(Student::yearStudy, yearStudy)
            sendMessage(student.id, ASK_STUD_PRO, markup = MarkupUtil.getStudProMarkup())
        } else {
            sendMessage(student.id, WRONG_YEAR_STUDY, markup = MarkupUtil.getYearStudyMarkup())
        }
    }

    private fun askRules(studProInfo: String? = null) {
        if (studProInfo != null) database.updateColumn(Student::studProInfo, studProInfo)
        sendMessage(student.id, ASK_RULES, null)
        sendMessage(student.id, RULES, markup = MarkupUtil.getAgreeRulesMarkup())
    }

    private fun finish(rules: String? = null) {
        if (rules == null || rules == AGREE) {
            val registerDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString()

            database.updateColumn(Student::registerDate, registerDate)
            database.updateColumn(Student::lastCheckinDate, registerDate)
            database.updateColumn(Student::checkinCount, "1")
            deleteStatus()

            sendMessage(student.id, FINISH_REGISTRATION)
        } else {
            sendMessage(student.id, FORCE_AGREE, markup = MarkupUtil.getAgreeRulesMarkup())
        }
    }

    fun saveAskNext() {
        when {
            student.firstName.isNullOrBlank() -> askLastName(firstName = message.text)
            student.lastName.isNullOrBlank() -> askUniversity(lastName = message.text)
            student.university.isNullOrBlank() -> askYearStudy(university = message.text)
            student.yearStudy.isNullOrBlank() -> askStudPro(yearStudy = message.text)
            student.studProInfo.isNullOrBlank() -> askRules(studProInfo = message.text)
            else -> finish(rules = message.text)
        }
    }

    fun askAgain() {
        if (student.status != REGISTRATION.name) saveStatus(REGISTRATION)
        when {
            student.firstName.isNullOrBlank() -> askFirstName()
            student.lastName.isNullOrBlank() -> askLastName()
            student.university.isNullOrBlank() -> askUniversity()
            student.yearStudy.isNullOrBlank() -> askYearStudy()
            student.studProInfo.isNullOrBlank() -> askStudPro()
            else -> finish()
        }
    }
}
