package ticklock.service.deadlock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ticklock.entity.TicketTypeEntity;
import ticklock.repository.TicketTypeRepository;

@Service
public class DeadlockFreeService {

    private final TicketTypeRepository ticketTypeRepository;

    public DeadlockFreeService(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @Transactional
    public boolean purchaseSafely(Long ticketTypeId1, Long ticketTypeId2) {
        // 항상 작은 ID부터 락 획득 (락 순서 통일)
        Long firstId = Math.min(ticketTypeId1, ticketTypeId2);
        Long secondId = Math.max(ticketTypeId1, ticketTypeId2);

        TicketTypeEntity first = ticketTypeRepository.findByIdWithPessimisticLock(firstId)
                .orElseThrow(() -> new IllegalArgumentException("티켓 타입을 찾을 수 없습니다: " + firstId));

        sleep(100);

        TicketTypeEntity second = ticketTypeRepository.findByIdWithPessimisticLock(secondId)
                .orElseThrow(() -> new IllegalArgumentException("티켓 타입을 찾을 수 없습니다: " + secondId));

        if (!first.hasRemainingSeats() || !second.hasRemainingSeats()) {
            return false;
        }

        first.decreaseSeat();
        second.decreaseSeat();
        return true;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}