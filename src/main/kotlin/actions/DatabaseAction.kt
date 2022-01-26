package actions

import Bot
import GoogleSheetsApi
import MarkupUtil
import MessageTexts.COUNTED
import MessageTexts.DATABASE
import MessageTexts.DOWNLOADED
import MessageTexts.STATS_CHECKINS
import MessageTexts.STATS_NEW_STDS
import MessageTexts.UPLOADED
import org.telegram.telegrambots.meta.api.objects.Message
import storage.SheetsDatabase
import storage.student.Student
import storage.student.StudentDao
import storage.student.StudentDao.adminChatId

class DatabaseAction(bot: Bot, message: Message): Action(bot, message) {
    private val database = SheetsDatabase()

    fun downloadFromSheet() {
        val studentsFromSheets = GoogleSheetsApi.getCheckinStudents().toSet()
        val studentsFromDatabase = StudentDao.findAll().toSet()
        val studentsToUpdate = studentsFromSheets - studentsFromDatabase

        StudentDao.saveAll(studentsToUpdate)

        editOldMessage(DOWNLOADED.format(studentsToUpdate.size), markup = MarkupUtil.getReturnMarkup())
    }

    fun uploadToSheet(scheduled: Boolean = false) {
        val studentsFromDatabase = StudentDao.findAll().toSet()
        val studentsFromSheets = GoogleSheetsApi.getCheckinStudents().toSet()
        val stats = database.updateCheckins(studentsFromDatabase, studentsFromSheets)

        if (scheduled) {
            if (stats.newStudents.isEmpty()) return sendNewMessage(adminChatId.toLong(), "$STATS_CHECKINS ${stats.checkins}")

            val newStudents = stats.newStudents.joinToString("\n") { it.firstName + " " + it.lastName }
            sendNewMessage(
                chatId = adminChatId.toLong(),
                msgTxt = "$STATS_CHECKINS ${stats.checkins}\n$STATS_NEW_STDS\n$newStudents",
                markup = MarkupUtil.getDefaultMarkup(Student(id = adminChatId.toLong(), isStaff = true))
            )
        } else {
            editOldMessage(
                msgTxt = UPLOADED.format(stats.updatedCount),
                markup = MarkupUtil.getDefaultMarkup(Student(id = adminChatId.toLong(), isStaff = true))
            )
        }
    }

    fun countStudents() {
        editOldMessage(
            msgTxt = COUNTED.format(GoogleSheetsApi.getCheckinStudents().size),
            markup = MarkupUtil.getDefaultMarkup(Student(id = adminChatId.toLong(), isStaff = true))
        )
    }

    fun database() = editOldMessage(DATABASE, markup = MarkupUtil.getDatabaseMarkup())
}
