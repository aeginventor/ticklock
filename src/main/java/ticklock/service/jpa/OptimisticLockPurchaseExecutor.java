package ticklock.service.jpa;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ticklock.entity.EventEntity;
import ticklock.repository.EventRepository;

@Component
public class OptimisticLockPurchaseExecutor {

    private final EventRepository eventRepository;

    public OptimisticLockPurchaseExecutor(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public boolean execute(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        if (!event.hasRemainingSeats()) {
            return false;
        }

        event.decreaseSeat();
        eventRepository.saveAndFlush(event);
        return true;
    }
}