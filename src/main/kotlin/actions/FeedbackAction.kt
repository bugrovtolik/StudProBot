package actions

import Bot
import MarkupUtil
import MessageTexts.NO
import MessageTexts.OK
import MessageTexts.RESULT_FEEDBACK
import MessageTexts.WANNA_FEEDBACK
import org.telegram.telegrambots.meta.api.objects.Message

class FeedbackAction(bot: Bot, message: Message): Action(bot, message) {

    fun ask() {
        sendMessage(message.chatId, WANNA_FEEDBACK, markup = MarkupUtil.getNoMarkup())
        saveState()
    }

    fun forward() {
        val chatId = message.chatId

        if (message.text == NO) {
            sendMessage(chatId, OK)
        } else {
            sendMessage(chatId, RESULT_FEEDBACK)
            forwardMessage(System.getenv("feedbackChatId").toLong(), chatId, message.messageId)
        }

        deleteState()
    }

    fun reply() {
        message.replyToMessage?.forwardFrom?.id?.let { sendMessage(message.chatId, message.text) }
    }

    fun isReply(): Boolean {
        val chatId = message.chatId
        val adminChatId = System.getenv("adminChatId").toLong()
        val feedbackChatId = System.getenv("feedbackChatId").toLong()

        return chatId == feedbackChatId || chatId == adminChatId
    }
}
