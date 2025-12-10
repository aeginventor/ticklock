package ticklock.service.jpa

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.EventRepository

@Component
class OptimisticLockPurchaseExecutor(
    private val eventRepository: EventRepository
) {

    @Transactional
    fun execute(eventId: Long): Boolean {
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다.") }

        if (!event.hasRemainingSeats()) {
            return false
        }

        event.decreaseSeat()
        eventRepository.saveAndFlush(event)
        return true
    }
}