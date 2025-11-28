package ticklock.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ticklock.domain.Event;

import static org.junit.jupiter.api.Assertions.*;

class NoLockTicketPurchaseServiceTest {

    private final NoLockTicketPurchaseService service = new NoLockTicketPurchaseService();

    @Test
    @DisplayName("남은 좌석이 있을 때는 예매에 성공한다")
    void purchase_withRemainingSeats_shouldSucceed() {
        // given
        Event event = new Event(1L, "테스트 공연", 1);

        // when
        boolean result = service.purchase(event);

        // then
        assertTrue(result);
        assertEquals(0, event.getRemainingSeats());
        assertFalse(event.hasRemainingSeats());
    }

    @Test
    @DisplayName("남은 좌석이 없으면 예매에 실패하고 false를 반환한다")
    void purchase_withoutRemainingSeats_shouldFail() {
        // given
        Event event = new Event(1L, "테스트 공연", 1);
        service.purchase(event);

        // when
        boolean result = service.purchase(event);

        // then
        assertFalse(result);
        assertEquals(0, event.getRemainingSeats());
    }
}