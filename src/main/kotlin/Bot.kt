import MessageTexts.CHECKIN
import MessageTexts.FEEDBACK
import MessageTexts.SUBSCRIBE
import MessageTexts.WHOAMI
import actions.ActionHandler
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update

class Bot : TelegramLongPollingBot() {
    override fun onUpdateReceived(update: Update) {
        try {
            val database = Database()
            val student = database.getStudentById(update.message.chatId)
            val actions = ActionHandler(this, update.message, student, database)
            val command = update.message.text
            val isMainCmd = command in listOf(CHECKIN, FEEDBACK, SUBSCRIBE, WHOAMI)

            when {
                student.isNew -> actions.start()
                student.hasMissingData() -> actions.fillMissingData(isMainCmd)
                else -> when (command) {
                    CHECKIN -> actions.checkin()
                    FEEDBACK -> actions.feedback()
                    SUBSCRIBE -> actions.subscribe()
                    WHOAMI -> actions.whoIAm()
                    else -> actions.continueAction()
                }
            }

            database.executeQueue(student.id)
        } catch (exception: Exception) {
            sendErrorMessage(update.message, exception)
        }
    }

    override fun getBotToken(): String {
        return System.getenv("botToken")
    }

    override fun getBotUsername(): String {
        return "@KyivCampusBot"
    }

    private fun sendErrorMessage(message: Message?, exception: Exception) {
        val sendMsg = SendMessage()
        sendMsg.chatId = System.getenv("adminChatId")
        sendMsg.text = """
            exception: ${exception.localizedMessage}
            action: ${exception.stackTrace.find { it.className == ActionHandler::class.java.name }?.methodName}
            user: {
                id: ${message?.chatId}
                firstName: ${message?.chat?.firstName}
                lastName: ${message?.chat?.lastName}
                userName: @${message?.chat?.userName}
            }
        """.trimIndent()

        execute(sendMsg)
    }
}
