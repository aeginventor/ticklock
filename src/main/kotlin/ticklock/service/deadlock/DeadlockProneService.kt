package ticklock.service.deadlock

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.TicketTypeRepository

@Service
class DeadlockProneService(
    private val ticketTypeRepository: TicketTypeRepository
) {

    @Transactional
    fun purchaseInOrder(firstTicketTypeId: Long, secondTicketTypeId: Long): Boolean {
        val first = ticketTypeRepository.findByIdWithPessimisticLock(firstTicketTypeId)
            .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: $firstTicketTypeId") }

        sleep(100)

        val second = ticketTypeRepository.findByIdWithPessimisticLock(secondTicketTypeId)
            .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: $secondTicketTypeId") }

        if (!first.hasRemainingSeats() || !second.hasRemainingSeats()) {
            return false
        }

        first.decreaseSeat()
        second.decreaseSeat()
        return true
    }

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}