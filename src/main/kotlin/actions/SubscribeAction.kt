package actions

import Bot
import MarkupUtil
import MessageTexts.SUBSCRIBED
import Student
import org.telegram.telegrambots.meta.api.objects.Message

class SubscribeAction(bot: Bot, message: Message, student: Student): Action(bot, message, student) {

    fun sendLink() {
        sendMessage(student.id, SUBSCRIBED, markup = MarkupUtil.getSubscribeMarkup())
    }
}
