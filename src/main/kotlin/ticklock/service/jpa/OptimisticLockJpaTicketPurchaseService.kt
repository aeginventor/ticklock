package ticklock.service.jpa

import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class OptimisticLockJpaTicketPurchaseService(
    private val executor: OptimisticLockPurchaseExecutor
) : JpaTicketPurchaseService {

    companion object {
        private const val MAX_RETRY = 10
    }

    override fun purchase(eventId: Long): Boolean {
        var retryCount = 0

        while (retryCount < MAX_RETRY) {
            try {
                return executor.execute(eventId)
            } catch (e: ObjectOptimisticLockingFailureException) {
                retryCount++
                if (retryCount >= MAX_RETRY) {
                    return false
                }
                try {
                    Thread.sleep(10 + Random.nextLong(50))
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return false
                }
            }
        }
        return false
    }
}