package ticklock.service.unified

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.EventRepository

@Service
class NoLockUnifiedService(
    private val eventRepository: EventRepository
) : UnifiedTicketPurchaseService {

    @Transactional
    override fun purchase(eventId: Long): Boolean {
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다.") }

        if (!event.hasRemainingSeats()) {
            return false
        }

        simulateBusinessLogic()

        event.decreaseSeat()
        return true
    }

    override fun getLockType(): String = "no-lock"

    private fun simulateBusinessLogic() {
        try {
            Thread.sleep(0, 100)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}