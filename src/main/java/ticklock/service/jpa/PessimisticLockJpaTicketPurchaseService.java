package ticklock.service.jpa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ticklock.entity.EventEntity;
import ticklock.repository.EventRepository;

/**
 * 비관적 락(Pessimistic Lock)을 사용한 티켓 구매 서비스
 * SELECT ... FOR UPDATE로 행 잠금
 */
@Service
public class PessimisticLockJpaTicketPurchaseService implements JpaTicketPurchaseService {

    private final EventRepository eventRepository;

    public PessimisticLockJpaTicketPurchaseService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public boolean purchase(Long eventId) {
        // 비관적 락으로 조회 - 다른 트랜잭션은 대기
        EventEntity event = eventRepository.findByIdWithPessimisticLock(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        if (!event.hasRemainingSeats()) {
            return false;
        }

        // 락이 걸려있으므로 안전하게 처리
        try {
            Thread.sleep(0, 100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        event.decreaseSeat();
        return true;
    }
}

