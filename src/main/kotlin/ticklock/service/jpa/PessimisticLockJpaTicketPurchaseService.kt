package ticklock.service.jpa

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.EventRepository

@Service
class PessimisticLockJpaTicketPurchaseService(
    private val eventRepository: EventRepository
) : JpaTicketPurchaseService {

    @Transactional
    override fun purchase(eventId: Long): Boolean {
        val event = eventRepository.findByIdWithPessimisticLock(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다.") }

        if (!event.hasRemainingSeats()) {
            return false
        }

        try {
            Thread.sleep(0, 100)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }

        event.decreaseSeat()
        return true
    }
}