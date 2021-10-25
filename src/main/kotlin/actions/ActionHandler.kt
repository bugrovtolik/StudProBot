package actions

import Bot
import Database
import Student
import Student.Status.*
import org.telegram.telegrambots.meta.api.objects.Message

class ActionHandler(
    private val bot: Bot,
    private val message: Message,
    private val student: Student,
    private val database: Database,
    private val registration: RegistrationAction = RegistrationAction(bot, message, student, database),
    private val checkIn: CheckInAction = CheckInAction(bot, message, student, database),
    private val feedback: FeedbackAction = FeedbackAction(bot, message, student, database),
    private val subscribe: SubscribeAction = SubscribeAction(bot, message, student, database),
    private val whoIAm: WhoIAmAction = WhoIAmAction(bot, message, student, database),
    private val default: DefaultAction = DefaultAction(bot, message, student, database)
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

    fun fillMissingData(isMainCmd: Boolean) {
        if (student.status == REGISTRATION.name && !isMainCmd) {
            registration.saveAskNext()
        } else {
            registration.askAgain()
        }
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
