package ticklock.service;

import ticklock.domain.Event;

import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTicketPurchaseService implements TicketPurchaseService {

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public boolean purchase(Event event) {
        lock.lock();
        try {
            if (!event.hasRemainingSeats()) {
                return false;
            }

            // 실무에서는 여기에서 DB 조회, 결제 처리 등이 일어날 수 있다.
            // 이 시간 동안 다른 스레드는 lock 때문에 진입하지 못한다.
            try {
                Thread.sleep(0, 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            event.decreaseSeat();
            return true;
        } finally {
            lock.unlock();
        }
    }
}