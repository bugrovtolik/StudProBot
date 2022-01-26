package storage.student

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.DAYS

data class WhoAmIStudent(
    val registerDate: LocalDateTime?,
    val fullName: String,
    val firstPilot: Student?,
    val contactedDate: LocalDate?,
    val firstMeetingDate: LocalDate?,
    val secondMeetingDate: LocalDate?,
    val thirdMeetingDate: LocalDate?,
    val firstFollowUpDate: LocalDate?,
    val secondFollowUpDate: LocalDate?,
    val thirdFollowUpDate: LocalDate?,
    val fourthFollowUpDate: LocalDate?,
    val phoneNumber: String?,
    val socialNetworks: String?
) {
    fun getLastDate() = fourthFollowUpDate
        ?: thirdFollowUpDate ?: secondFollowUpDate ?: firstFollowUpDate
        ?: thirdMeetingDate ?: secondMeetingDate ?: firstMeetingDate ?: contactedDate

    fun isRelevant(today: LocalDate): Boolean {
        val lastDate = getLastDate() ?: return true
        if (lastDate >= today) return true

        return DAYS.between(lastDate, today) < 21
    }
}
