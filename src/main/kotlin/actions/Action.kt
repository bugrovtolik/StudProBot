package actions

import Bot
import Database
import MarkupUtil
import Student
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

abstract class Action(
    private val bot: Bot,
    protected val message: Message,
    protected val student: Student,
    protected val database: Database
) {
    protected fun saveStatus(status: Student.Status) {
        database.updateColumn(Student::status, status.name)
    }

    protected fun deleteStatus() {
        database.updateColumn(Student::status, "")
    }

    protected fun sendMessage(chatId: String, text: String, markup: ReplyKeyboard? = MarkupUtil.getDefaultMarkup()) {
        val message = SendMessage(chatId, text)
        if (markup != null) message.replyMarkup = markup
        bot.execute(message)
    }

    protected fun forwardMessage(toChatId: String, fromChatId: String, messageId: Int) {
        bot.execute(ForwardMessage(toChatId, fromChatId, messageId))
    }
}
