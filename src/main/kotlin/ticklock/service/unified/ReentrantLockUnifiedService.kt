package ticklock.service.unified

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.EventRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Service
class ReentrantLockUnifiedService(
    private val eventRepository: EventRepository
) : UnifiedTicketPurchaseService {

    private val locks = ConcurrentHashMap<Long, ReentrantLock>()

    @Transactional
    override fun purchase(eventId: Long): Boolean {
        val lock = locks.computeIfAbsent(eventId) { ReentrantLock() }
        
        lock.lock()
        try {
            val event = eventRepository.findById(eventId)
                .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다.") }

            if (!event.hasRemainingSeats()) {
                return false
            }

            simulateBusinessLogic()

            event.decreaseSeat()
            return true
        } finally {
            lock.unlock()
        }
    }

    override fun getLockType(): String = "reentrant-lock"

    private fun simulateBusinessLogic() {
        try {
            Thread.sleep(0, 100)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}