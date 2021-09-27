package actions

import Bot
import MarkupUtil
import MessageTexts.NO
import MessageTexts.OK
import MessageTexts.RESULT_FEEDBACK
import MessageTexts.WANNA_FEEDBACK
import Student
import Student.Status.FEEDBACK
import org.telegram.telegrambots.meta.api.objects.Message

class FeedbackAction(bot: Bot, message: Message, student: Student): Action(bot, message, student) {

    fun ask() {
        sendMessage(student.id, WANNA_FEEDBACK, markup = MarkupUtil.getNoMarkup())
        Database.saveStatus(student.id, FEEDBACK)
    }

    fun forward() {
        if (message.text == NO) {
            sendMessage(student.id, OK)
        } else {
            sendMessage(student.id, RESULT_FEEDBACK)
            forwardMessage(System.getenv("feedbackChatId"), student.id, message.messageId)
        }

        Database.deleteStatus(student.id)
    }

    fun reply() {
        message.replyToMessage?.forwardFrom?.id?.let { sendMessage(it.toString(), message.text) }
    }

    fun isReply(): Boolean {
        val adminId = System.getenv("adminChatId")
        val feedbackId = System.getenv("feedbackChatId")

        return student.id in listOf(feedbackId, adminId) && message.replyToMessage?.forwardFrom?.id != null
    }
}
