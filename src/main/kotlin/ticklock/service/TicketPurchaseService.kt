package ticklock.service

import ticklock.domain.Event

interface TicketPurchaseService {
    fun purchase(event: Event): Boolean
}

