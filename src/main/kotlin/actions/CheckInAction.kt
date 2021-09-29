package actions

import Bot
import Database
import MarkupUtil
import MessageTexts.ALREADY_CHECKED_IN
import MessageTexts.OK
import MessageTexts.THANKS
import MessageTexts.WANNA_CHECKIN
import MessageTexts.YES
import Student
import Student.Status.CHECKIN
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class CheckInAction(bot: Bot, message: Message, student: Student): Action(bot, message, student) {

    fun ask() {
        if (student.lastCheckinDate?.takeIf { it.length > 9 }?.substring(0..9) == LocalDate.now().toString()) {
            sendMessage(student.id, ALREADY_CHECKED_IN)
        } else {
            sendMessage(student.id, WANNA_CHECKIN.format(student.firstName), markup = MarkupUtil.getYesNoMarkup())
            saveStatus(CHECKIN)
        }
    }

    fun update() {
        if (message.text == YES) {
            sendMessage(student.id, THANKS, markup = MarkupUtil.getDefaultMarkup())
            Database.updateColumn(Student::lastCheckinDate, student.id, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString())
            Database.updateColumn(Student::checkinCount, student.id, (student.checkinCount?.toInt()?.plus(1) ?: 1).toString())
        } else {
            sendMessage(student.id, OK)
        }

        deleteStatus()
    }
}
