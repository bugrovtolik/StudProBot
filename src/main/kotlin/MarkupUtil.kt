import MessageTexts.AGREE
import MessageTexts.CHECKIN
import MessageTexts.FEEDBACK
import MessageTexts.INVITE_CAMPUS_CHANNEL_LINK
import MessageTexts.INVITE_CAMPUS_CHANNEL_TEXT
import MessageTexts.NO
import MessageTexts.SUBSCRIBE
import MessageTexts.WHOIAM
import MessageTexts.WHO_I_AM_MORE_INFO_LINK
import MessageTexts.WHO_I_AM_MORE_INFO_TEXT
import MessageTexts.WHO_I_AM_REGISTRATION_LINK
import MessageTexts.WHO_I_AM_REGISTRATION_TEXT
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
        return ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply { add(CHECKIN) },
                KeyboardRow().apply { add(WHOIAM) },
                KeyboardRow().apply { add(SUBSCRIBE) },
                KeyboardRow().apply { add(FEEDBACK) }
            )
        }
    }

    fun getYesNoMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply { add(YES) },
                KeyboardRow().apply { add(NO) }
            )
            resizeKeyboard = true
        }
    }

    fun getNoMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply { add(NO) }
            )
            resizeKeyboard = true
        }
    }

    fun getAgreeRulesMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup().apply {
            keyboard = listOf(
                KeyboardRow().apply { add(AGREE) }
            )
            resizeKeyboard = true
        }
    }

    fun getUniversitiesMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup().apply {
            keyboard = universities.map { KeyboardRow().apply { add(it) } }
            resizeKeyboard = true
        }
    }

    fun getYearStudyMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup().apply {
            keyboard = (1..6).toList().map { KeyboardRow().apply { add(it.toString()) } }
            resizeKeyboard = true
        }
    }

    fun getStudProMarkup(): ReplyKeyboard {
        return ReplyKeyboardMarkup().apply {
            keyboard = studPro.map { KeyboardRow().apply { add(it) } }
            resizeKeyboard = true
        }
    }

    fun getWhoIAmMarkup(): ReplyKeyboard {
        return getInviteLinkMarkup(listOf(
            Pair(WHO_I_AM_REGISTRATION_TEXT, WHO_I_AM_REGISTRATION_LINK),
            Pair(WHO_I_AM_MORE_INFO_TEXT, WHO_I_AM_MORE_INFO_LINK)
        ))
    }

    fun getSubscribeMarkup(): ReplyKeyboard {
        return getInviteLinkMarkup(listOf(Pair(INVITE_CAMPUS_CHANNEL_TEXT, INVITE_CAMPUS_CHANNEL_LINK)))
    }

    private fun getInviteLinkMarkup(textLinks: List<Pair<String, String>>): ReplyKeyboard {
        return InlineKeyboardMarkup().apply {
            keyboard = listOf(
                textLinks.map {
                    InlineKeyboardButton().apply {
                        text = it.first
                        url = it.second
                    }
                }
            )
        }
    }
}
