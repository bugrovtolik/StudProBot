package actions

import Bot
import isAdmin
import org.telegram.telegrambots.meta.api.objects.Message
import storage.student.Student
import storage.student.Student.Status.*
import storage.student.StudentDao

class ActionHandler(
    private val bot: Bot,
    private val message: Message,
    private val registration: RegistrationAction = RegistrationAction(bot, message),
    private val checkIn: CheckInAction = CheckInAction(bot, message),
    private val feedback: FeedbackAction = FeedbackAction(bot, message),
    private val subscribe: SubscribeAction = SubscribeAction(bot, message),
    private val whoIAm: WhoAmIAction = WhoAmIAction(bot, message),
    private val default: DefaultAction = DefaultAction(bot, message),
    private val database: DatabaseAction = DatabaseAction(bot, message)
) {
    fun subscribe() = subscribe.sendLink()
    fun uploadToSheet() = database.uploadToSheet()
    fun downloadFromSheet() { if (message.chatId.isAdmin()) database.downloadFromSheet() }
    fun countStudents() { if (message.chatId.isAdmin()) database.countStudents() }
    fun database() { if (message.chatId.isAdmin()) database.database() }
    fun default() = getStudent()?.let { default.mainMenu(it) }
    fun checkIn() = getStudent()?.let { checkIn.checkIn(it) }
    fun whoAmI() = getStudent()?.let { whoIAm.sendAd(it) }
    fun feedback() = getStudent()?.let { feedback.ask(it) }
    fun whoAmIStaff() = getStudent()?.let { if (it.isStaff) whoIAm.staffWhoAmI(it) }
    fun whoAmICurrent() = getStudent()?.let { if (it.isStaff) whoIAm.current(it) }
    fun whoAmIResources() = getStudent()?.let { if (it.isStaff) whoIAm.resources(it) }
    fun whoAmINewStudents() = getStudent()?.let { if (it.isStaff) whoIAm.newStudents() }
    fun whoAmIAllowNotifications(allow: Boolean) = getStudent()?.let { if (it.isStaff) whoIAm.allowNotifications(it, allow) }
    fun getWhoAmIStudents() = getStudent()?.let { if (it.isStaff) whoIAm.getWhoAmIStudentsFor(it) }

    fun continueAction() {
        if (feedback.isReply()) return feedback.reply()
        val student = StudentDao.findById(message.chatId) ?: return registration.start()
        
        if (student.hasMissingData()) {
            if (student.status == REGISTRATION) registration.saveAskNext(student) else registration.askAgain(student)
        } else {
            when (student.status) {
                FEEDBACK -> feedback.forward(student)
                PICK_NAME -> whoIAm.askMeetingDate(student)
                PICK_DATE -> whoIAm.enterMeetingDate(student)
                else -> default.forwardAdmin(student)
            }
        }
    }

    private fun getStudent(): Student? {
        val student = StudentDao.findById(message.chatId)

        if (student == null) {
            registration.start()
            return null
        } else if (student.hasMissingData()) {
            registration.askAgain(student)
            return null
        }

        if (student.userName != message.chat.userName) student.userName = message.chat.userName

        return student
    }
}
