import MessageTexts.CHECKIN
import MessageTexts.DATABASE
import MessageTexts.DB_COUNT
import MessageTexts.DB_DOWNLOAD
import MessageTexts.DB_UPLOAD
import MessageTexts.ENTER_DATE
import MessageTexts.FEEDBACK
import MessageTexts.RETURN
import MessageTexts.SUBSCRIBE
import MessageTexts.WHOAMI
import MessageTexts.WHOAMI_NEW_STUDENTS
import MessageTexts.WHOAMI_CURRENT
import MessageTexts.WHOAMI_NOTIFICATIONS_OFF
import MessageTexts.WHOAMI_NOTIFICATIONS_ON
import MessageTexts.WHOAMI_RESOURCES
import MessageTexts.WHOAMI_STAFF
import actions.ActionHandler
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import storage.student.StudentDao.adminChatId
import storage.config.DBConfig

class Bot : TelegramLongPollingBot() {
    private val botToken = System.getenv("botToken")

    override fun onUpdateReceived(update: Update) {
        try {
            if (update.hasCallbackQuery()) {
                update.message = update.callbackQuery.message.apply {
                    caption = text
                    text = update.callbackQuery.message.replyMarkup.keyboard
                        .mapNotNull { row -> row.find { it.callbackData == update.callbackQuery.data } }
                        .firstOrNull()?.text
                }
            }

            val actions = ActionHandler(this, update.message)

            DBConfig.beginTransaction()
            when (update.message.text) {
                CHECKIN -> actions.checkIn()
                FEEDBACK -> actions.feedback()
                SUBSCRIBE -> actions.subscribe()
                WHOAMI -> actions.whoAmI()
                WHOAMI_STAFF -> actions.whoAmIStaff()
                WHOAMI_CURRENT -> actions.whoAmICurrent()
                WHOAMI_RESOURCES -> actions.whoAmIResources()
                WHOAMI_NEW_STUDENTS -> actions.whoAmINewStudents()
                WHOAMI_NOTIFICATIONS_ON -> actions.whoAmIAllowNotifications(true)
                WHOAMI_NOTIFICATIONS_OFF -> actions.whoAmIAllowNotifications(false)
                ENTER_DATE -> actions.getWhoAmIStudents()
                DATABASE -> actions.database()
                DB_COUNT -> actions.countStudents()
                DB_DOWNLOAD -> actions.downloadFromSheet()
                DB_UPLOAD -> actions.uploadToSheet()
                RETURN -> actions.default()
                else -> actions.continueAction()
            }
            DBConfig.commitTransaction()

        } catch (exception: Exception) {
            DBConfig.rollbackTransaction()
            sendErrorMessage(update.message ?: return, exception)
        }
    }

    override fun getBotToken(): String {
        return botToken
    }

    override fun getBotUsername(): String {
        return "@KyivCampusBot"
    }

    private fun sendErrorMessage(message: Message, exception: Exception) {
        val sendMsg = SendMessage()
        sendMsg.chatId = adminChatId
        sendMsg.text = """
            exception: ${exception.localizedMessage ?: return}
            action: ${exception.stackTrace.find { it.className == ActionHandler::class.java.name }?.methodName}
            user: {
                id: ${message.chatId}
                firstName: ${message.chat?.firstName}
                lastName: ${message.chat?.lastName}
                userName: @${message.chat?.userName}
            }
        """.trimIndent()

        execute(sendMsg)
    }
}
