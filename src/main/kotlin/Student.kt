data class Student(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val comment: String? = null,
    val lastCheckinDate: String? = null,
    val checkinCount: String? = null,
    val registerDate: String? = null,
    val university: String? = null,
    val yearStudy: String? = null,
    val studProInfo: String? = null,
    val seenWhoAmI: String? = null,
    val status: String? = null,

    val isNew: Boolean = false
) {
    enum class Status {
        REGISTRATION, CHECKIN, FEEDBACK
    }

    fun getDatabaseProps(): List<String?> {
        return listOf(id, firstName, lastName, comment, lastCheckinDate, checkinCount, registerDate, university, yearStudy, studProInfo, seenWhoAmI, status)
    }

    fun hasMissingData(): Boolean {
        return listOf(firstName, lastName, lastCheckinDate, checkinCount, registerDate, university, yearStudy, studProInfo).any { it.isNullOrBlank() }
    }
}
