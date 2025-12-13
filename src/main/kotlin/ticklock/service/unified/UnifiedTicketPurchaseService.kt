package ticklock.service.unified

interface UnifiedTicketPurchaseService {
    
    fun purchase(eventId: Long): Boolean

    fun getLockType(): String
}