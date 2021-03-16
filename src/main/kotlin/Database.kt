import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import redis.clients.jedis.Jedis

object Database {
    fun getStudentById(chatId: Long): Student? {
        return Jedis(System.getenv("redisUri")).use { db ->
            db[chatId.toString()]?.let { Json.decodeFromString(it) }
        }
    }

    fun saveStudent(student: Student) {
        Jedis(System.getenv("redisUri")).use { db ->
            db[student.id] = Json.encodeToString(student)
        }
    }

    fun deleteStudent(student: Student) {
        Jedis(System.getenv("redisUri")).use { db ->
            db.del(student.id)
        }
    }

    fun saveState(state: String) {
        Jedis(System.getenv("redisUri")).use { db ->
            db[state] = "1"
        }
    }

    fun deleteState(state: String) {
        Jedis(System.getenv("redisUri")).use { db ->
            db.del(state)
        }
    }

    fun hasState(state: String): Boolean {
        return Jedis(System.getenv("redisUri")).use { db ->
            db[state] != null
        }
    }
}
