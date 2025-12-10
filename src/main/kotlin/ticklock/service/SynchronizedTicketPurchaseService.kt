package ticklock.service

import org.springframework.stereotype.Service
import ticklock.domain.Event

@Service
class SynchronizedTicketPurchaseService : TicketPurchaseService {

    override fun purchase(event: Event): Boolean {
        synchronized(event) {
            if (!event.hasRemainingSeats()) {
                return false
            }

            try {
                Thread.sleep(0, 100)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            event.decreaseSeat()
            return true
        }
    }
}

