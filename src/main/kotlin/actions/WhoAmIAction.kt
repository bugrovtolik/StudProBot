package actions

import Bot
import GoogleSheetsApi
import GoogleSheetsApi.dateFormatter
import GoogleSheetsApi.dayAndMonthFormatter
import MarkupUtil
import MessageTexts.DEFAULT
import MessageTexts.EMPTY_WHOAMI_STUDENTS
import MessageTexts.SUCCESS
import MessageTexts.WHOAMI_RESOURCES
import MessageTexts.WHOAMI_STAFF
import MessageTexts.WHO_AM_I_AD
import MessageTexts.WISHES
import org.telegram.telegrambots.meta.api.objects.Message
import storage.SheetsDatabase
import storage.student.Student
import storage.student.Student.Status.PICK_DATE
import storage.student.Student.Status.PICK_NAME
import storage.student.StudentDao
import storage.student.WhoAmIStudent
import java.time.LocalDate
import java.time.format.DateTimeParseException

class WhoAmIAction(bot: Bot, message: Message): Action(bot, message) {
    private val database = SheetsDatabase()

    fun remind() {
        val msg = StringBuilder()
        val now = LocalDate.now()
        val volunteers = StudentDao.findAllVolunteers()
        val whoamiStudents = GoogleSheetsApi.getWhoamiStudents(volunteers)

        volunteers.filter { it.allowNotifications }.forEach volunteers@{ volunteer ->
            msg.clear()
            whoamiStudents.filter { it.firstPilot?.id == volunteer.id && it.isRelevant(now) }.forEach { student ->

                val text = when {
                    student.contactedDate == null -> "Познайомитися"
                    student.firstMeetingDate == now -> "Першу зустріч"
                    student.secondMeetingDate == now -> "Другу зустріч"
                    student.thirdMeetingDate == now -> "Третю зустріч"
                    student.firstFollowUpDate == now -> "Перший фолоу-ап"
                    student.secondFollowUpDate == now -> "Другий фолоу-ап"
                    student.thirdFollowUpDate == now -> "Третій фолоу-ап"
                    student.fourthFollowUpDate == now -> "Четвертий фолоу-ап"
                    else -> return@forEach
                }
                msg.append("$text з ${student.fullName}\n")
            }

            if (msg.isNotEmpty()) {
                sendNewMessage(
                    chatId = volunteer.id,
                    msgTxt = "Привіт! Ти сьогодні маєш:\n$msg\nРаджу завчасно нагадати про зустріч. ${WISHES.random()}",
                    markup = MarkupUtil.getReturnMarkup()
                )
            }
        }
    }

    fun askDate() {
        val text = "Як пройшли сьогоднішні зустрічі? Сподіваюсь чудово, вже маєш дату наступної зустрічі?"
        val now = LocalDate.now()
        val volunteers = StudentDao.findAllVolunteers()
        val whoamiStudents = GoogleSheetsApi.getWhoamiStudents(volunteers)

        volunteers.filter { it.allowNotifications }.forEach volunteers@{ volunteer ->
            var hadMeetings = false
            whoamiStudents.filter { it.firstPilot?.id == volunteer.id && it.isRelevant(now) }.forEach { student ->
                if (listOf(
                    student.firstMeetingDate, student.secondMeetingDate, student.thirdMeetingDate,
                    student.firstFollowUpDate, student.secondFollowUpDate, student.thirdFollowUpDate
                ).any { it == now }) hadMeetings = true
            }

            if (hadMeetings) sendNewMessage(volunteer.id, text, markup = MarkupUtil.getEnterDateReturnMarkup())
        }
    }

    fun sendAd(student: Student) {
        editOldMessage(WHO_AM_I_AD, markup = MarkupUtil.getWhoAmIAdMarkup())
        student.seenWhoAmI = true
    }

    fun staffWhoAmI(student: Student) {
        editOldMessage("$WHOAMI_STAFF: ${student.lastName} ${student.firstName}", markup = MarkupUtil.getStaffWhoAmIMarkup(student))
    }

    fun allowNotifications(student: Student, allow: Boolean) {
        editOldMessage("Сповіщення " + if (allow) "увімкнуто!" else "вимкнуто!", markup = MarkupUtil.getReturnMarkup())
        student.allowNotifications = allow
    }

    fun resources(student: Student) {
        editOldMessage(WHOAMI_RESOURCES)
        sendPhoto("meme.jpg")
        sendNewMessage(DEFAULT, markup = MarkupUtil.getDefaultMarkup(student))
    }

    fun getWhoAmIStudentsFor(volunteer: Student) {
        val now = LocalDate.now()
        val students = GoogleSheetsApi.getWhoamiStudents().filter { it.firstPilot?.id == volunteer.id && it.isRelevant(now) }

        if (students.isEmpty()) {
            editOldMessage(EMPTY_WHOAMI_STUDENTS, markup = MarkupUtil.getReturnMarkup())
        } else {
            volunteer.status = PICK_NAME
            val studentNames = students.sortedByDescending { it.getLastDate() }.map { it.fullName }
            editOldMessage("Обери студента зі списку", markup = MarkupUtil.getMultipleOptionsMarkup(studentNames))
        }
    }

