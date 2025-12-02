package ticklock.simulation;

import ticklock.domain.Event;
import ticklock.service.AtomicTicketPurchaseService;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AtomicSimulation {

    private final AtomicTicketPurchaseService service = new AtomicTicketPurchaseService();

    public void run() {
        int totalSeats = 100;
        int threadCount = 500;
        Event event = new Event(1L, "Atomic 시뮬레이션 공연", totalSeats);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

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

        try {
            ready.await();
            start.countDown();
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long successCount = results.stream().filter(b -> b).count();
        long failureCount = results.size() - successCount;
        int finalRemainingSeats = event.getRemainingSeatsAtomic().get();

        System.out.println("\n=== AtomicInteger 시뮬레이션 결과 ===");
        System.out.println("총 좌석 수: " + totalSeats);
        System.out.println("동시 구매 시도 수: " + threadCount);
        System.out.println();
        System.out.println("구매 성공 응답 수: " + successCount);
        System.out.println("구매 실패 응답 수: " + failureCount);
        System.out.println("최종 남은 좌석 수: " + finalRemainingSeats);
        System.out.println();

        if (successCount == totalSeats && finalRemainingSeats == 0) {
            System.out.println("동시성 제어 성공");
        } else {
            System.out.println("예상치 못한 결과 발생");
        }
    }
}

