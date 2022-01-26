package storage.student

import storage.student.StudentDao.feedbackChatId
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "students")
data class Student(
    @Id val id: Long,
    var firstName: String? = null,
    var lastName: String? = null,
    var userName: String? = null,
    var lastCheckinDate: LocalDateTime? = null,
    var checkinCount: Int = 0,
    var registerDate: LocalDateTime? = null,
    var university: String? = null,
    var yearStudy: Int? = null,
    var studProInfo: String? = null,
    var seenWhoAmI: Boolean = false,
    @Enumerated(EnumType.STRING)
    var status: Status? = null,
    var statusParam: String? = null,
    var isStaff: Boolean = false,
    var allowNotifications: Boolean = false
) {
    enum class Status {
        REGISTRATION, FEEDBACK, PICK_NAME, PICK_DATE
    }

    fun hasMissingData(): Boolean {
        if (id.toString() == feedbackChatId) return false
        val requiredProps = listOf(firstName, lastName, lastCheckinDate, checkinCount, registerDate, university, yearStudy, studProInfo)

        return requiredProps.any { it == null || it is String && it.isBlank() }
    }
}
