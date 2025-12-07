package ticklock.service.deadlock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ticklock.entity.TicketTypeEntity;
import ticklock.repository.TicketTypeRepository;

@Service
public class DeadlockProneService {

    private final TicketTypeRepository ticketTypeRepository;

    public DeadlockProneService(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @Transactional
    public boolean purchaseInOrder(Long firstTicketTypeId, Long secondTicketTypeId) {
        TicketTypeEntity first = ticketTypeRepository.findByIdWithPessimisticLock(firstTicketTypeId)
                .orElseThrow(() -> new IllegalArgumentException("티켓 타입을 찾을 수 없습니다: " + firstTicketTypeId));

        sleep(100);

        TicketTypeEntity second = ticketTypeRepository.findByIdWithPessimisticLock(secondTicketTypeId)
                .orElseThrow(() -> new IllegalArgumentException("티켓 타입을 찾을 수 없습니다: " + secondTicketTypeId));

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