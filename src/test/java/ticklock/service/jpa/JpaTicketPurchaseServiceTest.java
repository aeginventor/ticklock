package ticklock.service.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ticklock.entity.EventEntity;
import ticklock.repository.EventRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JpaTicketPurchaseServiceTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private NoLockJpaTicketPurchaseService noLockService;

    @Autowired
    private PessimisticLockJpaTicketPurchaseService pessimisticLockService;

    @Autowired
    private OptimisticLockJpaTicketPurchaseService optimisticLockService;

    private Long eventId;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("No-Lock: 동시 구매 시 초과 판매가 발생할 수 있다")
    void noLock_concurrentPurchase_mayOversell() throws InterruptedException {
        // given
        int totalSeats = 100;
        int threadCount = 150;
        EventEntity event = eventRepository.save(new EventEntity("No-Lock 테스트", totalSeats));
        eventId = event.getId();

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean success = noLockService.purchase(eventId);
                    results.add(success);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        long successCount = results.stream().filter(b -> b).count();
        EventEntity updated = eventRepository.findById(eventId).orElseThrow();

        System.out.println("\n=== No-Lock 테스트 결과 ===");
        System.out.println("총 좌석: " + totalSeats);
        System.out.println("동시 요청: " + threadCount);
        System.out.println("성공 응답: " + successCount);
        System.out.println("최종 재고: " + updated.getRemainingSeats());

        if (successCount > totalSeats || updated.getRemainingSeats() < 0) {
            System.out.println("초과 판매 발생");
        }
    }

    @Test
    @DisplayName("비관적 락: 동시 구매 시 초과 판매가 발생하지 않는다")
    void pessimisticLock_concurrentPurchase_noOversell() throws InterruptedException {
        // given
        int totalSeats = 100;
        int threadCount = 150;
        EventEntity event = eventRepository.save(new EventEntity("비관적 락 테스트", totalSeats));
        eventId = event.getId();

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean success = pessimisticLockService.purchase(eventId);
                    results.add(success);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        long successCount = results.stream().filter(b -> b).count();
        EventEntity updated = eventRepository.findById(eventId).orElseThrow();

        System.out.println("\n=== 비관적 락 테스트 결과 ===");
        System.out.println("총 좌석: " + totalSeats);
        System.out.println("동시 요청: " + threadCount);
        System.out.println("성공 응답: " + successCount);
        System.out.println("최종 재고: " + updated.getRemainingSeats());

        assertThat(successCount).isEqualTo(totalSeats);
        assertThat(updated.getRemainingSeats()).isEqualTo(0);
        System.out.println("동시성 제어 성공");
    }

    @Test
    @DisplayName("낙관적 락: 동시 구매 시 초과 판매가 발생하지 않는다")
    void optimisticLock_concurrentPurchase_noOversell() throws InterruptedException {
        // given
        int totalSeats = 100;
        int threadCount = 150;
        EventEntity event = eventRepository.save(new EventEntity("낙관적 락 테스트", totalSeats));
        eventId = event.getId();

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean success = optimisticLockService.purchase(eventId);
                    results.add(success);
                } catch (Exception e) {
                    exceptions.add(e);
                    results.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        long successCount = results.stream().filter(b -> b).count();
        EventEntity updated = eventRepository.findById(eventId).orElseThrow();

        System.out.println("\n=== 낙관적 락 테스트 결과 ===");
        System.out.println("총 좌석: " + totalSeats);
        System.out.println("동시 요청: " + threadCount);
        System.out.println("성공 응답: " + successCount);
        System.out.println("재시도 실패(예외): " + exceptions.size());
        System.out.println("최종 재고: " + updated.getRemainingSeats());

        assertThat(updated.getRemainingSeats()).isGreaterThanOrEqualTo(0);
        System.out.println("초과 판매 없음");
    }
}