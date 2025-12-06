package ticklock.service.jpa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ticklock.entity.EventEntity;
import ticklock.repository.EventRepository;

/**
 * 락 없이 티켓을 구매하는 서비스 (문제 상황 재현용)
 */
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

        // Race Condition 발생 가능 구간
        // 여러 트랜잭션이 동시에 여기까지 도달할 수 있음
        try {
            Thread.sleep(0, 100); // 문제 상황 재현을 위한 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        event.decreaseSeat();
        return true;
    }
}

