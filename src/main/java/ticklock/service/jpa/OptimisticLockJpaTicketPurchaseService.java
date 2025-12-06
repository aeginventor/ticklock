package ticklock.service.jpa;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import ticklock.repository.EventRepository;

@Service
public class OptimisticLockJpaTicketPurchaseService implements JpaTicketPurchaseService {

    private final OptimisticLockPurchaseExecutor executor;
    private final EventRepository eventRepository;
    private static final int MAX_RETRY = 10;

    public OptimisticLockJpaTicketPurchaseService(
            OptimisticLockPurchaseExecutor executor,
            EventRepository eventRepository) {
        this.executor = executor;
        this.eventRepository = eventRepository;
    }

    @Override
    public boolean purchase(Long eventId) {
        int retryCount = 0;

        while (retryCount < MAX_RETRY) {
            try {
                return executor.execute(eventId);
            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount++;
                if (retryCount >= MAX_RETRY) {
                    return false;
                }
                try {
                    Thread.sleep(10 + (int)(Math.random() * 50));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }
}