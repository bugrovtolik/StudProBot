import MessageTexts.AGREE
import MessageTexts.CHECKIN
import MessageTexts.DATABASE
import MessageTexts.DB_COUNT
import MessageTexts.DB_DOWNLOAD
import MessageTexts.DB_UPLOAD
import MessageTexts.ENTER_DATE
import MessageTexts.FEEDBACK
import MessageTexts.INVITE_CAMPUS_CHANNEL_LINK
import MessageTexts.INVITE_CAMPUS_CHANNEL_TEXT
import MessageTexts.RETURN
import MessageTexts.SUBSCRIBE
import MessageTexts.WHOAMI
import MessageTexts.WHOAMI_CURRENT
import MessageTexts.WHOAMI_NEW_STUDENTS
import MessageTexts.WHOAMI_NOTIFICATIONS_OFF
import MessageTexts.WHOAMI_NOTIFICATIONS_ON
import MessageTexts.WHOAMI_RESOURCES
import MessageTexts.WHOAMI_SHEET
import MessageTexts.WHOAMI_STAFF
import MessageTexts.WHO_AM_I_MORE_INFO_LINK
import MessageTexts.WHO_AM_I_MORE_INFO_TEXT
import MessageTexts.WHO_AM_I_REGISTRATION_LINK
import MessageTexts.WHO_AM_I_REGISTRATION_TEXT
import MessageTexts.WHO_AM_I_STAFF_LINK
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import storage.student.Student

object MarkupUtil {
    private val universities = listOf("КНУ ім.Т.Г.Шевченко", "НУХТ", "НУБіП", "КНУКіМ", "НУФВСУ", "КНЛУ")
    private val studPro = listOf("Отримав флаєр", "Реклама в інтернеті", "Через соцмережі Instagram/Facebook", "Через канал в Telegram", "Розповіли друзі")

    fun getDefaultMarkup(student: Student): InlineKeyboardMarkup {
        val buttons = mutableListOf<String>()
        with(buttons) {
            add(CHECKIN)
            add(WHOAMI)
            add(SUBSCRIBE)
            if (student.isStaff) add(WHOAMI_STAFF) else add(FEEDBACK)
            if (student.id.isAdmin()) add(DATABASE)
        }

        return getVerticalKeyboard(buttons)
    }

    fun getAgreeRulesMarkup() = getHorizontalKeyboard(listOf(AGREE))
    fun getReturnMarkup() = getHorizontalKeyboard(listOf(RETURN))
    fun getUniversitiesMarkup() = getVerticalKeyboard(universities)
    fun getYearStudyMarkup() = getHorizontalKeyboard((1..6).map(Int::toString))
    fun getStudProMarkup() = getVerticalKeyboard(studPro)
    fun getDatabaseMarkup() = getVerticalKeyboard(listOf(DB_COUNT, DB_DOWNLOAD, DB_UPLOAD, RETURN))
    fun getMultipleOptionsMarkup(options: List<String>) = getVerticalKeyboard(options + RETURN)

    fun getEnterDateReturnMarkup(): InlineKeyboardMarkup {
        val buttons = listOf(
            InlineKeyboardButton().apply {
                text = ENTER_DATE
                callbackData = "0"
            },
            InlineKeyboardButton().apply {
                text = RETURN
                callbackData = "1"
            }
        )

        return InlineKeyboardMarkup().apply { keyboard = buttons.map { listOf(it) } }
    }

    fun getStaffWhoAmIMarkup(student: Student): InlineKeyboardMarkup {
        val buttons = listOf(
            InlineKeyboardButton().apply {
                text = WHOAMI_CURRENT
                callbackData = "0"
            },
            InlineKeyboardButton().apply {
                text = WHOAMI_NEW_STUDENTS
                callbackData = "1"
            },
            InlineKeyboardButton().apply {
                text = ENTER_DATE
                callbackData = "2"
            },
            InlineKeyboardButton().apply {
                text = WHOAMI_SHEET
                url = WHO_AM_I_STAFF_LINK
            },
            InlineKeyboardButton().apply {
                text = if (student.allowNotifications) WHOAMI_NOTIFICATIONS_OFF else WHOAMI_NOTIFICATIONS_ON
                callbackData = "4"
            },
            InlineKeyboardButton().apply {
                text = WHOAMI_RESOURCES
                callbackData = "5"
            },
            InlineKeyboardButton().apply {
                text = RETURN
                callbackData = "6"
            }
        )

        return InlineKeyboardMarkup().apply { keyboard = buttons.map { listOf(it) } }
    }

    fun getWhoAmIAdMarkup(): InlineKeyboardMarkup {
        val buttons = listOf(
            InlineKeyboardButton().apply {
                text = WHO_AM_I_REGISTRATION_TEXT
                url = WHO_AM_I_REGISTRATION_LINK
            },
            InlineKeyboardButton().apply {
                text = WHO_AM_I_MORE_INFO_TEXT
                url = WHO_AM_I_MORE_INFO_LINK
            },
            InlineKeyboardButton().apply {
                text = RETURN
                callbackData = "2"
            }
        )

        return InlineKeyboardMarkup().apply { keyboard = buttons.map { listOf(it) } }
    }

    fun getSubscribeMarkup(): InlineKeyboardMarkup {
        val buttons = listOf(
            InlineKeyboardButton().apply {
                text = INVITE_CAMPUS_CHANNEL_TEXT
                url = INVITE_CAMPUS_CHANNEL_LINK
            },
            InlineKeyboardButton().apply {
                text = RETURN
                callbackData = "1"
            }
        )

        return InlineKeyboardMarkup().apply { keyboard = buttons.map { listOf(it) } }
    }

    private fun getHorizontalKeyboard(buttons: List<String>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = listOf(
                buttons.mapIndexed { index, btnTxt ->
                    InlineKeyboardButton().apply {
                        text = btnTxt
                        callbackData = index.toString()
                    }
                }
            )
        }
    }

    private fun getVerticalKeyboard(buttons: List<String>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup().apply {
            keyboard = buttons.mapIndexed { index, btnTxt ->
                listOf(
                    InlineKeyboardButton().apply {
                        text = btnTxt
                        callbackData = index.toString()
                    }
                )
            }
        }
    }
}
