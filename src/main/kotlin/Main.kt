import actions.DatabaseAction
import actions.WhoAmIAction
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import storage.student.StudentDao.adminChatId
import java.time.LocalTime

fun main() {
    val bot = Bot()
    TelegramBotsApi(DefaultBotSession::class.java).registerBot(bot)

    val scheduler = NotificationScheduler()
    // sync checkins
    scheduler.schedule(LocalTime.MIDNIGHT) { DatabaseAction(bot, Message()).uploadToSheet(scheduled = true) }
    // remind about meetings
    scheduler.schedule(LocalTime.NOON) { WhoAmIAction(bot, Message()).remind() }
    // ask about next meetings dates
    scheduler.schedule(LocalTime.of(22, 0)) { WhoAmIAction(bot, Message()).askDate() }
}

fun Long.isAdmin() = toString() == adminChatId
