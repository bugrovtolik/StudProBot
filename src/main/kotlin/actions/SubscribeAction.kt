package actions

import Bot
import Database
import MarkupUtil
import MessageTexts.SUBSCRIBED
import Student
import org.telegram.telegrambots.meta.api.objects.Message

class SubscribeAction(bot: Bot, message: Message, student: Student, database: Database): Action(bot, message, student, database) {

    fun sendLink() {
        sendMessage(student.id, SUBSCRIBED, markup = MarkupUtil.getSubscribeMarkup())
    }
}
