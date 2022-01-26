package actions

import Bot
import MarkupUtil
import MessageTexts.SUBSCRIBED
import org.telegram.telegrambots.meta.api.objects.Message

class SubscribeAction(bot: Bot, message: Message): Action(bot, message) {

    fun sendLink() = editOldMessage(SUBSCRIBED, markup = MarkupUtil.getSubscribeMarkup())
}
