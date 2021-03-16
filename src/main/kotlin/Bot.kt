import MessageTexts.START
import MessageTexts.CHECKIN
import MessageTexts.FEEDBACK
import MessageTexts.SUBSCRIBE
import MessageTexts.WHOIAM
import actions.ActionHandler
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

class Bot : TelegramLongPollingBot() {
    override fun onUpdateReceived(update: Update) {
        val actions = ActionHandler(this, update.message)

        when (update.message.text) {
            START -> actions.start()
            CHECKIN -> actions.checkin()
            FEEDBACK -> actions.feedback()
            SUBSCRIBE -> actions.subscribe()
            WHOIAM -> actions.whoIAm()
            else -> actions.default()
        }
    }

    override fun getBotToken(): String {
        return System.getenv("botToken")
    }

    override fun getBotUsername(): String {
        return "@KyivCampusBot"
    }
}
