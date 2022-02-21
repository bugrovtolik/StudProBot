import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.PemReader
import com.google.api.client.util.SecurityUtils
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ServiceAccountCredentials
import storage.student.Student
import storage.student.StudentDao
import storage.student.WhoAmIStudent
import java.io.StringReader
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object GoogleSheetsApi {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val dayAndMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
    private val clientEmail: String = System.getenv("clientEmail")
    private val privateKey: String = System.getenv("privateKey")
    val whoamiSheet: String = System.getenv("whoamiSheet")
    val checkinsSheet: String = System.getenv("checkinsSheet")
    val documentId: String = System.getenv("documentId")

    fun getCheckinStudents(): List<Student> {
        val students = getSheets().spreadsheets().values()[documentId, checkinsSheet].execute()["values"] as List<*>

        return students.drop(1).filterIsInstance<List<String>>().mapNotNull { row ->
            Student(
                id = row.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null,
                firstName = row.getOrNull(1),
                lastName = row.getOrNull(2),
                userName = row.getOrNull(3),
                lastCheckinDate = row.getOrNull(4)?.takeIf { it.isNotBlank() }?.let {
                    try { LocalDateTime.parse(it) }
                    catch (e: Exception) { LocalDateTime.parse(it.split(" ").joinToString("T")) }
                },
                checkinCount = row.getOrNull(5)?.toIntOrNull() ?: 0,
                registerDate = row.getOrNull(6)?.takeIf { it.isNotBlank() }?.let {
                    try { LocalDateTime.parse(it) }
                    catch (e: Exception) { LocalDateTime.parse(it.split(" ").joinToString("T")) }
                },
                university = row.getOrNull(7),
                yearStudy = row.getOrNull(8)?.toIntOrNull(),
                studProInfo = row.getOrNull(9),
                seenWhoAmI = row.getOrNull(10) == "TRUE",
                isStaff = row.getOrNull(11) == "TRUE",
                allowNotifications = row.getOrNull(12) == "TRUE"
            )
        }
    }

    fun getWhoamiStudents(volunteers: List<Student>? = null, getRejected: Boolean = false): List<WhoAmIStudent> {
        val volunteersNames = (volunteers ?: StudentDao.findAllVolunteers()).associateBy { it.firstName + " " + it.lastName }
        val students = getSheets().spreadsheets().values()[documentId, whoamiSheet].execute()["values"] as List<*>

        return students.drop(1).filterIsInstance<List<String>>().filter { getRejected || it.getOrNull(9) != "TRUE" }.mapNotNull { row ->
            WhoAmIStudent(
                registerDate = row.getOrNull(0)?.let { try { LocalDateTime.parse(it, timeFormatter) } catch (e: Exception) { null } },
                fullName = row.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null,
                firstPilot = row.getOrNull(3)?.takeIf { it.isNotBlank() }?.let { volunteersNames[it] ?: return@mapNotNull null },
                contactedDate = row.getOrNull(5)?.parseOrNull(dateFormatter),
                firstMeetingDate = row.getOrNull(6)?.parseOrNull(dateFormatter),
                secondMeetingDate = row.getOrNull(7)?.parseOrNull(dateFormatter),
                thirdMeetingDate = row.getOrNull(8)?.parseOrNull(dateFormatter),
                firstStepUpDate = row.getOrNull(10)?.parseOrNull(dateFormatter),
                secondStepUpDate = row.getOrNull(11)?.parseOrNull(dateFormatter),
                thirdStepUpDate = row.getOrNull(12)?.parseOrNull(dateFormatter),
                phoneNumber = row.getOrNull(14),
                socialNetworks = row.getOrNull(15)
            )
        }
    }

    fun getSheets(): Sheets {
        val credentials = ServiceAccountCredentials.newBuilder().apply {
            clientEmail = GoogleSheetsApi.clientEmail
            privateKey = getPrivateKey(GoogleSheetsApi.privateKey.replace("\\n", "\n"))
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

private fun String.parseOrNull(formatter: DateTimeFormatter): LocalDate? {
    return takeIf { isNotBlank() }?.let { try { LocalDate.parse(it, formatter) } catch (e: DateTimeParseException) { null } }
}
