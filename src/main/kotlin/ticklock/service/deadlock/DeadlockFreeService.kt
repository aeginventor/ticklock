package ticklock.service.deadlock

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticklock.repository.TicketTypeRepository
import kotlin.math.max
import kotlin.math.min

@Service
class DeadlockFreeService(
    private val ticketTypeRepository: TicketTypeRepository
) {

    @Transactional
    fun purchaseSafely(ticketTypeId1: Long, ticketTypeId2: Long): Boolean {
        // 항상 작은 ID부터 락 획득 (락 순서 통일)
        val firstId = min(ticketTypeId1, ticketTypeId2)
        val secondId = max(ticketTypeId1, ticketTypeId2)

        val first = ticketTypeRepository.findByIdWithPessimisticLock(firstId)
            .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: $firstId") }

        sleep(100)

        val second = ticketTypeRepository.findByIdWithPessimisticLock(secondId)
            .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: $secondId") }

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