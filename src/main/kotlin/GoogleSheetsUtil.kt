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
import java.time.LocalDate

object GoogleSheetsUtil {

    private fun readFromSheet(range: String): Any? {
        return getSheets().spreadsheets().values()[System.getenv("documentId"), range].execute()["values"]
    }

    private fun appendToSheet(content: ValueRange): Any? {
        val sheetName = System.getenv("sheetName")
        val documentId = System.getenv("documentId")
        return getSheets().spreadsheets().values().append(documentId, sheetName, content).setValueInputOption("USER_ENTERED").execute()
    }

    fun updateColumn(column: String, id: Long, text: String) {
        val sheetName = System.getenv("sheetName")
        val documentId = System.getenv("documentId")
        val students = getStudents()
        val stdIndex = students.indexOfFirst { it.id == id.toString() }
        val row = 2 + if (stdIndex >= 0) stdIndex else students.size
        val data = ValueRange().setValues(listOf(listOf(text)))
        getSheets().spreadsheets().values().update(documentId, "$sheetName!$column$row", data).setValueInputOption("USER_ENTERED").execute()
    }

    fun checkedInToday(chatId: Long): Boolean {
        return getStudents().any { it.id == chatId.toString() && it.lastCheckinDate?.takeIf { it.length > 9 }?.substring(0..9) == LocalDate.now().toString() }
    }

    fun getStudentById(chatId: Long): Student? {
        return getStudents().find { it.id == chatId.toString() }
    }

    fun addStudent(student: Student): Boolean {
        return with(student) {
            appendToSheet(ValueRange().setValues(listOf(listOf(
                id, firstName, lastName, "", lastCheckinDate, checkinCount, registerDate, university, yearStudy, studProInfo
            )))) != null
        }
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
                status = it.getOrNull(10)?.let { status -> Student.Status.values().find { it.name == status } }
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
