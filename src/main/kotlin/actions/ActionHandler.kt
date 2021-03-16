package actions

import Bot
import org.telegram.telegrambots.meta.api.objects.Message

class ActionHandler(
    private val bot: Bot,
    private val message: Message,
    private val registration: RegistrationAction = RegistrationAction(bot, message),
    private val checkIn: CheckInAction = CheckInAction(bot, message),
    private val feedback: FeedbackAction = FeedbackAction(bot, message),
    private val subscribe: SubscribeAction = SubscribeAction(bot, message),
    private val whoIAm: WhoIAmAction = WhoIAmAction(bot, message),
    private val default: DefaultAction = DefaultAction(bot, message)
) {
    fun start() {
        registration.start()
    }

    fun checkin() {
        checkIn.ask()
    }

    fun feedback() {
        feedback.ask()
    }

    fun subscribe() {
        subscribe.sendLink()
    }

    fun whoIAm() {
        whoIAm.sendInfo()
    }

    fun default() {
        when {
            registration.isInProgress() -> registration.fillMissing()
            checkIn.isInProgress() -> {
                val student = GoogleSheetsUtil.getStudentById(message.chatId)
                if (student?.checkinCount == "2") whoIAm()
                checkIn.update(student)
            }
            feedback.isInProgress() -> feedback.forward()
            feedback.isReply() -> feedback.reply()
            else -> default.forwardAdmin()
        }
    }
}
