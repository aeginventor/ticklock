package ticklock.service.jpa

/**
 * JPA 기반 티켓 구매 서비스 인터페이스
 */
interface JpaTicketPurchaseService {

    /**
     * 티켓을 구매합니다.
     *
     * @param eventId 이벤트 ID
     * @return 구매 성공 시 true, 실패 시 false
     */
    fun purchase(eventId: Long): Boolean
}