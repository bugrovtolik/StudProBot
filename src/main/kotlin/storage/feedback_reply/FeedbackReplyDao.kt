package storage.feedback_reply

import storage.config.DBConfig

object FeedbackReplyDao {
    private val manager = DBConfig.getEntityManager()

    fun findByFeedbackMessageId(messageId: Int): FeedbackReply? {
        val query = "select e from FeedbackReply e where e.feedbackMessageId = :messageId"
        return manager.createQuery(query, FeedbackReply::class.java).setParameter("messageId", messageId).resultList.firstOrNull()
    }

    fun save(feedbackReply: FeedbackReply) {
        manager.persist(feedbackReply)
    }

    fun delete(feedbackReply: FeedbackReply) {
        manager.remove(feedbackReply)
    }
}
