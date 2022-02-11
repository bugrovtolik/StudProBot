package actions

import Bot
import MarkupUtil
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import storage.student.Student

abstract class Action(private val bot: Bot, protected val message: Message) {

    protected fun editOldMessage(msgTxt: String, markup: InlineKeyboardMarkup? = null, parseMode: String? = null) {
        sendNewMessage(message.chatId, msgTxt, markup, parseMode)

        val deleteMessage = DeleteMessage(message.chatId.toString(), message.messageId)
        try { bot.execute(deleteMessage) } catch (e: Exception) {}
    }

    protected fun sendNewMessage(msgTxt: String, markup: InlineKeyboardMarkup? = null) {
        sendNewMessage(message.chatId, msgTxt, markup)
    }

    protected fun sendNewMessage(chatId: Long, msgTxt: String, markup: InlineKeyboardMarkup? = null, parseMode: String? = null) {
        val sendMsg = SendMessage()
        sendMsg.chatId = chatId.toString()
        sendMsg.text = msgTxt
        sendMsg.replyMarkup = markup
        sendMsg.parseMode = parseMode
        sendMsg.disableWebPagePreview = true
        bot.execute(sendMsg)
    }

    protected fun forwardMessage(toChatId: String, fromChatId: Long, messageId: Int): Message {
        return bot.execute(ForwardMessage(toChatId, fromChatId.toString(), messageId))
    }

    protected fun replyToMessage(chatId: Long, msgTxt: String, messageId: Int? = null) {
        val sendMsg = SendMessage()
        sendMsg.chatId = chatId.toString()
        sendMsg.text = msgTxt
        sendMsg.replyToMessageId = messageId
        sendMsg.replyMarkup = MarkupUtil.getDefaultMarkup(Student(0))
        bot.execute(sendMsg)
    }

    protected fun sendPhoto(photoName: String) {
        bot.execute(SendPhoto(message.chatId.toString(), InputFile().setMedia(javaClass.getResourceAsStream("/$photoName"), photoName)))
    }
}
