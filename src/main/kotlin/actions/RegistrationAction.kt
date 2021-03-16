package actions

import Bot
import GoogleSheetsUtil
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
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RegistrationAction(bot: Bot, message: Message): Action(bot, message) {

    fun start() {
        val chatId = message.chatId
        if (chatId == System.getenv("feedbackChatId").toLong() || GoogleSheetsUtil.getStudentById(chatId) != null) return

        sendMessage(chatId, GREETING, null)
        sendMessage(chatId, ASK_FIRST_NAME, null)
        Database.saveStudent(Student(id = chatId.toString()))
        saveState()
    }

    private fun askLastName(student: Student) {
        sendMessage(message.chatId, ASK_LAST_NAME, null)
        student.firstName = message.text
        Database.saveStudent(student)
    }

    private fun askUniversity(student: Student) {
        sendMessage(message.chatId, ASK_UNIVERSITY.format(student.firstName), markup = MarkupUtil.getUniversitiesMarkup())
        student.lastName = message.text
        Database.saveStudent(student)
    }

    private fun askYearStudy(student: Student) {
        sendMessage(message.chatId, ASK_YEAR_STUDY, markup = MarkupUtil.getYearStudyMarkup())
        student.university = message.text
        Database.saveStudent(student)
    }

    private fun askStudPro(student: Student) {
        if (message.text?.toIntOrNull() in 1..6) {
            sendMessage(message.chatId, ASK_STUD_PRO, markup = MarkupUtil.getStudProMarkup())
            student.yearStudy = message.text
            Database.saveStudent(student)
        } else {
            sendMessage(message.chatId, WRONG_YEAR_STUDY, markup = MarkupUtil.getYearStudyMarkup())
        }
    }

    private fun askRules(student: Student) {
        sendMessage(message.chatId, ASK_RULES, null)
        sendMessage(message.chatId, RULES, markup = MarkupUtil.getAgreeRulesMarkup())
        student.studProInfo = message.text
        Database.saveStudent(student)
    }

    private fun finish(student: Student) {
        if (message.text == AGREE) {
            sendMessage(message.chatId, FINISH_REGISTRATION)
            student.registerDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString()
            student.lastCheckinDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString()
            student.checkinCount = "1"

            if (GoogleSheetsUtil.addStudent(student)) {
                Database.deleteStudent(student)
                deleteState()
            }
        } else {
            sendMessage(message.chatId, FORCE_AGREE, markup = MarkupUtil.getAgreeRulesMarkup())
        }
    }

    fun fillMissing() {
        val student = Database.getStudentById(message.chatId) ?: return

        with (student) {
            when {
                firstName == null -> askLastName(student)
                lastName == null -> askUniversity(student)
                university == null -> askYearStudy(student)
                yearStudy == null -> askStudPro(student)
                studProInfo == null -> askRules(student)
                else -> finish(student)
            }
        }
    }
}
