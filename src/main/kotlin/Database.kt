import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.PemReader
import com.google.api.client.util.SecurityUtils
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ServiceAccountCredentials
import java.io.StringReader
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

object Database {
    private val studentColumns = mapOf(
        Student::id to "A",
        Student::firstName to "B",
        Student::lastName to "C",
        Student::comment to "D",
        Student::lastCheckinDate to "E",
        Student::checkinCount to "F",
        Student::registerDate to "G",
        Student::university to "H",
        Student::yearStudy to "I",
        Student::studProInfo to "J",
        Student::seenWhoAmI to "K",
        Student::status to "L"
    )

    fun updateColumn(property: KProperty<*>, studentId: String, value: String) {
        val column = studentColumns[property] ?: throw IllegalArgumentException()
        val sheetName = System.getenv("sheetName")
        val documentId = System.getenv("documentId")
        val students = getStudents()
        val stdIndex = students.indexOfFirst { it.id == studentId }
        val row = 2 + if (stdIndex >= 0) stdIndex else students.size
        val data = ValueRange().setValues(listOf(listOf(value)))
        getSheets().spreadsheets().values().update(documentId, "$sheetName!$column$row", data).setValueInputOption("USER_ENTERED").execute()
    }

    fun getStudentById(studentId: Long): Student {
        return getStudents().find { it.id == studentId.toString() } ?: Student(id = studentId.toString(), isNew = true)
    }

    fun addStudent(student: Student): Boolean {
        return appendToSheet(ValueRange().setValues(listOf(student.getDatabaseProps().map { it ?: "" }))) != null
    }

    private fun readFromSheet(range: String): Any? {
        return getSheets().spreadsheets().values()[System.getenv("documentId"), range].execute()["values"]
    }

    private fun appendToSheet(content: ValueRange): Any? {
        val sheetName = System.getenv("sheetName")
        val documentId = System.getenv("documentId")
        return getSheets().spreadsheets().values().append(documentId, sheetName, content).setValueInputOption("USER_ENTERED").execute()
    }

    private fun getStudents(): List<Student> {
        val sheetName = System.getenv("sheetName")
        return (readFromSheet(sheetName) as List<*>).drop(1).filterIsInstance<List<String>>().mapNotNull {
            Student(
                id = it.getOrNull(0) ?: return@mapNotNull null,
                firstName = it.getOrNull(1),
                lastName = it.getOrNull(2),
                comment = it.getOrNull(3),
                lastCheckinDate = it.getOrNull(4),
                checkinCount = it.getOrNull(5),
                registerDate = it.getOrNull(6),
                university = it.getOrNull(7),
                yearStudy = it.getOrNull(8),
                studProInfo = it.getOrNull(9),
                seenWhoAmI = it.getOrNull(10),
                status = it.getOrNull(11)
            )
        }
    }

    private fun getSheets(): Sheets {
        val credentials = ServiceAccountCredentials.newBuilder().apply {
            clientEmail = System.getenv("clientEmail")
            privateKey = getPrivateKey(System.getenv("privateKey").replace("\\n", "\n"))
            scopes = listOf(SheetsScopes.SPREADSHEETS)
        }.build()

        return Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        ).apply { applicationName = "app" }.build()
    }

    private fun getPrivateKey(privateKey: String): PrivateKey {
        val section = PemReader.readFirstSectionAndClose(StringReader(privateKey), "PRIVATE KEY")
        return SecurityUtils.getRsaKeyFactory().generatePrivate(PKCS8EncodedKeySpec(section.base64DecodedBytes))
    }
}
