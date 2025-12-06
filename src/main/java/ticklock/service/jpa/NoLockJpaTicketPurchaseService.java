package ticklock.service.jpa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ticklock.entity.EventEntity;
import ticklock.repository.EventRepository;

@Service
public class NoLockJpaTicketPurchaseService implements JpaTicketPurchaseService {

    private final EventRepository eventRepository;

    public NoLockJpaTicketPurchaseService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public boolean purchase(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        if (!event.hasRemainingSeats()) {
            return false;
        }

        try {
            Thread.sleep(0, 100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        event.decreaseSeat();
        return true;
    }
}

