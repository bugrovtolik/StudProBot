package actions

import Bot
import GoogleSheetsUtil
import MarkupUtil
import MessageTexts.ALREADY_CHECKED_IN
import MessageTexts.OK
import MessageTexts.THANKS
import MessageTexts.WANNA_CHECKIN
import MessageTexts.YES
import Student
import org.telegram.telegrambots.meta.api.objects.Message
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class CheckInAction(bot: Bot, message: Message): Action(bot, message) {

    fun ask() {
        val chatId = message.chatId

        if (GoogleSheetsUtil.checkedInToday(chatId)) {
            sendMessage(chatId, ALREADY_CHECKED_IN)
        } else {
            sendMessage(chatId, WANNA_CHECKIN, markup = MarkupUtil.getYesNoMarkup())
            saveState()
        }
    }

    fun update(student: Student?) {
        val chatId = message.chatId

        if (message.text == YES) {
            sendMessage(chatId, THANKS, markup = MarkupUtil.getDefaultMarkup())
            GoogleSheetsUtil.updateColumn("E", chatId, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString())
            GoogleSheetsUtil.updateColumn("F", chatId, (student?.checkinCount?.toInt()?.plus(1) ?: 1).toString())
        } else {
            sendMessage(chatId, OK)
        }

        deleteState()
    }
}
