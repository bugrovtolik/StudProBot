package actions

import Bot
import MarkupUtil
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import storage.student.Student

abstract class Action(private val bot: Bot, protected val message: Message) {

    protected fun editOldMessage(msgTxt: String, markup: InlineKeyboardMarkup? = null) {
        if (markup == message.replyMarkup && msgTxt == message.caption) return
        val editMsg = EditMessageText()
        editMsg.chatId = message.chatId.toString()
        editMsg.messageId = message.messageId
        editMsg.text = msgTxt
        editMsg.replyMarkup = markup
        try { bot.execute(editMsg) } catch (e: Exception) { sendNewMessage(msgTxt, markup) }
    }

    protected fun sendNewMessage(msgTxt: String, markup: InlineKeyboardMarkup? = null) {
        sendNewMessage(message.chatId, msgTxt, markup)
    }

    protected fun sendNewMessage(chatId: Long, msgTxt: String, markup: InlineKeyboardMarkup? = null) {
        val sendMsg = SendMessage()
        sendMsg.chatId = chatId.toString()
        sendMsg.text = msgTxt
        sendMsg.replyMarkup = markup
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
