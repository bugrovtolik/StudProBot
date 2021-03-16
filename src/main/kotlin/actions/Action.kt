package actions

import Bot
import MarkupUtil
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard

abstract class Action(
    private val bot: Bot,
    protected val message: Message
) {
    fun isInProgress(): Boolean {
        val state = message.chatId.toString() + this.javaClass.name
        return Database.hasState(state)
    }

    protected fun saveState() {
        val state = message.chatId.toString() + this.javaClass.name
        Database.saveState(state)
    }

    protected fun deleteState() {
        val state = message.chatId.toString() + this.javaClass.name
        Database.deleteState(state)
    }

    protected fun sendMessage(chatId: Long, text: String, markup: ReplyKeyboard? = MarkupUtil.getDefaultMarkup()) {
        val message = SendMessage()
        message.chatId = chatId.toString()
        message.text = text
        if (markup != null) message.replyMarkup = markup
        bot.execute(message)
    }

    protected fun forwardMessage(toChatId: Long, fromChatId: Long, messageId: Int) {
        bot.execute(ForwardMessage(toChatId.toString(), fromChatId.toString(), messageId))
    }
}
