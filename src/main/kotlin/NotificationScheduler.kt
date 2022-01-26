import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.SECONDS


class NotificationScheduler {
    private val scheduler = Executors.newScheduledThreadPool(1)

    fun schedule(targetTime: LocalTime, task: () -> Unit) {
        val thread = Runnable { task() }
        val delay = computeNextDelay(targetTime)

        scheduler.scheduleAtFixedRate(thread, delay, DAYS.toSeconds(1), SECONDS)
    }

    private fun computeNextDelay(targetTime: LocalTime): Long {
        val currentDate = LocalDateTime.now()
        var targetDate = currentDate.with(targetTime)

        if (targetDate < currentDate) targetDate = targetDate.plusDays(1)

        return Duration.between(currentDate, targetDate).seconds
    }
}
