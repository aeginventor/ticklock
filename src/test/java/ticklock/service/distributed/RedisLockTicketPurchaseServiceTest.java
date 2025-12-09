package ticklock.service.distributed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
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
@Testcontainers
class RedisLockTicketPurchaseServiceTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RedisLockTicketPurchaseService redisLockService;

    private Long eventId;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("Redis 분산 락: 동시 구매 시 초과 판매가 발생하지 않는다")
    void redisLock_concurrentPurchase_noOversell() throws InterruptedException {
        int totalSeats = 100;
        int threadCount = 150;
        EventEntity event = eventRepository.save(new EventEntity("Redis 락 테스트", totalSeats));
        eventId = event.getId();

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean success = redisLockService.purchase(eventId);
                    results.add(success);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long successCount = results.stream().filter(b -> b).count();
        EventEntity updated = eventRepository.findById(eventId).orElseThrow();

        System.out.println("\n=== Redis 분산 락 테스트 결과 ===");
        System.out.println("총 좌석: " + totalSeats);
        System.out.println("동시 요청: " + threadCount);
        System.out.println("성공 응답: " + successCount);
        System.out.println("최종 재고: " + updated.getRemainingSeats());

        assertThat(successCount).isEqualTo(totalSeats);
        assertThat(updated.getRemainingSeats()).isEqualTo(0);
        System.out.println("동시성 제어 성공");
    }
}