package storage.student

import storage.config.DBConfig

object StudentDao {
    private val manager = DBConfig.getEntityManager()
    val adminChatId: String = System.getenv("adminChatId")
    val feedbackChatId: String = System.getenv("feedbackChatId")

    fun findAll(): List<Student> {
        return manager.createQuery("select s from Student s", Student::class.java).resultList
    }

    fun findAllVolunteers(): List<Student> {
        return manager.createQuery("select s from Student s where s.isStaff = true", Student::class.java).resultList
    }

    fun findById(id: Long): Student? {
        if (id < 0) return Student(id) // check for groups, they have negative ids
        return manager.find(Student::class.java, id)
    }

    fun save(student: Student) {
        manager.persist(student)
    }

    fun saveAll(students: Iterable<Student>) {
        students.forEach { manager.merge(it) }
    }
}
