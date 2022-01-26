package actions

import Bot
import MarkupUtil
import MessageTexts.AGREE
import MessageTexts.ASK_FIRST_NAME
import MessageTexts.ASK_FIRST_NAME_AGAIN
import MessageTexts.ASK_LAST_NAME
import MessageTexts.ASK_LAST_NAME_AGAIN
import MessageTexts.ASK_RULES
import MessageTexts.ASK_STUD_PRO
import MessageTexts.ASK_UNIVERSITY
import MessageTexts.ASK_YEAR_STUDY
import MessageTexts.DEFAULT
import MessageTexts.FINISH_REGISTRATION
import MessageTexts.FORCE_AGREE
import MessageTexts.GREETING
import MessageTexts.RULES
import MessageTexts.WRONG_YEAR_STUDY
import org.telegram.telegrambots.meta.api.objects.Message
import storage.student.Student
import storage.student.Student.Status.REGISTRATION
import storage.student.StudentDao
import java.lang.Character.UnicodeBlock.CYRILLIC
import java.lang.Character.UnicodeBlock.of
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RegistrationAction(bot: Bot, message: Message): Action(bot, message) {
    private val feedbackChatId = System.getenv("feedbackChatId")

    fun start() {
        if (message.chatId.toString() == feedbackChatId) return

        sendNewMessage(GREETING)
        askFirstName()
        StudentDao.save(Student(id = message.chatId, status = REGISTRATION))
    }

    private fun askFirstName(again: Boolean = false) {
        sendNewMessage(if (again) ASK_FIRST_NAME_AGAIN else ASK_FIRST_NAME)
    }

    private fun askLastName(student: Student, firstName: String? = null, again: Boolean = false) {
        if (firstName != null) {
            if (firstName.all { of(it) == CYRILLIC || it in "'`-"}) {
                student.firstName = firstName
            } else {
                return askFirstName(again = true)
            }
        }

        sendNewMessage(if (again) ASK_LAST_NAME_AGAIN else ASK_LAST_NAME)
    }

    private fun askUniversity(student: Student, lastName: String? = null) {
        if (lastName != null) {
            if (lastName.all { of(it) == CYRILLIC  || it in "'`- " }) {
                student.lastName = lastName
            } else {
                return askLastName(student, again = true)
            }
        }

        sendNewMessage(ASK_UNIVERSITY.format(student.firstName), markup = MarkupUtil.getUniversitiesMarkup())
    }

    private fun askYearStudy(student: Student, university: String? = null) {
        if (university != null) student.university = university
        editOldMessage(ASK_YEAR_STUDY, markup = MarkupUtil.getYearStudyMarkup())
    }

    private fun askStudPro(student: Student, yearStudy: String? = null) {
        if (yearStudy != null) {
            if (yearStudy.toIntOrNull() in 1..6) {
                student.yearStudy = yearStudy.toInt()
            } else {
                return editOldMessage(WRONG_YEAR_STUDY, markup = MarkupUtil.getYearStudyMarkup())
            }
        }

        editOldMessage(ASK_STUD_PRO, markup = MarkupUtil.getStudProMarkup())
    }

    private fun askRules(student: Student, studProInfo: String? = null) {
        if (studProInfo != null) student.studProInfo = studProInfo
        editOldMessage(RULES, markup = MarkupUtil.getAgreeRulesMarkup())
        sendNewMessage(ASK_RULES)
    }

    private fun finish(student: Student, rules: String? = null) {
        if (rules == null || rules == AGREE) {
            val registerDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

            student.registerDate = registerDate
            student.lastCheckinDate = registerDate
            student.checkinCount = 1
            student.status = null

            editOldMessage(FINISH_REGISTRATION)
            sendNewMessage(DEFAULT, markup = MarkupUtil.getDefaultMarkup(student))
        } else {
            editOldMessage(FORCE_AGREE, markup = MarkupUtil.getAgreeRulesMarkup())
        }
    }

    fun saveAskNext(student: Student) {
        when {
            student.firstName.isNullOrBlank() -> askLastName(student, firstName = message.text)
            student.lastName.isNullOrBlank() -> askUniversity(student, lastName = message.text)
            student.university.isNullOrBlank() -> askYearStudy(student, university = message.text)
            student.yearStudy == null -> askStudPro(student, yearStudy = message.text)
            student.studProInfo.isNullOrBlank() -> askRules(student, studProInfo = message.text)
            else -> finish(student, rules = message.text)
        }
    }

    fun askAgain(student: Student) {
        if (student.status != REGISTRATION) student.status = REGISTRATION
        when {
            student.firstName.isNullOrBlank() -> askFirstName()
            student.lastName.isNullOrBlank() -> askLastName(student)
            student.university.isNullOrBlank() -> askUniversity(student)
            student.yearStudy == null -> askYearStudy(student)
            student.studProInfo.isNullOrBlank() -> askStudPro(student)
            else -> finish(student)
        }
    }
}
