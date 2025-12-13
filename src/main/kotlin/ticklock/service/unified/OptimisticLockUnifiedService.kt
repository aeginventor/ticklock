package ticklock.service.unified

import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import ticklock.repository.EventRepository
import kotlin.random.Random

@Service
class OptimisticLockUnifiedService(
    private val executor: OptimisticLockExecutor
) : UnifiedTicketPurchaseService {

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

    override fun getLockType(): String = "optimistic"
}