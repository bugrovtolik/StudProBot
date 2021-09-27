package actions

import Bot
import Database
import MarkupUtil
import MessageTexts.WHO_I_AM_INFO
import Student
import org.telegram.telegrambots.meta.api.objects.Message

class WhoIAmAction(bot: Bot, message: Message, student: Student): Action(bot, message, student) {

    fun sendInfo() {
        sendMessage(student.id, WHO_I_AM_INFO, markup = MarkupUtil.getWhoIAmMarkup())
        if (student.seenWhoAmI.isNullOrEmpty()) {
            Database.updateColumn(Student::seenWhoAmI, student.id, "'+")
        }
    }
}
