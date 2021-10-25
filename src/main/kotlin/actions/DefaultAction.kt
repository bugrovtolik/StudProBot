package actions

import Bot
import Database
import MessageTexts.DEFAULT
import Student
import org.telegram.telegrambots.meta.api.objects.Message

class DefaultAction(bot: Bot, message: Message, student: Student, database: Database): Action(bot, message, student, database) {

    fun forwardAdmin() {
        sendMessage(student.id, DEFAULT, markup = MarkupUtil.getDefaultMarkup())
        forwardMessage(System.getenv("adminChatId"), student.id, message.messageId)
    }
}
