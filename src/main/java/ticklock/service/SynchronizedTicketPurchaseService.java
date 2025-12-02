package ticklock.service;

import ticklock.domain.Event;

public class SynchronizedTicketPurchaseService implements TicketPurchaseService {

    @Override
    public boolean purchase(Event event) {
        synchronized (event) {
            if (!event.hasRemainingSeats()) {
                return false;
            }

            try {
                Thread.sleep(0, 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            event.decreaseSeat();
            return true;
        }
    }
}