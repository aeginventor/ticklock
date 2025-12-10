package ticklock.service

import ticklock.domain.Event

class AtomicTicketPurchaseService : TicketPurchaseService {

    override fun purchase(event: Event): Boolean {
        val remainingSeats = event.remainingSeatsAtomic

        while (true) {
            val current = remainingSeats.get()

            if (current <= 0) {
                return false
            }

            if (remainingSeats.compareAndSet(current, current - 1)) {
                return true
            }
        }
    }
}

