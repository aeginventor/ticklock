package ticklock.service;

import ticklock.domain.Event;

public interface TicketPurchaseService {

    boolean purchase(Event event);
}