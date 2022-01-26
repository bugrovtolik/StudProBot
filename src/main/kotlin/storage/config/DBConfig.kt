package storage.config

import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import storage.feedback_reply.FeedbackReply
import storage.student.Student
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.persistence.EntityManager

object DBConfig {
    private val postgresUrl: String = System.getenv("postgresUrl")
    private val postgresUsername: String = System.getenv("postgresUsername")
    private val postgresPassword: String = System.getenv("postgresPassword")
    private val config = Configuration().addAnnotatedClass(Student::class.java).addAnnotatedClass(FeedbackReply::class.java).setProperties(readProperties())
    private val registry = StandardServiceRegistryBuilder().applySettings(config.properties).build()
    private val manager: EntityManager = config.buildSessionFactory(registry).createEntityManager()

    fun getEntityManager() = manager
    fun beginTransaction() = manager.transaction.begin()
    fun commitTransaction() = manager.transaction.commit()
    fun rollbackTransaction() = manager.transaction.rollback()

    private fun readProperties(): Properties {
        val properties = Properties()
        properties["hibernate.dialect"] = "org.hibernate.dialect.PostgreSQLDialect"
        properties["hibernate.connection.driver_class"] = "org.postgresql.Driver"
        properties["hibernate.connection.url"] = postgresUrl
        properties["hibernate.connection.username"] = postgresUsername
        properties["hibernate.connection.password"] = postgresPassword
        properties["hibernate.hbm2ddl.auto"] = "update"
        properties["hibernate.show_sql"] = "true"
        return properties
    }

    init { Logger.getLogger("org.hibernate").level = Level.INFO }
}
