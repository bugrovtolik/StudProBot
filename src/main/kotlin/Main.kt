import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi

fun main() {
    ApiContextInitializer.init()
    val telegramBotsApi = TelegramBotsApi()
    val bot = Bot()
    telegramBotsApi.registerBot(bot)
}
