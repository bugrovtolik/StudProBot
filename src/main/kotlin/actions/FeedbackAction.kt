package actions

import Bot
import MarkupUtil
import MessageTexts.RESULT_FEEDBACK
import MessageTexts.WANNA_FEEDBACK
import org.telegram.telegrambots.meta.api.objects.Message
import storage.feedback_reply.FeedbackReply
import storage.feedback_reply.FeedbackReplyDao
import storage.student.Student
import storage.student.Student.Status.FEEDBACK
import storage.student.StudentDao.adminChatId
import storage.student.StudentDao.feedbackChatId

class FeedbackAction(bot: Bot, message: Message): Action(bot, message) {

    fun ask(student: Student) {
        editOldMessage(WANNA_FEEDBACK, markup = MarkupUtil.getReturnMarkup())
        student.status = FEEDBACK
    }

    fun forward(student: Student) {
        sendNewMessage(RESULT_FEEDBACK, markup = MarkupUtil.getDefaultMarkup(student))
        val msg = forwardMessage(feedbackChatId, message.chatId, message.messageId)
        FeedbackReplyDao.save(
            FeedbackReply(
                feedbackMessageId = msg.messageId,
                studentMessageId = message.messageId,
                studentId = message.chatId
            )
        )
        student.status = null
    }

    fun reply() {
        val feedbackReply = FeedbackReplyDao.findByFeedbackMessageId(message.replyToMessage.messageId)

        if (feedbackReply != null) {
            replyToMessage(feedbackReply.studentId, message.text, feedbackReply.studentMessageId)
            FeedbackReplyDao.delete(feedbackReply)
        } else {
            replyToMessage(message.replyToMessage.forwardFrom.id, message.text)
        }
    }

    fun isReply(): Boolean {
        return message.chatId.toString() in listOf(adminChatId, feedbackChatId)
            && message.replyToMessage != null
    }
}