    fun askMeetingDate(volunteer: Student) {
        val now = LocalDate.now()
        val studentName = message.text
        val student = GoogleSheetsApi.getWhoamiStudents().find { it.fullName == studentName } ?: return getWhoAmIStudentsFor(volunteer)
        val meeting = when {
            student.contactedDate == null -> "знайомства"
            student.firstMeetingDate == null -> "першої зустрічі"
            student.secondMeetingDate == null -> "другої зустрічі"
            student.thirdMeetingDate == null -> "третьої зустрічі"
            student.firstFollowUpDate == null -> "першого фолоу-апу"
            student.secondFollowUpDate == null -> "другого фолоу-апу"
            student.thirdFollowUpDate == null -> "третього фолоу-апу"
            student.fourthFollowUpDate == null -> "четвертого фолоу-апу"
            else -> return editOldMessage("У обраного студента немає незаповнених дат зустрічей", markup = MarkupUtil.getReturnMarkup())
        }

        volunteer.status = PICK_DATE
        volunteer.statusParam = studentName

        editOldMessage(
            msgTxt = "Введи дату $meeting з $studentName в форматі '${now.format(dayAndMonthFormatter)}' або '${now.format(dateFormatter)}'",
            markup = MarkupUtil.getReturnMarkup()
        )
    }

    fun enterMeetingDate(volunteer: Student) {
        val meetingDate = try {
            LocalDate.parse(message.text, dateFormatter)
        } catch (e: DateTimeParseException) {
            try {
                LocalDate.parse(message.text + "." + LocalDate.now().year, dateFormatter)
            } catch (e: DateTimeParseException) {
                return askMeetingDate(volunteer)
            }
        }

        val studentName = volunteer.statusParam
        val students = GoogleSheetsApi.getWhoamiStudents(getRejected = true)
        val stdIndex = students.indexOfFirst { it.fullName == studentName }.takeIf { it >= 0 } ?: return askMeetingDate(volunteer)
        val student = students[stdIndex]

        val property = when {
            student.contactedDate == null -> WhoAmIStudent::contactedDate
            student.firstMeetingDate == null -> WhoAmIStudent::firstMeetingDate
            student.secondMeetingDate == null -> WhoAmIStudent::secondMeetingDate
            student.thirdMeetingDate == null -> WhoAmIStudent::thirdMeetingDate
            student.firstFollowUpDate == null -> WhoAmIStudent::firstFollowUpDate
            student.secondFollowUpDate == null -> WhoAmIStudent::secondFollowUpDate
            student.thirdFollowUpDate == null -> WhoAmIStudent::thirdFollowUpDate
            student.fourthFollowUpDate == null -> WhoAmIStudent::fourthFollowUpDate
            else -> return editOldMessage("У обраного студента немає незаповнених дат зустрічей", markup = MarkupUtil.getReturnMarkup())
        }

        database.updateWhoAmIStudent(stdIndex, property, meetingDate)
        volunteer.status = null
        volunteer.statusParam = null

        editOldMessage(SUCCESS, markup = MarkupUtil.getReturnMarkup())
    }

    fun newStudents() {
        val now = LocalDate.now()
        val text = GoogleSheetsApi.getWhoamiStudents()
            .filter { it.firstPilot == null && it.isRelevant(now) }
            .joinToString("\n") { "Ім'я: ${it.fullName}\nНомер: ${it.phoneNumber ?: ""}\nСоц.мережі: ${it.socialNetworks ?: ""}\n" }
            .takeIf { it.isNotEmpty() } ?: "Не знайдено нових студентів"

        editOldMessage(text, markup = MarkupUtil.getReturnMarkup())
    }

    fun current(volunteer: Student) {
        val now = LocalDate.now()
        val students = GoogleSheetsApi.getWhoamiStudents().filter { it.firstPilot?.id == volunteer.id && it.isRelevant(now) }
        val msg = StringBuilder()

        students.forEach {
            when {
                it.contactedDate == null -> msg.append("${it.fullName}: познайомитися і розказати про ЯХТО\n")
                it.firstMeetingDate == null -> msg.append("${it.fullName}: запропонувати першу зустріч\n")
                it.firstMeetingDate > now -> msg.append("${it.fullName}: перша зустріч ${it.firstMeetingDate.format(dateFormatter)}\n")
                it.secondMeetingDate == null -> msg.append("${it.fullName}: запропонувати другу зустріч\n")
                it.secondMeetingDate > now -> msg.append("${it.fullName}: друга зустріч ${it.secondMeetingDate.format(dateFormatter)}\n")
                it.thirdMeetingDate == null -> msg.append("${it.fullName}: запропонувати третю зустріч\n")
                it.thirdMeetingDate > now -> msg.append("${it.fullName}: третя зустріч ${it.thirdMeetingDate.format(dateFormatter)}\n")
                it.firstFollowUpDate == null -> msg.append("${it.fullName}: запропонувати проходити фолоу-апи\n")
                it.firstFollowUpDate > now -> msg.append("${it.fullName}: перший фолоу-ап ${it.firstFollowUpDate.format(dateFormatter)}\n")
                it.secondFollowUpDate == null -> msg.append("${it.fullName}: запропонувати другий фолоу-ап\n")
                it.secondFollowUpDate > now -> msg.append("${it.fullName}: другий фолоу-ап ${it.secondFollowUpDate.format(dateFormatter)}\n")
                it.thirdFollowUpDate == null -> msg.append("${it.fullName}: запропонувати третій фолоу-ап\n")
                it.thirdFollowUpDate > now -> msg.append("${it.fullName}: третій фолоу-ап ${it.thirdFollowUpDate.format(dateFormatter)}\n")
                it.fourthFollowUpDate == null -> msg.append("${it.fullName}: запропонувати четвертий фолоу-ап\n")
                it.fourthFollowUpDate > now -> msg.append("${it.fullName}: четвертий фолоу-ап ${it.fourthFollowUpDate.format(dateFormatter)}\n")
            }
        }

        val resultMsg = msg.toString().takeIf { it.isNotEmpty() } ?: EMPTY_WHOAMI_STUDENTS
        editOldMessage(resultMsg, markup = MarkupUtil.getReturnMarkup())
    }
}
