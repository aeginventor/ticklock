package ticklock.service;

import ticklock.domain.Event;

public class NoLockTicketPurchaseService {

    public boolean purchase(Event event) {
        if (!event.hasRemainingSeats()) {
            return false;
        }
        event.decreaseSeat();
        return true;
    }
}