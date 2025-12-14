package ticklock.service.unified

import org.redisson.api.RedissonClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.EventRepository
import java.util.concurrent.TimeUnit

@Service
@ConditionalOnBean(RedissonClient::class)
class RedisLockUnifiedService(
    private val redissonClient: RedissonClient,
    private val eventRepository: EventRepository
) : UnifiedTicketPurchaseService {

    override fun purchase(eventId: Long): Boolean {
        val lockKey = "ticket:event:$eventId"
        val lock = redissonClient.getLock(lockKey)

        return try {
            val acquired = lock.tryLock(5, 10, TimeUnit.SECONDS)

            if (!acquired) {
                return false
            }

            try {
                doPurchase(eventId)
            } finally {
                if (lock.isHeldByCurrentThread) {
                    lock.unlock()
                }
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            false
        }
    }

    @Transactional
    fun doPurchase(eventId: Long): Boolean {
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다.") }

        if (!event.hasRemainingSeats()) {
            return false
        }

        event.decreaseSeat()
        return true
    }

    override fun getLockType(): String = "redis"
}