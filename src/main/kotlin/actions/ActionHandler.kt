package actions

import Bot
import Student
import Student.Status.*
import org.telegram.telegrambots.meta.api.objects.Message

class ActionHandler(
    private val bot: Bot,
    private val message: Message,
    private val student: Student,
    private val registration: RegistrationAction = RegistrationAction(bot, message, student),
    private val checkIn: CheckInAction = CheckInAction(bot, message, student),
    private val feedback: FeedbackAction = FeedbackAction(bot, message, student),
    private val subscribe: SubscribeAction = SubscribeAction(bot, message, student),
    private val whoIAm: WhoIAmAction = WhoIAmAction(bot, message, student),
    private val default: DefaultAction = DefaultAction(bot, message, student)
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

    fun askAgain() {
        registration.askAgain()
    }

    fun fillMissingData() {
        if (student.status == REGISTRATION.name) registration.saveAskNext() else registration.askAgain()
    }

    fun continueAction() {
        if (feedback.isReply()) return feedback.reply()

        when (student.status) {
            CHECKIN.name -> {
                if (student.seenWhoAmI.isNullOrEmpty() && student.checkinCount == "3") whoIAm()
                checkIn.update()
            }
            FEEDBACK.name -> feedback.forward()
            else -> default.forwardAdmin()
        }
    }
}
