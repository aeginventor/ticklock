package ticklock.service

import ticklock.domain.Event
import java.util.concurrent.locks.ReentrantLock

class ReentrantLockTicketPurchaseService : TicketPurchaseService {

    private val lock = ReentrantLock()

    override fun purchase(event: Event): Boolean {
        lock.lock()
        try {
            if (!event.hasRemainingSeats()) {
                return false
            }

            // 실무에서는 여기에서 DB 조회, 결제 처리 등이 일어날 수 있다.
            // 이 시간 동안 다른 스레드는 lock 때문에 진입하지 못한다.
            try {
                Thread.sleep(0, 100)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            event.decreaseSeat()
            return true
        } finally {
            lock.unlock()
        }
    }
}

