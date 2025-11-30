package ticklock.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import ticklock.domain.Event;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

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

    @RepeatedTest(10)
    @DisplayName("동시에 여러 스레드가 구매를 시도하면 초과 판매가 발생한다")
    void purchase_withConcurrentAccess_shouldCauseOverselling() throws InterruptedException {
        // given
        int totalSeats = 50;
        int threadCount = 200;
        Event event = new Event(1L, "동시성 테스트 공연", totalSeats);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        // when - 200개의 스레드가 동시에 50석에 대해 구매 시도
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

        // No-Lock 환경에서는 초과 판매가 발생하거나 데이터 불일치가 발생해야 함
        boolean hasOverselling = successCount > totalSeats;
        boolean hasNegativeSeats = finalRemainingSeats < 0;
        boolean hasDataInconsistency = finalRemainingSeats != (totalSeats - successCount);

        assertTrue(hasOverselling || hasNegativeSeats || hasDataInconsistency,
                "No-Lock 환경에서 동시성 문제가 발생해야 합니다. " +
                        "성공: " + successCount + ", 최종 재고: " + finalRemainingSeats);
    }
}