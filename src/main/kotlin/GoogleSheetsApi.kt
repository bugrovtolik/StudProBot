import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.PemReader
import com.google.api.client.util.SecurityUtils
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ServiceAccountCredentials
import java.io.StringReader
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import kotlin.reflect.KProperty

object GoogleSheetsApi {
    val studentColumns = mapOf(
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

    fun appendToSheet(content: ValueRange): Any? {
        val sheetName = System.getenv("sheetName")
        val documentId = System.getenv("documentId")
        return getSheets().spreadsheets().values().append(documentId, sheetName, content).setValueInputOption("USER_ENTERED").execute()
    }

    fun getAllStudents(): List<Student> {
        val documentId = System.getenv("documentId")
        val sheetName = System.getenv("sheetName")
        val students = getSheets().spreadsheets().values()[documentId, sheetName].execute()["values"] as List<*>

        return students.drop(1).filterIsInstance<List<String>>().mapNotNull {
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

    fun getSheets(): Sheets {
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
