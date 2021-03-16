package actions

import Bot
import GoogleSheetsUtil
import MarkupUtil
import MessageTexts.WHO_I_AM_INFO
import org.telegram.telegrambots.meta.api.objects.Message

class WhoIAmAction(bot: Bot, message: Message): Action(bot, message) {

    fun sendInfo() {
        sendMessage(message.chatId, WHO_I_AM_INFO, markup = MarkupUtil.getWhoIAmMarkup())
        GoogleSheetsUtil.updateColumn("K", message.chatId, "'+")
    }
}
