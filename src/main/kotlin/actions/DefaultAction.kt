package actions

import Bot
import MessageTexts.DEFAULT
import org.telegram.telegrambots.meta.api.objects.Message

class DefaultAction(bot: Bot, message: Message): Action(bot, message) {

    fun forwardAdmin() {
        val adminChatId = System.getenv("adminChatId").toLong()
        sendMessage(message.chatId, DEFAULT)
        forwardMessage(adminChatId, message.chatId, message.messageId)
    }
}
