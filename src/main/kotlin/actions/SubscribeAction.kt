package actions

import Bot
import MarkupUtil
import MessageTexts.SUBSCRIBED
import org.telegram.telegrambots.meta.api.objects.Message

class SubscribeAction(bot: Bot, message: Message): Action(bot, message) {

    fun sendLink() {
        sendMessage(message.chatId, SUBSCRIBED, markup = MarkupUtil.getSubscribeMarkup())
    }
}
