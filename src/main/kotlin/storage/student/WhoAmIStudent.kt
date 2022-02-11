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
    val firstStepUpDate: LocalDate?,
    val secondStepUpDate: LocalDate?,
    val thirdStepUpDate: LocalDate?,
    val phoneNumber: String?,
    val socialNetworks: String?
) {
    fun getLastDate() = thirdStepUpDate ?: secondStepUpDate ?: firstStepUpDate ?: thirdMeetingDate ?: secondMeetingDate ?: firstMeetingDate ?: contactedDate

    fun isRelevant(today: LocalDate): Boolean {
        val lastDate = getLastDate() ?: return true
        if (lastDate >= today) return true

        return DAYS.between(lastDate, today) < 21
    }
}
