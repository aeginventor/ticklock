package ticklock.service.unified

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.EventRepository

@Service
class SynchronizedUnifiedService(
    private val eventRepository: EventRepository
) : UnifiedTicketPurchaseService {

    private val locks = mutableMapOf<Long, Any>()

    @Transactional
    override fun purchase(eventId: Long): Boolean {
        val lock = getLock(eventId)
        
        synchronized(lock) {
            val event = eventRepository.findById(eventId)
                .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다.") }

            if (!event.hasRemainingSeats()) {
                return false
            }

            simulateBusinessLogic()

            event.decreaseSeat()
            return true
        }
    }

    override fun getLockType(): String = "synchronized"

    private fun getLock(eventId: Long): Any {
        return synchronized(locks) {
            locks.getOrPut(eventId) { Any() }
        }
    }

    private fun simulateBusinessLogic() {
        try {
            Thread.sleep(0, 100)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}