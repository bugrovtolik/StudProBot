import MessageTexts.AGREE
import MessageTexts.CHECKIN
import MessageTexts.FEEDBACK
import MessageTexts.NO
import MessageTexts.SUBSCRIBE
import MessageTexts.WHOIAM
import MessageTexts.YES
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

object MarkupUtil {
    private val universities = listOf("КНУ ім.Т.Г.Шевченко", "НУХТ", "НУБіП", "КНУКіМ", "НУФВСУ", "КНЛУ")
    private val studPro = listOf("Отримав флаєр", "Реклама в інтернеті", "Через соцмережі Instagram/Facebook", "Через канал в Telegram", "Розповіли друзі")

    fun getDefaultMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup(listOf(
            KeyboardRow().apply { add(CHECKIN) },
            KeyboardRow().apply { add(WHOIAM) },
            KeyboardRow().apply { add(SUBSCRIBE) },
            KeyboardRow().apply { add(FEEDBACK) }
        ))
    }

    fun getYesNoMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup(listOf(
            KeyboardRow().apply { add(YES) },
            KeyboardRow().apply { add(NO) }
        )).apply { resizeKeyboard = true }
    }

    fun getNoMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup(listOf(
            KeyboardRow().apply { add(NO) }
        )).apply { resizeKeyboard = true }
    }

    fun getAgreeRulesMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup(listOf(
            KeyboardRow().apply { add(AGREE) }
        )).apply { resizeKeyboard = true }
    }

    fun getUniversitiesMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup(universities.map { KeyboardRow().apply { add(it) } })
    }

    fun getYearStudyMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup((1..6).toList().map { KeyboardRow().apply { add(it.toString()) } }).apply { resizeKeyboard = true }
    }

    fun getStudProMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup(studPro.map { KeyboardRow().apply { add(it) } })
    }

    fun getWhoIAmMarkup(): ReplyKeyboard {
        return getInviteLinkMarkup(listOf(
            Pair(MessageTexts.WHO_I_AM_REGISTRATION_TEXT, MessageTexts.WHO_I_AM_REGISTRATION_LINK),
            Pair(MessageTexts.WHO_I_AM_MORE_INFO_TEXT, MessageTexts.WHO_I_AM_MORE_INFO_LINK)
        ))
    }

    fun getSubscribeMarkup(): ReplyKeyboard {
        return getInviteLinkMarkup(listOf(Pair(MessageTexts.INVITE_CAMPUS_CHANNEL_TEXT, MessageTexts.INVITE_CAMPUS_CHANNEL_LINK)))
    }

    private fun getInviteLinkMarkup(textLinks: List<Pair<String, String>>): ReplyKeyboard {
        return InlineKeyboardMarkup(listOf(
            textLinks.map {
                InlineKeyboardButton().apply {
                    text = it.first
                    url = it.second
                }
            }
        ))
    }
}
