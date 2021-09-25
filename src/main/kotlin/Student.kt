import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val id: String,
    var firstName: String? = null,
    var lastName: String? = null,
    var comment: String? = null,
    var lastCheckinDate: String? = null,
    var checkinCount: String? = null,
    var registerDate: String? = null,
    var university: String? = null,
    var yearStudy: String? = null,
    var studProInfo: String? = null,
    var status: Status? = null
) {
    enum class Status {
        REGISTRATION, CHECKIN, FEEDBACK
    }
}
