package actions

import Bot
import MarkupUtil
import MessageTexts.ALREADY_CHECKED_IN
import MessageTexts.THANKS
import org.telegram.telegrambots.meta.api.objects.Message
import storage.student.Student
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class CheckInAction(bot: Bot, message: Message): Action(bot, message) {

    fun checkIn(student: Student) {
        if (student.lastCheckinDate?.toLocalDate() == LocalDate.now()) {
            return editOldMessage(ALREADY_CHECKED_IN, markup = MarkupUtil.getDefaultMarkup(student))
        }

        editOldMessage(THANKS.format(student.firstName), markup = MarkupUtil.getDefaultMarkup(student))

        student.lastCheckinDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        student.checkinCount++
    }
}
