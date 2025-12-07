package ticklock.service.deadlock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ticklock.entity.EventEntity;
import ticklock.entity.TicketTypeEntity;
import ticklock.repository.EventRepository;
import ticklock.repository.TicketTypeRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DeadlockTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private DeadlockProneService deadlockProneService;

    @Autowired
    private DeadlockFreeService deadlockFreeService;

    private Long vipId;
    private Long rSeatId;

    @BeforeEach
    void setUp() {
        ticketTypeRepository.deleteAll();
        eventRepository.deleteAll();

        EventEntity event = eventRepository.save(new EventEntity("데드락 테스트 공연", 1000));
        TicketTypeEntity vip = ticketTypeRepository.save(new TicketTypeEntity("VIP", 150000, 50, event));
        TicketTypeEntity rSeat = ticketTypeRepository.save(new TicketTypeEntity("R석", 100000, 100, event));

        vipId = vip.getId();
        rSeatId = rSeat.getId();
    }

    @Test
    @DisplayName("데드락 발생 가능: 서로 다른 순서로 락 획득 시도")
    void deadlockProne_mayTimeout() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    boolean success;
                    if (index % 2 == 0) {
                        success = deadlockProneService.purchaseInOrder(vipId, rSeatId);
                    } else {
                        success = deadlockProneService.purchaseInOrder(rSeatId, vipId);
                    }
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();

        System.out.println("\n=== 데드락 발생 가능 테스트 결과 ===");
        System.out.println("스레드 수: " + threadCount);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failureCount.get());
        System.out.println("타임아웃 내 완료: " + completed);
        if (!errors.isEmpty()) {
            System.out.println("발생한 예외:");
            errors.forEach(e -> System.out.println("  - " + e));
        }

        if (!completed || !errors.isEmpty()) {
            System.out.println("데드락 또는 락 타임아웃 발생");
        }
    }

    @Test
    @DisplayName("데드락 방지: 락 순서를 ID 오름차순으로 통일")
    void deadlockFree_noDeadlock() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    boolean success;
                    if (index % 2 == 0) {
                        success = deadlockFreeService.purchaseSafely(vipId, rSeatId);
                    } else {
                        success = deadlockFreeService.purchaseSafely(rSeatId, vipId);
                    }
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);

        executor.shutdown();

        System.out.println("\n=== 데드락 방지 테스트 결과 ===");
        System.out.println("스레드 수: " + threadCount);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failureCount.get());
        System.out.println("타임아웃 내 완료: " + completed);
        if (!errors.isEmpty()) {
            System.out.println("발생한 예외:");
            errors.forEach(e -> System.out.println("  - " + e));
        }

        assertThat(completed).isTrue();
        assertThat(errors).isEmpty();
        System.out.println("데드락 없이 모든 요청 처리 완료");
    }
}