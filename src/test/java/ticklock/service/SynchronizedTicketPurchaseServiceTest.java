package ticklock.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ticklock.domain.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class SynchronizedTicketPurchaseServiceTest {

    private final SynchronizedTicketPurchaseService service = new SynchronizedTicketPurchaseService();

    @Test
    @DisplayName("synchronized 환경에서 남은 좌석이 있을 때는 예매에 성공한다")
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
    @DisplayName("synchronized 환경에서 남은 좌석이 없으면 예매에 실패하고 false를 반환한다")
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

    @Test
    @DisplayName("synchronized 환경에서는 동시 예매에도 초과 판매나 데이터 불일치가 발생하지 않는다")
    void purchase_withConcurrentAccess_shouldNotOversellOrCorruptState() throws InterruptedException {
        // given
        int totalSeats = 50;
        int threadCount = 200;
        Event event = new Event(1L, "동시성 테스트 공연", totalSeats);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                boolean success = service.purchase(event);
                results.add(success);

                done.countDown();
            }).start();
        }

        ready.await();
        start.countDown();
        done.await();

        // then
        long successCount = results.stream().filter(b -> b).count();
        int finalRemainingSeats = event.getRemainingSeats();

        boolean hasOverselling = successCount > totalSeats;
        boolean hasNegativeSeats = finalRemainingSeats < 0;
        boolean hasDataInconsistency = finalRemainingSeats != (totalSeats - successCount);

        assertFalse(hasOverselling, "초과 판매가 발생하면 안 됩니다. 성공 수: " + successCount);
        assertFalse(hasNegativeSeats, "재고가 음수가 되면 안 됩니다. 최종 재고: " + finalRemainingSeats);
        assertFalse(hasDataInconsistency,
                "좌석 수가 successCount와 일관된 상태여야 합니다. " +
                        "성공 수: " + successCount + ", 최종 재고: " + finalRemainingSeats);
    }
}