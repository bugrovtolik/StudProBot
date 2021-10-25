import GoogleSheetsApi.appendToSheet
import GoogleSheetsApi.getAllStudents
import GoogleSheetsApi.getSheets
import GoogleSheetsApi.studentColumns
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import kotlin.reflect.KProperty

class Database(
    private val students: List<Student> = getAllStudents(),
    private val updateQueue: MutableList<Update> = mutableListOf()
) {
    fun updateColumn(property: KProperty<*>, value: String) {
        updateQueue.add(Update(property, value))
    }

    fun getStudentById(studentId: Long): Student {
        return students.find { it.id == studentId.toString() } ?: Student(id = studentId.toString(), isNew = true)
    }

    fun addStudent(student: Student): Boolean {
        return appendToSheet(ValueRange().setValues(listOf(student.getDatabaseProps().map { it ?: "" }))) != null
    }

    fun executeQueue(studentId: String) {
        if (updateQueue.isEmpty()) return

        val sheetName = System.getenv("sheetName")
        val documentId = System.getenv("documentId")
        val stdIndex = students.indexOfFirst { it.id == studentId }
        val row = 2 + if (stdIndex >= 0) stdIndex else students.size
        val data = updateQueue.map {
            val column = studentColumns[it.property] ?: throw IllegalArgumentException()
            ValueRange().setRange("$sheetName!$column$row").setValues(listOf(listOf(it.value)))
        }
        val request = BatchUpdateValuesRequest().setData(data).setValueInputOption("USER_ENTERED")
        getSheets().spreadsheets().values().batchUpdate(documentId, request).execute()
    }

    data class Update(
        val property: KProperty<*>,
        val value: String
    )
}
