package storage.feedback_reply

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.Table

@Entity
@IdClass(FeedbackReply.FeedbackReplyId::class)
@Table(name = "feedback_replies")
data class FeedbackReply(
    @Id val feedbackMessageId: Int,
    @Id val studentMessageId: Int,
    val studentId: Long
) {
    data class FeedbackReplyId(
        val feedbackMessageId: Int = 0,
        val studentMessageId: Int = 0
    ) : Serializable
}
