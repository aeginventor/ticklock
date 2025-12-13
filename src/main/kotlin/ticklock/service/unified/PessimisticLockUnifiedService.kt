package ticklock.service.unified

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.EventRepository

@Service
class PessimisticLockUnifiedService(
    private val eventRepository: EventRepository
) : UnifiedTicketPurchaseService {

    @Transactional
    override fun purchase(eventId: Long): Boolean {
        val event = eventRepository.findByIdWithPessimisticLock(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다.") }

        if (!event.hasRemainingSeats()) {
            return false
        }

        simulateBusinessLogic()

        event.decreaseSeat()
        return true
    }

    override fun getLockType(): String = "pessimistic"

    private fun simulateBusinessLogic() {
        try {
            Thread.sleep(0, 100)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}