package storage

import GoogleSheetsApi.checkinsSheet
import GoogleSheetsApi.documentId
import GoogleSheetsApi.getSheets
import GoogleSheetsApi.whoamiSheet
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import storage.SheetsDatabase.CheckinUpdates.Property
import storage.student.Student
import storage.student.WhoAmIStudent
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

class SheetsDatabase {
    private val checkinColumns = mapOf(
        Student::id to "A",
        Student::firstName to "B",
        Student::lastName to "C",
        Student::userName to "D",
        Student::lastCheckinDate to "E",
        Student::checkinCount to "F",
        Student::registerDate to "G",
        Student::university to "H",
        Student::yearStudy to "I",
        Student::studProInfo to "J",
        Student::seenWhoAmI to "K",
        Student::isStaff to "L",
        Student::allowNotifications to "M"
    )
    private val whoamiColumns = mapOf(
        WhoAmIStudent::registerDate to "A",
        WhoAmIStudent::fullName to "B",
        WhoAmIStudent::firstPilot to "D",
        WhoAmIStudent::contactedDate to "F",
        WhoAmIStudent::firstMeetingDate to "G",
        WhoAmIStudent::secondMeetingDate to "H",
        WhoAmIStudent::thirdMeetingDate to "I",
        WhoAmIStudent::firstFollowUpDate to "K",
        WhoAmIStudent::secondFollowUpDate to "L",
        WhoAmIStudent::thirdFollowUpDate to "M",
        WhoAmIStudent::fourthFollowUpDate to "N"
    )

    fun updateCheckins(studentsFromDb: Set<Student>, studentsFromSheet: Set<Student>): DayStatistics {
        val statistics = DayStatistics()
        val updateQueue: MutableList<CheckinUpdates> = mutableListOf()
        
        studentsFromDb.forEach { dbStudent ->
            val sheetStudent = studentsFromSheet.find { it.id == dbStudent.id }
            if (sheetStudent == null) statistics.newStudents.add(dbStudent)

            val props: MutableList<Property> = mutableListOf()
            checkinColumns.keys.forEach { property ->
                if (sheetStudent == null || property.differs(sheetStudent, dbStudent)) {
                    if (property == Student::checkinCount) statistics.checkins++
                    props.add(Property(property, property.get(dbStudent)))
                }
            }
            if (props.isNotEmpty()) updateQueue.add(CheckinUpdates(dbStudent.id, props))
        }

        executeCheckinQueue(updateQueue, studentsFromSheet)

        return statistics.apply { updatedCount = updateQueue.map { it.studentId }.toSet().size }
    }

    fun updateWhoAmIStudent(stdIndex: Int, property: KProperty<*>, value: Any?) {
        val row = if (stdIndex >= 0) stdIndex + 2 else return
        val column = whoamiColumns[property] ?: throw IllegalArgumentException()
        val data = ValueRange().setValues(listOf(listOf(value?.toString() ?: "")))
        getSheets().spreadsheets().values().update(documentId, "$whoamiSheet!$column$row", data).setValueInputOption("USER_ENTERED").execute()
    }

    private fun executeCheckinQueue(updateQueue: List<CheckinUpdates>, studentsFromSheet: Set<Student>) {
        if (updateQueue.isEmpty()) return

        var newStudents = 0
        val data = updateQueue.flatMap { update ->
            val stdIndex = studentsFromSheet.indexOfFirst { it.id == update.studentId }
            val row = 2 + if (stdIndex >= 0) stdIndex else studentsFromSheet.size + newStudents++
            update.props.map {
                val column = checkinColumns[it.property] ?: throw IllegalArgumentException()
                ValueRange().setRange("$checkinsSheet!$column$row").setValues(listOf(listOf(it.value?.toString() ?: "")))
            }
        }
        val request = BatchUpdateValuesRequest().setData(data).setValueInputOption("USER_ENTERED")
        getSheets().spreadsheets().values().batchUpdate(documentId, request).execute()
    }

    class CheckinUpdates(
        val studentId: Long,
        val props: List<Property>
    ) {
        class Property(val property: KProperty<*>, val value: Any?)
    }

    data class DayStatistics(
        var updatedCount: Int = 0,
        var checkins: Int = 0,
        val newStudents: MutableList<Student> = mutableListOf()
    )

    private fun <T, V> KProperty1<T, V>.differs(first: T, second: T): Boolean {
        return get(first)?.toString()?.takeIf { it.isNotBlank() } != get(second)?.toString()?.takeIf { it.isNotBlank() }
    }
}
