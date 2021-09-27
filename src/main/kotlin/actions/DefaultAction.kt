package actions

import Bot
import Student
import org.telegram.telegrambots.meta.api.objects.Message

class DefaultAction(bot: Bot, message: Message, student: Student): Action(bot, message, student) {

    fun forwardAdmin() {
        forwardMessage(System.getenv("adminChatId"), student.id, message.messageId)
    }
}
