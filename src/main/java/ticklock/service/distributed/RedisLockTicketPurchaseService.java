package ticklock.service.distributed;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ticklock.entity.EventEntity;
import ticklock.repository.EventRepository;

import java.util.concurrent.TimeUnit;

@Service
public class RedisLockTicketPurchaseService {

    private final RedissonClient redissonClient;
    private final EventRepository eventRepository;

    public RedisLockTicketPurchaseService(RedissonClient redissonClient, EventRepository eventRepository) {
        this.redissonClient = redissonClient;
        this.eventRepository = eventRepository;
    }

    public boolean purchase(Long eventId) {
        String lockKey = "ticket:event:" + eventId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

            if (!acquired) {
                return false;
            }

            try {
                return doPurchase(eventId);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Transactional
    public boolean doPurchase(Long eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        if (!event.hasRemainingSeats()) {
            return false;
        }

        event.decreaseSeat();
        return true;
    }
}