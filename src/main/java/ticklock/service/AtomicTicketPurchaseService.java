package ticklock.service;

import ticklock.domain.Event;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicTicketPurchaseService implements TicketPurchaseService {

    @Override
    public boolean purchase(Event event) {
        
        AtomicInteger remainingSeats = event.getRemainingSeatsAtomic();
        
        while (true) {
            int current = remainingSeats.get();

            if (current <= 0) {
                return false;
            }

            if (remainingSeats.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }
}

